package com.tsevaj.musicapp.fragments;

import static com.tsevaj.musicapp.fragments.uielements.MusicFragment.setBackground;

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
import com.tsevaj.musicapp.fragments.uielements.DetailedFragment;

public class DetailedsongFragment extends DetailedFragment {
    private MainActivity main;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.song_detailed_view, container, false);
        main = (MainActivity) getActivity();
        setBackground(view, getResources());

        doLayout();

        return view;
    }

    @Nullable
    @Override
    public View getView() {
        return view;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

    public void showLyricLines() {

    }

    public void showNoLyrics() {

    }

}
