package com.tsak.ftb.mmmsearch.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SpManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private final static int DEFAULT_INT = 0;
    private final static String DEFAULT_STRING = "";

    public enum INT_KEY {
        SEARCH_WORD_INDEX(DEFAULT_INT),
        OPEN_APP_FLAG(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK),
        ;

        private int defaultValue;

        public int defaultValue() {
            return defaultValue;
        }

        INT_KEY(int defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    public enum STRING_KEY {
        APP_NAME(DEFAULT_STRING),
        PACKAGE_NAME(DEFAULT_STRING),
        CLASS_NAME(DEFAULT_STRING),
        OPEN_PROTOCOL(DEFAULT_STRING),
        ;

        private String defaultValue;

        STRING_KEY(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }

    private SpManager(SharedPreferences sharedPreferences, SharedPreferences.Editor editor) {

        this.sharedPreferences = sharedPreferences;
        this.editor = editor;
    }

    public static SpManager newInstance(Context context) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                context.getApplicationContext());
        return new SpManager(sharedPreferences, sharedPreferences.edit());
    }

    public void putInt(INT_KEY key, int value) {

        editor.putInt(key.name(), value);
        editor.apply();
    }

    public int getInt(INT_KEY key) {

        return sharedPreferences.getInt(key.name(), key.defaultValue);
    }

    public void putString(STRING_KEY key, String value) {

        editor.putString(key.name(), value);
        editor.apply();
    }

    public String getString(STRING_KEY key) {

        return sharedPreferences.getString(key.name(), key.defaultValue);
    }
}
