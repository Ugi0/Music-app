package com.tsevaj.musicapp.fragments;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tsevaj.musicapp.utils.FunctionClass;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.R;

public class LibraryFragment extends Fragment {
    private RecyclerView recyclerView;
    private final MusicPlayer player;
    private String filter = "";
    private final String nameFilter;

    public LibraryFragment(MusicPlayer player, String filter, String nameFilter) {
        this.player = player;
        if (!filter.isEmpty()) this.filter = filter;
        this.nameFilter = nameFilter;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ll = inflater.inflate(R.layout.songlist_recyclerview, container, false);
        recyclerView = ll.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MainActivity.setBackground(ll, getResources());
        player.main.setDrawer();

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) recyclerView.getParent();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            MainActivity.wholeSongList = null;
            FunctionClass.getMusicAndSet(recyclerView, requireActivity(), player, requireActivity(), filter, nameFilter);
            swipeRefreshLayout.setRefreshing(false);
        });

        FunctionClass.getMusicAndSet(recyclerView, requireActivity(), player, requireActivity(), this.filter, nameFilter);
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
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

}

