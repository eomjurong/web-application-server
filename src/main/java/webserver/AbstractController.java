package webserver;

import java.io.IOException;

public abstract class AbstractController implements Controller {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        HttpMethod method = request.getMethod();
        if (HttpMethod.GET.equals(method)) {
            doGet(request, response);
        } else {
            doPost(request, response);
        }
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {
    }
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
    }
}
