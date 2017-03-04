package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.HttpRequestUtils.Pair;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // request 정보 리딩
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = reader.readLine();
            if (line == null) {
                return;
            }

            // request 정보 파싱
            String url = HttpRequestUtils.parseRequestInfo(line).get("url");
            Map<String, String> params = null;
            boolean logined = false;
            Pair accept = null;

            int contentLength = 0;
            while (!"".equals(line)) {
                log.info(line);
                line = reader.readLine();
                if (line.contains("Content-Length")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
                if (line.contains("Cookie")) {
                    logined = isLogined(line);
                }
                if (line.contains("Accept:")) {
                    accept = HttpRequestUtils.parseHeader(line.split(",")[0].trim());
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = null;

            if ("/user/create".equals(url)) {
                params = HttpRequestUtils.parseQueryString(IOUtils.readData(reader, contentLength));
                url = "/index.html";

                User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
                DataBase.addUser(user);
                log.info(user.toString());

                response302Header(dos, url);
            } else if ("/user/login".equals(url)) {
                params = HttpRequestUtils.parseQueryString(IOUtils.readData(reader, contentLength));
                url = "/user/login_failed.html";

                User user = DataBase.findUserById(params.get("userId"));
                if (user == null) {
                    response302HeaderForLogin(dos, url, logined);
                    return;
                }
                logined = user.getPassword().equals(params.get("password"));
                url = logined ? "/index.html" : url;

                response302HeaderForLogin(dos, url, logined);
            } else if ("/user/list".equals(url)) {
                if (!logined) {
                    url = "/user/login.html";

                    response302Header(dos, url);
                    return;
                }
                StringBuilder builder = new StringBuilder();
                Collection<User> users = DataBase.findAll();

                builder.append("<table border='1'>");
                for (User user : users) {
                    builder.append("<tr>");
                    builder.append("<td>" + user.getUserId()+"</td>");
                    builder.append("<td>" + user.getName()+"</td>");
                    builder.append("<td>" + user.getEmail()+"</td>");
                    builder.append("</tr>");
                }
                builder.append("</table>");
                body = builder.toString().getBytes();

                response200Header(dos, builder.toString().length(), accept.getValue());
                responseBody(dos, body);
            } else {
                body = Files.readAllBytes(new File("./webapp" + url).toPath());

                response200Header(dos, body.length, accept.getValue());
                responseBody(dos, body);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogined(String line) {
        Map<String, String> cookie = HttpRequestUtils.parseCookies(line.split(":")[1].trim());
        if (cookie != null && Boolean.parseBoolean(cookie.get("logined"))) {
            return true;
        }
        return false;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType +"\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: " + location);
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302HeaderForLogin(DataOutputStream dos, String location, boolean logined) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Set-Cookie: logined=" + logined + "; Path=/ \r\n");
            dos.writeBytes("Location: " + location + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
