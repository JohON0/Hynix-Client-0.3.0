package io.hynix.ui.configui.listusers;

import java.util.ArrayList;
import java.util.List;

public class Users {
    private String message;
    private String url; // Цвет может быть представлен как целое число

    public Users(String message, String url) {
        this.message = message;
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public String getUrl() {
        return url;
    }
}
