package com.tsevaj.musicapp.fragments.interfaces;

import com.tsevaj.musicapp.utils.data.MusicItem;

public interface HasControlBar {
    void handlePause(boolean isPlaying);
    void handleSongChange(MusicItem song);
}
