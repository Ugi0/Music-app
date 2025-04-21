package com.tsevaj.musicapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.adapters.PagerAdapter;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.uielements.MusicFragment;
import com.tsevaj.musicapp.utils.data.MusicItem;
import com.tsevaj.musicapp.utils.MusicPlayer;


public class PagerFragment extends MusicFragment implements HasControlBar {
    MusicPlayer player;
    PagerAdapter pagerAdapter;
    ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View baseView = super.onCreateView(inflater, container);
        view = inflater.inflate(R.layout.fragment_pager, contentContainer, false);
        contentContainer.addView(view);

        pagerAdapter = new PagerAdapter(player, main);
        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);

        return baseView;
    }

    @Override
    public void handlePause(boolean isPlaying) {
        pagerAdapter.handlePause(isPlaying);
    }

    @Override
    public void handleSongChange(MusicItem song) {
        pagerAdapter.handleSongChange(song);
    }
}
