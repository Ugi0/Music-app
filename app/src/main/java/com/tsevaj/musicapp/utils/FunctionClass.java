package com.tsevaj.musicapp.utils;

import static org.jaudiotagger.audio.AudioFileIO.read;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.adapters.PlayListsAdapter;
import com.tsevaj.musicapp.fragments.PlaylistsFragment;

import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionClass {
    public static ArrayList<MusicItem> getMusic(Context activity, MusicPlayer player, FragmentActivity c, String filter, String nameFilter) {
        boolean REVERSE_ORDER = c.getSharedPreferences("SAVEDATA", 0).getBoolean("ASCENDING", true);

        if (MainActivity.wholeSongList == null) loadList(player.main, (Activity) activity);

        SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);

        ArrayList<MusicItem> li = filterMusicList(c, filter, nameFilter);

        String order = settings.getString("REPLAY", "");

        return sortMusicList(li, order, REVERSE_ORDER);
    }

    public static ArrayList<MusicItem> filterMusicList(FragmentActivity c, String filter, String nameFilter) {
        SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
        final int FILTER_SECONDS = settings.getInt("MIN_SIZE",120);
        boolean REVERSE_ORDER = settings.getBoolean("ASCENDING", true);
        final String musicFolder = settings.getString("SONG_FOLDER","");

        ArrayList<MusicItem> li = new ArrayList<>(MainActivity.wholeSongList);

        if (filter.equals("FAVORITES")) {
            li.removeIf(item -> !item.getFavorited());
        } else if (filter.startsWith("PLAYLIST")) {
            List<String> songs = Arrays.asList(settings.getString(filter, "").split("\n"));
            li.removeIf(item -> !songs.contains(item.getTitle()));
        }

        li.removeIf(item -> !(item.getTitle().toLowerCase().contains(nameFilter.toLowerCase()) || item.getArtist().toLowerCase().contains(nameFilter.toLowerCase())));

        int FILTER_LENGTH = FILTER_SECONDS * 1000;
        li.removeIf(item -> !(item.getLocation().contains(musicFolder)) && item.getDuration() > FILTER_LENGTH);

        if (REVERSE_ORDER) {
            Collections.reverse(li);
        }
        return li;
    }

    public static ArrayList<MusicItem> nameFilterMusicList(ArrayList<MusicItem> list, String nameFilter) {
        return (ArrayList<MusicItem>) list.stream().filter(musicItem ->
                musicItem.getTitle().toLowerCase().contains(nameFilter.toLowerCase()) || musicItem.getArtist().toLowerCase().contains(nameFilter.toLowerCase()))
                .collect(Collectors.toList());
    }

    public static ArrayList<MusicItem> sortMusicList(ArrayList<MusicItem> list, String order, boolean reverse) {
        if (reverse) {
            if (order.equals("LENGTH")) {
                list.sort(Comparator.comparingInt(MusicItem::getDuration));
            } else if (order.equals("TITLE")) {
                list.sort((o2, o1) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
            }
        } else {
            if (order.equals("LENGTH")) {
                list.sort((o1, o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));
            } else if (order.equals("TITLE")) {
                list.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
            } else {
                Collections.reverse(list);
            }
        }
        return list;
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
        ArrayList<PlaylistItem> list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
        list.add(new PlaylistItem());
        if (!settings.getString("PLAYLISTS", "").isEmpty()) {
            for (String playlist : settings.getString("PLAYLISTS", "").split("\n")) {
                list.add(new PlaylistItem(playlist));
            }
        }
        PlayListsAdapter adapter = new PlayListsAdapter(list, c, player, playlistsFragment) {};
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
    }
    public static void loadList(MainActivity main, Activity activity) {
        final int FILTER_SECONDS = activity.getSharedPreferences("SAVEDATA", 0).getInt("MIN_SIZE",60);
        int FILTER_LENGTH = FILTER_SECONDS * 1000;
        ArrayList<MusicItem> list = new ArrayList<>();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = main.getContentResolver().query(songUri, null, null, null, null);
        ArrayList<String> favorites = main.getFavorites();
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
                        currentTitle.replaceAll(main.getString(R.string.Regex_replace_filenameNumber), ""), //Replace SongName(1) with just SongName
                        milliSecondsToTime(currentLength),
                        currentArtist,
                        currentSize,
                        currentType,
                        currentModified,
                        currentLocation, currentLength,
                        favorites.contains(currentTitle)
                );
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
