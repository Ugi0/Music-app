package com.tsevaj.musicapp;

import static com.tsevaj.musicapp.services.notification.NotificationClass.ACTION_NOTIFY;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.tsevaj.musicapp.adapters.CustomAdapter;
import com.tsevaj.musicapp.fragments.DetailedLyricsFragment;
import com.tsevaj.musicapp.fragments.LibraryFragment;
import com.tsevaj.musicapp.fragments.PagerFragment;
import com.tsevaj.musicapp.fragments.PlaylistsFragment;
import com.tsevaj.musicapp.fragments.SettingsFragment;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.interfaces.MusicFragment;
import com.tsevaj.musicapp.fragments.interfaces.RefreshableFragment;
import com.tsevaj.musicapp.services.bluetooth.BluetoothService;
import com.tsevaj.musicapp.services.notification.NotificationController;
import com.tsevaj.musicapp.services.notification.NotificationService;
import com.tsevaj.musicapp.utils.ApplicationConfig;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.data.MusicItem;
import com.tsevaj.musicapp.utils.MusicList;
import com.tsevaj.musicapp.utils.SharedPreferencesHandler;
import com.tsevaj.musicapp.utils.data.SortValue;
import com.tsevaj.musicapp.utils.enums.MusicListType;
import com.tsevaj.musicapp.utils.enums.SortOption;
import com.tsevaj.musicapp.utils.files.MusicGetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, NotificationController {
    @Getter
    private MusicPlayer player;
    private DrawerLayout drawer;
    NavigationView navigationView;
    @Getter
    private MusicList musicList;

    @Getter
    public static Fragment currentFragment;
    public static List<MusicItem> wholeSongList = null;
    public BluetoothService mediaController;

    private ActionBarDrawerToggle toggle;
    @Getter
    private ApplicationConfig config;
    @Getter
    private SharedPreferencesHandler preferencesHandler;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferencesHandler = new SharedPreferencesHandler(this);

        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package",getPackageName(),null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            wholeSongList = MusicGetter.loadList(this);
        }

        if (savedInstanceState == null) {
            changeFragment(LibraryFragment.class, wholeSongList);

            this.musicList = new MusicList(this, wholeSongList);

            this.player = new MusicPlayer(getApplicationContext(), musicList);
            this.config = new ApplicationConfig(this, preferencesHandler.getConfig());
            this.mediaController = new BluetoothService(player, this);
            registerReceiver(mediaController, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

            NotificationService.setNotificationController(this);

            if (SharedPreferencesHandler.sharedPreferences.getBoolean("SAVE_STATE", false)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    String hash = SharedPreferencesHandler.sharedPreferences.getString("SAVED_SONG_HASH", "0");
                    int time = Integer.parseInt(SharedPreferencesHandler.sharedPreferences.getString("SAVED_SONG_TIME", "0"));
                    if (!hash.equals("0")) {
                        MainActivity.wholeSongList.stream().filter(e -> e.getHash().equals(hash)).findFirst().ifPresent(song -> player.resumeState(song, time));
                    }
                }, 250);
            }
        }
    }

    public void changePlayingList(ArrayList<MusicItem> li) {
        musicList.setList(li);
    }

    public void handleSongChange(MusicItem song) {
        //Handle musicPlayer
        player.play(song);
        //Change UI
        if (currentFragment instanceof HasControlBar) {
            ((HasControlBar) currentFragment).handleSongChange(song);
        }
        //TODO Handle notification
        Intent intent = new Intent(this, NotificationService.class);
        bindService(intent, player, 0);
        Intent intent1 = new Intent(this, NotificationService.class);
        intent1.setAction("NOTIFY");
        startService(intent1);
        showNotification(true);
        //Handle data
        //TODO Change PrevNextPlayList?
    }
    private void showNotification(boolean playing) {
        showNotification(playing ? R.drawable.ic_baseline_pause_24 : R.drawable.ic_baseline_play_arrow_24);
    }
    private void showNotification(int button) {
        Intent intent1 = new Intent(this, NotificationService.class);
        intent1.setAction(ACTION_NOTIFY);
        intent1.setType(String.valueOf(button));
        startService(intent1);
    }

    /*public void setDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home): {
                onBackPressed();
                currentFragment = new LibraryFragment(this, wholeSongList);
                //setDrawer();
                break;
            }
            case (R.id.action_search): {
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        SearchView searchView = (SearchView) item.getActionView();
                        searchView.setIconified(false);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        SearchView searchView = (SearchView) item.getActionView();
                        searchView.setQuery("",false);
                        menuItem.getActionView().clearFocus();
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        return true;
                    }
                });
                break;
            }
            case (R.id.action_sort): {
                showSortPopup(findViewById(R.id.action_sort));
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case (R.id.menu_library):
                changeFragment(LibraryFragment.class, wholeSongList);
                break;
            case (R.id.menu_favorites):
                changeFragment(LibraryFragment.class, MusicGetter.getMusic(this, new SortValue(MusicListType.FAVORITES), ""));
                break;
            case (R.id.menu_settings):
                changeFragment(SettingsFragment.class);
                break;
            case (R.id.menu_playlists):
                changeFragment(PlaylistsFragment.class);
                break;
            default:
                throw new RuntimeException("Option that does not exist selected");
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2909) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission", "Granted");
            } else {
                Log.e("Permission", "Denied");
            }
        }
    }

    @Override
    protected void onDestroy() {
        Intent intent1 = new Intent(getApplicationContext(), NotificationService.class);
        stopService(intent1);
        SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
        editor.putString("SAVED_SONG_HASH", String.valueOf(MusicPlayer.getCurrentSongHash()));
        editor.putString("SAVED_SONG_TIME", String.valueOf(player.getCurrentPosition()));
        editor.apply();
        player.destroy();
        DetailedLyricsFragment.t.stop();
        config.saveConfig();
        //t.stopThread();
        finish();
        super.onDestroy();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showSortPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.sort_menu, popup.getMenu());
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        popup.getMenu().getItem(4).setChecked(settings.getBoolean("ASCENDING", true));
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.sort_date) {
                editor.putString(SortOption.NAME, SortOption.DATE.toString());
                editor.apply();
            } else if (item.getItemId() == R.id.sort_length) {
                editor.putString(SortOption.NAME, SortOption.LENGTH.toString());
                editor.apply();
            } else if (item.getItemId() == R.id.sort_title) {
                editor.putString(SortOption.NAME, SortOption.TITLE.toString());
                editor.apply();
            } else if (item.getItemId() == R.id.sort_random) {
                editor.putString(SortOption.NAME, SortOption.RANDOM.toString());
                editor.apply();
            } else if (item.getItemId() == R.id.sort_reverse) {
                if (settings.getBoolean("ASCENDING", true)) {
                    popup.getMenu().getItem(4).setChecked(false);
                    editor.putBoolean("ASCENDING", false);
                }
                else {
                    popup.getMenu().getItem(4).setChecked(true);
                    editor.putBoolean("ASCENDING", true);
                }
                editor.apply();
            }
            musicList.sortFilterList(SortOption.valueOf(settings.getString(SortOption.NAME, SortOption.DATE.toString())), settings.getBoolean("ASCENDING", true));

            //TODO Handle sort change
            /*Fragment newFragment = null;
            if (currentFragment.getClass().equals(LibraryFragment.class)) {
                newFragment = new LibraryFragment(player,"", PrevAndNextSongs.getLastFilter());
            }
            else if (currentFragment.getClass().equals(FavoritesFragment.class)) {
                newFragment = new FavoritesFragment(player,"");
            }*/
            /*FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            assert newFragment != null;
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            */
            return true;
        });
        popup.show();
    }

    public void changeFragment(Class<? extends MusicFragment> fragmentClass) { changeFragment(fragmentClass, null);}
    public void changeFragment(Class<? extends MusicFragment> fragmentClass, List<MusicItem> songList) {
        try {
            Constructor<? extends MusicFragment> constructor;
            MusicFragment newFragment;
            if (songList != null) {
                constructor = fragmentClass.getConstructor(MainActivity.class, List.class);
                newFragment = constructor.newInstance(this, songList);
            } else {
                constructor = fragmentClass.getConstructor(MainActivity.class);
                newFragment = constructor.newInstance(this);
            }
            MainActivity.currentFragment = newFragment;
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            //setClickable();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        menu.getItem(0).setVisible(!(currentFragment.getClass().equals(PagerFragment.class) ||
                            currentFragment.getClass().equals(PlaylistsFragment.class) ||
                            currentFragment.getClass().equals(SettingsFragment.class)
                ));
        menu.getItem(1).setVisible(!(currentFragment.getClass().equals(PagerFragment.class) ||
                            currentFragment.getClass().equals(PlaylistsFragment.class) ||
                            currentFragment.getClass().equals(SettingsFragment.class)
                    ));
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnCloseListener(() -> {

            return false;
        });
        final Handler handler = new Handler(Looper.getMainLooper());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    if (currentFragment instanceof RefreshableFragment) {
                        RefreshableFragment fragment = (RefreshableFragment) currentFragment;
                        List<MusicItem> songList = MusicGetter.getMusic(MainActivity.this, fragment.getListType(), s);
                        fragment.getRecyclerView().setAdapter(
                                new CustomAdapter(songList, MainActivity.this)
                        );
                    }
                }, 500);

                return true;
            }});
        searchView.setOnCloseListener(() -> {
            //TODO Clear search params
            return false;
        });
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public void handleNextSong(Boolean force) {
        handleSongChange(musicList.Next(force));
    }

    @Override
    public void handlePrevSong(Boolean force) {
        handleSongChange(musicList.Prev());
    }

    @Override
    public void handlePause() {
        player.playPause();
        showNotification(player.isPlaying());
        if (currentFragment instanceof HasControlBar) {
            ((HasControlBar) currentFragment).handlePause();
        }
    }

}