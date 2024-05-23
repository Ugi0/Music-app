package com.tsevaj.musicapp.services;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class NotificationClass extends Application {
    public static final String Channel = "Control Notification";
    public static final String NOTIFICATION_TAG = "Notification";
    public static final String ACTION_PREV  = "PREVIOUS";
    public static final String ACTION_NEXT  = "NEXT";
    public static final String ACTION_PAUSE  = "PAUSE";

    public static final int NOTIFICATION_ID = 1;

    public final static String OPEN_NOTIFICATION = "notification_open";

    @Override
    public void onCreate() {
        super.onCreate();
        CreateNotificationChannel();
    }

    private void CreateNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(Channel, "Notification controls", NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setDescription("Will allow app to set a notification which allows you to control the music while app is in the background.");
        notificationChannel.enableVibration(false);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
