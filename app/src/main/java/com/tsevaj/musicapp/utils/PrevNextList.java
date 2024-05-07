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
import java.util.Random;

public class PrevNextList {
    private final int listSize = 20;
    public static ArrayList<MyList> allSongs;
    private ArrayList<MyList> currentlyPlayingSongs;
    private ArrayList<MyList> Prev;
    private ArrayList<MyList> songOrder;
    private MyList current;
    private Random randomizer;

    public Fragment createdFragment;
    public Context c;
    SharedPreferences settings;

    public boolean wholeList;

    public PrevNextList(ArrayList<MyList> list, MyList current, Fragment currentFragment, Context c) {
        this.currentlyPlayingSongs = new ArrayList<>(list);
        this.current = current;
        this.c = c;
        this.createdFragment = currentFragment;
        allSongs = new ArrayList<>(MainActivity.wholeSongList);
        this.settings = c.getSharedPreferences("SAVEDATA", 0);
        initializePrev();
        reRoll();
    }

    public PrevNextList(Context c) {
        this.c = c;
    }

    private void initializePrev() {
        Prev = new ArrayList<>();
    }

    public void setList(ArrayList<MyList> li) {
        this.currentlyPlayingSongs = li;
        this.songOrder = li;
        reRoll();
    }

    public void reRoll() {
        randomizer = new Random(Instant.now().toEpochMilli());

        songOrder = new ArrayList<>(currentlyPlayingSongs);
        Collections.shuffle(songOrder, randomizer);
        Collections.shuffle(allSongs, randomizer);
    }

    public void setCurrent(MyList item) {
        if (!Prev.remove(current) && Prev.size() >= 40) {
            Prev.remove(0);
        }
        Prev.add(current);
        this.current = item;
    }

    public void removeFromPrev(MyList item) {
        try {
            this.Prev.remove(item);
        } catch (Exception ignored) {}
    }

    public MyList Next(Boolean force) {
        ArrayList<MyList> li;
        if (settings.getInt("REPLAY_MODE",1) == 1 && !force) { //Play one song
            return current;
        }
        if (!settings.getBoolean("SHUFFLE", false)) { //Play songs in list order
            if (wholeList) li = MainActivity.wholeSongList;
            else { li = currentlyPlayingSongs; }
            int index = li.indexOf(current)+1;
            if (index == 0) {
                index = randomizer.nextInt(li.size());
            }
            if (!Prev.remove(current) && Prev.size() >= 40) {
                Prev.remove(0);
            }
            Prev.add(current);
            current = li.get((index) % li.size());
            return current;
        }
        if (wholeList) {
            int index = allSongs.indexOf(current)+1;
            if (!Prev.remove(current) && Prev.size() >= 40) {
                Prev.remove(0);
            }
            Prev.add(current);
            current = allSongs.get((index) % allSongs.size());
            return current;
        }
        int index = songOrder.indexOf(current)+1;
        if (index == 0) {
            index = randomizer.nextInt(songOrder.size());
        }
        if (!Prev.remove(current) && Prev.size() >= 40) {
            Prev.remove(0);
        }
        Prev.add(current);
        current = songOrder.get((index) % songOrder.size());
        return current;
    }

    public MyList Prev() {
        if (Prev.size() == 0) return current;
        MyList item = Prev.get(Prev.size()-1);
        Prev.remove(Prev.size()-1);
        return item;
    }
}
