package com.app.utils;

import java.util.Map;
import java.util.HashMap;

public class CookieUtils {
    private static Map<String, String> cookieStore = new HashMap<>();

    public static String createCookie(String username) {
        return "session=" + username;
    }

    public static boolean validateCookie(String cookie) {
        return "session=admin".equals(cookie);
    }

    public static Map<String, String> parseCookies(String cookieHeader) {
        Map<String, String> cookieMap = new HashMap<>();
        String[] cookies = cookieHeader.split(";");

        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2) {
                String name = parts[0].trim();
                String value = parts[1].trim();
                cookieMap.put(name, value);
            }
        }

        return cookieMap;
    }

}
