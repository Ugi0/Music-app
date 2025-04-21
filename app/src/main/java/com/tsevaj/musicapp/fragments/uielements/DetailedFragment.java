package com.tsevaj.musicapp.fragments.uielements;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.interfaces.HasProgressBar;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.SharedPreferencesHandler;
import com.tsevaj.musicapp.utils.data.MusicItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public abstract class DetailedFragment extends Fragment implements HasControlBar, HasProgressBar {
    public View view;

    TextView songNameView ;
    TextView songDescView;
    TextView songLocView;
    CircularSeekBar progressBar;
    ImageButton BtnPrev;
    ImageButton BtnNext;
    ImageButton BtnPause;
    ImageButton shuffle;
    ImageButton replay;
    ImageButton favoriteButton;
    ImageButton menuButton;

    private MusicPlayer player;
    public abstract View getView();

    public abstract void showLyricLines();
    public abstract void showNoLyrics();
    public void handlePause(boolean isPlaying) {
        BtnPause.setBackgroundResource(isPlaying ? R.drawable.ic_baseline_pause_24 : R.drawable.ic_baseline_play_arrow_24);
    }
    public void handleSongChange(MusicItem song) {
        doLayout();
        BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
    }

    public void updateProgress() {
        progressBar.setProgress((float) player.getCurrentProgress() * 1000);
    }

    protected void doLayout() {
        MainActivity main = (MainActivity) getActivity();
        player = main.getPlayer();
        main.getPThread().start(this);

        int textColor = Color.parseColor(getContext().getSharedPreferences("SAVEDATA", 0).getString("THEME_COLOR", "#FFFFFF"));

        List<MusicItem> favorite = main.getPreferencesHandler().getFavorites();

        songNameView = view.findViewById(R.id.song_information_name);
        songDescView = view.findViewById(R.id.song_information_author);
        songLocView = view.findViewById(R.id.song_information_location);
        BtnPrev = view.findViewById(R.id.BtnPrev);
        BtnNext = view.findViewById(R.id.BtnNext);
        BtnPause = view.findViewById(R.id.BtnPause);
        shuffle = view.findViewById(R.id.detailed_shuffle);
        replay = view.findViewById(R.id.detailed_replay);
        favoriteButton = view.findViewById(R.id.detailed_add_to_favorites);
        menuButton = view.findViewById(R.id.detailed_menu_button);
        progressBar = view.findViewById(R.id.progress_bar);

        songNameView.setTextColor(textColor);
        songDescView.setTextColor(textColor);
        songLocView.setTextColor(textColor);
        songDescView.setCompoundDrawableTintList(ColorStateList.valueOf(textColor));
        songLocView.setCompoundDrawableTintList(ColorStateList.valueOf(textColor));

        favoriteButton.setActivated(favorite.contains(MusicPlayer.getCurrentPlayingSong()));

        songNameView.setText(MusicPlayer.getCurrentPlayingSong().getTitle());
        songDescView.setText(MusicPlayer.getCurrentPlayingSong().getDesc());
        songLocView.setText(MusicPlayer.getCurrentPlayingSong().getLocationFolder());
        if (!player.isPlaying()) {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
        } else {
            BtnPause.setBackgroundResource(R.drawable.ic_baseline_pause_24);
        }
        SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
        switch (SharedPreferencesHandler.sharedPreferences.getInt("REPLAY_MODE", 0)) {
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
        if (SharedPreferencesHandler.sharedPreferences.getBoolean("SHUFFLE", false)) shuffle.setActivated(true);
        shuffle.setOnClickListener(view -> {
            shuffle.setActivated(!shuffle.isActivated());
            editor.putBoolean("SHUFFLE", shuffle.isActivated());
            editor.apply();
            //player.prepareButtons();
            shuffle.setActivated(shuffle.isActivated());
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
            replay.setActivated(replay.isActivated());
        });
        menuButton.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(getContext(), view);
            popup.inflate(R.menu.detailed_menu);
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.addtoplaylist: {
                        PopupMenu menu = new PopupMenu(getContext(), view);
                        for (String menuItem : SharedPreferencesHandler.sharedPreferences.getString("PLAYLISTS", "").split("\n")) {
                            menu.getMenu().add(menuItem);
                        }
                        menu.setOnMenuItemClickListener(item1 -> {
                            SharedPreferences.Editor editor2 = SharedPreferencesHandler.sharedPreferences.edit();
                            String currentPlaylist = SharedPreferencesHandler.sharedPreferences.getString("PLAYLIST_" + item1.getTitle(), "");
                            if (Arrays.asList(currentPlaylist.split("\n")).contains(MusicPlayer.getCurrentPlayingSong().getTitle()))
                                return true;
                            if (currentPlaylist.isEmpty())
                                editor2.putString("PLAYLIST_" + item1.getTitle(), MusicPlayer.getCurrentPlayingSong().getTitle());
                            else {
                                editor2.putString("PLAYLIST_" + item1.getTitle(), currentPlaylist + "\n" + MusicPlayer.getCurrentPlayingSong().getTitle());
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
                                Files.delete(Paths.get(MusicPlayer.getCurrentPlayingSong().getLocation()));
                                MainActivity.wholeSongList.remove(MusicPlayer.getCurrentPlayingSong());
                                main.getMusicList().handleSongDeletion(MusicPlayer.getCurrentPlayingSong());
                                main.handleNextSong(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        };
                        new ConfirmPopup(getContext(), "Delete", "Are you sure you want to delete?", callback).show();
                        break;
                    }
                }
                return true;
            });
            popup.show();
        });
        favoriteButton.setOnClickListener(view -> {
            if (favoriteButton.isActivated()) {
                main.getPreferencesHandler().removeFromFavorites(MusicPlayer.getCurrentPlayingSong().getTitle());
                favoriteButton.setActivated(false);
            } else {
                main.getPreferencesHandler().addToFavorites(MusicPlayer.getCurrentPlayingSong().getTitle());
                MusicPlayer.getCurrentPlayingSong().setFavorited(true);
                favoriteButton.setActivated(true);
            }
            favoriteButton.setActivated(favoriteButton.isActivated());
        });
        BtnNext.setOnClickListener(view -> main.handleNextSong(true));
        BtnPrev.setOnClickListener(view -> main.handlePrevSong(true));
        BtnPause.setOnClickListener(view -> main.handlePause());
    }
}
