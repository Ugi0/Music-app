package com.tsevaj.musicapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {
    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PREV  = "PREVIOUS";
    public static final String ACTION_NEXT  = "NEXT";
    public static final String ACTION_PAUSE  = "PAUSE";

    private MusicPlayer player;

    public NotificationUtils() {
    }

    public NotificationUtils(MusicPlayer player) {
        this.player = player;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void displayNotification(MainActivity main, String songName) {


        Intent intent = new Intent(main.getBaseContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(main.getBaseContext(), 0, intent, 0);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(main.getBaseContext(), NotificationClass.Channel)
                    .setContentTitle(songName)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setStyle(new Notification.DecoratedMediaCustomViewStyle())
                    .addAction(new Notification.Action(R.drawable.ic_baseline_skip_previous_24, ACTION_PREV, PendingIntent.getService(main.getBaseContext(), 0, new Intent(main.getBaseContext() , NotificationActionService.class ).setAction(NotificationClass.ACTION_PREV), 0)))
                    .addAction(new Notification.Action(R.drawable.ic_baseline_pause_24, ACTION_PAUSE, PendingIntent.getService(main.getBaseContext(), 0, new Intent(main.getBaseContext() , NotificationActionService.class ).setAction(NotificationClass.ACTION_PAUSE), 0)))
                    .addAction(new Notification.Action(R.drawable.ic_baseline_skip_next_24, ACTION_NEXT, PendingIntent.getService(main.getBaseContext(), 0, new Intent(main.getBaseContext() , NotificationActionService.class ).setAction(NotificationClass.ACTION_NEXT), 0)))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(contentIntent)
                    .build();
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(main.getBaseContext());
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            //TODO FATAL EXCEPTION: IntentService[NotificationActionService]
            Intent intent1 = new Intent(getBaseContext(), MusicPlayer.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getBaseContext().startActivity(intent1);
            }
        }
    }