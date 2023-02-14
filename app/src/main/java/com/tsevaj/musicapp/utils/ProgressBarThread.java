package com.tsevaj.musicapp.utils;

import android.util.Log;
import android.widget.SeekBar;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.CircularSeekBar;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProgressBarThread extends Thread {
    public Thread t;
    public MainActivity main;
    SeekBar seekBar;
    CircularSeekBar circularSeekBar;
    double progressBarValue;
    private boolean stopped = false;

    public ProgressBarThread(SeekBar progressbar, MainActivity main) {
        this.main = main;
        this.seekBar = progressbar;
        this.start();
    }

    public ProgressBarThread(CircularSeekBar progressbar, MainActivity main) {
        this.main = main;
        this.circularSeekBar = progressbar;
        this.start();
    }

    public void run() {
        progressBarValue = 0;
        if (seekBar != null) {
            t = new Thread(() -> {
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleAtFixedRate(() -> {
                    if (!stopped) {
                        try {
                            BigDecimal playedSoFar = new BigDecimal(main.player.getCurrentPosition() + "000");
                            progressBarValue = (playedSoFar.divide(main.player.currentDuration, 2, RoundingMode.HALF_UP).doubleValue());}
                        catch (Exception exception) {progressBarValue = 0;}

                        try { seekBar.setProgress((int) progressBarValue); }
                        catch (Exception ignored) {}
                    }
                }
                , 0, 100, TimeUnit.MILLISECONDS);
                });}
        else {
            t = new Thread(() -> {
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleAtFixedRate(() -> {
                    if (!stopped) {
                        try {
                            BigDecimal playedSoFar = new BigDecimal(main.player.getCurrentPosition() + "0000");
                            progressBarValue = (playedSoFar.divide(main.player.currentDuration, 2, RoundingMode.HALF_UP).doubleValue());}
                        catch (Exception exception) {progressBarValue = 0;}

                        try { circularSeekBar.setProgress((int) progressBarValue); }
                        catch (Exception ignored) {}
                    }
                }, 0, 100, TimeUnit.MILLISECONDS);
            });}
        t.start();
    }
    public void stopThread() {
        stopped = true;
    }
    public void resumeThread() {
        stopped = false;
    }
}
