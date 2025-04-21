package com.tsevaj.musicapp.utils;

import com.tsevaj.musicapp.fragments.interfaces.HasProgressBar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Setter;

public class ProgressBarThread {
    private HasProgressBar menuBar;
    ScheduledExecutorService executor;

    public void start(HasProgressBar menuBar) {
        if (executor != null) {
            executor.shutdownNow();
        }
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
