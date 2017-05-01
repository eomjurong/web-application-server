package webserver;

import java.io.IOException;
import java.util.Map;

import db.DataBase;
import model.User;

public class LoginController extends AbstractController {

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> params = request.getParams();

        User user = DataBase.findUserById(params.get("userId"));
        if (user != null && user.getPassword().equals(params.get("password"))) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect("/index.html");
        }

        response.sendRedirect("/user/login_failed.html");
    }
}
