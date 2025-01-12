package com.app.utils;

import java.io.IOException;
import java.io.InputStream;

public class QueryUtils {
    public static String convertPostQuery(InputStream ios) throws IOException {
        StringBuilder postQuery = new StringBuilder();
        int i;
        while ((i = ios.read()) != -1) {
            postQuery.append((char) i);
        }
        return postQuery.toString();
    }
}
