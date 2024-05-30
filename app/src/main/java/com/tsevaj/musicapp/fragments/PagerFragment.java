package com.tsevaj.musicapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.adapters.PagerAdapter;
import com.tsevaj.musicapp.utils.MusicItem;
import com.tsevaj.musicapp.utils.MusicPlayer;


public class PagerFragment extends Fragment {
    MusicPlayer player;
    MainActivity main;

    PagerAdapter pagerAdapter;
    ViewPager2 viewPager;

    public PagerFragment(MusicPlayer player, MainActivity main) {
        this.player = player;
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainActivity.currentFragment = this;
        return inflater.inflate(R.layout.fragment_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pagerAdapter = new PagerAdapter(requireActivity(), player, main);
        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
    }

    public void changeSong(MusicItem song) {pagerAdapter.changeSong(song);}

    public void setPause(boolean value) {pagerAdapter.setPauseButton(value);}
}
