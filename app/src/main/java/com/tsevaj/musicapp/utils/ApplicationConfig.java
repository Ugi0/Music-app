package com.tsevaj.musicapp.utils;

import java.util.HashMap;

public class ApplicationConfig {
    private HashMap<String, String> config;

    public static final String TEXT_COLOR_KEY = "text_color";

    public ApplicationConfig(String savedConfig) {
        for (String keyValuepair : savedConfig.split(";")) {
            String key = keyValuepair.split(":")[0];
            String value = keyValuepair.split(":")[1];
            config.put(key, value);
        }
    }

    public void saveConfig() {
        //TODO Save config to permanent storage
    }

    public void setTextColor(String color) {
        this.config.put(TEXT_COLOR_KEY, color);    }

    public String getTextColor() {
        return this.config.get(TEXT_COLOR_KEY);
    }
}
