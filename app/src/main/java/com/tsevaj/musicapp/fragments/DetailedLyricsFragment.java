package com.tsevaj.musicapp.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.adapters.PagerAdapter;
import com.tsevaj.musicapp.utils.MusicPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DetailedLyricsFragment extends Fragment {
    View ll;
    PagerAdapter parent;

    public static LyricsThread t;

    public DetailedLyricsFragment(PagerAdapter parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ll = inflater.inflate(R.layout.song_detailed_view_lyrics, container, false);
        parent.main.setBackground(ll, getResources());

        t = new LyricsThread(parent.main.player, this);
        t.start();

        parent.initWindowElements(ll);
        return ll;
    }

    public static class LyricItem {
        public String lyric;
        public boolean current;
        public int displayTime;

        public LyricItem(String lyric, boolean current, int displayTime) {
            this.lyric = lyric;
            this.current = current;
            this.displayTime = displayTime;
        }

        public void setCurrent(boolean value) {
            this.current = value;
        }
    }

    public static class LyricsThread extends Thread {
        Thread t;
        MusicPlayer player;
        DetailedLyricsFragment parent;

        public LyricsThread(MusicPlayer player, DetailedLyricsFragment parent) {
            this.player = player;
            this.parent = parent;
        }

        public void run() {
            File dir = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC+"/Musiclrc");
            File[] files = dir.listFiles();
            List<LyricItem> lyrics = null;

            assert files != null;
            List<String> lines = null;

            Optional<File> currentSongLyricsOptional = Arrays.stream(files)
                    .parallel()
                    .filter(file -> file.getAbsolutePath().contains(String.format("%s - %s.lrc",MusicPlayer.currentPlayingSong.getArtist(), MusicPlayer.currentPlayingSong.getTitle())))
                    .findAny();
            if (currentSongLyricsOptional.isPresent()) {
                File currentSongLyrics = currentSongLyricsOptional.get();
                try {
                    lines = Files.readAllLines(Paths.get(currentSongLyrics.getAbsolutePath()), StandardCharsets.UTF_8);
                } catch (Exception ignored) {}
                if (lines != null) {
                    Pattern pattern = Pattern.compile("\\[(.*?)].*");
                    lines = lines.stream().filter(line -> line.matches("\\[[0-9][0-9]:[0-9][0-9].[0-9][0-9]].+")).collect(Collectors.toList());
                    lyrics = lines.stream().map(line -> {
                            Matcher matcher = pattern.matcher(line);
                            assert matcher.matches();
                            String timePart = matcher.group(1);
                            return new LyricItem(
                                    line.split("]")[1],
                                    false,
                                    Integer.parseInt(timePart.split(":")[0])*60+Integer.parseInt(timePart.split("\\.")[0].split(":")[1])
                            );
                        }
                    ).collect(Collectors.toList());
                }
            }
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            List<LyricItem> finalLyrics = lyrics;
            if (finalLyrics != null) {
                executor.scheduleAtFixedRate(() -> {
                            try {
                                player.main.runOnUiThread(() -> parent.parent.setLyrics(getDisplayLyrics(finalLyrics, player.getCurrentPosition() / 1000.0)));
                            } catch (Exception e) {
                                Log.d("test", String.valueOf(e));
                            }
                            // player.getCurrentPosition() / 1000.0;
                            //Update and change the lyrics
                        }
                        , 0, 100, TimeUnit.MILLISECONDS);
            }
        }
    }

    public static List<LyricItem> getDisplayLyrics(List<LyricItem> lyrics, double curTime) {
        boolean found = false;
        List<LyricItem> returnValue = null;
        for (int i = 0; i < lyrics.size(); i++) {
            lyrics.get(i).current = false;
            if (lyrics.get(i).displayTime > curTime && !found) {
                found = true;
                if (i >= 1) { {
                    lyrics.get(i-1).current = true;
                }}
                if (i > lyrics.size()-6) {
                    returnValue = lyrics.subList(lyrics.size()-7, lyrics.size());
                } else if (i > 2) {
                    returnValue = lyrics.subList(i-2, i+6);
                }
            }
            if (i == lyrics.size()-1 && !found) {
                lyrics.get(i).current = true;
                returnValue = lyrics.subList(lyrics.size()-7, lyrics.size());
            }
        }
        if (returnValue == null) return lyrics.subList(0,7);
        return returnValue;
    }
}
