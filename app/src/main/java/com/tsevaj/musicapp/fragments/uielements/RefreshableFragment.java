package com.tsevaj.musicapp.fragments.uielements;

import static com.tsevaj.musicapp.utils.files.MusicGetter.getAuthors;
import static com.tsevaj.musicapp.utils.files.MusicGetter.loadList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.utils.data.MusicItem;
import com.tsevaj.musicapp.utils.data.SortValue;
import com.tsevaj.musicapp.utils.files.MusicGetter;

import lombok.Getter;

public abstract class RefreshableFragment extends MusicFragment implements HasControlBar {
    @Getter
    protected RecyclerView recyclerView;
    protected VisibleMenuBarImpl menuBar;

    protected abstract void handleRefresh();
    public abstract SortValue getListType();
    protected void makeRefreshable(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) recyclerView.getParent();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            handleReloadList();
            handleRefresh();
            swipeRefreshLayout.setRefreshing(false);
        });

        MusicGetter.getAuthors(recyclerView.getAdapter(), main);
    }
    protected void makeMenubar(View ll) {
        menuBar = new VisibleMenuBarImpl(ll.findViewById(R.id.music_bar), main, this);
    }
    private void ensureMenuBar() {
        if (menuBar == null) {
            makeMenubar(view);
        }
    }
    private void handleReloadList() {
        MainActivity.wholeSongList = loadList(recyclerView.getContext());
        getAuthors(recyclerView.getAdapter(), (Activity) recyclerView.getContext());
    }

    @Override
    public void handlePause(boolean isPlaying) {
        ensureMenuBar();
        if (isPlaying) {
            menuBar.showPlayButton();
        } else {
            menuBar.showPauseButton();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void handleSongChange(MusicItem song) {
        ensureMenuBar();
        menuBar.handleSongChange(song);
    }
}
