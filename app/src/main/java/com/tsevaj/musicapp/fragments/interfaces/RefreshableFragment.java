package com.tsevaj.musicapp.fragments.interfaces;

import static com.tsevaj.musicapp.utils.files.MusicGetter.getAuthors;
import static com.tsevaj.musicapp.utils.files.MusicGetter.loadList;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tsevaj.musicapp.MainActivity;

public abstract class RefreshableFragment extends Fragment {
    private RecyclerView recyclerView;
    protected abstract void handleRefresh();
    protected void makeRefreshable(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) recyclerView.getParent();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            handleReloadList();
            handleRefresh();
        });
    }
    private void handleReloadList() {
        MainActivity.wholeSongList = loadList(recyclerView.getContext());
        getAuthors(recyclerView.getAdapter(), recyclerView.getContext());
    }
    public RecyclerView getRecyclerView() {
        return this.recyclerView;
    }
}
