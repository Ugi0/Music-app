package com.tsevaj.musicapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.tsevaj.musicapp.fragments.DetailedsongFragment;
import com.tsevaj.musicapp.fragments.FavoritesFragment;
import com.tsevaj.musicapp.fragments.LibraryFragment;
import com.tsevaj.musicapp.fragments.PlaylistsFragment;
import com.tsevaj.musicapp.fragments.SettingsFragment;
import com.tsevaj.musicapp.services.MyController;
import com.tsevaj.musicapp.services.NotificationService;
import com.tsevaj.musicapp.services.NotificationUtils;
import com.tsevaj.musicapp.utils.FunctionClass;
import com.tsevaj.musicapp.utils.MusicPlayer;
import com.tsevaj.musicapp.utils.MusicItem;
import com.tsevaj.musicapp.utils.PrevNextList;
import com.tsevaj.musicapp.utils.ProgressBarThread;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public MusicPlayer player;
    private DrawerLayout drawer;
    public static Fragment currentFragment;
    NavigationView navigationView;
    public PrevNextList PrevAndNextSongs = new PrevNextList(getBaseContext());
    public NotificationUtils utils;
    public ProgressBarThread t;
    ActionBarDrawerToggle toggle;
    BroadcastReceiver receiver;
    public ArrayList<MusicItem> songQueue = new ArrayList<>();
    public static ArrayList<MusicItem> wholeSongList = null;

    public File BackgroundDestinationPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package",getPackageName(),null);
            intent.setData(uri);
            startActivity(intent); }

        setDrawer();

        this.player = new MusicPlayer(getBaseContext());
        this.player.c = this;
        this.player.main = this;
        this.player.manager = getSupportFragmentManager();
        registerReceiver(new MyController(player, this), new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

        BackgroundDestinationPath = getExternalFilesDir("");
        boolean result = Objects.requireNonNull(BackgroundDestinationPath.getParentFile()).mkdirs();

        utils = new NotificationUtils();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new LibraryFragment(player,"","")).commit();
            navigationView.setCheckedItem(R.id.menu_library);
        }

    }
    public void changePlayingList(ArrayList<MusicItem> li) {
        PrevAndNextSongs.setList(li);
    }

    public static void changeSongListWholeList(ArrayList<MusicItem> li) {
        PrevNextList.allSongs = new ArrayList<>(li);
    }

    public void showNotification(int playPauseButton, String songName) {
        utils.displayNotification(this, songName, playPauseButton);
    }

    public void setBackground(View view, Resources resources) {
        if (new File(this.player.main.BackgroundDestinationPath+"/background").exists()) {
            view.setBackground(new BitmapDrawable(resources, BitmapFactory.decodeFile(this.player.main.BackgroundDestinationPath+"/background")));
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
                currentFragment = new LibraryFragment(player, "","");
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (currentFragment.getClass().equals(DetailedsongFragment.class)) {
            onBackPressed();
            currentFragment = new LibraryFragment(player, "","");
            setDrawer();
            return true;
        }
        int itemId = item.getItemId();
        FragmentTransaction createdFragment = null;
        if (itemId == R.id.menu_library) {
            createdFragment = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new LibraryFragment(player, "", ""));
        } else if (itemId == R.id.menu_favorites) {
            createdFragment = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new FavoritesFragment(player,"", this));
        } else if (itemId == R.id.menu_settings) {
            createdFragment = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SettingsFragment(this));
        } else if (itemId == R.id.menu_playlists) {
            createdFragment = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PlaylistsFragment(player, this));
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            assert createdFragment != null;
            createdFragment.addToBackStack(null);
        }
        assert createdFragment != null;
        createdFragment.commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (currentFragment.getClass().equals(DetailedsongFragment.class)) {
            currentFragment = new LibraryFragment(player, "","");
            setDrawer();
        }
        super.onBackPressed();
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
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("SAVED_SONG_NAME", player.currentPlayingSong.getHead());
        editor.putString("SAVED_SONG_DURATION", player.currentDuration.toString());
        editor.apply();
        player.destroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        //TODO save state here
        super.onDestroy();
    }

    private void showSortPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.sort_menu, popup.getMenu());
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        popup.getMenu().getItem(3).setChecked(settings.getBoolean("ASCENDING", true));
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.sort_date) {
                editor.putString("REPLAY", "DATE");
                editor.apply();
            } else if (item.getItemId() == R.id.sort_length) {
                editor.putString("REPLAY", "LENGTH");
                editor.apply();
            } else if (item.getItemId() == R.id.sort_title) {
                editor.putString("REPLAY", "TITLE");
                editor.apply();
            } else if (item.getItemId() == R.id.sort_reverse) {
                if (settings.getBoolean("ASCENDING", true)) {
                    popup.getMenu().getItem(3).setChecked(false);
                    editor.putBoolean("ASCENDING", false);
                }
                else {
                    popup.getMenu().getItem(3).setChecked(true);
                    editor.putBoolean("ASCENDING", true);
                }
                editor.apply();
            }
            Fragment newFragment = null;
            if (currentFragment.getClass().equals(LibraryFragment.class)) {
                newFragment = new LibraryFragment(player, "","");
            }
            else if (currentFragment.getClass().equals(FavoritesFragment.class)) {
                newFragment = new FavoritesFragment(player,"", this);
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            assert newFragment != null;
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        });
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        menu.getItem(0).setVisible(!(currentFragment.getClass().equals(DetailedsongFragment.class) ||
                            currentFragment.getClass().equals(PlaylistsFragment.class) ||
                            currentFragment.getClass().equals(SettingsFragment.class)
                ));
        menu.getItem(1).setVisible(!(currentFragment.getClass().equals(DetailedsongFragment.class) ||
                            currentFragment.getClass().equals(PlaylistsFragment.class) ||
                            currentFragment.getClass().equals(SettingsFragment.class)
                    ));
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @SuppressLint("NewApi")
            @Override
            public boolean onQueryTextChange(String s) {
                if (currentFragment.getClass().equals(LibraryFragment.class)) {
                    FunctionClass.getMusicAndSet(player.recyclerview, player.main, player, player.main, "", s);
                } else if (currentFragment.getClass().equals(FavoritesFragment.class)) {
                    FunctionClass.getMusicAndSet(player.recyclerview, player.main, player, player.main, "FAVORITES", s);
                }
                return false;
            }});
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    public ArrayList<String> getFavorites() {
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        String favorites = settings.getString("FAVORITES", "");
        return new ArrayList<>(Arrays.asList(favorites.split("\n")));
    }

    public void addToFavorites(String s) {
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        String favorites = settings.getString("FAVORITES", "");
        editor.putString("FAVORITES", favorites + "\n" + s);
        editor.apply();
    }

    @SuppressLint("NewApi")
    public void setFavorites(ArrayList<String> li) {
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("FAVORITES", String.join("\n",li));
        editor.apply();
    }

    public void removeFromFavorites(String s) {
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        String favorites = settings.getString("FAVORITES", "");
        ArrayList<String> li = new ArrayList<>();
        for (String i: favorites.split("\n")) {
            if (!i.equals(s)) {
                li.add(i);
            }
        }
        setFavorites(li);
    }
}