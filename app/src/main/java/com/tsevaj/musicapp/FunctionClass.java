package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class FunctionClass {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void getMusic(RecyclerView recyclerView, Context activity, MusicPlayer player, FragmentActivity c, String filter, String nameFilter) {
        final int FILTER_SECONDS = activity.getSharedPreferences("SAVEDATA", 0).getInt("MIN_SIZE",120);
        boolean REVERSE_ORDER = c.getSharedPreferences("SAVEDATA", 0).getBoolean("ASCENDING", true);
        final String musicFolder = activity.getSharedPreferences("SAVEDATA", 0).getString("SONG_FOLDER","");
        ArrayList<MyList> list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        ContentResolver contentResolver = activity.getApplicationContext().getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLength = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songSize = songCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int songType = songCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            int songModified = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
            //type, modified
            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                int currentLength = songCursor.getInt(songLength);
                String currentLocation = songCursor.getString(songLocation);
                int currentSize = songCursor.getInt(songSize);
                String currentType = songCursor.getString(songType);
                long currentModified = songCursor.getLong(songModified);
                // filtering for favorites etc..
                if (!filter.equals("")) {
                    SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
                    String wanted = settings.getString(filter, "");
                    if ((!Arrays.asList(wanted.split("\n")).contains(currentTitle))) continue;
                }
                if (!currentTitle.toLowerCase().contains(nameFilter.toLowerCase())) {
                    continue;
                }
                //
                MyList myList = new MyList(
                        currentTitle,
                        milliSecondsToTime(currentLength),
                        currentArtist,
                        currentSize,
                        currentType,
                        currentModified,
                        currentLocation, currentLength);
                int FILTER_LENGTH = FILTER_SECONDS * 1000;
                if ((currentLocation.contains(musicFolder)) && currentLength > FILTER_LENGTH) {
                    if (REVERSE_ORDER) {
                        list.add(0, myList);
                    } else {
                        list.add(myList);
                    }
                }
            } while (songCursor.moveToNext());
        }
        assert songCursor != null;
        songCursor.close();

        //Sorting of the music list
        SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
        String favorites = settings.getString("REPLAY", "");

        if (REVERSE_ORDER) {
            if (favorites.equals("LENGTH")) {
                Collections.sort(list, Comparator.comparingInt(MyList::getDuration));
            }
            else if (favorites.equals("TITLE")) {
                Collections.sort(list, (o2,o1) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
            }
            else {
                Collections.reverse(list);
            }
        }
        else {
            if (favorites.equals("LENGTH")) {
                Collections.sort(list, (o1,o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));
            }
            else if (favorites.equals("TITLE")) {
                Collections.sort(list, (o1,o2) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
            }
            else {
                Collections.reverse(list);
            }
        }

        CustomAdapter adapter = new CustomAdapter(list, activity, c, player) {};
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
        player.recyclerview = recyclerView;
        player.adapter = adapter;
    }
    @SuppressLint("DefaultLocale")
    public static String milliSecondsToTime(int time) {
        int first = (time/1000)/60;
        int second = (time/1000)%60;
        if (first >= 60) {
            int hours = first / 60;
            first = first % 60;
            return hours+":"+String.format("%02d", first)+":"+String.format("%02d", second);
        }
        return String.format("%02d", first)+":"+String.format("%02d", second);
    }
    public static void playlistView(RecyclerView recyclerView, Context activity, MusicPlayer player, FragmentActivity c, PlaylistsFragment playlistsFragment) {
        ArrayList<MyList> list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
        list.add(new MyList("Create a new playlist","","",0, "", 0, "", 0));
        if (!settings.getString("PLAYLISTS", "").isEmpty()) {
            for (String playlist : settings.getString("PLAYLISTS", "").split("\n")) {
                list.add(0, new MyList(playlist, "","",0, "", 0, "", 0));
            }
        }

        PlayListsAdapter adapter = new PlayListsAdapter(list, c, player, playlistsFragment) {};
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
    }
}
