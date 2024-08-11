package com.tsevaj.musicapp.utils;

import com.tsevaj.musicapp.uielements.VisibleMenuBarImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProgressBarThread extends Thread {
    private Thread t;
    private VisibleMenuBarImpl menuBar;
    //SeekBar seekBar;
    //CircularSeekBar circularSeekBar;
    double progressBarValue;
    private boolean stopped = false;

    public ProgressBarThread(VisibleMenuBarImpl menuBar) {
        this.menuBar = menuBar;
        //this.seekBar = progressbar;
        this.start();
    }

    /*public ProgressBarThread(CircularSeekBar progressbar, VisibleMenuBarImpl menuBar) {
        this.menuBar = menuBar;
        this.circularSeekBar = progressbar;
        this.start();
    }*/

    public void run() {
        progressBarValue = 0;
        //if (seekBar != null) {
        t = new Thread(() -> {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(() -> {
                if (!stopped) {
                    menuBar.updateProgress();
                    /*try {
                        BigDecimal playedSoFar = new BigDecimal(
                                String.format("%s0000", main.player.getCurrentPosition()));
                        progressBarValue = (playedSoFar.divide(new BigDecimal(String.valueOf(MusicPlayer.currentPlayingSong.getDuration())), 2, RoundingMode.HALF_UP).doubleValue());}
                    catch (Exception exception) {progressBarValue = 0;}

                    try { seekBar.setProgress((int) progressBarValue); }
                    catch (Exception ignored) {}*/
                }
            }
            , 0, 100, TimeUnit.MILLISECONDS);
            });
        //}
        /*else {
            t = new Thread(() -> {
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                executor.scheduleAtFixedRate(() -> {
                    if (!stopped) {
                        try {
                            BigDecimal playedSoFar = new BigDecimal(String.format("%s0000", main.player.getCurrentPosition()));
                            progressBarValue = (playedSoFar.divide(new BigDecimal(String.valueOf(MusicPlayer.currentPlayingSong.getDuration())), 2, RoundingMode.HALF_UP).doubleValue());}
                        catch (Exception exception) {progressBarValue = 0;}

                        try { circularSeekBar.setProgress((int) progressBarValue); }
                        catch (Exception ignored) {}
                    }
                }, 0, 100, TimeUnit.MILLISECONDS);
            });}*/
        t.start();
    }
    public void stopThread() {
        stopped = true;
    }
    public void resumeThread() {
        stopped = false;
    }
}
