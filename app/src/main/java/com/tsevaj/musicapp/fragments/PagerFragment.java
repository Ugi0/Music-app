package com.tsevaj.musicapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.adapters.PagerAdapter;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.interfaces.MusicFragment;
import com.tsevaj.musicapp.utils.data.MusicItem;
import com.tsevaj.musicapp.utils.MusicPlayer;


public class PagerFragment extends MusicFragment implements HasControlBar {
    MusicPlayer player;

    PagerAdapter pagerAdapter;
    ViewPager2 viewPager;

    public PagerFragment(MainActivity main) {
        super(main);
        this.player = main.getPlayer();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.fragment_pager);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pagerAdapter = new PagerAdapter(requireActivity(), player, main);
        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
    }

    public void changeSong(MusicItem song) {pagerAdapter.changeSong(song);}

    public void setPause(boolean value) {pagerAdapter.setPauseButton(value);}

    @Override
    public void handlePause() {
        pagerAdapter.handlePause();
    }

    @Override
    public void handleResume() {
        pagerAdapter.handleResume();
    }

    @Override
    public void handleSongChange(MusicItem song) {
        pagerAdapter.handleSongChange(song);
    }
}
