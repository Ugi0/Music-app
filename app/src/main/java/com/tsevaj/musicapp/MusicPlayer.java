package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MusicPlayer implements NotificationController, ServiceConnection {
    public MediaPlayer player;
    public MediaSession.Token sessionToken;
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
    public BigDecimal currentDuration;
    MyController controller;
    NotificationService notificationService;
    public Context c;

    TextView songNameView;
    TextView songDescView;
    ImageButton BtnPrev;
    ImageButton BtnNext;
    ImageButton BtnPause;

    Detailed_song newFragment;

    @SuppressLint("NewApi")
    public MusicPlayer(Context c) {
        this.c = c;
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.currentSong = null;
        Intent intent = new Intent(c, NotificationService.class);
        c.bindService(intent, this, 0);
    }

    public void stopPlayer() {
        if (this.player != null) {
            if (main.t != null) main.t.stopThread();
            this.player.reset();
            this.player.release();
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

    @SuppressLint("NewApi")
    public void play(MyList mylist) {
        if (BtnPause != null) {
            BtnPause.setOnClickListener(null);
            BtnNext.setOnClickListener(null);
            BtnPrev.setOnClickListener(null);
        }
        String song = mylist.getLocation();
        if (currentSong != null) if (currentSong.equals(song) && this.player.isPlaying()) return;
        relativeLayout = ((Activity) c).findViewById(R.id.music_bar);
        player.stop();
        player.reset();
        player.setOnErrorListener((mediaPlayer, i, i1) -> true);
        if (songDone) main.showNotification(R.drawable.ic_baseline_pause_24, mylist.getHead());
        this.songDone = false;
        Log.d("test", "Play");
        playing = true;
        currentSong = song;
        currentDuration = new BigDecimal(String.valueOf(mylist.getDuration()));
        currentPlayingSong = mylist;
        try {
            player.setDataSource(song);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(mediaPlayer -> {
            c.registerReceiver(new MyController(main.player, c), new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
            player.setOnCompletionListener(mp -> donePlayNext());
            if (!MainActivity.currentFragment.equals(main.PrevAndNextSongs.createdFragment)) {
                main.PrevAndNextSongs = new PrevNextList(new ArrayList<>(visibleSongs), mylist, MainActivity.currentFragment, c);
            }
            player.start();
            prepareButtons();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void prepareButtons() {
        if (!MainActivity.currentFragment.getClass().equals(Detailed_song.class)) {
            BtnNext.setOnClickListener(view -> {
                playNext(true);
                showBar();
            });
            BtnPrev.setOnClickListener(view -> {
                playPrev(true);
                showBar();
            });
            BtnPause.setOnClickListener(view -> playPause());
        }
    }

    public void setNext(MyList mylist) { main.songList.add(mylist); }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void donePlayNext() {
        playNext(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void playNext(Boolean force) {
        Log.d("test", "PlayNext");
        if (!playing) try { BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);  } catch (Exception ignored) {}
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

    @SuppressLint({"NewApi", "NotifyDataSetChanged"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void playPrev(Boolean force) {
        if (!playing) try { BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);  } catch (Exception ignored) {}
        play(main.PrevAndNextSongs.Prev(force));
        adapter.reset();
        adapter.notifyDataSetChanged();
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class)) newFragment.initWindowElements();
    }

    public void playPause() {
        if (this.playing) {
            Log.d("test", "Paused");
            pause();
            try { BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24); } catch (Exception ignored) {}
            if (MainActivity.currentFragment.getClass().equals(Detailed_song.class)) newFragment.initWindowElements();
        }
        else {
            Log.d("test", "Resumed");
            resume();
        }
    }

    public void pause() {
        this.playing = false;
        Log.d("test", "Paused");
        main.t.stopThread();
        player.pause();
    }

    public void resume() {
        this.playing = true;
        Log.d("test", "Resumed");
        try { BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24); } catch (Exception ignored) {}
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class)) newFragment.initWindowElements();
        player.start();
        main.t.resumeThread();
    }
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
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
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
          //  main.showNotification(R.drawable.ic_baseline_play_arrow_24, currentPlayingSong.getHead());
        }
        else {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            //main.showNotification(R.drawable.ic_baseline_pause_24, currentPlayingSong.getHead());
        }

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
                main.t.stopThread();
                player.pause();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                main.t.resumeThread();
                    player.start();
            }
        });
        main.t = new ProgressBarThread(progressBar, main);
        this.relativeLayout.setOnClickListener(view -> {
            newFragment = new Detailed_song(c, main.player, main);
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            transaction.commit();

            main.setClickable();
        });
        prepareButtons();
    }

    public void seekTo(int i) { this.player.seekTo(i); }

    public int getCurrentPosition() {
        try { return this.player.getCurrentPosition(); }
        catch (Exception e) { return 0; }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        NotificationService.myBinder binder = (NotificationService.myBinder) iBinder;
        notificationService = binder.getService();
        notificationService.setCallBack(MusicPlayer.this);
        Log.d("test", "onServiceConnected: ");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        notificationService = null;
    }
}
