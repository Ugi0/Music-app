package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
public class PlayListsAdapter extends RecyclerView.Adapter<PlayListsAdapter.ViewHolder> {

    private ArrayList<MyList> list;
    private final Context context;
    private final MusicPlayer player;
    private final PlaylistsFragment parent;

    public PlayListsAdapter(ArrayList<MyList> list, Context mCtx, MusicPlayer player, PlaylistsFragment playlistsFragment) {
        this.list = list;
        this.context = mCtx;
        this.player = player;
        this.parent = playlistsFragment;
    }

    @Override
    public PlayListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlists_item, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.textViewHead.setText(list.get(position).getHead());
        holder.itemView.setOnClickListener(view -> {
            if (list.get(position).getHead().equals("Create a new playlist")) {
                TextInputLayout textInputLayout = new TextInputLayout(context);
                textInputLayout.setPadding(
                        10,
                        0,
                        10,
                        0
                );
                EditText input = new EditText(context);
                textInputLayout.addView(input);
                new AlertDialog.Builder(context)
                        .setTitle("Create playlist")
                        .setView(textInputLayout)
                        .setPositiveButton("Create", (dialog, whichButton) -> {
                            if (input.getText().toString().equals("wipe")) {
                                SharedPreferences settings = context.getSharedPreferences("SAVEDATA", 0);
                                SharedPreferences.Editor editor = settings.edit();
                                String playlists = settings.getString("PLAYLISTS", "");
                                for (String key: playlists.split("\n")) {
                                    editor.remove("PLAYLIST_"+key);
                                }
                                editor.putString("PLAYLISTS", "");
                                editor.apply();
                                list = new ArrayList<>();
                                list.add(new MyList("Create a new playlist","","",0, "", 0, "", 0));
                                dialog.cancel();
                                notifyDataSetChanged();
                            }
                            else if (!input.getText().toString().isEmpty()) {
                                SharedPreferences settings = context.getSharedPreferences("SAVEDATA", 0);
                                SharedPreferences.Editor editor = settings.edit();
                                String playlists = settings.getString("PLAYLISTS", "");
                                if (playlists.isEmpty()) editor.putString("PLAYLISTS", input.getText().toString());
                                else { editor.putString("PLAYLISTS", playlists + "\n" + input.getText().toString()); }
                                editor.putString("PLAYLIST_"+ input.getText().toString(),"");
                                editor.apply();
                                list.add(0, new MyList(input.getText().toString(),"","",0, "", 0, "", 0));
                                dialog.cancel();
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, whichButton) -> dialog.cancel())
                        .show();
            }
            else {
                SharedPreferences settings = context.getSharedPreferences("SAVEDATA", 0);
                String[] playlists = settings.getString("PLAYLISTS", "").split("\n");
                String clickedPlaylist = playlists[playlists.length-position-1];
                parent.changeFragments(new LibraryFragment(player,"PLAYLIST_"+clickedPlaylist,""));
            }
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
}