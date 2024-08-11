package com.tsevaj.musicapp.fragments.interfaces;

import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.MainActivity;

public class MusicFragment extends Fragment {
    protected MainActivity main;

    public MusicFragment(MainActivity main) {
        this.main = main;
    }
}
