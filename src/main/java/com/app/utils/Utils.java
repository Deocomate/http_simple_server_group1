package com.app.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    /**
     * Phân tích cú pháp một chuỗi query thành một Map.
     *
     * @param query Chuỗi query (ví dụ: key1=value1&key2=value2)
     * @return Map chứa các cặp key-value của query
     */
    public static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            // Sử dụng limit=2 để đảm bảo chỉ tách thành hai phần: key và value
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = decodeURLComponent(keyValue[0]);
                String value = decodeURLComponent(keyValue[1]);
                result.put(key, value);
            } else if (keyValue.length == 1) {
                String key = decodeURLComponent(keyValue[0]);
                result.put(key, "");
            }
        }
        return result;
    }

    /**
     * Phân tích cú pháp dữ liệu POST từ InputStream thành một Map.
     *
     * @param is InputStream của body request
     * @return Map chứa các cặp key-value của tham số
     * @throws IOException nếu có lỗi khi đọc dữ liệu
     */
    public static Map<String, String> parsePostQuery(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;

        // Đọc toàn bộ dữ liệu từ InputStream
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String postData = sb.toString();
        return parseQuery(postData);
    }

    /**
     * Giải mã một thành phần URL-encoded.
     *
     * @param s Chuỗi đã được URL-encode
     * @return Chuỗi đã được giải mã
     */
    private static String decodeURLComponent(String s) {
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            // Nếu có lỗi khi giải mã, trả về chuỗi gốc
            return s;
        }
    }
}
