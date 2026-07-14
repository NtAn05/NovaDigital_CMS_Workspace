package com.example.demo.audit.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class JsonDiffUtils {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    // Danh sách các trường cần che giấu (Data Masking)
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList("password", "token", "secret"));

    /**
     * So sánh 2 object (Old State và New State) và trả về danh sách các trường thay đổi dưới dạng JSON string.
     * Nếu không có thay đổi, trả về null.
     */
    public static String getDiff(Object oldState, Object newState) {
        if (oldState == null && newState == null) return null;

        Map<String, Object> oldMap = oldState == null ? Collections.emptyMap() : toFlatMap(oldState);
        Map<String, Object> newMap = newState == null ? Collections.emptyMap() : toFlatMap(newState);

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(oldMap.keySet());
        allKeys.addAll(newMap.keySet());

        List<Map<String, Object>> diffs = new ArrayList<>();

        for (String key : allKeys) {
            // Bỏ qua các trường metadata hệ thống để tránh loãng thông tin
            if (key.equalsIgnoreCase("id") || key.equalsIgnoreCase("createdAt") || 
                key.equalsIgnoreCase("updatedAt") || key.equalsIgnoreCase("version")) {
                continue;
            }

            Object oldVal = oldMap.get(key);
            Object newVal = newMap.get(key);
            boolean changed = !Objects.equals(oldVal, newVal);

            addChange(diffs, key, oldVal, newVal, changed);
        }

        if (diffs.isEmpty()) {
            return null;
        }

        try {
            return mapper.writeValueAsString(diffs);
        } catch (Exception e) {
            return "[]";
        }
    }

    private static void addChange(List<Map<String, Object>> diffs, String key, Object oldVal, Object newVal, boolean changed) {
        Map<String, Object> change = new HashMap<>();
        change.put("field", key);
        change.put("changed", changed);
        if (SENSITIVE_FIELDS.contains(key.toLowerCase())) {
            change.put("old", oldVal != null ? "********" : null);
            change.put("new", newVal != null ? "********" : null);
        } else {
            change.put("old", oldVal != null ? oldVal.toString() : null);
            change.put("new", newVal != null ? newVal.toString() : null);
        }
        diffs.add(change);
    }

    private static Map<String, Object> toFlatMap(Object obj) {
        try {
            // Chuyển Object thành Map dạng phẳng (Key-Value)
            // Trong thực tế nếu object có cấu trúc lồng nhau phức tạp, cần làm phẳng (flatten map).
            // Ở đây đơn giản hóa bằng TypeReference Map String Object.
            return mapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
        } catch (IllegalArgumentException e) {
            return Collections.emptyMap();
        }
    }
}
