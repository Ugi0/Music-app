package com.tsevaj.musicapp;

import android.util.Log;
import android.widget.SeekBar;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
            while (progressBarValue < 1000) {
                if (!stopped) {
                    BigDecimal playedSoFar = new BigDecimal(main.player.getCurrentPosition() + "000");
                    progressBarValue = (
                            playedSoFar.divide(main.player.currentDuration, 2, RoundingMode.HALF_UP).doubleValue());
                    seekBar.setProgress((int) progressBarValue);
                }
                try {
                    sleep(100);
                } catch (Exception ignored) {
                }
            }
            });}
        else {
            t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (progressBarValue < 10000) {
                        if (!stopped) {
                            BigDecimal playedSoFar = new BigDecimal(main.player.getCurrentPosition() + "0000");
                            progressBarValue = (
                                    playedSoFar.divide(main.player.currentDuration, 2, RoundingMode.HALF_UP).doubleValue());
                            circularSeekBar.setProgress((int) progressBarValue);
                        }
                        try {
                            sleep(100);
                        } catch (Exception ignored) {
                        }
                    }
                }
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
