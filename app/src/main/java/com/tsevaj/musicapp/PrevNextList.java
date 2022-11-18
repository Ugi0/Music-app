package com.tsevaj.musicapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Random;

public class PrevNextList {
    private ArrayList<MyList> songList;
    private ArrayList<MyList> tempList;
    private ArrayList<MyList> Prev;
    private ArrayList<MyList> Next;
    private final Random rand = new Random(System.currentTimeMillis());
    private MyList current;
    public Fragment createdFragment;
    public Context c;
    int LIST_SIZE;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    private boolean initialized = true;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PrevNextList(ArrayList<MyList> list, MyList current, Fragment currentFragment, Context c) {
        this.songList = list;
        this.current = current;
        this.Prev = new ArrayList<>();
        this.Next = new ArrayList<>();
        this.tempList = new ArrayList<>();
        this.createdFragment = currentFragment;
        LIST_SIZE = c.getSharedPreferences("SAVEDATA", 0).getInt("LOOPING_SIZE",20);
        this.c = c;
        this.initializePrev();
        this.reRoll();
    }

    public PrevNextList(Context c) {
        this.c = c;
    }

    public void initializePrev() {
        int chosen;
        this.tempList = new ArrayList<>(songList);
        int size = Math.min(LIST_SIZE, songList.size());
        for (int i = 0; i < size; i++) {
            chosen = rand.nextInt(this.tempList.size());
            this.Prev.add(this.tempList.get(chosen));
            i++;
            tempList.remove(chosen);
        }
    }

    public void removeFromTemp(ArrayList<MyList> li) {
        for (MyList t: li) {
            this.tempList.remove(t);
        }
    }

    public void addToPrev(MyList item) {
        this.Prev.remove(Prev.size()-1);
        this.Prev.add(0,item);
        for (MyList t: this.Prev) {
            Log.d("test", t.getHead());
        }
        Log.d("test", "-------");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void reRoll() {
        settings = c.getSharedPreferences("SAVEDATA", 0);
        editor = settings.edit();

        Next = new ArrayList<>();
        this.tempList = new ArrayList<>(songList);
        removeFromTemp(this.Prev);
        int chosen;
        this.tempList.remove(current);

        if (settings.getInt("REPLAY_MODE",0) == -1) {
            int i = 0;
            while (tempList.size() != 0) {
                chosen = rand.nextInt(this.tempList.size());
                this.Next.add(this.tempList.get(chosen));
                i++;
                tempList.remove(chosen);
            }
            return;
        }
        for (int i = 0; i < LIST_SIZE && !this.tempList.isEmpty() ; i ++) {
            chosen = rand.nextInt(this.tempList.size());
            this.Next.add(this.tempList.get(chosen));
            tempList.remove(chosen);
        }
    }

    public MyList Next(MyList next) {
        if (Next.size() == 0) return current;
        Prev.add(0,current);
        tempList.add(Prev.get(Prev.size()-1));
        Prev.remove(Prev.size()-1);
        int chosen = rand.nextInt(tempList.size());
        Next.add(tempList.get(chosen));
        tempList.remove(chosen);
        Next.remove(0);
        current = next;
        return next;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public MyList Next(Boolean force) {
        settings = c.getSharedPreferences("SAVEDATA", 0);
        editor = settings.edit();
        if (settings.getInt("REPLAY_MODE",0) == 1 && !force) {
            return current;
        }
        if (!settings.getBoolean("SHUFFLE",false)) {
            int curInd = songList.indexOf(current);
            return Next(songList.get((curInd+1)%songList.size()));
        }
        if (Next.size() == 0) return current;
        MyList next = this.Next.get(0);
        Prev.add(0,current);
        tempList.add(Prev.get(Prev.size()-1));
        Prev.remove(Prev.size()-1);
        int chosen = rand.nextInt(tempList.size());
        Next.add(tempList.get(chosen));
        tempList.remove(chosen);
        Next.remove(0);
        current = next;
        return next;
    }

    public MyList Prev(MyList prev) {
        Next.add(0,current);
        tempList.add(Next.get(Next.size()-1));
        Next.remove(Next.size()-1);
        int chosen = rand.nextInt(tempList.size());
        Prev.add(tempList.get(chosen));
        tempList.remove(chosen);
        Prev.remove(0);
        current = prev;
        return prev;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MyList Prev(Boolean force) {
        settings = c.getSharedPreferences("SAVEDATA", 0);
        editor = settings.edit();
        if (settings.getInt("REPLAY_MODE",0) == 1 && !force) {
            return current;
        }
        if (!settings.getBoolean("SHUFFLE",false)) {
            int curInd = songList.indexOf(current);
            return Prev(songList.get((curInd-1+songList.size())%songList.size()));
        }

        if (Prev.size() == 0) return current;
        MyList prev = this.Prev.get(0);
        Next.add(0,current);
        tempList.add(Next.get(Next.size()-1));
        Next.remove(Next.size()-1);
        int chosen = rand.nextInt(tempList.size());
        Prev.add(tempList.get(chosen));
        tempList.remove(chosen);
        Prev.remove(0);
        current = prev;
        return prev;
    }

    public void reduceInSize() {
        while (this.Prev.size() > LIST_SIZE) {
            tempList.add(Prev.get(Prev.size()-1));
            Prev.remove(Prev.get(Prev.size()-1));
        }
        while (this.Next.size() > LIST_SIZE) {
            tempList.add(Next.get(Next.size()-1));
            Next.remove(Next.get(Next.size()-1));
        }
    }
}
