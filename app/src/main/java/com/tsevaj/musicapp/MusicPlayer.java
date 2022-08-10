package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class MusicPlayer implements NotificationController, ServiceConnection {
    public MediaPlayer player;
    public MediaSessionCompat.Token sessionToken;
    private String currentSong;
    public MyList currentPlayingSong = new MyList("", "", "", 0, "", 0, "", 0);
    public CustomAdapter adapter = null;
    public ArrayList<MyList> visibleSongs;
    public RecyclerView recyclerview;
    public View relativeLayout;
    public boolean songDone = true;
    public static boolean playing = false;
    public static String songName;
    public FragmentManager manager;
    public MainActivity main;
    public BigDecimal currentDuration;
    NotificationService notificationService;
    public Context c;
    private boolean serviceStarted = false;
    private static AudioManager audioManager;
    private static int changedFocus;
    private static boolean focusGranted;

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
    }

    @SuppressLint("NewApi")
    public void play(MyList mylist) {
        String song = mylist.getLocation();
        if (currentSong != null) if (currentSong.equals(song) && this.player.isPlaying()) return;
        relativeLayout = ((Activity) c).findViewById(R.id.music_bar);
        player.stop();
        player.reset();
        player.setOnErrorListener((mediaPlayer, i, i1) -> true);
        if (songDone) main.showNotification(R.drawable.ic_baseline_pause_24, mylist.getHead());
        this.songDone = false;
        playing = true;
        currentSong = song;
        songName = mylist.getHead();
        if (!serviceStarted) {
            Intent intent = new Intent(c, NotificationService.class);
            c.bindService(intent, this, 0);
            Intent intent1 = new Intent(c, NotificationService.class);
            c.startService(intent1);
            requestFocus(c);
            serviceStarted = true;
        }
        currentDuration = new BigDecimal(String.valueOf(mylist.getDuration()));
        currentPlayingSong = mylist;
        main.showNotification(R.drawable.ic_baseline_pause_24, currentPlayingSong.getHead());
        try {
            player.setDataSource(song);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(mediaPlayer -> {
            player.setOnCompletionListener(mp -> donePlayNext());
            if (!MainActivity.currentFragment.equals(main.PrevAndNextSongs.createdFragment)) {
                main.PrevAndNextSongs = new PrevNextList(new ArrayList<>(visibleSongs), mylist, MainActivity.currentFragment, c);
            }
            player.start();
            prepareButtons();
        });
    }

    @SuppressLint("NewApi")
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

    public void setNext(MyList mylist) {
        main.songList.add(mylist);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void donePlayNext() {
        playNext(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void playNext(Boolean force) {
        if (!playing) try {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        } catch (Exception ignored) {
        }
        if (main.songList.isEmpty()) {
            play(main.PrevAndNextSongs.Next(force));
        } else {
            play(main.songList.get(0));
            main.songList.remove(0);
        }
        adapter.reset();
        adapter.notifyDataSetChanged();
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class))
            newFragment.initWindowElements();
        else {
            showBar();
        }
    }

    @SuppressLint({"NewApi", "NotifyDataSetChanged"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void playPrev(Boolean force) {
        if (!playing) try {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        } catch (Exception ignored) {
        }
        play(main.PrevAndNextSongs.Prev(force));
        adapter.reset();
        adapter.notifyDataSetChanged();
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class))
            newFragment.initWindowElements();
        else {
            showBar();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void playPause() {
        if (playing) {
            main.showNotification(R.drawable.ic_baseline_play_arrow_24, currentPlayingSong.getHead());
            pause();
            try {
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            } catch (Exception ignored) {
            }
            if (MainActivity.currentFragment.getClass().equals(Detailed_song.class))
                newFragment.initWindowElements();
        } else {
            main.showNotification(R.drawable.ic_baseline_pause_24, currentPlayingSong.getHead());
            resume();
            try {
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            } catch (Exception ignored) {
            }
            if (MainActivity.currentFragment.getClass().equals(Detailed_song.class))
                newFragment.initWindowElements();
        }
    }

    public void pause() {
        playing = false;
        main.t.stopThread();
        player.pause();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void resume() {
        playing = true;
        try {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        } catch (Exception ignored) {
        }
        if (MainActivity.currentFragment.getClass().equals(Detailed_song.class))
            newFragment.initWindowElements();
        player.start();
        main.t.resumeThread();
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showBar() {
        try {
            this.relativeLayout.setVisibility(View.VISIBLE);
        } catch (Exception ignored) {
        }
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
        } else {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        }

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    player.seekTo((int) (currentPlayingSong.getDuration() * (1.0 * i / 1000)));
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
            newFragment = new Detailed_song(main.player, main);
            MainActivity.currentFragment = newFragment;
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            transaction.commit();

            main.setClickable();
        });
        prepareButtons();
    }

    public void seekTo(int i) {
        this.player.seekTo(i);
    }

    public int getCurrentPosition() {
        try {
            return this.player.getCurrentPosition();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        NotificationService.myBinder binder = (NotificationService.myBinder) iBinder;
        notificationService = binder.getService();
        notificationService.setCallBack(MusicPlayer.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { notificationService = null; }

    public void destroy() {
        if (this.player != null) {
            if (main.t != null) main.t.stopThread();
            this.player.reset();
            this.player.release();
            this.player = null;
            currentSong = null;
        }
    }
    public void requestFocus(final Context context) {
        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        switch (audioManager.requestAudioFocus((new OnFocusChangeListener()).getInstance(),
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                focusGranted = true;
                break;

            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                focusGranted = false;
                break;
        }
    }

    private final class OnFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        private OnFocusChangeListener instance;

        protected OnFocusChangeListener getInstance() {
            if (instance == null) {
                instance = new OnFocusChangeListener();
            }
            return instance;
        }

        @Override
        public void onAudioFocusChange(final int focusChange) {
            changedFocus = focusChange;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    player.start();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {player.pause();}
                    catch (Exception ignored) {}
                    break;
            }
        }
    }
}