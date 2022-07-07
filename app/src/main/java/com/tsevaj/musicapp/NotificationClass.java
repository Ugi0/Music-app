package com.tsevaj.musicapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationClass extends Application {
    public static final String Channel = "Control Notification";
    public static final String ACTION_PREV  = "PREVIOUS";
    public static final String ACTION_NEXT  = "NEXT";
    public static final String ACTION_PAUSE  = "PAUSE";

    @Override
    public void onCreate() {
        super.onCreate();
        CreateNotificationChannel();
    }

    private void CreateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Channel, "Notification controls", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription("Will allow app to set a notification which allows you to control the music while app is in the background.");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
