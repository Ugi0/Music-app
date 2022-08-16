package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.media.session.MediaButtonReceiver;

public class MyController extends BroadcastReceiver {
    MusicPlayer player;
    Context c;
    MediaSessionCompat ms;
    long lastButtonPressTime;

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
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent mbrIntent = PendingIntent.getBroadcast(c, 0, mediaButtonIntent, 0);
        ms.setMediaButtonReceiver(mbrIntent);
        ms.setCallback(new MediaSessionCompat.Callback() {
            @SuppressLint("NewApi")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                if (player.songDone) return true;
                if (System.currentTimeMillis() - lastButtonPressTime < 300) return true;
                lastButtonPressTime = System.currentTimeMillis();
                KeyEvent event = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                Intent intent1 = new Intent(c, NotificationService.class);
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        intent1.setAction("PAUSE");
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        intent1.setAction("NEXT");
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        intent1.setAction("PREVIOUS");
                        break;
                }
                c.startService(intent1);
                return true;
            }
        });
    }
}
