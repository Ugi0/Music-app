package com.tsevaj.musicapp.utils;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

import static com.tsevaj.musicapp.services.notification.NotificationClass.ACTION_DELETE;
import static com.tsevaj.musicapp.services.notification.NotificationClass.ACTION_NOTIFY;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tsevaj.musicapp.adapters.CustomAdapter;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.PagerFragment;
import com.tsevaj.musicapp.services.notification.NotificationController;
import com.tsevaj.musicapp.services.notification.NotificationService;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayer implements NotificationController, ServiceConnection {
    private static MediaPlayer player;
    private static MediaSession.Token sessionToken;
    private static MusicItem currentPlayingSong;
    //private CustomAdapter adapter = null;
    //private ArrayList<MusicItem> visibleSongs;
    //private RecyclerView recyclerview;
    //private FragmentManager manager;
    private PrevNextList musicList;
    private MainActivity main;
    //private NotificationService notificationService;
    private Context c;

    AudioManager audioManager;

    PagerFragment newFragment;

    //TODO Change this class to only handle the playback, not any UI changes
    public MusicPlayer(Context c) {
        this.c = c;
        player = new MediaPlayer();
        player.setOnErrorListener((mediaPlayer, i, i1) -> true);
        player.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnCompletionListener(mediaPlayer -> donePlayNext());
        player.setOnPreparedListener(MediaPlayer::start);
    }

    public void play(MusicItem song) {
        if (currentPlayingSong != null) {
            if (currentPlayingSong.getHash() == (song.getHash()) && player.isPlaying()) return;
        }
            //Intent intent = new Intent(c, NotificationService.class);
            //c.bindService(intent, this, 0);
            //Intent intent1 = new Intent(c, NotificationService.class);
            //intent1.setAction("NOTIFY");
            //c.startService(intent1);
            //requestFocus(c);
        //relativeLayout = ((Activity) c).findViewById(R.id.music_bar);

        currentPlayingSong = song;
        player.reset();
        try {
            player.setDataSource(song.getLocation());
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //main.showNotification(R.drawable.ic_baseline_pause_24);

        //main.mediaController.updateTrackInformation(song.getTitle(), song.getArtist());
        //prepareButtons();
    }

    /*public void recreateList(MusicItem song) {
        if (main.PrevAndNextSongs.createdFragment == null) {
            main.PrevAndNextSongs = new PrevNextList(new ArrayList<>(visibleSongs), song, MainActivity.currentFragment, c);
        }
        if (main.PrevAndNextSongs.wholeList) {
            main.PrevAndNextSongs.setList(MainActivity.wholeSongList);
        } else {
            main.PrevAndNextSongs.setList(visibleSongs);
        }
    }*/

    public void resumeState(MusicItem song, int SecDuration) {
        play(song);
        player.seekTo(SecDuration);
        this.pause();
        //adapter.notifyItemChanged(adapter.getList().indexOf(song));
    }

    public void setNext(MusicItem song) {
        main.songQueue.add(song);
    }

    //TODO change it so the UI is not updated in these methods
    @SuppressLint("NotifyDataSetChanged")
    public void playNext(Boolean force) {
        //if (!player.isPlaying()) try {
        //    BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        //} catch (Exception ignored) {
        //}
        MusicItem song = main.PrevAndNextSongs.Next(force);

        play(song);

        //adapter.reset();
        //adapter.notifyDataSetChanged();
        //if (MainActivity.currentFragment.getClass().equals(PagerFragment.class)) newFragment.setPause(false);
        //else {
        //    showBar();
        //}
    }

    @SuppressLint("NotifyDataSetChanged")
    public void playPrev(Boolean force) {
        //if (!player.isPlaying()) try {
        //    BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        //} catch (Exception ignored) {
        //}
        MusicItem song = main.PrevAndNextSongs.Prev();
        play(song);
        //adapter.reset();
        //adapter.notifyDataSetChanged();
        //if (MainActivity.currentFragment.getClass().equals(PagerFragment.class)) newFragment.changeSong(song);
        //else {
        //    showBar();
        //}
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void playPause() {
        if (player.isPlaying()) {
            //main.showNotification(R.drawable.ic_baseline_play_arrow_24);
            pause();
            //try {
            //    BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            //} catch (Exception ignored) {}
        } else {
            //main.showNotification(R.drawable.ic_baseline_pause_24);
            resume();
            //try {
            //    BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            //} catch (Exception ignored) {}
        }
        //if (MainActivity.currentFragment.getClass().equals(PagerFragment.class)) newFragment.setPause(player.isPlaying());
    }

    public void pause() {
        //if (main.t != null) {
        //    main.t.stopThread();
        //}
        player.pause();
    }

    public void resume() {
        //try {
        //    BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        //} catch (Exception ignored) {}
        if (MainActivity.currentFragment.getClass().equals(PagerFragment.class)) newFragment.setPause(false);
        player.start();
        main.t.resumeThread();
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
            if (main.t != null) main.t.stopThread();
            player.release();
            player = null;
            main.player = null;
        }
    }

    public boolean isInitialized() {
        return currentPlayingSong != null;
    }

    private void donePlayNext() {
        playNext(false);
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