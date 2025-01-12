package com.app.utils;

public class AuthUtils {
    public static boolean authenticate(String username, String password) {
        return "admin".equals(username) && "admin".equals(password);
    }
}
