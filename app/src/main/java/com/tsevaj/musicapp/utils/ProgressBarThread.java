package com.tsevaj.musicapp.utils;

import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.interfaces.HasProgressBar;
import com.tsevaj.musicapp.uielements.VisibleMenuBarImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProgressBarThread {
    private final HasProgressBar menuBar;
    ScheduledExecutorService executor;
    public ProgressBarThread(HasProgressBar menuBar) {
        this.menuBar = menuBar;
    }

    public void start() {
        if (executor != null) return;
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(menuBar::updateProgress
                , 0, 100, TimeUnit.MILLISECONDS);
    }

    public void pause() {
        executor.shutdownNow();
    }

    public void resume() {
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(menuBar::updateProgress
                , 0, 100, TimeUnit.MILLISECONDS);
    }
}
