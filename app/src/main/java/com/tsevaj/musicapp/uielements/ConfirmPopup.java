package com.tsevaj.musicapp.uielements;

import android.app.AlertDialog;
import android.content.Context;

public class ConfirmPopup {
    private final AlertDialog.Builder alert;

    public ConfirmPopup(Context c, String title, String message, Runnable callback) {
        alert = new AlertDialog.Builder(c);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton("Yes", (dialog, which) -> {
            callback.run();

            dialog.dismiss();
        });
        alert.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
    }

    public void show() {
        alert.show();
    }
}
