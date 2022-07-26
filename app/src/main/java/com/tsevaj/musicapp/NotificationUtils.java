package com.tsevaj.musicapp;

import static android.app.Notification.*;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {
    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PREV  = "PREVIOUS";
    public static final String ACTION_NEXT  = "NEXT";
    public static final String ACTION_PAUSE  = "PAUSE";
    public final static String OPEN_NOTIFICATION = "notification_open";

    private NotificationManager notificationManager;
    private MusicPlayer player;
    private NotificationCompat.Builder notification;

    public NotificationUtils(MusicPlayer player) {
        this.player = player;
    }

    @SuppressLint({"UnspecifiedImmutableFlag", "NewApi"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void displayNotification(MainActivity main, String songName, int playPauseButton) {

        Intent intent = new Intent(main.getApplicationContext(), MainActivity.class);
        intent.setAction(OPEN_NOTIFICATION);
        PendingIntent contentIntent = PendingIntent.getActivity(main.getBaseContext(), 0, intent, 0);

        Intent prevIntent = new Intent(main.getApplicationContext(), NotificationReceiver.class)
                .setAction("PREVIOUS");
        Intent pauseIntent = new Intent(main.getApplicationContext(), NotificationReceiver.class)
                .setAction("PAUSE");
        Intent nextIntent = new Intent(main.getApplicationContext(), NotificationReceiver.class)
                .setAction("NEXT");

        notificationManager = (NotificationManager) main.getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(main.getApplicationContext(), NotificationClass.Channel)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setColor(0xae27ff)
                   .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2))
                    .addAction(new NotificationCompat.Action(R.drawable.ic_baseline_skip_previous_24, ACTION_PREV, PendingIntent.getBroadcast(main.getApplicationContext(), 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                    .addAction(new NotificationCompat.Action(playPauseButton, ACTION_PAUSE, PendingIntent.getBroadcast(main.getApplicationContext(), 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                    .addAction(new NotificationCompat.Action(R.drawable.ic_baseline_skip_next_24, ACTION_NEXT, PendingIntent.getBroadcast(main.getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setProgress(100, 50, false)
                    .setChannelId("Control Notification")
                    .setContentIntent(contentIntent)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false);
        notification.setContentTitle(songName);
        NotificationManager notificationManager = (NotificationManager) main.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

}