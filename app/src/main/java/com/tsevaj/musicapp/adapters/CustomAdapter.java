package com.tsevaj.musicapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.FavoritesFragment;
import com.tsevaj.musicapp.utils.AlertPopup;
import com.tsevaj.musicapp.utils.ApplicationConfig;
import com.tsevaj.musicapp.utils.files.MusicGetter;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.MusicItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private ArrayList<MusicItem> list;
    //private final ArrayList<MusicItem> backupList;
    //private final Context mCtx;
    //MusicPlayer player;
    //FragmentActivity c;
    //private CustomAdapter.ViewHolder lastClicked = null;
    //private final int defaultColor;
    //private final String playlist;
    //private String chosenColor;
    private MainActivity main;
    private ApplicationConfig config;

    private static final String useWholeListColor = "#2A0B35";
    private static final String useVisibleListColor = "#DC143C";

    public CustomAdapter(ArrayList<MusicItem> list, MainActivity main) {
        this.list = new ArrayList<>(list);
        this.main = main;
        this.config = main.getConfig();
        //this.backupList = new ArrayList<>(list);
        //this.mCtx = mCtx;
        //this.c = c;
        //this.player = player;
        //this.player.adapter = this;
        //this.playlist = playlist;
        //this.defaultColor = Color.parseColor(mCtx.getSharedPreferences("SAVEDATA", 0).getString("THEME_COLOR", "#FFFFFF"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(main.getApplicationContext())
                .inflate(R.layout.songlist_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint({"ResourceType", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MusicItem song = list.get(position);
        String colorToUse = main.PrevAndNextSongs.getWholeListValue() ? useWholeListColor : useVisibleListColor;
        holder.textViewHead.setText(song.getTitle());
        holder.textViewDesc.setText(song.getDesc());
        //TOCO If the fragment is changed, we want a different behaviour
        // -> Change the list that songs are chosen from
        if (song.getHash() == MusicPlayer.getCurrentSongHash()) {
            holder.textViewHead.setTextColor(Color.parseColor(colorToUse));
            holder.textViewDesc.setTextColor(Color.parseColor(colorToUse));
        }
        else {
            holder.textViewHead.setTextColor(Color.parseColor(config.getTextColor()));
            holder.textViewDesc.setTextColor(Color.parseColor(config.getTextColor()));
        }
        holder.itemView.setOnClickListener(view -> {
            if (song.getHash() == MusicPlayer.getCurrentSongHash()) {
                main.PrevAndNextSongs.setWholeListValue(!main.PrevAndNextSongs.getWholeListValue());
            } else {
                main.handleSongChange(song);
            }
            // Decide what list the player will use in future songs
            /*if (!MainActivity.currentFragment.equals(main.PrevAndNextSongs.createdFragment)) player.recreateList(myList);
            //
            if (player.main.PrevAndNextSongs.wholeList) {
                chosenColor = "#2A0B35";
                player.main.changePlayingList(MainActivity.wholeSongList);
            }
                chosenColor = "#DC143C";
                player.main.changePlayingList(player.visibleSongs);
            }
            if (lastClicked != null) {
                lastClicked.textViewHead.setTextColor(defaultColor);
                lastClicked.textViewDesc.setTextColor(defaultColor);
            }
            holder.textViewHead.setTextColor(Color.parseColor(chosenColor));
            holder.textViewDesc.setTextColor(Color.parseColor(chosenColor));
            player.main.PrevAndNextSongs.setCurrent(myList);
            player.play(myList);
            player.showBar();
            lastClicked = holder;*/
        });
        holder.buttonViewOption.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(main.getApplicationContext(), holder.buttonViewOption);
            popup.inflate(R.menu.popup_menu);
            if (playlist.contains("PLAYLIST")) popup.getMenu().getItem(2).setTitle("Remove from playlist");
            if (main.getPreferencesHandler().getFavorites().contains(song.getTitle())) popup.getMenu().getItem(0).setTitle("Remove from favorites");
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.addtofavorites) {
                    SharedPreferences settings = main.getSharedPreferences("SAVEDATA", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    String favorites = settings.getString("FAVORITES", "");
                    if (Arrays.asList(favorites.split("\n")).contains(song.getTitle()) && MainActivity.currentFragment.getClass().equals(FavoritesFragment.class)) {
                        ArrayList<String> li = new ArrayList<>(Arrays.asList(favorites.split("\n")));
                        li.remove(song.getTitle());
                        int ind = 0;
                        editor.putString("FAVORITES", String.join("\n", li));
                        editor.apply();
                        for (int i = 0; i < getList().size(); i++) {
                            MusicItem listItem = getList().get(i);
                            if (listItem.getTitle().equals(song.getTitle())) {
                                ind = i;
                                break;
                            }
                        }
                        song.setFavorited(true);
                        removeFromList(ind);
                        notifyItemRemoved(ind);
                        return false;
                    }
                    else if (main.getPreferencesHandler().getFavorites().contains(song.getTitle())) {
                        ArrayList<String> li = new ArrayList<>(Arrays.asList(favorites.split("\n")));
                        li.remove(song.getTitle());
                        int ind = 0;
                        editor.putString("FAVORITES", String.join("\n", li));
                        editor.apply();
                        for (int i = 0; i < getList().size(); i++) {
                            MusicItem listItem = getList().get(i);
                            if (listItem.getTitle().equals(song.getTitle())) {
                                ind = i;
                                break;
                            }
                        }
                        removeFromList(ind);
                        return false;
                    }
                    else {
                        editor.putString("FAVORITES", favorites + "\n" + song.getTitle());
                        editor.apply();
                    }
                } else if (itemId == R.id.addtoqueue) {
                    //TODO Handle adding song to queue
                    //player.setNext(song);
                } else if (itemId == R.id.addtoplaylist) {
                    SharedPreferences settings = main.getSharedPreferences("SAVEDATA", 0);
                    if (playlist.contains("PLAYLIST")) {
                        String playlistItems = settings.getString(playlist,"");
                        ArrayList<String> li = new ArrayList<>();
                        for (String item2: playlistItems.split("\n")) {
                            if (!item2.equals(song.getTitle())) {
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
                    PopupMenu menu = new PopupMenu(main.getApplicationContext(), view);
                    for (String menuItem : settings.getString("PLAYLISTS", "").split("\n")) {
                        menu.getMenu().add(menuItem);
                    }
                    menu.setOnMenuItemClickListener(item1 -> {
                        SharedPreferences.Editor editor2 = settings.edit();
                        String currentPlaylist = settings.getString(String.format("PLAYLIST_%s", item1.getTitle()), "");
                        if (Arrays.asList(currentPlaylist.split("\n")).contains(song.getTitle())) return true;
                        if (currentPlaylist.isEmpty())
                            editor2.putString(String.format("PLAYLIST_%s", item1.getTitle()), song.getTitle());
                        else {
                            editor2.putString("PLAYLIST_" + item1.getTitle(), currentPlaylist + "\n" + song.getTitle());
                        }
                        editor2.apply();
                        return true;
                    });
                    menu.show();
                } else if (itemId == R.id.song_delete) {
                    Runnable callback = () -> {
                        try {
                            Files.delete(Paths.get(song.getLocation()));
                            MainActivity.wholeSongList.remove(song);
                            list.remove(position);
                            //TODO Make sure deleted song is deleted from references everywhere
                            //main.PrevAndNextSongs.removeFromPrev(song);
                            notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    };
                    new AlertPopup(main.getApplicationContext(), "Delete", "Are you sure you want to delete?", callback).show();
                } else if (itemId == R.id.song_properties) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(main.getApplicationContext());
                    builder.setTitle("Song properties")
                            .setMessage(String.format(
                                      "Song name: %s\n\n"
                                    + "Artist name: %s\n\n"
                                    + "Song duration: %s\n\n"
                                    + "Song location: %s\n\n"
                                    + "File size: %s\n\n"
                                    + "File type: %s\n\n"
                                    + "Modified date: %s",
                                    song.getTitle(), song.getArtist(), MusicGetter.milliSecondsToTime(song.getDuration()),
                                    song.getLocation(), song.getCurrentSize(), song.getType(), song.getDateModified()
                            ))
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

    public ArrayList<MusicItem> getList() {
        return this.list;
    }

}