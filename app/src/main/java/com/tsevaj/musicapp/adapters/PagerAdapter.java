package com.tsevaj.musicapp.adapters;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.DetailedLyricsFragment;
import com.tsevaj.musicapp.fragments.DetailedsongFragment;
import com.tsevaj.musicapp.utils.AlertPopup;
import com.tsevaj.musicapp.utils.CircularSeekBar;
import com.tsevaj.musicapp.utils.MusicItem;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.ProgressBarThread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class PagerAdapter extends FragmentStateAdapter {
    MusicPlayer player;
    public MainActivity main;

    DetailedsongFragment fragment1;
    DetailedLyricsFragment fragment2;

    public PagerAdapter(@NonNull FragmentActivity fragmentActivity, MusicPlayer player, MainActivity main) {
        super(fragmentActivity);
        this.player = player;
        this.main = main;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            fragment1 = new DetailedsongFragment(this);
            return fragment1;
        } else {
            fragment2 = new DetailedLyricsFragment(this);
            return fragment2;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void changeSong(MusicItem song) {
        if (fragment1 != null) {
            View parentView = fragment1.getView();
            assert parentView != null;
            TextView songNameView = parentView.findViewById(R.id.song_information_name);
            TextView songDescView = parentView.findViewById(R.id.song_information_author);
            TextView songLocView = parentView.findViewById(R.id.song_information_location);
            songNameView.setText(song.getTitle());
            songDescView.setText(song.getDesc());
            songLocView.setText(song.getLocationFolder());
        }
        if (fragment2 != null) {
            View parentView = fragment2.getView();
            assert parentView != null;
            TextView songNameView = parentView.findViewById(R.id.song_information_name);
            TextView songDescView = parentView.findViewById(R.id.song_information_author);
            TextView songLocView = parentView.findViewById(R.id.song_information_location);
            songNameView.setText(song.getTitle());
            songDescView.setText(song.getDesc());
            songLocView.setText(song.getLocationFolder());
        }
    }

    public void setShuffle(boolean value) {
        if (fragment1 != null) {
            View parentView = fragment1.getView();
            assert parentView != null;
            ImageButton shuffle = parentView.findViewById(R.id.detailed_shuffle);
            shuffle.setActivated(value);
        }
        if (fragment2 != null) {
            View parentView = fragment2.getView();
            assert parentView != null;
            ImageButton shuffle = parentView.findViewById(R.id.detailed_shuffle);
            shuffle.setActivated(value);
        }
    }

    public void setReplay(boolean value) {
        if (fragment1 != null) {
            View parentView = fragment1.getView();
            assert parentView != null;
            ImageButton replay = parentView.findViewById(R.id.detailed_replay);
            replay.setActivated(value);
        }
        if (fragment2 != null) {
            View parentView = fragment2.getView();
            assert parentView != null;
            ImageButton replay = parentView.findViewById(R.id.detailed_replay);
            replay.setActivated(value);
        }
    }

    public void setFavorite(boolean value) {
        if (fragment1 != null) {
            View parentView = fragment1.getView();
            assert parentView != null;
            ImageButton favoriteButton = parentView.findViewById(R.id.detailed_add_to_favorites);
            favoriteButton.setActivated(value);
        }
        if (fragment2 != null) {
            View parentView = fragment2.getView();
            assert parentView != null;
            ImageButton favoriteButton = parentView.findViewById(R.id.detailed_add_to_favorites);
            favoriteButton.setActivated(value);
        }
    }

    public void setPauseButton(boolean value) {
        if (fragment1 != null) {
            View parentView = fragment1.getView();
            assert parentView != null;
            ImageButton BtnPause = parentView.findViewById(R.id.BtnPause);
            BtnPause.setBackgroundResource(value ? R.drawable.ic_baseline_pause_24 : R.drawable.ic_baseline_play_arrow_24);
        }
        if (fragment2 != null) {
            View parentView = fragment2.getView();
            assert parentView != null;
            ImageButton BtnPause = parentView.findViewById(R.id.BtnPause);
            BtnPause.setBackgroundResource(value ? R.drawable.ic_baseline_pause_24 : R.drawable.ic_baseline_play_arrow_24);
        }
    }

    public void initWindowElements() {
        try {
            initWindowElements(fragment1.getView());
        } catch (NullPointerException ignored){}
        try {
            initWindowElements(fragment2.getView());
        } catch (NullPointerException ignored){}
    }

    @SuppressLint({"UseCompatTextViewDrawableApis", "NonConstantResourceId"})
    public void initWindowElements(View parentView) {
        if (parentView == null) return;
        int textColor = Color.parseColor(main.getApplicationContext().getSharedPreferences("SAVEDATA", 0).getString("THEME_COLOR", "#FFFFFF"));

        SharedPreferences settings = main.getApplicationContext().getSharedPreferences("SAVEDATA", 0);

        ArrayList<String> favorite = main.getFavorites();

        TextView songNameView = parentView.findViewById(R.id.song_information_name);
        TextView songDescView = parentView.findViewById(R.id.song_information_author);
        TextView songLocView = parentView.findViewById(R.id.song_information_location);
        ImageButton BtnPrev = parentView.findViewById(R.id.BtnPrev);
        ImageButton BtnNext = parentView.findViewById(R.id.BtnNext);
        ImageButton BtnPause = parentView.findViewById(R.id.BtnPause);
        CircularSeekBar progressBar = parentView.findViewById(R.id.progress_bar);
        ImageButton shuffle = parentView.findViewById(R.id.detailed_shuffle);
        ImageButton replay = parentView.findViewById(R.id.detailed_replay);
        ImageButton favoriteButton = parentView.findViewById(R.id.detailed_add_to_favorites);
        ImageButton menuButton = parentView.findViewById(R.id.detailed_menu_button);

        songNameView.setTextColor(textColor);
        songDescView.setTextColor(textColor);
        songLocView.setTextColor(textColor);
        songDescView.setCompoundDrawableTintList(ColorStateList.valueOf(textColor));
        songLocView.setCompoundDrawableTintList(ColorStateList.valueOf(textColor));

        favoriteButton.setActivated(favorite.contains(MusicPlayer.currentPlayingSong.getTitle()));

        songNameView.setText(MusicPlayer.currentPlayingSong.getTitle());
        songDescView.setText(MusicPlayer.currentPlayingSong.getDesc());
        songLocView.setText(MusicPlayer.currentPlayingSong.getLocationFolder());
        if (!player.isPlaying()) {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        } else {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        }
        main.t.resumeThread();
        SharedPreferences.Editor editor = settings.edit();
        switch (settings.getInt("REPLAY_MODE", 0)) {
            case -1: {
                replay.setActivated(true);
                break;
            }
            case 1: {
                replay.setActivated(false);
                replay.setSelected(true);
                break;
            }
        }
        if (settings.getBoolean("SHUFFLE", false)) shuffle.setActivated(true);
        shuffle.setOnClickListener(view -> {
            shuffle.setActivated(!shuffle.isActivated());
            editor.putBoolean("SHUFFLE", shuffle.isActivated());
            editor.apply();
            player.prepareButtons();
            setShuffle(shuffle.isActivated());
        });
        replay.setOnClickListener(view -> {
            if (replay.isActivated()) {
                //Play just one song
                replay.setActivated(false);
                replay.setSelected(true);
                editor.putInt("REPLAY_MODE", 1);
                editor.apply();
            } else {
                //Keep playing songs again
                replay.setActivated(true);
                editor.putInt("REPLAY_MODE", -1);
                editor.apply();
            }
            setReplay(replay.isActivated());
        });
        menuButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(main.getApplicationContext(), view);
            popup.inflate(R.menu.detailed_menu);
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.addtoplaylist: {
                        PopupMenu menu = new PopupMenu(main.getApplicationContext(), view);
                        for (String menuItem : main.getApplicationContext().getSharedPreferences("SAVEDATA", 0).getString("PLAYLISTS", "").split("\n")) {
                            menu.getMenu().add(menuItem);
                        }
                        menu.setOnMenuItemClickListener(item1 -> {
                            SharedPreferences.Editor editor2 = main.getApplicationContext().getSharedPreferences("SAVEDATA", 0).edit();
                            String currentPlaylist = main.getApplicationContext().getSharedPreferences("SAVEDATA", 0).getString("PLAYLIST_" + item1.getTitle(), "");
                            if (Arrays.asList(currentPlaylist.split("\n")).contains(MusicPlayer.currentPlayingSong.getTitle()))
                                return true;
                            if (currentPlaylist.isEmpty())
                                editor2.putString("PLAYLIST_" + item1.getTitle(), MusicPlayer.currentPlayingSong.getTitle());
                            else {
                                editor2.putString("PLAYLIST_" + item1.getTitle(), currentPlaylist + "\n" + MusicPlayer.currentPlayingSong.getTitle());
                            }
                            editor2.apply();
                            return true;
                        });
                        menu.show();
                        break;
                    }
                    case R.id.song_delete: {
                        Runnable callback = () -> {
                            try {
                                Files.delete(Paths.get(MusicPlayer.currentPlayingSong.getLocation()));
                                MainActivity.wholeSongList.remove(MusicPlayer.currentPlayingSong);
                                main.PrevAndNextSongs.removeFromPrev(MusicPlayer.currentPlayingSong);
                                player.playNext(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        };
                        new AlertPopup(main.getApplicationContext(), "Delete", "Are you sure you want to delete?", callback).show();
                        break;
                    }
                }
                return true;
            });
            popup.show();
        });
        favoriteButton.setOnClickListener(view -> {
            if (favoriteButton.isActivated()) {
                main.removeFromFavorites(MusicPlayer.currentPlayingSong.getTitle());
                favoriteButton.setActivated(false);
            } else {
                main.addToFavorites(MusicPlayer.currentPlayingSong.getTitle());
                MusicPlayer.currentPlayingSong.setFavorited(true);
                favoriteButton.setActivated(true);
            }
            setFavorite(favoriteButton.isActivated());
        });
        BtnNext.setOnClickListener(view -> detailed_next(parentView));
        BtnPrev.setOnClickListener(view -> detailed_prev(parentView));
        BtnPause.setOnClickListener(view -> {
            if (player.isPlaying()) {
                player.playPause();
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            } else {
                player.playPause();
                BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            }
            setPauseButton(player.isPlaying());
        });
        if (progressBar != null) {
            progressBar.setOnSeekBarChangeListener(new DetailedsongFragment.CircleSeekBarListener(player, progressBar, BtnPause));
            main.t = new ProgressBarThread(progressBar, main);
        }
    }
    public void detailed_next(View ll) {
        player.playNext(true);
        initWindowElements(ll);
    }
    public void detailed_prev(View ll) {
        player.playPrev(true);
        initWindowElements(ll);
    }
}
