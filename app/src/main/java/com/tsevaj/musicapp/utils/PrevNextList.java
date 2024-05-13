package com.tsevaj.musicapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.MainActivity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrevNextList {
    private final int listSize = 20;
    public static ArrayList<MusicItem> allSongs;
    private ArrayList<MusicItem> currentlyPlayingSongs;
    private ArrayList<MusicItem> Prev;
    private ArrayList<MusicItem> songOrder;
    private MusicItem current;
    private Random randomizer;

    public Fragment createdFragment;
    public Context c;
    SharedPreferences settings;

    public boolean wholeList;

    public PrevNextList(ArrayList<MusicItem> list, MusicItem current, Fragment currentFragment, Context c) {
        this.currentlyPlayingSongs = new ArrayList<>(list);
        this.current = current;
        this.c = c;
        this.createdFragment = currentFragment;
        allSongs = new ArrayList<>(MainActivity.wholeSongList);
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
        this.songOrder = li;
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
            Log.d("test", list.toString());

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
}
