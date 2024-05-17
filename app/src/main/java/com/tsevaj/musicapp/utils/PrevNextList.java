package com.tsevaj.musicapp.utils;

import static com.tsevaj.musicapp.utils.FunctionClass.filterMusicList;
import static com.tsevaj.musicapp.utils.FunctionClass.getMusic;
import static com.tsevaj.musicapp.utils.FunctionClass.nameFilterMusicList;
import static com.tsevaj.musicapp.utils.FunctionClass.sortMusicList;

import static org.jaudiotagger.audio.AudioFileIO.read;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.adapters.CustomAdapter;

import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrevNextList {
    private ArrayList<MusicItem> currentlyPlayingSongs;
    private ArrayList<MusicItem> Prev;
    private MusicItem current;
    private Random randomizer;

    public Fragment createdFragment;
    public Context c;
    SharedPreferences settings;

    public boolean wholeList;

    private String lastFilter = "";
    private String lastNameFilter = "";

    public PrevNextList(ArrayList<MusicItem> list, MusicItem current, Fragment currentFragment, Context c) {
        this.currentlyPlayingSongs = new ArrayList<>(list);
        this.current = current;
        this.c = c;
        this.createdFragment = currentFragment;
        this.settings = c.getSharedPreferences("SAVEDATA", 0);
        randomizer = new Random(Instant.now().toEpochMilli());
        initializePrev();
    }

    public PrevNextList(Context c) {
        this.c = c;
    }

    private void initializePrev() {
        Prev = new ArrayList<>();
    }

    public void setList(ArrayList<MusicItem> li) {
        this.currentlyPlayingSongs = li;
    }

    public void setCurrent(MusicItem item) {
        if (!Prev.remove(current) && Prev.size() >= 40) {
            Prev.remove(0);
        }
        Prev.add(current);
        this.current = item;
    }

    public void removeFromPrev(MusicItem item) {
        try {
            this.Prev.remove(item);
        } catch (Exception ignored) {}
    }

    public MusicItem Next(Boolean force) {
        ArrayList<MusicItem> li;
        int index;
        if (settings.getInt("REPLAY_MODE",1) == 1 && !force) { //Play one song
            return current;
        }
        if (!settings.getBoolean("SHUFFLE", false)) { //Play songs in list order
            if (wholeList) li = MainActivity.wholeSongList;
            else { li = currentlyPlayingSongs; }

            index = li.indexOf(current)+1;
        }
        else { //else Play a random song
            if (wholeList) li = MainActivity.wholeSongList;
            else { li = currentlyPlayingSongs; }
            List<Integer> list = IntStream.rangeClosed(0, li.size()-1).boxed().collect(Collectors.toList());
            list.remove(li.indexOf(current));
            index = list.get(randomizer.nextInt(li.size()-1));
            if (index == li.indexOf(current)) { //If next song happens to be the same
                index = list.get(randomizer.nextInt(li.size()-1));
            }
        }
        MusicItem cur = li.get(index % li.size());
        if (!Prev.remove(current) && Prev.size() >= 40) {
            Prev.remove(0);
        }
        Prev.add(current);
        current = cur;
        return current;
    }

    public MusicItem Prev() {
        if (Prev.isEmpty()) return current;
        MusicItem item = Prev.get(Prev.size()-1);
        Prev.remove(Prev.size()-1);
        return item;
    }

    public void sortFilterList(String sort, boolean reverse) {
        switch (sort) {
            case "DATE":
                if (reverse) {
                    currentlyPlayingSongs.sort((o1, o2) -> o2.getDateModified().compareTo(o1.getDateModified()));
                } else {
                    //currentlyPlayingSongs.sort(Comparator.comparing(MusicItem::getDateModified));
                    currentlyPlayingSongs.sort((o1, o2) -> o1.getDateModified().compareTo(o2.getDateModified()));
                }
                break;
            case "TITLE":
                if (reverse) {
                    currentlyPlayingSongs.sort((o1, o2) -> o2.getHead().compareToIgnoreCase(o1.getHead()));
                } else {
                    currentlyPlayingSongs.sort((o1, o2) -> o1.getHead().compareToIgnoreCase(o2.getHead()));
                }
                break;
            case "LENGTH":
                if (reverse) {
                    currentlyPlayingSongs.sort(Comparator.comparingInt(MusicItem::getDuration));
                } else {
                    currentlyPlayingSongs.sort((o1, o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));
                }
                break;
            case "RANDOM":
                Collections.shuffle(currentlyPlayingSongs);
                break;
            default:
                break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getMusicAndSet(RecyclerView recyclerView, Context activity, MusicPlayer player, FragmentActivity c, String filter, String nameFilter) {
        ArrayList<MusicItem> li;
        if (currentlyPlayingSongs == null) {
            //if (Objects.equals(filter, lastFilter) && !Objects.equals(nameFilter, lastNameFilter))
            li = getMusic(activity, player, c, filter, nameFilter);
            currentlyPlayingSongs = li;
        } else if (!(filter.equals(lastFilter))) {
            lastFilter = filter;
            li = getMusic(activity, player, c, filter, nameFilter);
            currentlyPlayingSongs = li;
        } else if (!(Objects.equals(nameFilter, lastNameFilter))) {
            lastNameFilter = nameFilter;
            li = filterMusicList((FragmentActivity) activity, filter, nameFilter);
            currentlyPlayingSongs = li;
        } else {
            li = currentlyPlayingSongs;
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        CustomAdapter adapter = new CustomAdapter(li, activity, c, player, filter) {
        };
        recyclerView.setAdapter(adapter);
        if (recyclerView.getItemDecorationCount() == 0)
            recyclerView.addItemDecoration(new DividerItemDecoration(activity, layoutManager.getOrientation()));
        //player.main.changePlayingList(li);
        player.recyclerview = recyclerView;
        player.adapter = adapter;
        adapter.reset();
        adapter.notifyDataSetChanged();
    }
}
