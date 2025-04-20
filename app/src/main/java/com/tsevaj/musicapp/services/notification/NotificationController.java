package com.tsevaj.musicapp.services.notification;

public interface NotificationController {
    void handleNextSong(Boolean force);
    void handlePrevSong(Boolean force);
    void handlePause();
}
