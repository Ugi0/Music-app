package com.tsevaj.musicapp.utils;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.tsevaj.musicapp.adapters.CustomAdapter;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.DetailedsongFragment;
import com.tsevaj.musicapp.services.NotificationController;
import com.tsevaj.musicapp.services.NotificationService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class MusicPlayer implements NotificationController, ServiceConnection {
    private MediaPlayer player;
    public MediaSessionCompat.Token sessionToken;
    private String currentSong;
    public MusicItem currentPlayingSong = new MusicItem("", "", "", 0, "", 0, "", 0);
    public CustomAdapter adapter = null;
    public ArrayList<MusicItem> visibleSongs;
    public RecyclerView recyclerview;
    public View relativeLayout;
    public boolean songDone = true;
    public static boolean playing = false;
    public static String songName;
    public static String songArtist;
    public FragmentManager manager;
    public MainActivity main;
    public BigDecimal currentDuration;
    NotificationService notificationService;
    public Context c;
    private boolean serviceStarted = false;
    private static AudioManager audioManager;
    private static boolean focusGranted;
    private boolean pausedByAudioFocus;

    TextView songNameView;
    TextView songDescView;
    ImageButton BtnPrev;
    ImageButton BtnNext;
    ImageButton BtnPause;

    DetailedsongFragment newFragment;

    @SuppressLint("NewApi")
    public MusicPlayer(Context c) {
        this.c = c;
        player = new MediaPlayer();
        this.currentSong = null;
        pausedByAudioFocus = false;
        player.setOnErrorListener((mediaPlayer, i, i1) -> true);
        player.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnCompletionListener(mediaPlayer -> donePlayNext());
    }

    @SuppressLint("NewApi")
    public void play(MusicItem mylist) {
        String song = mylist.getLocation();
        if (currentSong != null) if (currentSong.equals(song) && this.player.isPlaying()) return;
        relativeLayout = ((Activity) c).findViewById(R.id.music_bar);
        this.songDone = false;
        playing = true;
        currentSong = song;
        songName = mylist.getHead();
        songArtist = mylist.getArtist();

        currentDuration = new BigDecimal(String.valueOf(mylist.getDuration()));
        currentPlayingSong = mylist;
        main.showNotification(R.drawable.ic_baseline_pause_24, currentPlayingSong.getHead());
        player.release();
        player = MediaPlayer.create(c, Uri.parse(song));
        player.setOnErrorListener((mediaPlayer, i, i1) -> true);
        player.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnCompletionListener(mediaPlayer -> donePlayNext());
        player.start();
        if (!serviceStarted) {
            Intent intent = new Intent(c, NotificationService.class);
            c.bindService(intent, this, 0);
            Intent intent1 = new Intent(c, NotificationService.class);
            c.startService(intent1);
            requestFocus(c);
            serviceStarted = true;
        }
        else {
            prepareButtons();
        }
    }

    public void recreateList(MusicItem mylist) {
        if (main.PrevAndNextSongs.createdFragment == null) {
            main.PrevAndNextSongs = new PrevNextList(new ArrayList<>(visibleSongs), mylist, MainActivity.currentFragment, c);
        }
        if (main.PrevAndNextSongs.wholeList) {
            main.PrevAndNextSongs.setList(MainActivity.wholeSongList);
        } else {
            main.PrevAndNextSongs.setList(visibleSongs);
        }
    }

    @SuppressLint("NewApi")
    public void prepareButtons() {
        if (!MainActivity.currentFragment.getClass().equals(DetailedsongFragment.class)) {
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

    public void setNext(MusicItem mylist) {
        main.songQueue.add(mylist);
    }

    public void donePlayNext() {
        playNext(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void playNext(Boolean force) {
        if (!playing) try {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        } catch (Exception ignored) {
        }
        MusicItem song = main.PrevAndNextSongs.Next(force);

        play(song);

        adapter.reset();
        adapter.notifyDataSetChanged();
        if (MainActivity.currentFragment.getClass().equals(DetailedsongFragment.class))
            newFragment.initWindowElements();
        else {
            showBar();
        }
    }

    @SuppressLint({"NewApi", "NotifyDataSetChanged"})
    public void playPrev(Boolean force) {
        if (!playing) try {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        } catch (Exception ignored) {
        }
        play(main.PrevAndNextSongs.Prev());
        adapter.reset();
        adapter.notifyDataSetChanged();
        if (MainActivity.currentFragment.getClass().equals(DetailedsongFragment.class))
            newFragment.initWindowElements();
        else {
            showBar();
        }
    }

    public void playPause() {
        if (playing) {
            main.showNotification(R.drawable.ic_baseline_play_arrow_24, currentPlayingSong.getHead());
            pause();
            try {
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            } catch (Exception ignored) {
            }
        } else {
            main.showNotification(R.drawable.ic_baseline_pause_24, currentPlayingSong.getHead());
            resume();
            try {
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            } catch (Exception ignored) {
            }
        }
        if (MainActivity.currentFragment.getClass().equals(DetailedsongFragment.class))
            newFragment.initWindowElements();
    }

    public void pause() {
        playing = false;
        main.t.stopThread();
        player.pause();
    }

    public void resume() {
        playing = true;
        try {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        } catch (Exception ignored) {
        }
        if (MainActivity.currentFragment.getClass().equals(DetailedsongFragment.class))
            newFragment.initWindowElements();
        player.start();
        main.t.resumeThread();
    }

    @SuppressLint("NewApi")
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
            newFragment = new DetailedsongFragment(main.player, main);
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
            this.player.release();
            this.player = null;
            currentSong = null;
        }
    }
    public void requestFocus(final Context context) {
        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        audioManager.requestAudioFocus(new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChange -> {
                    switch (audioManager.requestAudioFocus((new OnFocusChangeListener()).getInstance(),
                            AudioManager.STREAM_MUSIC, AUDIOFOCUS_GAIN)) {
                        case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                            focusGranted = true;
                            break;

                        case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                            focusGranted = false;
                            break;
                    }
                }).build()
        );
    }

    private final class OnFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        private OnFocusChangeListener instance;

        private OnFocusChangeListener getInstance() {
            if (instance == null) {
                instance = new OnFocusChangeListener();
            }
            return instance;
        }

        @Override
        public void onAudioFocusChange(final int focusChange) {
            switch (focusChange) {
                case AUDIOFOCUS_GAIN:
                    if (pausedByAudioFocus && !playing) {
                        player.start();
                        playing = true;
                        pausedByAudioFocus = false;
                        break;
                    }

                case AUDIOFOCUS_LOSS:
                case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                case AUDIOFOCUS_LOSS_TRANSIENT:
                    if (playing) {
                        try {
                            playing = false;
                            main.player.pause();
                            pausedByAudioFocus = true;
                        } catch (Exception ignored) {
                        }
                        break;
                    }
            }
        }
    }
}