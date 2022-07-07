package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MusicPlayer extends Service {
    private MediaPlayer player;
    private String currentSong;
    public MyList currentPlayingSong = new MyList("","","",0);
    public CustomAdapter adapter = null;
    public ArrayList<MyList> visibleSongs;
    public RecyclerView recyclerview;
    public View relativeLayout;
    public boolean songDone = true;
    public boolean playing = false;
    public FragmentManager manager;
    public MainActivity main;
    private final Handler mHandler = new Handler();
    private double progressBarValue = 0;
    private boolean stopProgressBarUpdates = false;
    public BigDecimal currentDuration;
    private BigDecimal playedSoFar;
    MyController controller;
    Context c;

    TextView songNameView;
    TextView songDescView;
    ImageButton BtnPrev;
    ImageButton BtnNext;
    ImageButton BtnPause;

    Detailed_song newFragment;

    public MusicPlayer(Context context) {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.currentSong = null;
        this.c = context;
    }

    public void stopPlayer() {
        if (this.player != null) {
            //TODO Stop/resume the progressbar thread instead of using stopProgressBarUpdates
            stopProgressBarUpdates = true;
            this.player.reset();
            this.player.release();
            this.player = null;
            currentSong = null;
            if (controller != null) {
                c.unregisterReceiver(controller);
                controller = null;
            }
            try {
                relativeLayout.setVisibility(View.GONE);
            }
            catch (Exception ignored) {
            }
        }
    }

    public void play(MyList mylist) {
        String song = mylist.getLocation();
        if (currentSong != null) if (currentSong.equals(song) && this.player.isPlaying()) return;
        relativeLayout = ((Activity) c).findViewById(R.id.music_bar);
        this.stopPlayer();
        this.player = new MediaPlayer();
        this.songDone = false;
        currentSong = song;
        currentDuration = new BigDecimal(String.valueOf(mylist.getDuration()));
        currentPlayingSong = mylist;
        c.registerReceiver(new MyController(this, c), new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
        this.playing = true;
        this.player.setOnCompletionListener(mp -> donePlayNext());
        if (!MainActivity.currentFragment.equals(main.PrevAndNextSongs.createdFragment)) {
            main.PrevAndNextSongs = new PrevNextList(new ArrayList<>(visibleSongs), mylist, MainActivity.currentFragment, c);
        }
        try {
            player.setDataSource(song);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNext(MyList mylist) { main.songList.add(mylist); }

    public void donePlayNext() {
        playNext(false);
    }

    public void playNext(Boolean force) {
        if (!playing) BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        if (main.songList.isEmpty()) {
            play(main.PrevAndNextSongs.Next(force));
        }
        else {
            play(main.songList.get(0));
            main.songList.remove(0);
        }
        adapter.reset();
        adapter.notifyDataSetChanged();
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class)) newFragment.initWindowElements();
        else { showBar(); }
    }

    public void playPrev(Boolean force) {
        if (!playing) BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        play(main.PrevAndNextSongs.Prev(force));
        adapter.reset();
        adapter.notifyDataSetChanged();
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class)) newFragment.initWindowElements();
    }

    public void playPause() {
        if (playing) {
            pause();
        }
        else {
            resume();
        }
    }

    public void pause() {
        playing = false;
        BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class)) newFragment.initWindowElements();
        player.pause();
    }

    public void resume() {
        playing = true;
        BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class)) newFragment.initWindowElements();
        player.start();
    }
    public void showBar() {
        try {
            this.relativeLayout.setVisibility(View.VISIBLE);
        } catch (Exception ignored) {}
        songNameView = relativeLayout.findViewById(R.id.Song_name);
        songDescView = relativeLayout.findViewById(R.id.Song_desc);
        BtnPrev = relativeLayout.findViewById(R.id.BtnPrev);
        BtnNext = relativeLayout.findViewById(R.id.BtnNext);
        BtnPause = relativeLayout.findViewById(R.id.BtnPause);
        SeekBar progressBar = relativeLayout.findViewById(R.id.progress_bar);
        songNameView.setText(currentPlayingSong.getHead());
        songDescView.setText(currentPlayingSong.getDesc());
        if (!playing) {
            playing = true;
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                main.showNotification(R.drawable.ic_baseline_play_arrow_24, currentPlayingSong.getHead());
            }
        }
        else {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                main.showNotification(R.drawable.ic_baseline_pause_24, currentPlayingSong.getHead());
            }
        }
        progressBar.setProgress(0);
        stopProgressBarUpdates = false;
        BtnNext.setOnClickListener(view -> {
            playNext(true);
            showBar();
        });
        BtnPrev.setOnClickListener(view -> {
            playPrev(true);
            showBar();
        });
        BtnPause.setOnClickListener(view -> {
            if (playing) {
                pause();
            }
            else {
                resume();
            }
        });
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    player.seekTo((int) (currentPlayingSong.getDuration()*(1.0*i/1000)));
                    progressBar.setProgress(i);
                    if (!playing) {
                        playing = true;
                        BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopProgressBarUpdates = true;
                player.pause();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                stopProgressBarUpdates = false;
                player.start();
            }
        });
        if (main.t != null && !main.t.isCancelled()) {
            main.t.cancel(true);
        }
        main.t = new AsyncTask<Void, Void, Void>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Void doInBackground(final Void... params) {
                try {
                    progressBarValue = (player.getCurrentPosition() / currentDuration.intValue());
                } catch (Exception e) {
                    return null;
                }
                while (progressBarValue < 1000) {
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
                        playedSoFar = new BigDecimal(player.getCurrentPosition() + "000");
                        progressBarValue = (
                                playedSoFar.divide(currentDuration, 2, RoundingMode.HALF_UP).doubleValue());
                    } catch (Exception e) {
                        e.printStackTrace();
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
        this.relativeLayout.setOnClickListener(view -> {
            newFragment = new Detailed_song(c, main.player, main);
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            transaction.commit();

            main.setClickable();
        });
    }

    public void seekTo(int i) { this.player.seekTo(i); }

    public void start() {
        this.player.start();
    }

    public int getCurrentPosition() {
        return this.player.getCurrentPosition();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO Crashes because Intent doesn't reach this part
        Log.d("test", intent.getAction());
        return super.onStartCommand(intent, flags, startId);
    }
}
