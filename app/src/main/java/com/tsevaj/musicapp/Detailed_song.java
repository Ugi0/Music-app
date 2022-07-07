package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class Detailed_song extends Fragment {
    View ll;
    private final MusicPlayer player;
    private double progressBarValue;
    private BigDecimal playedSoFar;
    private boolean stopProgressBarUpdates = false;
    private final Handler mHandler = new Handler();
    private final MainActivity main;
    private View currentView;

    public Detailed_song(Context c, MusicPlayer player, MainActivity main) {
        this.player = player;
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ll = inflater.inflate(R.layout.song_detailed_view, container, false);
        ll.setBackground(getContext().getDrawable(R.drawable.background));
        MainActivity.currentFragment = this;

        initWindowElements(ll);

        return ll;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

    public void initWindowElements() {
        initWindowElements(currentView);
    }

    public void initWindowElements(View parentView) {
        currentView = parentView;
        SharedPreferences settings = main.getBaseContext().getSharedPreferences("SAVEDATA", 0);

        ArrayList<String> favo = main.getFavorites();

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

        favoriteButton.setActivated(favo.contains(player.currentPlayingSong.getHead()));

        songNameView.setText(player.currentPlayingSong.getHead());
        songDescView.setText(player.currentPlayingSong.getDesc());
        songLocView.setText(player.currentPlayingSong.getLocation().split("/")[player.currentPlayingSong.getLocation().split("/").length-2]);
        if (!player.playing) {
            player.playing = true;
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        }
        else {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        }
        stopProgressBarUpdates = false;
        SharedPreferences.Editor editor = settings.edit();
        //TODO Shuffle control -> save in settings
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
                });
        replay.setOnClickListener(view -> {
            //TODO Replay controls
            if (replay.isActivated()) {
                //Play just one song
                replay.setActivated(false);
                replay.setSelected(true);
                editor.putInt("REPLAY_MODE",1);
                editor.apply();
            }
            else if (replay.isSelected()) {
                //Default, no replay
                replay.setSelected(false);
                editor.putInt("REPLAY_MODE",0);
                editor.apply();
                //TODO Keep Current first PrevNExtList.LIST_SIZE Amount of songs in Next and Prev list
                // -> Make method in PrevNextList
            }
            else {
                //Keep playing songs again
                replay.setActivated(true);
                editor.putInt("REPLAY_MODE",-1);
                editor.apply();
                main.PrevAndNextSongs.reRoll();
            }
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
            if (player.playing) {
                player.pause();
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            }
            else {
                player.resume();
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            }
        });
        progressBar.setOnSeekBarChangeListener(new CircleSeekBarListener(progressBar, BtnPause));
        main.t.cancel(true);
        main.t = new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Void doInBackground(final Void... params) {
                try {
                    progressBarValue = (player.getCurrentPosition() / player.currentDuration.intValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (progressBarValue < 10000) {
                    if(isCancelled()) {
                        break;
                    }
                    if (stopProgressBarUpdates) {
                        try {
                            Thread.sleep(300);
                            continue;
                        } catch (InterruptedException ignored) {
                        }
                    }
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {
                    }
                    try {
                        playedSoFar = new BigDecimal(player.getCurrentPosition() + "0000");
                        progressBarValue = (
                                playedSoFar.divide(main.player.currentDuration, 2, RoundingMode.HALF_UP).doubleValue());
                    } catch (Exception ignored) {
                    }
                    mHandler.post(() -> progressBar.setProgress((int) progressBarValue));
                }
                return null;
            }
        };
        try {
            main.t.execute();
        }
        catch (Exception ignored) {}
    }
    public void detailed_next() {
        player.playNext(true);
        initWindowElements(ll);
    }
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
                if (!player.playing) {
                    player.playing = true;
                    BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {
            stopProgressBarUpdates = true;
            player.pause();
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            stopProgressBarUpdates = false;
            player.start();
        }
    }
}
