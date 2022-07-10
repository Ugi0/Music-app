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
                    intent1.setAction("PREVIOUS");
                    break;
                }
                case "PAUSE": {
                    intent1.setAction("PAUSE");
                    break;
                }
                case "NEXT": {
                    intent1.setAction("NEXT");
                    break;
                }
            }
            context.startService(intent1);
        }
    }
}
