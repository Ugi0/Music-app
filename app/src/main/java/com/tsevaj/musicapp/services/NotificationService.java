package com.tsevaj.musicapp.services;

import static com.tsevaj.musicapp.services.NotificationClass.ACTION_NEXT;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PAUSE;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PREV;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class NotificationService extends Service {
    private final IBinder mBinder = new myBinder();
    private NotificationController notificationController;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class myBinder extends Binder {
        public NotificationService getService() { return NotificationService.this; }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PREV: {
                    if (notificationController != null) {
                        notificationController.playPrev(true);
                    }
                    break;
                }
                case ACTION_PAUSE: {
                    if (notificationController != null) {
                        notificationController.playPause();
                    }
                    break;
                }
                case ACTION_NEXT: {
                    if (notificationController != null) {
                        notificationController.playNext(true);
                    }
                    break;
                }
            }
        }
        return START_STICKY;
    }

    public void setCallBack(NotificationController notificationController) {
        this.notificationController = notificationController;
    }
}
