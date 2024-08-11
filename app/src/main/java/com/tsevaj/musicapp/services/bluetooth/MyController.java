package com.tsevaj.musicapp.services.bluetooth;

import static com.tsevaj.musicapp.services.notification.NotificationClass.ACTION_NEXT;
import static com.tsevaj.musicapp.services.notification.NotificationClass.ACTION_PAUSE;
import static com.tsevaj.musicapp.services.notification.NotificationClass.ACTION_PREV;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.media.session.MediaButtonReceiver;

import com.tsevaj.musicapp.services.notification.NotificationService;
import com.tsevaj.musicapp.utils.MusicPlayer;

public class MyController extends BroadcastReceiver {
    MusicPlayer player;
    Context c;
    MediaSession ms;

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    public MyController(MusicPlayer player, Context c) {
        this.c = c;
        this.player = player;
        this.ms = new MediaSession(c, c.getPackageName());
        MusicPlayer.sessionToken = ms.getSessionToken();
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(c, MediaButtonReceiver.class);
        PendingIntent mbrIntent = PendingIntent.getBroadcast(c, 0, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE);
        ms.setMediaButtonReceiver(mbrIntent);
        ms.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event.getAction() == KeyEvent.ACTION_UP) return true; //Make so only ACTION_DOWN event passes

                Intent intent1 = new Intent(c, NotificationService.class);
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        intent1.setAction(ACTION_PAUSE);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        intent1.setAction(ACTION_NEXT);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        intent1.setAction(ACTION_PREV);
                        break;
                }
                c.startService(intent1);
                return true;
            }
        });
    }

    public void updateTrackInformation(String title, String author) {
        MediaMetadata.Builder metadata = new MediaMetadata.Builder();
        metadata.putString(MediaMetadata.METADATA_KEY_ARTIST, author);
        metadata.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        ms.setMetadata(metadata.build());
        ms.setPlaybackState(new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING, 0, 1f).build());
    }
}
