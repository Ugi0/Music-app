package com.tsevaj.musicapp.uielements;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.PagerFragment;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.ProgressBarThread;

public class VisibleMenuBarImpl extends View {
    private SeekBar progressbar;
    private View menuBarLayout;
    private MusicPlayer player;
    private ProgressBarThread thread;

    public VisibleMenuBarImpl(Context context, MusicPlayer player) {
        super(context);
        this.player = player;

        init();
    }

    public void updateProgress() {
        //TODO Update seekbar progress here based on the value from player
        //Called every 100 ms
    }

    private void init() {

        doLayout();
    }

    private void doLayout() {

    }

    TextView songNameView;
    TextView songDescView;
    ImageButton BtnPrev;
    ImageButton BtnNext;
    ImageButton BtnPause;

    public void showPauseButton() {

    }

    public void showPlayButton() {

    }

    private void prepareButtons() {
        if (BtnNext != null) {
            BtnNext.setOnClickListener(view -> {
                player.playNext(true);
                doMenuBar();
            });
        }
        if (BtnPrev != null) {
            BtnPrev.setOnClickListener(view -> {
                player.playPrev(true);
                doMenuBar();
            });
        }
        if (BtnPause != null) {
            BtnPause.setOnClickListener(view -> player.playPause());
        }
    }

    public View doMenuBar() {
        try {
            setVisibility(View.VISIBLE);
        } catch (Exception ignored) {}
        songNameView = findViewById(R.id.Song_name);
        songDescView = findViewById(R.id.Song_desc);
        BtnPrev = findViewById(R.id.BtnPrev);
        BtnNext = findViewById(R.id.BtnNext);
        BtnPause = findViewById(R.id.BtnPause);
        SeekBar progressBar = findViewById(R.id.progress_bar);
        songNameView.setText(currentPlayingSong.getTitle());
        songDescView.setText(currentPlayingSong.getDesc());
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
                thread.stopThread();
                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                thread.resumeThread();
                player.start();
            }
        });
        thread = new ProgressBarThread(progressBar, main);
        this.setOnClickListener(view -> {
            //newFragment = new DetailedsongFragment(main.player, main);
            newFragment = new PagerFragment(player, main);
            MainActivity.currentFragment = newFragment;
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            transaction.commit();

            main.setClickable();
        });
        prepareButtons();
    }
}
