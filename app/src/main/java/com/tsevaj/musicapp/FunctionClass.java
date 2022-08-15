package com.tsevaj.musicapp;

import static org.jaudiotagger.audio.AudioFileIO.read;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioMetadata;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.wav.WavTag;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
        ArrayList<MyList> li = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);

        if (MainActivity.savedList == null) loadList(player.main, (Activity) activity);

        SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
        for (MyList item: MainActivity.savedList) {
            // filtering for favorites etc..
            if (!filter.equals("")) {
                String wanted = settings.getString(filter, "");
                if ((!Arrays.asList(wanted.split("\n")).contains(item.getHead()))) continue;
            }
            if (!item.getHead().toLowerCase().contains(nameFilter.toLowerCase())) {
                continue;
            }
            int FILTER_LENGTH = FILTER_SECONDS * 1000;
            if ((item.getLocation().contains(musicFolder)) && item.getDuration() > FILTER_LENGTH) {
                if (REVERSE_ORDER) {
                    li.add(0, item);
                } else {
                    li.add(item);
                }
            }
        }
        String favorites = settings.getString("REPLAY", "");
        if (REVERSE_ORDER) {
            if (favorites.equals("LENGTH")) {
                Collections.sort(li, Comparator.comparingInt(MyList::getDuration));
            } else if (favorites.equals("TITLE")) {
                Collections.sort(li, (o2, o1) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
            } else {
                Collections.reverse(li);
            }
        } else {
            if (favorites.equals("LENGTH")) {
                Collections.sort(li, (o1, o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));
            } else if (favorites.equals("TITLE")) {
                Collections.sort(li, (o1, o2) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
            } else {
                Collections.reverse(li);
            }
        }
        CustomAdapter adapter = new CustomAdapter(li, activity, c, player, filter) {};
        recyclerView.setAdapter(adapter);
        if (recyclerView.getItemDecorationCount() == 0) recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
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
    public static void loadList(MainActivity main, Activity activity) {
        ArrayList<MyList> list = new ArrayList<>();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = main.getContentResolver().query(songUri, null, null, null, null);
        int count = 0;
        File cFile;
        long GetTime = 0;
        long startTime;
        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songLength = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songSize = songCursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int songType = songCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            int songModified = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
            do {
                String currentTitle = songCursor.getString(songTitle);
                int currentLength = songCursor.getInt(songLength);
                String currentLocation = songCursor.getString(songLocation);
                int currentSize = songCursor.getInt(songSize);
                String currentType = songCursor.getString(songType);
                long currentModified = songCursor.getLong(songModified);
                String currentArtist = "<unknown>";
                MyList myList = new MyList(
                        currentTitle,
                        milliSecondsToTime(currentLength),
                        currentArtist,
                        currentSize,
                        currentType,
                        currentModified,
                        currentLocation, currentLength);
                list.add(myList);
            } while (songCursor.moveToNext());
        }
        assert songCursor != null;
        songCursor.close();
        MainActivity.savedList = list;
        getRest(main.player, activity);
    }
    public static void getRest(MusicPlayer player, Activity activity) {
        @SuppressLint("NotifyDataSetChanged") Thread t = new Thread(() -> {
            int count2 = 0;
            for (MyList item: MainActivity.savedList) {
                try {
                    String Artist = read(new File(item.getLocation())).getTag().getFirst(FieldKey.ARTIST);
                    MainActivity.savedList.get(count2).setArtist(Artist);
                    count2 += 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            activity.runOnUiThread(() -> player.adapter.notifyDataSetChanged());
        });
        t.start();
    }
}
