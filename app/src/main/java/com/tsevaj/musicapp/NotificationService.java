package com.tsevaj.musicapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class NotificationService extends Service {
    private IBinder mBinder = new myBinder();
    private NotificationController notificationController;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class myBinder extends Binder {
        NotificationService getService() { return NotificationService.this; }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case "PREVIOUS": {
                    if (notificationController != null) {
                        notificationController.playPrev(true);
                    }
                    break;
                }
                case "PAUSE": {
                    if (notificationController != null) {
                        notificationController.playPause();
                    }
                    break;
                }
                case "NEXT": {
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
