package webserver;

import java.util.HashMap;
import java.util.Map;

public class RequestMapping {

    private static Map<String, Controller> controllers = new HashMap<String, Controller>();

    static {
        controllers.put("/user/list", new UserListController());
        controllers.put("/user/create", new CreateUserController());
        controllers.put("/user/login", new LoginController());
    }

    public static Controller getController(String key) {
        return controllers.get(key);
    }
}
