package com.tsevaj.musicapp.fragments.uielements;

import static com.tsevaj.musicapp.utils.ApplicationConfig.BackgroundDestinationPath;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.LibraryFragment;
import com.tsevaj.musicapp.fragments.PlaylistsFragment;
import com.tsevaj.musicapp.fragments.SettingsFragment;

import java.io.File;

public abstract class MusicFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    protected MainActivity main;
    private View parentView;
    protected View view;
    protected ViewGroup contentContainer;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    protected View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        parentView = inflater.inflate(R.layout.music_fragment, container, false);
        main = (MainActivity) getActivity();

        contentContainer = parentView.findViewById(R.id.fragment_container);

        setDrawer();

        return parentView;
    }
    public static void setBackground(View view, Resources resources) {
        if (new File(BackgroundDestinationPath+"/background").exists()) {
            view.setBackground(new BitmapDrawable(resources, BitmapFactory.decodeFile(BackgroundDestinationPath+"/background")));
        }
        else {
            view.setBackground(ResourcesCompat.getDrawable(resources, R.drawable.background, null));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case (R.id.menu_library):
                main.changeFragment(LibraryFragment.class); //, wholeSongList);
                break;
            case (R.id.menu_favorites):
                main.changeFragment(LibraryFragment.class); //, MusicGetter.getMusic(main, new SortValue(MusicListType.FAVORITES), ""));
                break;
            case (R.id.menu_settings):
                main.changeFragment(SettingsFragment.class);
                break;
            case (R.id.menu_playlists):
                main.changeFragment(PlaylistsFragment.class);
                break;
            default:
                throw new RuntimeException("Option that does not exist selected");
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void setDrawer() {
        Toolbar toolbar = parentView.findViewById(R.id.toolbar);
        main.setSupportActionBar(toolbar);
        main.setTitle("");

        ActionBar actionBar = main.getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        drawer = parentView.findViewById(R.id.drawer_layout);
        navigationView = parentView.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(getActivity(), drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
    }
}
