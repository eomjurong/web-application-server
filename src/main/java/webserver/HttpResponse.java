package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {

    DataOutputStream dos;

    private Map<String, String> header;
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    public HttpResponse(OutputStream out) {
        header = new HashMap<String, String>();
        this.dos = new DataOutputStream(out);
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public void addHeader(String key, String value) {
        header.put(key, value);
    }

    public void forward(String url) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
        addHeader("Content-Length", Integer.toString(body.length));

        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");

            writeHeader();

            dos.writeBytes("\r\n");
            writeBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }


    public void forwardBody(String contents) throws IOException {
        byte[] body = contents.getBytes();
        addHeader("Content-Length", Integer.toString(contents.length()));

        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");

            writeHeader();

            dos.writeBytes("\r\n");
            writeBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String url) throws IOException {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            writeHeader();
            dos.writeBytes("Location: " + url + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void writeHeader() throws IOException {
        for (String key : header.keySet()) {
            dos.writeBytes(key + ": " + header.get(key) + " \r\n");
            log.info(key + ": " + header.get(key) + " \r\n");
        }
    }

    private void writeBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
