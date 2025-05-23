package com.tsevaj.musicapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.LibraryFragment;
import com.tsevaj.musicapp.fragments.PlaylistsFragment;
import com.tsevaj.musicapp.utils.PlaylistItem;
import com.tsevaj.musicapp.utils.SharedPreferencesHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayListsAdapter extends RecyclerView.Adapter<PlayListsAdapter.ViewHolder> {

    private ArrayList<PlaylistItem> list;
    private View listView;

    private final MainActivity main;

    private final int VIEW_TYPE_ITEM = 1;

    public PlayListsAdapter(ArrayList<PlaylistItem> list, MainActivity main) {
        this.list = list;
        this.main = main;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View v = LayoutInflater.from(main.getApplicationContext())
                    .inflate(R.layout.playlists_item, parent, false);
            listView = v;
            return new ViewHolder(v);
        }
        else {
            View v = LayoutInflater.from(main.getApplicationContext())
                    .inflate(R.layout.playlist_defaultitem, parent, false);
            listView = v;
            return new ViewHolder(v);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (position == 0) {
            //Code for the default item
            holder.itemView.setOnClickListener(view -> {
                TextInputLayout textInputLayout = new TextInputLayout(main);
                textInputLayout.setPadding(
                        10,
                        0,
                        10,
                        0
                );
                textInputLayout.setBoxStrokeWidth(0);
                textInputLayout.setBoxStrokeWidthFocused(0);
                EditText input = new EditText(main);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                //input.requestFocus();
                InputMethodManager imm = (InputMethodManager)   main.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                textInputLayout.addView(input);
                new AlertDialog.Builder(main)
                        .setTitle("Create playlist")
                        .setView(textInputLayout)
                        .setPositiveButton("Create", (dialog, whichButton) -> {
                            if (!input.getText().toString().isEmpty()) {
                                main.getPreferencesHandler().addPlaylist(input.getText().toString());
                                //SharedPreferences settings = main.getSharedPreferences("SAVEDATA", 0);
                                //SharedPreferences.Editor editor = settings.edit();
                                //String playlists = settings.getString("PLAYLISTS", "");
                                //if (playlists.isEmpty()) editor.putString("PLAYLISTS", input.getText().toString());
                                //else { editor.putString("PLAYLISTS", playlists + "\n" + input.getText().toString()); }
                                //editor.putString("PLAYLIST_"+ input.getText().toString(),"");
                                //editor.apply();
                                list.add(0, new PlaylistItem(input.getText().toString()));
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                dialog.cancel();
                                main.changeFragment(PlaylistsFragment.class);
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, whichButton) -> {
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            dialog.cancel();
                        })
                        .show();
            });
        } else {
            holder.textViewHead.setText(list.get(position).getTitle());
            listView.findViewById(R.id.textViewOptions).setOnClickListener(view -> {
                PopupMenu popupMenu = new PopupMenu(main, listView);
                popupMenu.inflate(R.menu.playlists_menu);
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    if (menuItem.getItemId() == R.id.song_delete) {
                        main.getPreferencesHandler().removePlayList(list.get(position).getTitle());
                        //SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
                        //String playlists = SharedPreferencesHandler.sharedPreferences.getString("PLAYLISTS", "");
                        //editor.remove("PLAYLIST_"+list.get(position).getTitle());
                        //li.remove(position-1);
                        //editor.putString("PLAYLISTS",String.join("\n",li));
                        //editor.apply();
                        list.remove(position);
                        main.changeFragment(PlaylistsFragment.class);
                    }
                    return false;
                });
                popupMenu.show();
            });
            holder.itemView.setOnClickListener(view -> {
                String[] playlists = SharedPreferencesHandler.sharedPreferences.getString("PLAYLISTS", "").split("\n");
                String clickedPlaylist = playlists[position-1];
                main.changeFragment(LibraryFragment.class);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        int VIEW_TYPE_DEFAULT = 0;

        return (position == 0) ? VIEW_TYPE_DEFAULT : VIEW_TYPE_ITEM;
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
}