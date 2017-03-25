package webserver;

import java.io.IOException;
import java.util.Map;

import db.DataBase;
import model.User;

public class LoginController extends AbstractController {

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> params = request.getParams();
        boolean isSuccess = false;

        User user = DataBase.findUserById(params.get("userId"));
        if (user != null && user.getPassword().equals(params.get("password"))) {
            isSuccess = true;
        }

        response.addHeader("Set-Cookie", "logined="+ isSuccess +"; Path=/ \r\n");
        response.sendRedirect(isSuccess ? "/index.html" : "/user/login_failed.html");
    }
}
