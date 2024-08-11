package com.tsevaj.musicapp.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.adapters.PagerAdapter;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.interfaces.MusicFragment;
import com.tsevaj.musicapp.utils.CircularSeekBar;
import com.tsevaj.musicapp.utils.MusicItem;
import com.tsevaj.musicapp.utils.MusicPlayer;

public class DetailedsongFragment extends MusicFragment implements HasControlBar {
    View ll;
    //PagerAdapter parent;

    public DetailedsongFragment(MainActivity main) {
        super(main);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ll = inflater.inflate(R.layout.song_detailed_view, container, false);

        main.setBackground(ll, getResources());
        MainActivity.currentFragment = this;

        parent.initWindowElements(ll);

        return ll;
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

    public static class CircleSeekBarListener implements CircularSeekBar.OnCircularSeekBarChangeListener {
        private final CircularSeekBar progressBar;
        private final ImageButton BtnPause;
        private final MusicPlayer player;

        public CircleSeekBarListener(MusicPlayer player, CircularSeekBar progressbar, ImageButton BtnPause) {
            this.player = player;
            this.progressBar = progressbar;
            this.BtnPause = BtnPause;
        }
        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
            if (fromUser) {
                player.seekTo(progress);
                progressBar.setProgress(progress);
                if (!player.isPlaying()) {
                    BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                }
            }
        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {
            player.pause();
            player.main.t.stopThread();
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {
            player.resume();
            main.t.resumeThread();
        }
    }

}
