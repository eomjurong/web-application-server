package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

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
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            String url = request.getPath();
            if ("/user/create".equals(url)) {
                createUser(request, response);
            } else if ("/user/login".equals(url)) {
                login(request, response);
            } else if ("/user/list".equals(url)) {
                getUserList(request, response);
            } else {
                response.addHeader("Content-Type", request.getAccept());
                response.forward(url);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void getUserList(HttpRequest request, HttpResponse response) throws IOException {
        if (!isLogined(request)) {
            response.sendRedirect("/user/login.html");
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

        response.addHeader("Content-Type", request.getAccept());
        response.forwardBody(builder.toString());
    }

    private void login(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> params = request.getParams();
        boolean isSuccess = false;

        User user = DataBase.findUserById(params.get("userId"));
        if (user != null && user.getPassword().equals(params.get("password"))) {
            isSuccess = true;
        }

        response.addHeader("Set-Cookie", "logined="+ isSuccess +"; Path=/ \r\n");
        response.sendRedirect(isSuccess ? "/index.html" : "/user/login_failed.html");
    }

    private void createUser(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> params = request.getParams();
        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        DataBase.addUser(user);
        log.info(user.toString());

        response.sendRedirect("/index.html");
    }

    private boolean isLogined(HttpRequest request) {
        String value = request.getCookie("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

}
