package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import webserver.HttpResponse;

public class HttpResponseTest {

    private String testDir = "./src/test/resources/";

    @Test
    public void responseForward() throws Exception {
        HttpResponse response = new HttpResponse(createOutputStream("Http_Forward"));
        response.forward("/index.html");
    }

    @Test
    public void responseRedirect() throws IOException {
        HttpResponse response = new HttpResponse(createOutputStream("Http_Redirect"));
        response.sendRedirect("/index.html");
    }

    @Test
    public void responseCookies() throws IOException {
        HttpResponse response = new HttpResponse(createOutputStream("Http_Cookie"));
        response.addHeader("Set-Cookie", "logined=true");
        response.sendRedirect("/index.html");
    }

    private OutputStream createOutputStream(String fileName) throws FileNotFoundException {
        return new FileOutputStream(new File(testDir + fileName));
    }
}
