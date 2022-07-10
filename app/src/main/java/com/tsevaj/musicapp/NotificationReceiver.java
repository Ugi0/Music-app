package com.tsevaj.musicapp;

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
                case "PREVIOUS": {
                    intent1.putExtra("myActionName", intent.getAction());
                    context.startService(intent1);
                    break;
                }
                case "PAUSE": {
                    intent1.putExtra("myActionName", intent.getAction());
                    context.startService(intent1);
                    break;
                }
                case "NEXT": {
                    intent1.putExtra("myActionName", intent.getAction());
                    context.startService(intent1);
                    break;
                }
            }
        }
    }
}
