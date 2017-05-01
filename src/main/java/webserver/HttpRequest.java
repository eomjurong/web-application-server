package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;

import util.HttpRequestUtils;
import util.IOUtils;
import util.HttpRequestUtils.Pair;

public class HttpRequest {

    private HttpMethod method;
    private String path;
    private Map<String, String> params;
    private String version;
    private Map<String, String> header = new HashMap<String, String>();
    private Map<String, String> cookie = null;

    public HttpRequest (InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = reader.readLine();
        if (line == null) {
            return;
        }

        // request line
        this.setRequestLine(line);

        // request header
        while(!"".equals(line = reader.readLine())) {
            if (line.contains("Cookie")) {
                this.setCookie(line);
            }
            this.setHeader(line);
        }

        // request contents
        if (header.get("Content-Length") != null) {
            this.setParams(IOUtils.readData(reader, Integer.parseInt(header.get("Content-Length"))));
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public String getHeader(String key) {
        return header.get(key);
    }

    public void setHeader(String line) {
        Pair pair = HttpRequestUtils.parseHeader(line);
        header.put(pair.getKey(), pair.getValue());
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setParams(String data) {
        this.params = HttpRequestUtils.parseQueryString(data);
    }

    private void setRequestLine(String line) {
        if (Strings.isNullOrEmpty(line)) {
            return;
        }

        String [] tokens = line.split(" ");
        if (tokens.length != 3) {
            return;
        }

        this.setMethod(HttpMethod.valueOf(tokens[0]));
        if (tokens[1].contains("?")) {
            int index = tokens[1].indexOf("?");
            this.setPath(tokens[1].substring(0, index));
            this.setParams(tokens[1].substring(index + 1));
        } else {
            this.setPath(tokens[1]);
        }
        this.setVersion(tokens[2]);
    }

    public Map<String, String> getCookie() {
        return cookie;
    }

    public String getCookie(String key) {
        if (key == null) {
            return null;
        }
        return cookie.get(key);
    }

    public void setCookie(String line) {
        this.cookie = HttpRequestUtils.parseCookies(line.split(":")[1].trim());
    }

    public String getAccept() {
        if (header.get("Accept") != null) {
            return header.get("Accept").split(",")[0].trim();
        }
        return null;
    }

    public HttpCookie getCookies() {
        return new HttpCookie(getHeader("Cookie"));
    }

    public HttpSession getSession() {
        return HttpSessions.getSession(getCookies().getCookie("JSESSIONID"));
    }
}
