package com.tsevaj.musicapp.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.adapters.PagerAdapter;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.interfaces.HasProgressBar;
import com.tsevaj.musicapp.fragments.interfaces.MusicFragment;
import com.tsevaj.musicapp.utils.data.MusicItem;

public class DetailedsongFragment extends MusicFragment implements HasControlBar, HasProgressBar {
    private PagerAdapter adapter;

    public DetailedsongFragment(MainActivity main, PagerAdapter adapter) {
        super(main);
        this.adapter = adapter;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.song_detailed_view);

        adapter.initWindowElements(view);

        return view;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

    @Override
    public void handlePause() {

    }

    @Override
    public void handleResume() {

    }

    @Override
    public void handleSongChange(MusicItem song) {

    }

    @Override
    public void updateProgress() {

    }

}
