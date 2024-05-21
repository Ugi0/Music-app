package com.tsevaj.musicapp.services;

import static com.tsevaj.musicapp.services.NotificationClass.ACTION_NEXT;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PAUSE;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PREV;
import static com.tsevaj.musicapp.services.NotificationUtils.OPEN_NOTIFICATION;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.utils.MusicPlayer;

public class NotificationService extends Service {
    private final IBinder mBinder = new myBinder();
    private NotificationController notificationController;
    private boolean playing;

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

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onCreate() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(OPEN_NOTIFICATION);
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction("PREVIOUS");
        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction("PAUSE");
        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction("NEXT");
        int playPauseButton;
        if (playing) {
            playPauseButton = R.drawable.ic_baseline_pause_24;
        }
        else {
            playPauseButton = R.drawable.ic_baseline_play_arrow_24;
        }
        @SuppressLint("InlinedApi") NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NotificationClass.Channel)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.background))
                .setColor(0xae27ff)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                
                .addAction(new NotificationCompat.Action(R.drawable.ic_baseline_skip_previous_24, ACTION_PREV, PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_MUTABLE)))
                .addAction(new NotificationCompat.Action(playPauseButton, ACTION_PAUSE, PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_MUTABLE)))
                .addAction(new NotificationCompat.Action(R.drawable.ic_baseline_skip_next_24, ACTION_NEXT, PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_MUTABLE)))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setChannelId("Control Notification")
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false);
        notification.setContentTitle(MusicPlayer.currentPlayingSong.getHead());
        notification.setContentText(MusicPlayer.currentPlayingSong.getArtist());
        startForeground(1, notification.build());
        super.onCreate();
    }

    public void setCallBack(NotificationController notificationController) {
        this.notificationController = notificationController;
    }
}
