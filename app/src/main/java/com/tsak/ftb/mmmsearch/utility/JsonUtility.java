package com.tsak.ftb.mmmsearch.utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtility {

    private JsonUtility() {
        throw new AssertionError();
    }

    public static String getString(Object target, String invalid, Object... dic) {

        Object value = JsonUtility.getObject(target, dic);
        if (value instanceof String) {
            return (String) value;
        }
        return invalid;
    }

    public static Integer getInteger(Object target, Integer invalid, Object... dic) {

        Object value = JsonUtility.getObject(target, dic);
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return invalid;
    }

    public static Long getLong(Object target, Long invalid, Object... dic) {

        Object value = JsonUtility.getObject(target, dic);
        if (value instanceof Long) {
            return (Long) value;
        }
        return invalid;
    }


    public static JSONObject getJson(Object target, Object... dic) {

        Object value = JsonUtility.getObject(target, dic);
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        return new JSONObject();
    }

    public static JSONArray getArray(Object target, Object... dic) {

        Object value = JsonUtility.getObject(target, dic);
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        return new JSONArray();
    }

    private static Object getObject(Object target, Object... dic) {

        Object value = target;
        boolean isValid = true;

        if (dic.length == 0) {
            isValid = false;
        }

        for (Object key : dic) {
            if (value instanceof JSONObject && key instanceof String) {
                if (((JSONObject) value).has((String) key)) {
                    try {
                        value = ((JSONObject) value).get((String) key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                isValid = false;
                break;
            } else if(value instanceof JSONArray && key instanceof Integer) {
                Integer index = (Integer) key;
                if (0 <= index && index < ((JSONArray) value).length()) {
                    try {
                        value = ((JSONArray) value).get((Integer) key);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                isValid = false;
                break;
            } else {
                isValid = false;
                break;
            }
        }

        if (isValid) {
            return value;
        }
        return new Object();
    }
}
