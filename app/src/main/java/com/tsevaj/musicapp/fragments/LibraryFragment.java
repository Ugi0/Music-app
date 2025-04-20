package com.tsevaj.musicapp.fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.adapters.CustomAdapter;
import com.tsevaj.musicapp.fragments.interfaces.RefreshableFragment;
import com.tsevaj.musicapp.utils.data.MusicItem;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.utils.data.SortValue;
import com.tsevaj.musicapp.utils.enums.MusicListType;
import com.tsevaj.musicapp.utils.files.MusicGetter;

import java.util.List;

public class LibraryFragment extends RefreshableFragment {
    List<MusicItem> songs;

    public LibraryFragment(MainActivity main, List<MusicItem> songs) {
        super(main);
        this.songs = songs;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.songlist_recyclerview);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new CustomAdapter(songs, main));

        makeRefreshable(recyclerView);
        makeMenubar(view);

        return view;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

    @Override
    protected void handleRefresh() {
        //view.requestLayout();
    }

    @Override
    public SortValue getListType() {
        return new SortValue(MusicListType.ALL);
    }
}

