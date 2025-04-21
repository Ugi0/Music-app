package com.tsevaj.musicapp.utils;

import com.tsevaj.musicapp.MainActivity;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class ApplicationConfig {
    private HashMap<String, String> config;
    public static File BackgroundDestinationPath;

    public static final String TEXT_COLOR_KEY = "text_color";

    public ApplicationConfig(MainActivity main, String savedConfig) {
        config = new HashMap<>();
        for (String keyValuepair : savedConfig.split(";")) {
            if (keyValuepair.isEmpty()) continue;
            String key = keyValuepair.split(":")[0];
            String value = keyValuepair.split(":")[1];
            config.put(key, value);
        }

        BackgroundDestinationPath = main.getExternalFilesDir("");
        Objects.requireNonNull(BackgroundDestinationPath.getParentFile()).mkdirs();
    }

    public void saveConfig() {
        //TODO Save config to permanent storage
    }

    public void setTextColor(String color) {
        this.config.put(TEXT_COLOR_KEY, color);    }

    public String getTextColor() {
        return config.getOrDefault(TEXT_COLOR_KEY, "#000000");
    }
}
