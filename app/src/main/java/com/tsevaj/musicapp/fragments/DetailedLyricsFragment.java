package com.tsevaj.musicapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.adapters.PagerAdapter;

public class DetailedLyricsFragment extends Fragment {
    View ll;
    PagerAdapter parent;

    public DetailedLyricsFragment(PagerAdapter parent) {
        this.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ll = inflater.inflate(R.layout.song_detailed_view_lyrics, container, false);
        parent.main.setBackground(ll, getResources());

        parent.initWindowElements(ll);
        return ll;
    }
}
