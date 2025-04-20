package com.tsevaj.musicapp.utils.files;

import static com.tsevaj.musicapp.MainActivity.wholeSongList;
import static org.jaudiotagger.audio.AudioFileIO.read;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.recyclerview.widget.RecyclerView;

import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.SharedPreferencesHandler;
import com.tsevaj.musicapp.utils.data.MusicItem;
import com.tsevaj.musicapp.utils.data.SortValue;
import com.tsevaj.musicapp.utils.enums.MusicListType;
import com.tsevaj.musicapp.utils.enums.SortOption;

import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MusicGetter {
    public static List<MusicItem> getMusic(Context context, SortValue filter, String nameFilter) {
        boolean REVERSE_ORDER = SharedPreferencesHandler.sharedPreferences.getBoolean("ASCENDING", true);

        List<MusicItem> li = getFilteredMusicList(context, filter, nameFilter);

        String order = SharedPreferencesHandler.sharedPreferences.getString(SortOption.NAME, "");

        return getSortedMusicList(li, SortOption.valueOf(order), REVERSE_ORDER);
    }

    //TODO Remove side effects from all methods

    private static List<MusicItem> getFilteredMusicList(Context c, SortValue filter, String nameFilter) {
        final int FILTER_SECONDS = SharedPreferencesHandler.sharedPreferences.getInt("MIN_SIZE",120);
        boolean REVERSE_ORDER = SharedPreferencesHandler.sharedPreferences.getBoolean("ASCENDING", true);
        final String musicFolder = SharedPreferencesHandler.sharedPreferences.getString("SONG_FOLDER","");

        List<MusicItem> li = new ArrayList<>(wholeSongList);

        if (filter.getSortOption().equals(MusicListType.FAVORITES)) {
            li.removeIf(item -> !item.isFavorited());
        } else if (filter.getSortOption().equals(MusicListType.PLAYLIST)) {
            List<String> songs = Arrays.asList(SharedPreferencesHandler.sharedPreferences.getString(String.format("%s_%s", filter.getSortOption().toString(), filter.getData()), "").split("\n"));
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

    private static List<MusicItem> getSortedMusicList(List<MusicItem> list, SortOption order, boolean reverse) {
        if (order.equals(SortOption.DATE)) {
            list.sort(Comparator.comparingInt(MusicItem::getDuration));
        } else if (order.equals(SortOption.TITLE)) {
            list.sort((o2, o1) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
        } else if (order.equals(SortOption.LENGTH)) {
            list.sort((o1, o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));
        } else if (order.equals(SortOption.RANDOM)) {
            Collections.shuffle(list);
        }
        if (reverse) {
            Collections.reverse(list);
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
    /*public static void playlistView(RecyclerView recyclerView, Context activity, MusicPlayer player, FragmentActivity c, PlaylistsFragment playlistsFragment) {
        ArrayList<PlaylistItem> list = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        list.add(new PlaylistItem());
        if (!SharedPreferencesHandler.sharedPreferences.getString("PLAYLISTS", "").isEmpty()) {
            for (String playlist : SharedPreferencesHandler.sharedPreferences.getString("PLAYLISTS", "").split("\n")) {
                list.add(new PlaylistItem(playlist));
            }
        }
        PlayListsAdapter adapter = new PlayListsAdapter(list, c, player, playlistsFragment) {};
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
    }*/

    /**
     * Load music list. This method should only be called once on start. If user refreshes the view, call this method to fetch songs again
     * @param context
     */
    public static ArrayList<MusicItem> loadList(Context context) {
        final int FILTER_SECONDS = SharedPreferencesHandler.sharedPreferences.getInt("MIN_SIZE",60);
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
                MusicItem myList = new MusicItem(
                        currentTitle.replaceAll(context.getString(R.string.Regex_replace_filenameNumber), ""), //Replace SongName(1) with just SongName
                        milliSecondsToTime(currentLength),
                        "",
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
     *
     * @param adapter       adapter that will be refreshed after getting authors is done
     * @param activity       context of the action
     */
    public static void getAuthors(RecyclerView.Adapter<?> adapter, Activity activity) {
        @SuppressLint("NotifyDataSetChanged") Thread t = new Thread(() -> {
            for (MusicItem item: wholeSongList) {
                try {
                    String Artist = read(new File(item.getLocation())).getTag().getFirst(FieldKey.ARTIST);
                    item.setArtist(Artist);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            activity.runOnUiThread(adapter::notifyDataSetChanged);
        });
        t.start();
    }

    public static ArrayList<String> getFavorites(Context context) {
        String favorites = SharedPreferencesHandler.sharedPreferences.getString("FAVORITES", "");
        return new ArrayList<>(Arrays.asList(favorites.split("\n")));
    }

    public static void addToFavorites(Context context, String s) {
        SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
        String favorites = SharedPreferencesHandler.sharedPreferences.getString("FAVORITES", "");
        editor.putString("FAVORITES", favorites + "\n" + s);
        editor.apply();
    }

    public static void setFavorites(Context context, ArrayList<String> li) {
        SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
        editor.putString("FAVORITES", String.join("\n",li));
        editor.apply();
    }

    public static void removeFromFavorites(Context context, String s) {
        String favorites = SharedPreferencesHandler.sharedPreferences.getString("FAVORITES", "");
        ArrayList<String> li = new ArrayList<>();
        for (String i: favorites.split("\n")) {
            if (!i.equals(s)) {
                li.add(i);
            }
        }
        setFavorites(context, li);
    }
}
