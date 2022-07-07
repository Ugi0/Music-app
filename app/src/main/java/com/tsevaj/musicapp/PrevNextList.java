package com.tsevaj.musicapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

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
    SharedPreferences settings;
    SharedPreferences.Editor editor;


    public PrevNextList(ArrayList<MyList> list, MyList current, Fragment currentFragment, Context c) {
        this.songList = list;
        this.current = current;
        this.Prev = new ArrayList<>();
        this.Next = new ArrayList<>();
        this.tempList = new ArrayList<>();
        this.createdFragment = currentFragment;
        this.c = c;
        this.reRoll();
    }

    public PrevNextList(Context c) {
        this.c = c;
    }

    public void reRoll() {
        settings = c.getSharedPreferences("SAVEDATA", 0);
        editor = settings.edit();

        this.tempList = songList;
        int chosen;
        this.tempList.remove(current);
        int LIST_SIZE = 20;
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

    public MyList Next(Boolean force) {
        settings = c.getSharedPreferences("SAVEDATA", 0);
        editor = settings.edit();
        if (settings.getInt("REPLAY_MODE",0) == 1 && !force) {
            return current;
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

    public MyList Prev(Boolean force) {
        settings = c.getSharedPreferences("SAVEDATA", 0);
        editor = settings.edit();
        if (settings.getInt("REPLAY_MODE",0) == 1 && !force) {
            return current;
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
}
