package com.tsevaj.musicapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.uielements.ConfirmPopup;
import com.tsevaj.musicapp.utils.ApplicationConfig;
import com.tsevaj.musicapp.utils.SharedPreferencesHandler;
import com.tsevaj.musicapp.utils.files.MusicGetter;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.data.MusicItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    @Getter
    private final List<MusicItem> list;
    private final MainActivity main;
    private final ApplicationConfig config;
    private ViewHolder last;

    private static final String useWholeListColor = "#2A0B35";
    private static final String useVisibleListColor = "#DC143C";

    public CustomAdapter(List<MusicItem> list, MainActivity main) {
        this.list = list;
        this.main = main;
        this.config = main.getConfig();
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
        String colorToUse = main.getMusicList().getWholeListValue() ? useWholeListColor : useVisibleListColor;
        holder.textViewHead.setText(song.getTitle());
        holder.textViewDesc.setText(song.getDesc());
        //TODO If the fragment is changed, we want a different behaviour
        // -> Change the list that songs are chosen from
        if (song.getHash().equals(MusicPlayer.getCurrentSongHash())) {
            holder.textViewHead.setTextColor(Color.parseColor(colorToUse));
            holder.textViewDesc.setTextColor(Color.parseColor(colorToUse));
        }
        else {
            holder.textViewHead.setTextColor(Color.parseColor(config.getTextColor()));
            holder.textViewDesc.setTextColor(Color.parseColor(config.getTextColor()));
        }
        holder.itemView.setOnClickListener(view -> {
            if (song.getHash().equals(MusicPlayer.getCurrentSongHash())) {
                //Handle clicking the same song again
                main.getMusicList().setWholeListValue(!main.getMusicList().getWholeListValue());
                return;
            }
            main.handleSongChange(song);
            if (last != null) {
                last.setColor(Color.parseColor(config.getTextColor()));
            }
            holder.setColor(Color.parseColor(colorToUse));
            last = holder;
        });
        holder.buttonViewOption.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(main.getApplicationContext(), holder.buttonViewOption);
            popup.inflate(R.menu.popup_menu);
            //if (playlist.contains("PLAYLIST")) popup.getMenu().getItem(2).setTitle("Remove from playlist");
            if (main.getPreferencesHandler().getFavorites().contains(song)) popup.getMenu().getItem(0).setTitle("Remove from favorites");
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.addtofavorites) {
                    SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
                    String favorites = SharedPreferencesHandler.sharedPreferences.getString("FAVORITES", "");
                    /*if (Arrays.asList(favorites.split("\n")).contains(song.getTitle()) && MainActivity.currentFragment.getClass().equals(FavoritesFragment.class)) {
                        ArrayList<String> li = new ArrayList<>(Arrays.asList(favorites.split("\n")));
                        li.remove(song.getTitle());b
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
                    }*/
                    if (main.getPreferencesHandler().getFavorites().contains(song)) {
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
                    PopupMenu menu = new PopupMenu(main.getApplicationContext(), view);
                    for (String menuItem : main.getPreferencesHandler().getPlayLists()) {
                        MenuItem tmp = menu.getMenu().add(menuItem);
                        //TODO either make menu items checkable or show which playlist item is in
                    }
                    menu.setOnMenuItemClickListener(item1 -> {
                        if (main.getPreferencesHandler().songInPlayList(String.valueOf(item1.getTitle()), song)) {
                            main.getPreferencesHandler().removeFromFavorites(song.getHash());
                        } else {
                            main.getPreferencesHandler().addToFavorites(song.getHash());
                        }
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
                    new ConfirmPopup(main.getApplicationContext(), "Delete", "Are you sure you want to delete?", callback).show();
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

        private final TextView textViewHead;
        private final TextView textViewDesc;
        private final TextView buttonViewOption;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewHead = itemView.findViewById(R.id.textViewHead);
            textViewDesc = itemView.findViewById(R.id.textViewDesc);
            buttonViewOption = itemView.findViewById(R.id.textViewOptions);
        }

        public void setColor(int color) {
            textViewHead.setTextColor(color);
            textViewDesc.setTextColor(color);
        }
    }

    public void removeFromList(int ind) {
        this.list.remove(ind);
    }

}