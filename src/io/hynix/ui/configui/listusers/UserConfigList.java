package io.hynix.ui.configui.listusers;

import java.util.ArrayList;
import java.util.List;

public class UserConfigList {
    private static List<Users> configs = new ArrayList<>();

    public static void add(String message, String url) {
        configs.add(new Users(message, url));
    }

    public static List<Users> getConfigs() {
        return configs;
    }
}
