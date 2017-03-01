package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        	int contentLength = 0;
        	while (!"".equals(line)) {
        		log.info(line);
        		line = reader.readLine();
        		if (line.contains("Content-Length")) {
        			contentLength = Integer.parseInt(line.split(":")[1].trim());
        		}
        	}
        	
        	DataOutputStream dos = new DataOutputStream(out);
        	byte[] body = null;

        	if ("/user/create".equals(url)) {
        		params = HttpRequestUtils.parseQueryString(IOUtils.readData(reader, contentLength));
        		url = "/index.html";

        		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        		log.info(user.toString());
        		response302Header(dos, url);
        	} else {
        		body = Files.readAllBytes(new File("./webapp" + url).toPath());
        		response200Header(dos, body.length);
        	}
        	responseBody(dos, body);

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
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
    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
