package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import static org.junit.Assert.*;

import webserver.HttpRequest;

public class HttpRequestTest {

    private String testDir = "./src/test/resources/";

    @Test
    public void request_GET() throws IOException {
        InputStream in = new FileInputStream(new File(testDir + "Http_GET"));
        HttpRequest req = new HttpRequest(in);

        System.out.println(req.getMethod());
        System.out.println(req.getPath());
        assertEquals("GET", req.getMethod());
        assertEquals("/user/create", req.getPath());
        assertEquals("keep-alive", req.getHeader().get("Connection"));
        assertEquals("javajigi", req.getParams().get("userId"));
    }

    @Test
    public void request_POST() throws IOException {
        InputStream in = new FileInputStream(new File(testDir + "Http_POST"));
        HttpRequest req = new HttpRequest(in);

        System.out.println(req.getMethod());
        System.out.println(req.getPath());
        System.out.println(req.getParams().keySet());
        assertEquals("POST", req.getMethod());
        assertEquals("/user/create", req.getPath());
        assertEquals("keep-alive", req.getHeader().get("Connection"));
        assertEquals("javajigi", req.getParams().get("userId"));
    }
}
