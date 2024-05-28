package com.tsevaj.musicapp.utils;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

import static com.tsevaj.musicapp.services.NotificationClass.ACTION_DELETE;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_NOTIFY;

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
import android.media.session.MediaSession;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

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
import java.util.ArrayList;

public class MusicPlayer implements NotificationController, ServiceConnection {
    private MediaPlayer player;
    public static MediaSession.Token sessionToken;
    public static MusicItem currentPlayingSong;
    public CustomAdapter adapter = null;
    public ArrayList<MusicItem> visibleSongs;
    public RecyclerView recyclerview;
    public View relativeLayout;
    public FragmentManager manager;
    public MainActivity main;
    public NotificationService notificationService;
    public Context c;

    AudioManager audioManager;

    TextView songNameView;
    TextView songDescView;
    ImageButton BtnPrev;
    ImageButton BtnNext;
    ImageButton BtnPause;

    DetailedsongFragment newFragment;

    public MusicPlayer(Context c) {
        this.c = c;
        player = new MediaPlayer();
        player.setOnErrorListener((mediaPlayer, i, i1) -> true);
        player.setWakeMode(c, PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnCompletionListener(mediaPlayer -> donePlayNext());
    }

    public void play(MusicItem mylist) {
        if (currentPlayingSong != null) {
            if (currentPlayingSong.getHash() == (mylist.getHash()) && this.player.isPlaying()) return;
        } else {
            Intent intent = new Intent(c, NotificationService.class);
            c.bindService(intent, this, 0);
            Intent intent1 = new Intent(c, NotificationService.class);
            intent1.setAction("NOTIFY");
            c.startService(intent1);
            requestFocus(c);
        }
        relativeLayout = ((Activity) c).findViewById(R.id.music_bar);

        currentPlayingSong = mylist;
        main.showNotification(R.drawable.ic_baseline_pause_24);
        player.reset();
        try {
            player.setDataSource(mylist.getLocation());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPlayingSong = mylist;
        main.showNotification(R.drawable.ic_baseline_pause_24);

        main.mediaController.updateTrackInformation(mylist.getTitle(), mylist.getArtist());
        prepareButtons();
    }

    public void recreateList(MusicItem song) {
        if (main.PrevAndNextSongs.createdFragment == null) {
            main.PrevAndNextSongs = new PrevNextList(new ArrayList<>(visibleSongs), song, MainActivity.currentFragment, c);
        }
        if (main.PrevAndNextSongs.wholeList) {
            main.PrevAndNextSongs.setList(MainActivity.wholeSongList);
        } else {
            main.PrevAndNextSongs.setList(visibleSongs);
        }
    }

    public void prepareButtons() {
        if (BtnNext != null) {
            BtnNext.setOnClickListener(view -> {
                playNext(true);
                showBar();
            });
        }
        if (BtnPrev != null) {
            BtnPrev.setOnClickListener(view -> {
                playPrev(true);
                showBar();
            });
        }
        if (BtnPause != null) {
            BtnPause.setOnClickListener(view -> playPause());
        }
    }

    public void resumeState(MusicItem song, int SecDuration) {
        play(song);
        player.seekTo(SecDuration);
        this.pause();
        adapter.notifyItemChanged(adapter.getList().indexOf(song));
    }

    public void setNext(MusicItem song) {
        main.songQueue.add(song);
    }

    public void donePlayNext() {
        playNext(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void playNext(Boolean force) {
        if (!player.isPlaying()) try {
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

    @SuppressLint("NotifyDataSetChanged")
    public void playPrev(Boolean force) {
        if (!player.isPlaying()) try {
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
        if (player.isPlaying()) {
            main.showNotification(R.drawable.ic_baseline_play_arrow_24);
            pause();
            try {
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            } catch (Exception ignored) {}
        } else {
            main.showNotification(R.drawable.ic_baseline_pause_24);
            resume();
            try {
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            } catch (Exception ignored) {}
        }
        if (MainActivity.currentFragment.getClass().equals(DetailedsongFragment.class))
            newFragment.initWindowElements();
    }

    public void pause() {
        if (main.t != null) {
            main.t.stopThread();
        }
        player.pause();
    }

    public void resume() {
        try {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        } catch (Exception ignored) {}
        if (MainActivity.currentFragment.getClass().equals(DetailedsongFragment.class))
            newFragment.initWindowElements();
        player.start();
        main.t.resumeThread();
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
        songNameView.setText(currentPlayingSong.getTitle());
        songDescView.setText(currentPlayingSong.getDesc());
        if (!player.isPlaying()) {
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
                    if (!player.isPlaying()) {
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
        Intent intent1 = new Intent(c, NotificationService.class);
        intent1.setAction(ACTION_NOTIFY);
        c.startService(intent1);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Intent intent1 = new Intent(c, NotificationService.class);
        intent1.setAction(ACTION_DELETE);
        c.startService(intent1);
        notificationService = null;
    }

    public void destroy() {
        if (this.player != null) {
            if (main.t != null) main.t.stopThread();
            this.player.release();
            this.player = null;
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

    private final class OnFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {
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