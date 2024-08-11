package com.tsevaj.musicapp;

import static com.tsevaj.musicapp.services.notification.NotificationClass.ACTION_NOTIFY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.tsevaj.musicapp.fragments.DetailedLyricsFragment;
import com.tsevaj.musicapp.fragments.FavoritesFragment;
import com.tsevaj.musicapp.fragments.LibraryFragment;
import com.tsevaj.musicapp.fragments.PagerFragment;
import com.tsevaj.musicapp.fragments.PlaylistsFragment;
import com.tsevaj.musicapp.fragments.SettingsFragment;
import com.tsevaj.musicapp.fragments.interfaces.HasControlBar;
import com.tsevaj.musicapp.fragments.interfaces.MusicFragment;
import com.tsevaj.musicapp.fragments.interfaces.RefreshableFragment;
import com.tsevaj.musicapp.services.bluetooth.MyController;
import com.tsevaj.musicapp.services.notification.NotificationService;
import com.tsevaj.musicapp.utils.ApplicationConfig;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.MusicItem;
import com.tsevaj.musicapp.utils.PrevNextList;
import com.tsevaj.musicapp.utils.ProgressBarThread;
import com.tsevaj.musicapp.utils.SharedPreferencesHandler;
import com.tsevaj.musicapp.utils.enums.SortOptions;
import com.tsevaj.musicapp.utils.files.MusicGetter;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public MusicPlayer player;
    private DrawerLayout drawer;
    public static Fragment currentFragment;
    NavigationView navigationView;
    public PrevNextList PrevAndNextSongs;
    public ProgressBarThread t;
    ActionBarDrawerToggle toggle;
    BroadcastReceiver receiver;
    public ArrayList<MusicItem> songQueue = new ArrayList<>();
    public static ArrayList<MusicItem> wholeSongList = null;

    public File BackgroundDestinationPath;

    public MyController mediaController;

    private ApplicationConfig config;

    private SharedPreferencesHandler preferencesHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDrawer();

        preferencesHandler = new SharedPreferencesHandler(this);

        BackgroundDestinationPath = getExternalFilesDir("");
        boolean result = Objects.requireNonNull(BackgroundDestinationPath.getParentFile()).mkdirs();

        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package",getPackageName(),null);
            intent.setData(uri);
            startActivity(intent);
        } else {
            wholeSongList = MusicGetter.getMusic(this, "", "");
        }

        if (savedInstanceState == null) {
            SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
            Fragment newFragment = new LibraryFragment(this);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    newFragment).commit();
            navigationView.setCheckedItem(R.id.menu_library);

            this.PrevAndNextSongs = new PrevNextList(wholeSongList, newFragment, getApplicationContext());
            this.player = new MusicPlayer(getApplicationContext());
            this.config = new ApplicationConfig(settings.getString("CONFIG", ""));
            //this.player.c = this;
            //this.player.main = this;
            //this.player.manager = getSupportFragmentManager();
            this.mediaController = new MyController(player, this);
            registerReceiver(mediaController, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

            if (settings.getBoolean("SAVE_STATE", false)) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    int hash = Integer.parseInt(settings.getString("SAVED_SONG_HASH", "0"));
                    int time = Integer.parseInt(settings.getString("SAVED_SONG_TIME", "0"));
                    if (hash != 0) {
                        MainActivity.wholeSongList.stream().filter(e -> e.getHash() == hash).findFirst().ifPresent(song -> player.resumeState(song, time));
                    }
                }, 250);
            }
        }
    }

    public SharedPreferencesHandler getPreferencesHandler() {
        return this.preferencesHandler;
    }

    public ApplicationConfig getConfig() {
        return this.config;
    }

    public void changePlayingList(ArrayList<MusicItem> li) {
        PrevAndNextSongs.setList(li);
    }

    public void handleSongChange(MusicItem song) {
        if (currentFragment instanceof HasControlBar) {
            ((HasControlBar) currentFragment).handleSongChange(song);
        }
    }

    public void handleSongPause() {
        if (currentFragment instanceof HasControlBar) {
            ((HasControlBar) currentFragment).handlePause();
        }
    }

    public void handleSongResume() {
        if (currentFragment instanceof HasControlBar) {
            ((HasControlBar) currentFragment).handleResume();
        }
    }

    public void showNotification(int pauseButton) {
        Intent intent1 = new Intent(this, NotificationService.class);
        intent1.setAction(ACTION_NOTIFY);
        intent1.setType(String.valueOf(pauseButton));
        startService(intent1);
        //utils.displayNotification(this, songName, playPauseButton);
    }

    public void setBackground(View view, Resources resources) {
        if (new File(BackgroundDestinationPath+"/background").exists()) {
            view.setBackground(new BitmapDrawable(resources, BitmapFactory.decodeFile(BackgroundDestinationPath+"/background")));
        }
        else {
            view.setBackground(ResourcesCompat.getDrawable(resources, R.drawable.background, null));
        }
    }

    public void setClickable() {
        toggle.setDrawerIndicatorEnabled(false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    public void setDrawer() {
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home): {
                onBackPressed();
                currentFragment = new LibraryFragment(this);
                setDrawer();
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

    private Class<? extends MusicFragment> getDrawerMenuOptionClass(int id) {
        switch (id) {
            case (R.id.menu_library):
                return LibraryFragment.class;
            case (R.id.menu_favorites):
                return FavoritesFragment.class;
            case (R.id.menu_settings):
                return SettingsFragment.class;
            case (R.id.menu_playlists):
                return PlaylistsFragment.class;
            default:
                throw new RuntimeException("Option that does not exist selected");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Constructor<? extends MusicFragment> constructor;
        try {
            constructor = getDrawerMenuOptionClass(itemId).getConstructor(MainActivity.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        MusicFragment newFragment;
        try {
             newFragment = constructor.newInstance(this);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        FragmentTransaction createdFragment = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                newFragment);
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            createdFragment.addToBackStack(null);
        }
        createdFragment.commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2909) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission", "Granted");
                wholeSongList = MusicGetter.getMusic(this, "", "");
            } else {
                Log.e("Permission", "Denied");
            }
        }
    }

    @Override
    protected void onDestroy() {
        Intent intent1 = new Intent(getApplicationContext(), NotificationService.class);
        stopService(intent1);
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("SAVED_SONG_HASH", String.valueOf(MusicPlayer.getCurrentSongHash()));
        editor.putString("SAVED_SONG_TIME", String.valueOf(player.getCurrentPosition()));
        editor.apply();
        player.destroy();
        DetailedLyricsFragment.t.stop();
        config.saveConfig();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        t.stopThread();
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
                editor.putString(SortOptions.NAME, SortOptions.DATE.toString());
                editor.apply();
            } else if (item.getItemId() == R.id.sort_length) {
                editor.putString(SortOptions.NAME, SortOptions.LENGTH.toString());
                editor.apply();
            } else if (item.getItemId() == R.id.sort_title) {
                editor.putString(SortOptions.NAME, SortOptions.TITLE.toString());
                editor.apply();
            } else if (item.getItemId() == R.id.sort_random) {
                editor.putString(SortOptions.NAME, SortOptions.RANDOM.toString());
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
            PrevAndNextSongs.sortFilterList(settings.getString(SortOptions.NAME, SortOptions.DATE.toString()), settings.getBoolean("ASCENDING", true));

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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //TODO find a smarter way to change the ordering of items here
                //The full list of items can be found in wholeSongList
                if (currentFragment instanceof RefreshableFragment) {
                    ((RefreshableFragment) currentFragment).getRecyclerView().setAdapter();
                }
                //TODO Handle changing the list is UI
                /*if (currentFragment.getClass().equals(LibraryFragment.class)) {
                    PrevAndNextSongs.getMusicAndSet(player.recyclerview, this, player, this, "", s);
                } else if (currentFragment.getClass().equals(FavoritesFragment.class)) {
                    PrevAndNextSongs.getMusicAndSet(player.recyclerview, this, player, this, "FAVORITES", s);
                }*/
                return false;
            }});
        searchView.setOnCloseListener(() -> {
            //TODO Clear search params
            return false;
        });
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }
}