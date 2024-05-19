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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.R;

public class FavoritesFragment extends Fragment {
    private final MusicPlayer player;
    private final String nameFilter;
    private final MainActivity main;


    public FavoritesFragment(MusicPlayer player, String nameFilter, MainActivity main) {
        this.nameFilter = nameFilter;
        this.player = player;
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ll = inflater.inflate(R.layout.songlist_recyclerview, container, false);
        RecyclerView recyclerView = ll.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        main.setBackground(ll, getResources());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) recyclerView.getParent();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            MainActivity.wholeSongList = null;
            player.main.PrevAndNextSongs.setList(null);
            player.main.PrevAndNextSongs.getMusicAndSet(recyclerView, requireActivity(), player, requireActivity(), "FAVORITES", nameFilter);
            swipeRefreshLayout.setRefreshing(false);
        });

        player.main.PrevAndNextSongs.getMusicAndSet(recyclerView, requireActivity(), player, requireActivity(), "FAVORITES", nameFilter);
        MainActivity.currentFragment = this;
        main.setDrawer();

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
