package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.media.session.MediaButtonReceiver;

//TODO Listen to calls from other apps
public class MyController extends BroadcastReceiver {
    MusicPlayer player;
    Context c;
    MediaSessionCompat ms;

    @Override
    public void onReceive(Context context, Intent intent) {
    }

    public MyController(MusicPlayer player, Context c) {
        this.c = c;
        this.player = player;
        this.ms = new MediaSessionCompat(c, c.getPackageName());
        this.player.sessionToken = ms.getSessionToken();
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(c, MediaButtonReceiver.class);
        PendingIntent mbrIntent = PendingIntent.getBroadcast(c, 0, mediaButtonIntent, 0);
        ms.setMediaButtonReceiver(mbrIntent);
        ms.setCallback(new MediaSessionCompat.Callback() {
            @SuppressLint("NewApi")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                if (player.songDone) return super.onMediaButtonEvent(mediaButtonIntent);
                KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        player.playPause();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        player.playNext(true);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        player.playPrev(true);
                        break;
                }
                return true;
            }
        });
    }
}
