package com.tsevaj.musicapp.utils;

import static org.jaudiotagger.audio.AudioFileIO.read;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.tsevaj.musicapp.adapters.CustomAdapter;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.adapters.PlayListsAdapter;
import com.tsevaj.musicapp.fragments.PlaylistsFragment;

import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FunctionClass {
    @SuppressLint("NotifyDataSetChanged")
    public static void getMusicAndSet(RecyclerView recyclerView, Context activity, MusicPlayer player, FragmentActivity c, String filter, String nameFilter) {
        ArrayList<MusicItem> li = getMusic(activity, player, c, filter, nameFilter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        CustomAdapter adapter = new CustomAdapter(li, activity, c, player, filter) {};
        recyclerView.setAdapter(adapter);
        if (recyclerView.getItemDecorationCount() == 0) recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
        player.main.changePlayingList(li);
        player.recyclerview = recyclerView;
        player.adapter = adapter;
        adapter.reset();
        adapter.notifyDataSetChanged();
    }

    public static ArrayList<MusicItem> getMusic(Context activity, MusicPlayer player, FragmentActivity c, String filter, String nameFilter) {
        final int FILTER_SECONDS = activity.getSharedPreferences("SAVEDATA", 0).getInt("MIN_SIZE",120);
        boolean REVERSE_ORDER = c.getSharedPreferences("SAVEDATA", 0).getBoolean("ASCENDING", true);
        final String musicFolder = activity.getSharedPreferences("SAVEDATA", 0).getString("SONG_FOLDER","");
        ArrayList<MusicItem> li = new ArrayList<>();

        if (MainActivity.wholeSongList == null) loadList(player.main, (Activity) activity);

        SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
        for (MusicItem item: MainActivity.wholeSongList) {
            // filtering for favorites etc..
            if (!filter.equals("")) {
                String wanted = settings.getString(filter, "");
                if ((!Arrays.asList(wanted.split("\n")).contains(item.getHead()))) continue;
            }
            if (!(item.getHead().toLowerCase().contains(nameFilter.toLowerCase()) || item.getArtist().toLowerCase().contains(nameFilter.toLowerCase()))) {
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
        ArrayList<MusicItem> li2 = new ArrayList<>(MainActivity.wholeSongList);
        if (REVERSE_ORDER) {
            if (favorites.equals("LENGTH")) {
                li.sort(Comparator.comparingInt(MusicItem::getDuration));
                li2.sort(Comparator.comparingInt(MusicItem::getDuration));
            } else if (favorites.equals("TITLE")) {
                li.sort((o2, o1) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
                li2.sort((o2, o1) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
            } else {
                Collections.reverse(li);
                Collections.reverse(li2);
            }
        } else {
            if (favorites.equals("LENGTH")) {
                li.sort((o1, o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));
                li2.sort((o1, o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));
            } else if (favorites.equals("TITLE")) {
                li.sort((o1, o2) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
                li2.sort((o1, o2) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
            } else {
                Collections.reverse(li);
                Collections.reverse(li2);
            }
        }
        MainActivity.changeSongListWholeList(li2);
        return li;
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
        ArrayList<MusicItem> list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
        list.add(new MusicItem("Create a new playlist","","",0, "", 0, "", 0));
        if (!settings.getString("PLAYLISTS", "").isEmpty()) {
            for (String playlist : settings.getString("PLAYLISTS", "").split("\n")) {
                list.add(0, new MusicItem(playlist, "","",0, "", 0, "", 0));
            }
        }
        PlayListsAdapter adapter = new PlayListsAdapter(list, c, player, playlistsFragment) {};
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
    }
    @SuppressLint("NewApi")
    public static void loadList(MainActivity main, Activity activity) {
        final int FILTER_SECONDS = activity.getSharedPreferences("SAVEDATA", 0).getInt("MIN_SIZE",120);
        int FILTER_LENGTH = FILTER_SECONDS * 1000;
        ArrayList<MusicItem> list = new ArrayList<>();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = main.getContentResolver().query(songUri, null, null, null, null);
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
                MusicItem myList = new MusicItem(
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
        list.removeIf(n -> (n.getDuration() < FILTER_LENGTH));
        MainActivity.wholeSongList = list;
        getAuthors(main.player, activity);
    }
    public static void getAuthors(MusicPlayer player, Activity activity) {
        @SuppressLint("NotifyDataSetChanged") Thread t = new Thread(() -> {
            for (MusicItem item: MainActivity.wholeSongList) {
                try {
                    String Artist = read(new File(item.getLocation())).getTag().getFirst(FieldKey.ARTIST);
                    item.setArtist(Artist);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            activity.runOnUiThread(() -> player.adapter.notifyDataSetChanged());
        });
        t.start();
    }
}
