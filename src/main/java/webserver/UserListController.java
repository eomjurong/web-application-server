package webserver;

import java.io.IOException;
import java.util.Collection;

import db.DataBase;
import model.User;

public class UserListController extends AbstractController {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
        if (!isLogined(request.getSession())) {
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

    private boolean isLogined(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return false;
        }
        return true;
    }
}
