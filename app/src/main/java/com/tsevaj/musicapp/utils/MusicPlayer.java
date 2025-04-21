package com.tsevaj.musicapp.utils;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.IBinder;
import android.os.PowerManager;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.fragments.PagerFragment;
import com.tsevaj.musicapp.utils.data.MusicItem;

import java.io.IOException;

import lombok.Getter;

public class MusicPlayer implements ServiceConnection {
    private static MediaPlayer player;
    private static MediaSession.Token sessionToken;
    @Getter
    private static MusicItem currentPlayingSong;
    private final MusicList musicList;

    AudioManager audioManager;

    PagerFragment newFragment;

    public MusicPlayer(Context c, MusicList musicList) {
        this.musicList = musicList;
        player = new MediaPlayer();
        player.setOnErrorListener((mediaPlayer, i, i1) -> true);
        player.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnCompletionListener(mediaPlayer -> play(musicList.Next(false)));
        player.setOnPreparedListener(MediaPlayer::start);
    }

    public void play(MusicItem song) {
        if (currentPlayingSong != null) {
            if (currentPlayingSong.getHash().equals(song.getHash()) && player.isPlaying()) return;
        }

        currentPlayingSong = song;
        player.reset();
        try {
            player.setDataSource(song.getLocation());
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentSongHash() {
        return currentPlayingSong == null ? "" : currentPlayingSong.getHash();
    }

    public void resumeState(MusicItem song, int SecDuration) {
        play(song);
        player.seekTo(SecDuration);
        this.pause();
    }

    //TODO change it so the UI is not updated in these methods

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void playPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            resume();
        }
    }

    public void pause() {
        player.pause();
    }

    public void resume() {
        player.start();
    }

    public void seekTo(int i) {
        int placeToSeek = (int) (currentPlayingSong.getDuration() * (1.0 * i / 1000));
        player.seekTo(placeToSeek);
    }

    public int getCurrentPosition() {
        try {
            return player.getCurrentPosition();
        } catch (Exception e) {
            return 0;
        }
    }

    public double getCurrentProgress() {
        return ((double) getCurrentPosition()) / player.getDuration();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        /*NotificationService.myBinder binder = (NotificationService.myBinder) iBinder;
        notificationService = binder.getService();
        notificationService.setCallBack(MusicPlayer.this);
        Intent intent1 = new Intent(c, NotificationService.class);
        intent1.setAction(ACTION_NOTIFY);
        c.startService(intent1);*/
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        /*Intent intent1 = new Intent(c, NotificationService.class);
        intent1.setAction(ACTION_DELETE);
        c.startService(intent1);
        notificationService = null;*/
    }

    public void destroy() {
        if (player != null) {
            //if (main.t != null) main.t.stopThread();
            player.release();
            player = null;
        }
    }

    public boolean isInitialized() {
        return currentPlayingSong != null;
    }

    public void requestFocus(final Context context) {
        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        audioManager.requestAudioFocus(new AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(new OnFocusChangeListener().getInstance()).build()
        );
    }

    private static final class OnFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
        boolean pausedByAudioFocus = false;

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
                    if (pausedByAudioFocus && currentPlayingSong != null) {
                        player.start();
                        pausedByAudioFocus = false;
                        break;
                    }
                case AUDIOFOCUS_LOSS:
                case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                case AUDIOFOCUS_LOSS_TRANSIENT:
                    if (player.isPlaying()) {
                        try {
                            player.pause();
                            pausedByAudioFocus = true;
                        } catch (Exception ignored) {}
                        break;
                    }
            }
        }
    }
}