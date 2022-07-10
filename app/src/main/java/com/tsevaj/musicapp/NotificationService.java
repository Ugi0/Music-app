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
        Log.d("test", "onBind: ");
        return mBinder;
    }

    public class myBinder extends Binder {
        NotificationService getService() { return NotificationService.this; }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction() != null) {
            Log.d("test", "onStartCommand: ");
            switch (intent.getAction()) {
                case "PREVIOUS": {
                    if (notificationController != null) {
                        notificationController.playPrev(true);
                    }
                }
                case "PAUSE": {
                    if (notificationController != null) {
                        Log.d("test", "onStartCommand: ");
                        notificationController.playPause();
                    }
                }
                case "NEXT": {
                    if (notificationController != null) {
                        notificationController.playNext(true);
                    }
                }

            }
        }
        return START_STICKY;
    }

    public void setCallBack(NotificationController notificationController) {
        this.notificationController = notificationController;
    }
}
