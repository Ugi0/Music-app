package com.tsevaj.musicapp.fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tsevaj.musicapp.fragments.interfaces.MusicFragment;
import com.tsevaj.musicapp.fragments.interfaces.RefreshableFragment;
import com.tsevaj.musicapp.utils.files.MusicGetter;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.R;

public class PlaylistsFragment extends MusicFragment {

    public PlaylistsFragment(MainActivity main) {
        super(main);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, R.layout.songlist_recyclerview);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        //MusicGetter.playlistView(recyclerView, getActivity(), player, requireActivity(), this);
        return view;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }
}