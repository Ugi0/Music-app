package com.tsevaj.musicapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.MainActivity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrevNextList {
    //Prev list is static so every instance we make of this class will have the same Prev values
    //Although realistically we only want to have one instance of this class at a time
    private static ArrayList<MusicItem> previousSongs = new ArrayList<>();

    private ArrayList<MusicItem> songList;
    private MusicItem current;

    private final Random randomizer;
    private final SharedPreferences settings;

    private Fragment createdFragment;
    //private Context c;

    public boolean wholeList;

    //private String lastFilter = "";
    //private String lastNameFilter = "";

    public PrevNextList(ArrayList<MusicItem> list, Fragment currentFragment, Context c) {
        this.songList = new ArrayList<>(list);
        //this.Prev = new ArrayList<>();
        //this.current = current;
        //this.c = c;
        this.createdFragment = currentFragment;
        this.settings = c.getSharedPreferences("SAVEDATA", 0);
        randomizer = new Random(Instant.now().toEpochMilli());
    }

    //public PrevNextList(Context c) {
    //    this.c = c;
    //}

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

    /*public void removeFromPrev(MusicItem item) {
        try {
            this.previousSongs.remove(item);
        } catch (Exception ignored) {}
    }*/

    private void addToPrevList(MusicItem item) {
        if (!previousSongs.remove(item) && previousSongs.size() >= 40) {
            previousSongs.remove(0);
        }
        previousSongs.add(item);
    }

    public MusicItem Next(Boolean force) {
        ArrayList<MusicItem> li;
        int index;
        if (settings.getInt("REPLAY_MODE",1) == 1 && !force) { //Play one song
            return current;
        }
        if (!settings.getBoolean("SHUFFLE", false)) { //Play songs in list order
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

    public void sortFilterList(String sort, boolean reverse) {
        switch (sort) {
            case "DATE":
                if (reverse) {
                    songList.sort((o1, o2) -> o2.getDateModified().compareTo(o1.getDateModified()));
                } else {
                    songList.sort(Comparator.comparing(MusicItem::getDateModified));
                }
                break;
            case "TITLE":
                if (reverse) {
                    songList.sort((o1, o2) -> o2.getTitle().compareToIgnoreCase(o1.getTitle()));
                } else {
                    songList.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
                }
                break;
            case "LENGTH":
                if (reverse) {
                    songList.sort(Comparator.comparingInt(MusicItem::getDuration));
                } else {
                    songList.sort((o1, o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));
                }
                break;
            case "RANDOM":
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
