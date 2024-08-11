package com.tsevaj.musicapp.utils.files;

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
import com.tsevaj.musicapp.utils.MusicItem;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.PlaylistItem;

import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MusicGetter {
    public static ArrayList<MusicItem> getMusic(Context context, String filter, String nameFilter) {
        boolean REVERSE_ORDER = context.getSharedPreferences("SAVEDATA", 0).getBoolean("ASCENDING", true);

        //if (MainActivity.wholeSongList == null) {
        //    MainActivity.wholeSongList = loadList(context);
        //    getAuthors(player, context);
        //6}

        SharedPreferences settings = context.getSharedPreferences("SAVEDATA", 0);

        ArrayList<MusicItem> li = filterMusicList(context, filter, nameFilter);

        String order = settings.getString("REPLAY", "");

        return sortMusicList(li, order, REVERSE_ORDER);
    }

    //TODO Remove side effects from all methods

    public static ArrayList<MusicItem> filterMusicList(Context c, String filter, String nameFilter) {
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

    private static ArrayList<MusicItem> nameFilterMusicList(ArrayList<MusicItem> list, String nameFilter) {
        return (ArrayList<MusicItem>) list.stream().filter(musicItem ->
                musicItem.getTitle().toLowerCase().contains(nameFilter.toLowerCase()) || musicItem.getArtist().toLowerCase().contains(nameFilter.toLowerCase()))
                .collect(Collectors.toList());
    }

    private static ArrayList<MusicItem> sortMusicList(ArrayList<MusicItem> list, String order, boolean reverse) {
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

    /**
     * Load music list. This method should only be called once on start. If user refreshes the view, call this method to fetch songs again
     * @param context
     */
    public static ArrayList<MusicItem> loadList(Context context) {
        final int FILTER_SECONDS = context.getSharedPreferences("SAVEDATA", 0).getInt("MIN_SIZE",60);
        int FILTER_LENGTH = FILTER_SECONDS * 1000;
        ArrayList<MusicItem> list = new ArrayList<>();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = context.getContentResolver().query(songUri, null, null, null, null);
        ArrayList<String> favorites = getFavorites(context);
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
                        currentTitle.replaceAll(context.getString(R.string.Regex_replace_filenameNumber), ""), //Replace SongName(1) with just SongName
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
        //MainActivity.wholeSongList = list;

        return list;
    }

    /**
     * Runs a separate thread to get the authors for the songs
     * @param adapter adapter that will be refreshed after getting authors is done
     * @param context context of the action
     */
    public static void getAuthors(RecyclerView.Adapter<?> adapter, Context context) {
        @SuppressLint("NotifyDataSetChanged") Thread t = new Thread(() -> {
            for (MusicItem item: MainActivity.wholeSongList) {
                try {
                    String Artist = read(new File(item.getLocation())).getTag().getFirst(FieldKey.ARTIST);
                    item.setArtist(Artist);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ((Activity) context).runOnUiThread(adapter::notifyDataSetChanged);
        });
        t.start();
    }

    public static ArrayList<String> getFavorites(Context context) {
        SharedPreferences settings = context.getSharedPreferences("SAVEDATA", 0);
        String favorites = settings.getString("FAVORITES", "");
        return new ArrayList<>(Arrays.asList(favorites.split("\n")));
    }

    public static void addToFavorites(Context context, String s) {
        SharedPreferences settings = context.getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        String favorites = settings.getString("FAVORITES", "");
        editor.putString("FAVORITES", favorites + "\n" + s);
        editor.apply();
    }

    public static void setFavorites(Context context, ArrayList<String> li) {
        SharedPreferences settings = context.getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("FAVORITES", String.join("\n",li));
        editor.apply();
    }

    public static void removeFromFavorites(Context context, String s) {
        SharedPreferences settings = context.getSharedPreferences("SAVEDATA", 0);
        String favorites = settings.getString("FAVORITES", "");
        ArrayList<String> li = new ArrayList<>();
        for (String i: favorites.split("\n")) {
            if (!i.equals(s)) {
                li.add(i);
            }
        }
        setFavorites(context, li);
    }
}
