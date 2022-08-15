package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private ArrayList<MyList> list;
    private final ArrayList<MyList> backupList;
    private final Context mCtx;
    MusicPlayer player;
    FragmentActivity c;
    private CustomAdapter.ViewHolder lastClicked = null;
    private final int defaultColor;
    private final String playlist;
    //Color.parseColor("#FFFFFF");
    //Color.BLACK;

    @SuppressLint("ResourceType")
    public CustomAdapter(ArrayList<MyList> list, Context mCtx, FragmentActivity c, MusicPlayer player, String playlist) {
        this.list = list;
        this.backupList = new ArrayList<>(list);
        this.mCtx = mCtx;
        this.c = c;
        this.player = player;
        this.playlist = playlist;
        this.defaultColor = Color.parseColor(mCtx.getSharedPreferences("SAVEDATA", 0).getString("THEME_COLOR", "#FFFFFF"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songlist_item, parent, false);
        return new ViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"ResourceType", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MyList myList = list.get(position);
        holder.textViewHead.setText(myList.getHead());
        holder.textViewDesc.setText(myList.getDesc());
        if (myList.getHead().equals(player.currentPlayingSong.getHead())) {
            holder.textViewHead.setTextColor(Color.parseColor("#DC143C"));
            holder.textViewDesc.setTextColor(Color.parseColor("#DC143C"));
            lastClicked = holder;
        }
        else {
            holder.textViewHead.setTextColor(defaultColor);
            holder.textViewDesc.setTextColor(defaultColor);
        }
        holder.itemView.setOnClickListener(view -> {
            if (lastClicked != null) {
                lastClicked.textViewHead.setTextColor(defaultColor);
                lastClicked.textViewDesc.setTextColor(defaultColor);
            }
            holder.textViewHead.setTextColor(Color.parseColor("#DC143C"));
            holder.textViewDesc.setTextColor(Color.parseColor("#DC143C"));
            player.visibleSongs = list;
            player.play(myList);
            player.showBar();
            lastClicked = holder;
        });
        holder.buttonViewOption.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(mCtx, holder.buttonViewOption);
            popup.inflate(R.menu.popup_menu);
            if (playlist.contains("PLAYLIST")) popup.getMenu().getItem(2).setTitle("Remove from playlist");
            if (player.main.getFavorites().contains(myList.getHead())) popup.getMenu().getItem(0).setTitle("Remove from favorites");
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.addtofavorites) {
                    SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    String favorites = settings.getString("FAVORITES", "");
                    if (Arrays.asList(favorites.split("\n")).contains(myList.getHead()) && MainActivity.currentFragment.getClass().equals(FavoritesFragment.class)) {
                        ArrayList<String> li = new ArrayList<>(Arrays.asList(favorites.split("\n")));
                        li.remove(myList.getHead());
                        int ind = 0;
                        editor.putString("FAVORITES", String.join("\n", li));
                        editor.apply();
                        for (int i = 0; i < getList().size(); i++) {
                            MyList listItem = getList().get(i);
                            if (listItem.getHead().equals(myList.getHead())) {
                                ind = i;
                                break;
                            }
                        }
                        removeFromList(ind);
                        notifyItemRemoved(ind);
                        return false;
                    }
                    else if (player.main.getFavorites().contains(myList.getHead())) {
                        ArrayList<String> li = new ArrayList<>(Arrays.asList(favorites.split("\n")));
                        li.remove(myList.getHead());
                        int ind = 0;
                        editor.putString("FAVORITES", String.join("\n", li));
                        editor.apply();
                        for (int i = 0; i < getList().size(); i++) {
                            MyList listItem = getList().get(i);
                            if (listItem.getHead().equals(myList.getHead())) {
                                ind = i;
                                break;
                            }
                        }
                        removeFromList(ind);
                        return false;
                    }
                    else {
                        editor.putString("FAVORITES", favorites + "\n" + myList.getHead());
                        editor.apply();
                    }
                } else if (itemId == R.id.addtoqueue) {
                    player.setNext(myList);
                } else if (itemId == R.id.addtoplaylist) {
                    SharedPreferences settings = c.getSharedPreferences("SAVEDATA", 0);
                    if (playlist.contains("PLAYLIST")) {
                        String playlistItems = settings.getString(playlist,"");
                        ArrayList<String> li = new ArrayList<>();
                        for (String item2: playlistItems.split("\n")) {
                            if (!item2.equals(myList.getHead())) {
                                li.add(item2);
                            }
                        }
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(playlist, String.join("\n",li));
                        editor.apply();
                        list.remove(position);
                        notifyDataSetChanged();
                        return true;
                    }
                    PopupMenu menu = new PopupMenu(c, view);
                    for (String menuItem : settings.getString("PLAYLISTS", "").split("\n")) {
                        menu.getMenu().add(menuItem);
                    }
                    menu.setOnMenuItemClickListener(item1 -> {
                        SharedPreferences.Editor editor2 = settings.edit();
                        String currentPlaylist = settings.getString("PLAYLIST_" + item1.getTitle(), "");
                        if (Arrays.asList(currentPlaylist.split("\n")).contains(myList.getHead())) return true;
                        if (currentPlaylist.isEmpty())
                            editor2.putString("PLAYLIST_" + item1.getTitle(), myList.getHead());
                        else {
                            editor2.putString("PLAYLIST_" + item1.getTitle(), currentPlaylist + "\n" + myList.getHead());
                        }
                        editor2.apply();
                        return true;
                    });
                    menu.show();
                } else if (itemId == R.id.song_delete) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Files.delete(Paths.get(myList.getLocation()));
                        }
                        list.remove(position);
                        notifyItemRemoved(position);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (itemId == R.id.song_properties) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                    builder.setTitle("Song properties")
                            .setMessage( "\n" +
                                    "Song name: "+myList.getHead() + "\n" + "\n" +
                                    "Artist name: "+myList.getArtist()  + "\n" + "\n" +
                                    "Song duration: "+ FunctionClass.milliSecondsToTime(myList.getDuration())  + "\n" + "\n" +
                                    "Song location: "+myList.getLocation()  + "\n" + "\n" +
                                    "File size: "+ myList.getCurrentSize() + "\n" + "\n" +
                                    "File type: "+ myList.getType() + "\n" + "\n" +
                                    "Modified date: " + myList.getDateModified()
                            )
                            .setNegativeButton("Back", (dialogInterface, i) -> {
                                //close dialog
                            });
                    builder.create().show();
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewHead;
        public TextView textViewDesc;
        public TextView buttonViewOption;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewHead = itemView.findViewById(R.id.textViewHead);
            textViewDesc = itemView.findViewById(R.id.textViewDesc);
            buttonViewOption = itemView.findViewById(R.id.textViewOptions);
        }
    }

    public void removeFromList(int ind) {
        this.list.remove(ind);
    }

    public ArrayList<MyList> getList() {
        return this.list;
    }

    public void reset() {
        this.list = new ArrayList<>(backupList);
    }

}