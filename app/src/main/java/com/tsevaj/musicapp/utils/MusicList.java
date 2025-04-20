package com.tsevaj.musicapp.utils;

import static com.tsevaj.musicapp.utils.enums.SortOption.*;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.data.MusicItem;
import com.tsevaj.musicapp.utils.enums.SortOption;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MusicList {
    //Prev list is static so every instance we make of this class will have the same Prev values
    //Although realistically we only want to have one instance of this class at a time
    private static ArrayList<MusicItem> previousSongs = new ArrayList<>();

    private MainActivity main;
    private ArrayList<MusicItem> songList;
    private MusicItem current;

    private final Random randomizer;

    //private Context c;

    private boolean wholeList;

    //private String lastFilter = "";
    //private String lastNameFilter = "";

    public MusicList(MainActivity main, List<MusicItem> list) {
        this.main = main;
        this.songList = new ArrayList<>(list);
        randomizer = new Random(Instant.now().toEpochMilli());
    }

    //public PrevNextList(Context c) {
    //    this.c = c;
    //}

    public boolean getWholeListValue() {
        return this.wholeList;
    }

    public void setWholeListValue(boolean value) {
        this.wholeList = value;
    }

    public void setList(ArrayList<MusicItem> li) {
        this.songList = li;
    }

    //public String getLastFilter() {
    //    return lastFilter;
    //}

    public void setCurrent(MusicItem item) {
        if (!previousSongs.remove(current) && previousSongs.size() >= 40) {
            previousSongs.remove(0);
        }
        previousSongs.add(current);
        this.current = item;
    }

    public void handleSongDeletion(MusicItem song) {
        //TODO handle removing references here
    }

    private void addToPrevList(MusicItem item) {
        if (!previousSongs.remove(item) && previousSongs.size() >= 40) {
            previousSongs.remove(0);
        }
        previousSongs.add(item);
    }

    public MusicItem Next(Boolean force) {
        List<MusicItem> li;
        int index;
        if (SharedPreferencesHandler.sharedPreferences.getInt("REPLAY_MODE",1) == 1 && !force) { //Play one song
            return current;
        }
        if (!SharedPreferencesHandler.sharedPreferences.getBoolean("SHUFFLE", false)) { //Play songs in list order
            if (wholeList) li = MainActivity.wholeSongList;
            else { li = songList; }

            index = li.indexOf(current)+1;
        }
        else { //else Play a random song
            if (wholeList) li = MainActivity.wholeSongList;
            else { li = songList; }
            List<Integer> list = IntStream.rangeClosed(0, li.size()-1).boxed().collect(Collectors.toList());
            list.remove(li.indexOf(current));
            index = list.get(randomizer.nextInt(li.size()-1));
            if (index == li.indexOf(current)) { //If next song happens to be the same
                index = list.get(randomizer.nextInt(li.size()-1));
            }
        }
        MusicItem cur = li.get(index % li.size());
        addToPrevList(current);
        current = cur;
        return current;
    }

    public MusicItem Prev() {
        if (previousSongs.isEmpty()) return current;
        MusicItem item = previousSongs.get(previousSongs.size()-1);
        previousSongs.remove(previousSongs.size()-1);
        return item;
    }

    public void sortFilterList(SortOption sort, boolean reverse) {
        switch (sort) {
            case DATE:
                if (reverse) {
                    songList.sort(Comparator.comparingLong(MusicItem::getDateModified).reversed());
                } else {
                    songList.sort(Comparator.comparing(MusicItem::getDateModified));
                }
                break;
            case TITLE:
                if (reverse) {
                    songList.sort((o1, o2) -> o2.getTitle().compareToIgnoreCase(o1.getTitle()));
                } else {
                    songList.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
                }
                break;
            case LENGTH:
                if (reverse) {
                    songList.sort(Comparator.comparingInt(MusicItem::getDuration));
                } else {
                    songList.sort(Comparator.comparing(MusicItem::getDuration));
                }
                break;
            case RANDOM:
                Collections.shuffle(songList);
                break;
            default:
                break;
        }
    }

    /*@SuppressLint("NotifyDataSetChanged")
    public void getMusicAndSet(RecyclerView recyclerView, Context activity, MusicPlayer player, FragmentActivity c, String filter, String nameFilter) {
        ArrayList<MusicItem> li;
        if (songList == null) {
            //if (Objects.equals(filter, lastFilter) && !Objects.equals(nameFilter, lastNameFilter))
            li = getMusic(activity, player, c, filter, nameFilter);
            songList = li;
        } else if (!(filter.equals(lastFilter))) {
            Log.d("test", "Different filter");
            lastFilter = filter;
            li = getMusic(activity, player, c, filter, nameFilter);
            songList = li;
        } else if (!(Objects.equals(nameFilter, lastNameFilter))) {
            lastNameFilter = nameFilter;
            li = filterMusicList((FragmentActivity) activity, filter, nameFilter);
            songList = li;
        } else {
            li = songList;
        }
        //LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        //CustomAdapter adapter = new CustomAdapter(li, activity, c, player, filter) {
        //};
        //recyclerView.setAdapter(adapter);
        //if (recyclerView.getItemDecorationCount() == 0)
        //    recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
        //player.main.changePlayingList(li);
        //player.recyclerview = recyclerView;
        //player.adapter = adapter;
        //adapter.reset();
        //adapter.notifyDataSetChanged();
    }*/
}
