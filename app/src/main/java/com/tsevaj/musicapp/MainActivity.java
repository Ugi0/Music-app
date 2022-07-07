package com.tsevaj.musicapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.media.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public MusicPlayer player;
    private DrawerLayout drawer;
    public static Fragment currentFragment;
    NavigationView navigationView;
    public ArrayList<MyList> songList = new ArrayList<>();
    public PrevNextList PrevAndNextSongs = new PrevNextList(getBaseContext());
    AsyncTask<Void, Void, Void> t;
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

        this.player = new MusicPlayer(this);
        this.player.main = this;
        this.player.manager = getSupportFragmentManager();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new LibraryFragment(player,"","")).commit();
            navigationView.setCheckedItem(R.id.menu_library);
        }

    }

    //TODO Style the notification better
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showNotification(int playPauseButton, String songName) {
        NotificationUtils utils = new NotificationUtils(player);
        utils.displayNotification(this, songName);
      /* Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        session = new MediaSession(this, "test");
        session.setMetadata(new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, songName)
                .build());

        Notification notification = new Notification.Builder(getBaseContext(), NotificationClass.Channel)
                .setContentTitle(songName)
                .setSmallIcon(R.mipmap.app_icon)
                .setStyle(new Notification.DecoratedMediaCustomViewStyle())
               // .setStyle(new Notification.MediaStyle().setMediaSession(session.getSessionToken())) // sets the notification in the top bar
                .addAction(generateAction(R.drawable.ic_baseline_skip_previous_24, "PREVIOUS", NotificationClass.ACTION_PREV))
                .addAction(generateAction(R.drawable.ic_baseline_pause_24, "PAUSE", NotificationClass.ACTION_PAUSE))
                .addAction(generateAction(R.drawable.ic_baseline_skip_next_24, "NEXT", NotificationClass.ACTION_NEXT))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(contentIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification); */
    }


    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( this, NotificationClass.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }


    public void setClickable() {

        ActionBar actionBar = getSupportActionBar();
        toggle.setDrawerIndicatorEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void setDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        ActionBar actionBar = getSupportActionBar();
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
        if (itemId == R.id.menu_library) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new LibraryFragment(player, "","")).commit();
        } else if (itemId == R.id.menu_favorites) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new FavoritesFragment(player,"", this)).commit();
        } else if (itemId == R.id.menu_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SettingsFragment()).commit();
        } else if (itemId == R.id.menu_playlists) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PlaylistsFragment(player, this)).commit();
        }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
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
        menu.getItem(0).setVisible(!(currentFragment.getClass().equals(Detailed_song.class) || currentFragment.getClass().equals(PlaylistsFragment.class)));
        menu.getItem(1).setVisible(!(currentFragment.getClass().equals(Detailed_song.class) || currentFragment.getClass().equals(PlaylistsFragment.class)));
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
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