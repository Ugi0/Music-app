package com.tsevaj.musicapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    @RequiresApi(api = Build.VERSION_CODES.N)
    public PrevNextList(ArrayList<MyList> list, MyList current, Fragment currentFragment, Context c) {
        this.songList = list;
        this.current = current;
        this.Prev = new ArrayList<>();
        this.Next = new ArrayList<>();
        this.tempList = new ArrayList<>();
        this.createdFragment = currentFragment;
        LIST_SIZE = ((Activity) c).getSharedPreferences("SAVEDATA", 0).getInt("LOOPING_SIZE",20);
        this.c = c;
        this.reRoll();
    }

    public PrevNextList(Context c) {
        this.c = c;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void reRoll() {
        settings = c.getSharedPreferences("SAVEDATA", 0);
        editor = settings.edit();

        Next = new ArrayList<>();
        Prev = new ArrayList<>();
        this.tempList = new ArrayList<>(songList);
        int chosen;
        this.tempList.remove(current);

        if (settings.getInt("REPLAY_MODE",0) == -1) {
            int i = 0;
            while (tempList.size() != 0) {
                chosen = rand.nextInt(this.tempList.size());
                if (i % 2 == 0) {
                    this.Next.add(this.tempList.get(chosen));
                }
                else {
                    this.Prev.add(tempList.get(chosen));
                }
                i++;
                tempList.remove(chosen);
            }
            return;
        }
        for (int i = 0; i < 2* LIST_SIZE && !this.tempList.isEmpty() ; i ++) {
            chosen = rand.nextInt(this.tempList.size());
            if (i % 2 == 0) {
                this.Next.add(this.tempList.get(chosen));
            }
            else {
                this.Prev.add(tempList.get(chosen));
            }
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

     /*   Log.d("Next", "");
        for (int i = 0; i < Next.size(); i ++) {
            Log.d("", String.valueOf(Next.get(i).getHead())); } */
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

      /*  Log.d("Prev", "");
        for (int i = 0; i < Prev.size(); i ++) {
            Log.d("", String.valueOf(Prev.get(i).getHead()));} */
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
