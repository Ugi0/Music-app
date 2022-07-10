package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {
    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PREV  = "PREVIOUS";
    public static final String ACTION_NEXT  = "NEXT";
    public static final String ACTION_PAUSE  = "PAUSE";

    private MusicPlayer player;

    public NotificationUtils(MusicPlayer player) {
        this.player = player;
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void displayNotification(MainActivity main, String songName) {


        Intent intent = new Intent(main.getBaseContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(main.getBaseContext(), 0, intent, 0);

        Notification notification = null;
        Intent prevIntent = new Intent(main.getApplicationContext(), NotificationReceiver.class)
                .setAction("PREVIOUS");
        Intent pauseIntent = new Intent(main.getApplicationContext(), NotificationReceiver.class)
                .setAction("PAUSE");
        Intent nextIntent = new Intent(main.getApplicationContext(), NotificationReceiver.class)
                .setAction("NEXT");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(main.getBaseContext(), NotificationClass.Channel)
                    .setContentTitle(songName)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setStyle(new Notification.MediaStyle().setMediaSession(player.sessionToken))
                    .addAction(new Notification.Action(R.drawable.ic_baseline_skip_previous_24, ACTION_PREV, PendingIntent.getBroadcast(main.getApplicationContext(), 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                    .addAction(new Notification.Action(R.drawable.ic_baseline_pause_24, ACTION_PAUSE, PendingIntent.getBroadcast(main.getApplicationContext(), 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                    .addAction(new Notification.Action(R.drawable.ic_baseline_skip_next_24, ACTION_NEXT, PendingIntent.getBroadcast(main.getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setContentIntent(contentIntent)
                    .setOnlyAlertOnce(true)
                    .build();
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(main.getBaseContext());
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    }