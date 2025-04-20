package com.tsevaj.musicapp.uielements;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.PagerFragment;
import com.tsevaj.musicapp.fragments.interfaces.HasProgressBar;
import com.tsevaj.musicapp.fragments.interfaces.RefreshableFragment;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.ProgressBarThread;
import com.tsevaj.musicapp.utils.data.MusicItem;

import java.util.Objects;

public class VisibleMenuBarImpl implements HasProgressBar {
    private SeekBar progressBar;
    private final MusicPlayer player;
    private final View layout;
    private ProgressBarThread thread;

    private final MainActivity main;
    private RefreshableFragment parent;

    public VisibleMenuBarImpl(View barLayout, MainActivity main, RefreshableFragment parent) {
        layout = barLayout;
        this.player = main.getPlayer();
        this.main = main;
        this.parent = parent;

        init();
    }

    public void updateProgress() {
        //TODO Make this not throw error for attempting to call in wrong state
        return;
        //progressBar.setProgress((int) (player.getCurrentProgress() * 1000));
    }

    private void init() {

        doMenuBar();
    }

    TextView songNameView;
    TextView songDescView;
    ImageButton BtnPrev;
    ImageButton BtnNext;
    ImageButton BtnPause;

    public void showPauseButton() {
        BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
    }

    public void showPlayButton() {
        BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
    }

    public void stopThread() {
        try {
            thread.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resumeThread() {
        thread.notify();
    }

    private void prepareButtons() {
        BtnNext.setOnClickListener(view -> {
            main.handleNextSong(true);
            handleSongChange(MusicPlayer.getCurrentPlayingSong());
            doMenuBar();
        });

        BtnPrev.setOnClickListener(view -> {
            main.handlePrevSong(true);
            handleSongChange(MusicPlayer.getCurrentPlayingSong());
            doMenuBar();
        });

        BtnPause.setOnClickListener(view -> {
            main.handlePause();
            if (player.isPlaying()) {
                showPauseButton();
            } else {
                showPlayButton();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void handleSongChange(MusicItem song) {
        layout.setVisibility(View.VISIBLE);
        songNameView.setText(song.getTitle());
        songDescView.setText(song.getDesc());
        BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        Objects.requireNonNull(parent.getRecyclerView().getAdapter()).notifyDataSetChanged();
    }

    public void doMenuBar() {
        songNameView = layout.findViewById(R.id.Song_name);
        songDescView = layout.findViewById(R.id.Song_desc);
        BtnPrev = layout.findViewById(R.id.BtnPrev);
        BtnNext = layout.findViewById(R.id.BtnNext);
        BtnPause = layout.findViewById(R.id.BtnPause);
        progressBar = layout.findViewById(R.id.progress_bar);
        if (MusicPlayer.getCurrentPlayingSong() != null) {
            songNameView.setText(MusicPlayer.getCurrentPlayingSong().getTitle());
            songDescView.setText(MusicPlayer.getCurrentPlayingSong().getDesc());
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.INVISIBLE);
        }
        BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    player.seekTo(i);
                    progressBar.setProgress(i);
                    if (!player.isPlaying()) {
                        BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                thread.pause();
                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                thread.resume();
                player.resume();
            }
        });
        thread = new ProgressBarThread(this);
        thread.start();
        this.layout.setOnClickListener(view -> {
            main.changeFragment(PagerFragment.class);
        });
        prepareButtons();
    }
}
