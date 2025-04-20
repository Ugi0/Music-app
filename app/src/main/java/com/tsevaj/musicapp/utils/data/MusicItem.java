package com.tsevaj.musicapp.utils.data;

import static com.tsevaj.musicapp.utils.files.MusicGetter.milliSecondsToTime;

import android.annotation.SuppressLint;
import android.text.format.DateFormat;

import androidx.annotation.NonNull;

import java.sql.Date;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MusicItem implements Comparable<MusicItem> {
    private final String title;
    private final String location;
    private final int duration;
    @Setter
    private String artist;
    private final String currentSize;
    private final String type;
    private final String hash;
    private final long dateModified;
    private final String dateModifiedString;
    @Setter
    private boolean favorited;
    private String locationFolder;

    public MusicItem(String currentTitle, String length, String currentArtist, int currentSize, String currentType, long currentModified, String currentLocation, int currentLength, boolean favorited) {
        this.title = currentTitle;
        this.location = currentLocation;
        this.duration = currentLength;
        this.artist = currentArtist;
        this.currentSize = humanReadableByteCountSI(currentSize);
        this.type = currentType;
        this.hash = String.valueOf(title.hashCode()+duration+location.hashCode()+artist.hashCode());
        this.dateModified = currentModified;
        this.dateModifiedString = DateFormat.format("MM/dd/yyyy", new Date(currentModified*1000)).toString();
        this.favorited = favorited;
        try { this.locationFolder = currentLocation.split("/", 0)[currentLocation.split("/", 0).length-2]; }
        catch (Exception ignored) {this.locationFolder = "<unknown>";}

    }

    public String getDesc() {
        return String.format("%s %s", milliSecondsToTime(this.duration), getArtist());
    }

    public String getArtist() {
        if (artist.isEmpty()) return "<unknown>";
        return artist; }

    @NonNull
    public String toString(){
        return this.title;
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

    @Override
    public int compareTo(MusicItem o) {
        if (o == null) {
            return -1;
        }
        if (o.duration > this.duration) {
            return 1;
        }
        else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof MusicItem)) return false;
        MusicItem item = (MusicItem) o;
        return Objects.equals(this.hash, item.getHash());
    }
}