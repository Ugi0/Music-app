package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;

import java.sql.Date;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class MyList {
    private final String head;
    private final String desc;
    private final String location;
    private final int duration;
    private final String artist;
    private final String currentSize;
    private final String type;
    private final String dateModified;

    public MyList(String currentTitle, String length, String currentArtist, int currentSize, String currentType, long currentModified, String currentLocation, int currentLength) {
        this.head = currentTitle;
        this.desc = length+"   "+currentArtist;
        this.location = currentLocation;
        this.duration = currentLength;
        this.artist = currentArtist;
        this.currentSize = humanReadableByteCountSI(currentSize);
        this.type = currentType;
        this.dateModified = DateFormat.format("MM/dd/yyyy", new Date(currentModified*1000)).toString();
    }

    //getters
    public String getHead() {
        return head;
    }

    public String getDesc() {
        return desc;
    }

    public String getLocation() { return location; }

    public int getDuration() { return duration; }

    public String getArtist() { return artist; }

    public String getCurrentSize() { return currentSize; }

    public String getType() { return type; }

    public String getDateModified() { return dateModified; }

    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}