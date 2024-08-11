package com.tsevaj.musicapp.utils.enums;

public enum SortOptions {
    DATE("DATE"),
    LENGTH("LENGTH"),
    TITLE("TITLE"),
    RANDOM("RANDOM");

    public static final String NAME = "REPLAY";
    private String label;

    private SortOptions(String label) {
        this.label = label;
    }
}
