package com.tsevaj.musicapp;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class LibraryFragment extends Fragment {
    private RecyclerView recyclerView;
    private final MusicPlayer player;
    private String filter = "";
    private final String nameFilter;

    public LibraryFragment(MusicPlayer player, String filter, String nameFilter) {
        this.player = player; if (!filter.isEmpty()) this.filter = filter;
        this.nameFilter = nameFilter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ll = inflater.inflate(R.layout.songlist_recyclerview, container, false);
        recyclerView = (RecyclerView) ll.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ll.setBackground(getContext().getDrawable(R.drawable.background));

        FunctionClass.getMusic(recyclerView, getActivity(), player, getActivity(), this.filter, nameFilter);
        MainActivity.currentFragment = this;

        if (!player.songDone) {
            player.relativeLayout = ll.findViewById(R.id.music_bar);
            player.showBar();
        }

        return ll;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

}

