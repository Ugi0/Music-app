package com.tsevaj.musicapp.services;

import static com.tsevaj.musicapp.services.NotificationClass.ACTION_DELETE;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_NEXT;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_NOTIFY;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PAUSE;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PREV;
import static com.tsevaj.musicapp.services.NotificationClass.Channel;
import static com.tsevaj.musicapp.services.NotificationClass.NOTIFICATION_ID;
import static com.tsevaj.musicapp.services.NotificationClass.OPEN_NOTIFICATION;
import static com.tsevaj.musicapp.utils.MusicPlayer.currentPlayingSong;
import static com.tsevaj.musicapp.utils.MusicPlayer.sessionToken;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.utils.MusicPlayer;

public class NotificationService extends Service {
    private final IBinder mBinder = new myBinder();
    private NotificationController notificationController;

    private Notification.Builder builder;
    private Notification.Action[] actions;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.setAction(OPEN_NOTIFICATION);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent1, PendingIntent.FLAG_MUTABLE);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREV);
        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PAUSE);
        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);

        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_MUTABLE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_MUTABLE);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_MUTABLE);
        int pauseButton;
        if (intent.getType() == null) {
            pauseButton = R.drawable.ic_baseline_pause_24;
        } else {
            pauseButton = Integer.parseInt(intent.getType());
        }

        Icon prevIcon = Icon.createWithResource(this, R.drawable.ic_baseline_skip_previous_24);
        Icon pauseIcon = Icon.createWithResource(this, pauseButton);
        Icon nextIcon = Icon.createWithResource(this, R.drawable.ic_baseline_skip_next_24);

        Notification.Action prevAction = new Notification.Action.Builder(prevIcon, ACTION_PREV, prevPendingIntent).build();
        Notification.Action pauseAction = new Notification.Action.Builder(pauseIcon, ACTION_PAUSE, pausePendingIntent).build();
        Notification.Action nextAction = new Notification.Action.Builder(nextIcon, ACTION_NEXT, nextPendingIntent).build();

        actions = new Notification.Action[]{prevAction, pauseAction, nextAction};

        builder = new Notification.Builder(this, NotificationClass.Channel)
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.background))
                .setColor(0xae27ff)
                .setStyle(new Notification.MediaStyle().setMediaSession(sessionToken).setShowActionsInCompactView(0, 1, 2))
                .setActions(actions)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setChannelId(Channel)
                .setContentIntent(contentIntent)
                .setAutoCancel(false);
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
                case ACTION_NOTIFY: {
                    int pauseButton;
                    if (intent.getType() == null) {
                        pauseButton = R.drawable.ic_baseline_pause_24;
                    } else {
                        pauseButton = Integer.parseInt(intent.getType());
                    }
                    Icon pauseIcon = Icon.createWithResource(this, pauseButton);
                    Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PAUSE);
                    PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_MUTABLE);
                    actions[1] = new Notification.Action.Builder(pauseIcon, ACTION_PAUSE, pausePendingIntent).build();
                    builder.setContentTitle(MusicPlayer.currentPlayingSong.getTitle());
                    builder.setContentText(MusicPlayer.currentPlayingSong.getArtist());
                    builder.setDeleteIntent(pausePendingIntent);
                    builder.setActions(actions);
                    Notification notification = builder.build();
                    //NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    //notificationManager.notify(NOTIFICATION_ID, notification);
                    startForeground(NOTIFICATION_ID, notification);
                    break;
                }
                case ACTION_DELETE: {
                    //NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    //notificationManager.cancel(NOTIFICATION_ID);
                    stopForeground(Service.STOP_FOREGROUND_REMOVE);
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
