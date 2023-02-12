package com.tsevaj.musicapp.services;

public interface NotificationController {
    void playNext(Boolean force);
    void playPrev(Boolean force);
    void playPause();
}
