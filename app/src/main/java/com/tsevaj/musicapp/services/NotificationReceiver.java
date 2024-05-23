package com.tsevaj.musicapp.services;

import static com.tsevaj.musicapp.services.NotificationClass.ACTION_NEXT;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PAUSE;
import static com.tsevaj.musicapp.services.NotificationClass.ACTION_PREV;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, NotificationService.class);
        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PREV: {
                    intent1.setAction(ACTION_PREV);
                    break;
                }
                case ACTION_PAUSE: {
                    intent1.setAction(ACTION_PAUSE);
                    break;
                }
                case ACTION_NEXT: {
                    intent1.setAction(ACTION_NEXT);
                    break;
                }
            }
            context.startService(intent1);
        }
    }
}
