package com.tsevaj.musicapp;

public interface NotificationController {
    void playNext(Boolean force);
    void playPrev(Boolean force);
    void playPause();
}
