package com.tsevaj.musicapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
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
import android.widget.SearchView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public MusicPlayer player;
    private DrawerLayout drawer;
    public static Fragment currentFragment;
    NavigationView navigationView;
    public ArrayList<MyList> songList = new ArrayList<>();
    public PrevNextList PrevAndNextSongs = new PrevNextList(getBaseContext());
    public NotificationUtils utils;
    ProgressBarThread t;
    ActionBarDrawerToggle toggle;
    BroadcastReceiver receiver;

    @RequiresApi(api = Build.VERSION_CODES.R)
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

        PrevAndNextSongs.c = getBaseContext();

        this.player = new MusicPlayer(getBaseContext());
        this.player.c = this;
        this.player.main = this;
        this.player.manager = getSupportFragmentManager();
        registerReceiver(new MyController(player, getApplicationContext()), new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

        utils = new NotificationUtils(player);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new LibraryFragment(player,"","")).commit();
            navigationView.setCheckedItem(R.id.menu_library);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification(int playPauseButton, String songName) {
        utils.displayNotification(this, songName, playPauseButton);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setBackground(View view, Resources resources) {
        if (new File(SettingsFragment.destination).exists()) {
            view.setBackgroundDrawable(new BitmapDrawable(resources, BitmapFactory.decodeFile(SettingsFragment.destination)));
        }
        else {
            view.setBackground(resources.getDrawable(R.drawable.background));
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
        if (currentFragment.getClass().equals(Detailed_song.class)) {
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
        if (currentFragment.getClass().equals(Detailed_song.class)) {
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
        player.destroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    private void showSortPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.sort_menu, popup.getMenu());
        SharedPreferences settings = getSharedPreferences("SAVEDATA", 0);
        SharedPreferences.Editor editor = settings.edit();
        popup.getMenu().getItem(3).setChecked(settings.getBoolean("ASCENDING", true));
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.sort_date:{
                    editor.putString("REPLAY", "DATE");
                    editor.apply();
                    break;
                }
                case R.id.sort_length: {
                    editor.putString("REPLAY", "LENGTH");
                    editor.apply();
                    break;
                }
                case R.id.sort_title: {
                    editor.putString("REPLAY", "TITLE");
                    editor.apply();
                    break;
                }
                case R.id.sort_reverse: {
                    if (settings.getBoolean("ASCENDING", true)) {
                        popup.getMenu().getItem(3).setChecked(false);
                        editor.putBoolean("ASCENDING", false);
                    }
                    else {
                        popup.getMenu().getItem(3).setChecked(true);
                        editor.putBoolean("ASCENDING", true);
                    }
                    editor.apply();
                    break;
                }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        menu.getItem(0).setVisible(!(currentFragment.getClass().equals(Detailed_song.class) ||
                            currentFragment.getClass().equals(PlaylistsFragment.class) ||
                            currentFragment.getClass().equals(SettingsFragment.class)
                ));
        menu.getItem(1).setVisible(!(currentFragment.getClass().equals(Detailed_song.class) ||
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
                Fragment newFragment = null;
                if (currentFragment.getClass().equals(LibraryFragment.class)) {
                    newFragment = new LibraryFragment(player, "", s);
                } else if (currentFragment.getClass().equals(FavoritesFragment.class)) {
                    newFragment = new FavoritesFragment(player,s, player.main);
                }
                if (newFragment == null) return false;
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack(null);

                transaction.commit();
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