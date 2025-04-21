package com.tsevaj.musicapp.adapters;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.DetailedLyricsFragment;
import com.tsevaj.musicapp.fragments.DetailedsongFragment;
import com.tsevaj.musicapp.fragments.uielements.DetailedFragment;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.interfaces.HasProgressBar;
import com.tsevaj.musicapp.utils.data.MusicItem;
import com.tsevaj.musicapp.utils.MusicPlayer;

import java.util.List;

public class PagerAdapter extends FragmentStateAdapter implements HasControlBar, HasProgressBar {
    MusicPlayer player;
    public MainActivity main;
    public DetailedFragment fragment;

    public PagerAdapter(MusicPlayer player, MainActivity main) {
        super(main);
        this.player = player;
        this.main = main;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            fragment = new DetailedsongFragment();
        } else {
            fragment = new DetailedLyricsFragment();
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void changeSong(MusicItem song) {
        View parentView = fragment.getView();
        assert parentView != null;
        TextView songNameView = parentView.findViewById(R.id.song_information_name);
        TextView songDescView = parentView.findViewById(R.id.song_information_author);
        TextView songLocView = parentView.findViewById(R.id.song_information_location);
        songNameView.setText(song.getTitle());
        songDescView.setText(song.getDesc());
        songLocView.setText(song.getLocationFolder());
    }

    public void setLyrics(List<DetailedLyricsFragment.LyricItem> lyrics) {
        View parentView = fragment.getView();
        assert parentView != null;
        LinearLayout linearLayout = parentView.findViewById(R.id.lyrics_container);
        for (int i = 0; i < 7; i++) {
            TextView textView = (TextView) linearLayout.getChildAt(i);
            textView.setText(lyrics.get(i).lyric);
            textView.setTextColor(lyrics.get(i).current ? Color.parseColor("#FF0000") : Color.parseColor("#FFFFFF"));
        }
    }

    public void setPauseButton(boolean value) {
        View parentView = fragment.getView();
        assert parentView != null;
        ImageButton BtnPause = parentView.findViewById(R.id.BtnPause);
        BtnPause.setBackgroundResource(value ? R.drawable.ic_baseline_pause_24 : R.drawable.ic_baseline_play_arrow_24);
    }

    //TODO reduce overhead by making this method only change the necessary things

    public void showLyricLines() {
        fragment.showLyricLines();
    }

    public void showNoLyrics() {
        fragment.showNoLyrics();
    }

    @Override
    public void handlePause(boolean isPlaying) {
        fragment.handlePause(isPlaying);
    }

    @Override
    public void handleSongChange(MusicItem song) {
        fragment.handleSongChange(song);
    }

    @Override
    public void updateProgress() {
        fragment.updateProgress();
    }
}
