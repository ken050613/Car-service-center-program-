package com.apu.asc.util;

import java.util.ArrayList;
import java.util.List;

public final class ServiceCodeParser {
    private ServiceCodeParser() {}

    public static List<String> parseCommaSeparated(String codes) {
        List<String> list = new ArrayList<>();
        if (codes == null || codes.isEmpty()) {
            return list;
        }
        for (String p : codes.split(",")) {
            String t = p.trim().toUpperCase();
            if (!t.isEmpty()) {
                list.add(t);
            }
        }
        return list;
    }
}
