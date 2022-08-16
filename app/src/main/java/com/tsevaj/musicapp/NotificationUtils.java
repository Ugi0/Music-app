package com.tsevaj.musicapp;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationUtils {
    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PREV  = "PREVIOUS";
    public static final String ACTION_NEXT  = "NEXT";
    public static final String ACTION_PAUSE  = "PAUSE";
    public final static String OPEN_NOTIFICATION = "notification_open";

    @SuppressLint({"UnspecifiedImmutableFlag", "NewApi"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void displayNotification(MainActivity main, String songName, int playPauseButton) {

        Intent intent = new Intent(main, MainActivity.class);
        intent.setAction(OPEN_NOTIFICATION);
        PendingIntent contentIntent = PendingIntent.getActivity(main.getBaseContext(), 0, intent, 0);

        Intent prevIntent = new Intent(main, NotificationReceiver.class)
                .setAction("PREVIOUS");
        Intent pauseIntent = new Intent(main, NotificationReceiver.class)
                .setAction("PAUSE");
        Intent nextIntent = new Intent(main, NotificationReceiver.class)
                .setAction("NEXT");

        NotificationCompat.Builder notification = new NotificationCompat.Builder(main, NotificationClass.Channel)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(main.getResources(), R.drawable.background))
                .setColor(0xae27ff)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                .addAction(new NotificationCompat.Action(R.drawable.ic_baseline_skip_previous_24, ACTION_PREV, PendingIntent.getBroadcast(main, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                .addAction(new NotificationCompat.Action(playPauseButton, ACTION_PAUSE, PendingIntent.getBroadcast(main, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                .addAction(new NotificationCompat.Action(R.drawable.ic_baseline_skip_next_24, ACTION_NEXT, PendingIntent.getBroadcast(main, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setChannelId("Control Notification")
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false);
        notification.setContentTitle(songName);
        notification.setContentText(MusicPlayer.songArtist);
        NotificationManager notificationManager = (NotificationManager) main.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

}