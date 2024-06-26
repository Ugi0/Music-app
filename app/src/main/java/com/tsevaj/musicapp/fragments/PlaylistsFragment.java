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

import com.tsevaj.musicapp.utils.FunctionClass;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.R;

public class PlaylistsFragment extends Fragment {
    private final MusicPlayer player;
    public MainActivity main;

    public PlaylistsFragment(MusicPlayer player, MainActivity main) {
        this.main = main;
        this.player = player;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ll = inflater.inflate(R.layout.songlist_recyclerview, container, false);
        RecyclerView recyclerView = ll.findViewById(R.id.recyclerView);
        player.main.setBackground(ll, getResources());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) recyclerView.getParent();
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefreshLayout.setRefreshing(false));

        main.setDrawer();

        FunctionClass.playlistView(recyclerView, getActivity(), player, requireActivity(), this);
        MainActivity.currentFragment = this;
        return ll;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = requireActivity().getMenuInflater();
        inflater.inflate(R.menu.popup_menu, menu);
    }

    public void changeFragments(Fragment newFragment, boolean addToStack) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        if (addToStack) transaction.addToBackStack(null);
        transaction.commit();
    }
}