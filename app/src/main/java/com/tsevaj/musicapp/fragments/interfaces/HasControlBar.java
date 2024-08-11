package com.tsevaj.musicapp.fragments.interfaces;

import com.tsevaj.musicapp.utils.MusicItem;

public interface HasControlBar {
    public abstract void handlePause();
    public abstract void handleResume();
    public abstract void handleSongChange(MusicItem song);
}
