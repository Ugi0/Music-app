package com.tsevaj.musicapp.services;

import static com.tsevaj.musicapp.services.NotificationClass.ACTION_NEXT;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PAUSE;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PREV;
import static com.tsevaj.musicapp.services.NotificationClass.NOTIFICATION_ID;
import static com.tsevaj.musicapp.services.NotificationClass.NOTIFICATION_TAG;
import static com.tsevaj.musicapp.services.NotificationClass.OPEN_NOTIFICATION;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.R;

public class NotificationUtils {

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void displayNotification(MainActivity main, String songName, int playPauseButton) {

        Intent intent = new Intent(main, MainActivity.class);
        intent.setAction(OPEN_NOTIFICATION);
        PendingIntent contentIntent = PendingIntent.getActivity(main.getBaseContext(), 0, intent, PendingIntent.FLAG_MUTABLE);

        Intent prevIntent = new Intent(main, NotificationReceiver.class)
                .setAction("PREVIOUS");
        Intent pauseIntent = new Intent(main, NotificationReceiver.class)
                .setAction("PAUSE");
        Intent nextIntent = new Intent(main, NotificationReceiver.class)
                .setAction("NEXT");

        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(main.getApplicationContext(), 0, prevIntent, PendingIntent.FLAG_MUTABLE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(main.getApplicationContext(), 0, pauseIntent, PendingIntent.FLAG_MUTABLE);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(main.getApplicationContext(), 0, nextIntent, PendingIntent.FLAG_MUTABLE);

        Icon prevIcon = Icon.createWithResource(main, R.drawable.ic_baseline_skip_previous_24);
        Icon pauseIcon = Icon.createWithResource(main, playPauseButton);
        Icon nextIcon = Icon.createWithResource(main, R.drawable.ic_baseline_skip_next_24);

        Notification.Action prevAction = new Notification.Action.Builder(prevIcon, ACTION_PREV, prevPendingIntent).build();
        Notification.Action pauseAction = new Notification.Action.Builder(pauseIcon, ACTION_PAUSE, pausePendingIntent).build();
        Notification.Action nextAction = new Notification.Action.Builder(nextIcon, ACTION_NEXT, nextPendingIntent).build();

        Notification.Builder notification = new Notification.Builder(main, NotificationClass.Channel)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(main.getResources(), R.drawable.background))
                .setColor(0xae27ff)
                .setStyle(new Notification.MediaStyle().setMediaSession(main.player.sessionToken).setShowActionsInCompactView(0,1,2))
                .addAction(prevAction)
                .addAction(pauseAction)
                .addAction(nextAction)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId("Control Notification")
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setAutoCancel(false);
        notification.setContentTitle(songName);
        notification.setContentText(MusicPlayer.currentPlayingSong.getArtist());
        NotificationManager notificationManager = (NotificationManager) main.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }
    public void deleteNotification(MainActivity main) {
        NotificationManager notificationManager = (NotificationManager) main.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}