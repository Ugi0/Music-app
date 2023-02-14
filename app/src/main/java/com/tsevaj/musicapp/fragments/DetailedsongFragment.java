package com.tsevaj.musicapp.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.ProgressBarThread;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.utils.CircularSeekBar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class DetailedsongFragment extends Fragment {
    View ll;
    private final MusicPlayer player;
    private final MainActivity main;
    private View currentView;

    public DetailedsongFragment(MusicPlayer player, MainActivity main) {
        this.player = player;
        this.main = main;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ll = inflater.inflate(R.layout.song_detailed_view, container, false);

        main.setBackground(ll, getResources());
        MainActivity.currentFragment = this;

        initWindowElements(ll);

        return ll;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

    public void initWindowElements() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initWindowElements(currentView);
        }
    }

    @SuppressLint({"UseCompatTextViewDrawableApis", "NewApi", "NonConstantResourceId"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void initWindowElements(View parentView) {
        int textColor = Color.parseColor(main.getBaseContext().getSharedPreferences("SAVEDATA", 0).getString("THEME_COLOR", "#FFFFFF"));

        currentView = parentView;
        SharedPreferences settings = main.getBaseContext().getSharedPreferences("SAVEDATA", 0);

        ArrayList<String> favorite = main.getFavorites();

        TextView songNameView = parentView.findViewById(R.id.song_information_name);
        TextView songDescView = parentView.findViewById(R.id.song_information_author);
        TextView songLocView = parentView.findViewById(R.id.song_information_location);
        ImageButton BtnPrev = parentView.findViewById(R.id.BtnPrev);
        ImageButton BtnNext = parentView.findViewById(R.id.BtnNext);
        ImageButton BtnPause = parentView.findViewById(R.id.BtnPause);
        CircularSeekBar progressBar = parentView.findViewById(R.id.progress_bar);
        ImageButton shuffle = parentView.findViewById(R.id.detailed_shuffle);
        ImageButton replay = parentView.findViewById(R.id.detailed_replay);
        ImageButton favoriteButton = parentView.findViewById(R.id.detailed_add_to_favorites);
        ImageButton menuButton = parentView.findViewById(R.id.detailed_menu_button);

        songNameView.setTextColor(textColor);
        songDescView.setTextColor(textColor);
        songLocView.setTextColor(textColor);
        songDescView.setCompoundDrawableTintList(ColorStateList.valueOf(textColor));
        songLocView.setCompoundDrawableTintList(ColorStateList.valueOf(textColor));

        favoriteButton.setActivated(favorite.contains(player.currentPlayingSong.getHead()));

        songNameView.setText(player.currentPlayingSong.getHead());
        songDescView.setText(player.currentPlayingSong.getDesc());
        songLocView.setText(player.currentPlayingSong.getLocationFolder());
        if (!MusicPlayer.playing) {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
        else {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        }
        main.t.resumeThread();
        SharedPreferences.Editor editor = settings.edit();
        switch (settings.getInt("REPLAY_MODE",0)) {
            case -1: {
                replay.setActivated(true);
                break;
            }
            case 1: {
                replay.setActivated(false);
                replay.setSelected(true);
                break;
            }
        }
        if (settings.getBoolean("SHUFFLE",false)) shuffle.setActivated(true);
        shuffle.setOnClickListener(view -> {
            shuffle.setActivated(!shuffle.isActivated());
            editor.putBoolean("SHUFFLE",shuffle.isActivated());
            editor.apply();
            main.PrevAndNextSongs.reRoll();
            player.prepareButtons();
        });
        replay.setOnClickListener(view -> {
            if (replay.isActivated()) {
                //Play just one song
                replay.setActivated(false);
                replay.setSelected(true);
                editor.putInt("REPLAY_MODE",1);
                editor.apply();
            }
            else {
                //Keep playing songs again
                replay.setActivated(true);
                editor.putInt("REPLAY_MODE",-1);
                editor.apply();
                main.PrevAndNextSongs.reRoll();
            }
        });
        menuButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(requireContext(), view);
            popup.inflate(R.menu.detailed_menu);
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.addtoplaylist: {
                        PopupMenu menu = new PopupMenu(requireContext(), view);
                        for (String menuItem : requireContext().getSharedPreferences("SAVEDATA", 0).getString("PLAYLISTS", "").split("\n")) {
                            menu.getMenu().add(menuItem);
                        }
                        menu.setOnMenuItemClickListener(item1 -> {
                            SharedPreferences.Editor editor2 = requireContext().getSharedPreferences("SAVEDATA", 0).edit();
                            String currentPlaylist = requireContext().getSharedPreferences("SAVEDATA", 0).getString("PLAYLIST_" + item1.getTitle(), "");
                            if (Arrays.asList(currentPlaylist.split("\n")).contains(player.currentPlayingSong.getHead())) return true;
                            if (currentPlaylist.isEmpty())
                                editor2.putString("PLAYLIST_" + item1.getTitle(), player.currentPlayingSong.getHead());
                            else {
                                editor2.putString("PLAYLIST_" + item1.getTitle(), currentPlaylist + "\n" + player.currentPlayingSong.getHead());
                            }
                            editor2.apply();
                            return true;
                        });
                        menu.show();
                        break;
                    }
                    case R.id.song_delete: {
                        try {
                            Files.delete(Paths.get(player.currentPlayingSong.getLocation()));
                            MainActivity.wholeSongList.remove(player.currentPlayingSong);
                            main.PrevAndNextSongs.removeFromPrev(player.currentPlayingSong);
                            player.playNext(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                return true;
            });
            popup.show();
        });
        favoriteButton.setOnClickListener(view -> {
            if (favoriteButton.isActivated()) {
                main.removeFromFavorites(player.currentPlayingSong.getHead());
                favoriteButton.setActivated(false);
            }
            else {
                main.addToFavorites(player.currentPlayingSong.getHead());
                favoriteButton.setActivated(true);
            }
        });
        BtnNext.setOnClickListener(view -> detailed_next());
        BtnPrev.setOnClickListener(view -> detailed_prev());
        BtnPause.setOnClickListener(view -> {
            if (MusicPlayer.playing) {
                player.playPause();
                MusicPlayer.playing = false;
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            }
            else {
                player.playPause();
                MusicPlayer.playing = true;
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            }
        });
        progressBar.setOnSeekBarChangeListener(new CircleSeekBarListener(progressBar, BtnPause));
        main.t = new ProgressBarThread(progressBar, main);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void detailed_next() {
        player.playNext(true);
        initWindowElements(ll);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void detailed_prev() {
        player.playPrev(true);
        initWindowElements(ll);
    }

    public class CircleSeekBarListener implements CircularSeekBar.OnCircularSeekBarChangeListener {
        private final CircularSeekBar progressBar;
        private final ImageButton BtnPause;

        public CircleSeekBarListener(CircularSeekBar progressbar, ImageButton BtnPause) {
            this.progressBar = progressbar;
            this.BtnPause = BtnPause;
        }
        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
            if (fromUser) {
                player.seekTo((int) (player.currentPlayingSong.getDuration()*(1.0*progress/10000)));
                progressBar.setProgress(progress);
                if (!MusicPlayer.playing) {
                    MusicPlayer.playing = true;
                    BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {
            player.pause();
            main.t.stopThread();
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            player.resume();
            main.t.resumeThread();
        }
    }
}
