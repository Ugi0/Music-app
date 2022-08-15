package com.tsevaj.musicapp;

import static com.tsevaj.musicapp.FunctionClass.milliSecondsToTime;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;
import android.util.Log;

import java.sql.Date;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;

public class MyList {
    private final String head;
    private String desc;
    private final String location;
    private final int duration;
    private String artist;
    private final String currentSize;
    private final String type;
    private final String dateModified;
    private String locationFolder;

    public MyList(String currentTitle, String length, String currentArtist, int currentSize, String currentType, long currentModified, String currentLocation, int currentLength) {
        this.head = currentTitle;
        this.desc = length+"   "+currentArtist;
        this.location = currentLocation;
        this.duration = currentLength;
        this.artist = currentArtist;
        this.currentSize = humanReadableByteCountSI(currentSize);
        this.type = currentType;
        this.dateModified = DateFormat.format("MM/dd/yyyy", new Date(currentModified*1000)).toString();
        try { this.locationFolder = currentLocation.split("/", 0)[currentLocation.split("/", 0).length-2]; }
        catch (Exception ignored) {this.locationFolder = "<unknown>";}

    }

    //getters
    public String getHead() {
        return head;
    }

    public String getDesc() {
        return desc;
    }

    public String getLocation() { return location; }

    public String getLocationFolder() { return locationFolder; }

    public int getDuration() { return duration; }

    public String getArtist() {
        if (artist.equals("")) return "<unknown>";
        return artist; }

    public String getCurrentSize() { return currentSize; }

    public String getType() { return type; }

    public String getDateModified() { return dateModified; }

    public void setArtist(String a) {
        this.artist = a;
        if (this.artist.equals("")) this.desc = milliSecondsToTime(this.duration)+"   "+"<unknown>";
        else { this.desc = milliSecondsToTime(this.duration)+"   "+this.artist; }
    }

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