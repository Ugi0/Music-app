package com.tsevaj.musicapp.utils;

import android.content.SharedPreferences;

import com.tsevaj.musicapp.MainActivity;

import java.util.Arrays;
import java.util.List;

public class SharedPreferencesHandler {
    SharedPreferences sharedPreferences;

    private static final String FAVORITES = "FAVORTES";
    private static final String PLAYLISTS = "PLAYLISTS";

    //TODO handle favorites and playlists through this class
    public SharedPreferencesHandler(MainActivity main) {
        sharedPreferences = main.getSharedPreferences("SAVEDATA", 0);
    }

    public List<String> getFavorites() {
        return Arrays.asList(sharedPreferences.getString(FAVORITES, "").split(";"));
    }

    public void removeFromFavorites(String hash) {
        List<String> favorites = getFavorites();
        favorites.remove(hash);
        sharedPreferences.edit().putString(FAVORITES, String.join(";", favorites)).apply();
    }

    public void addToFavorites(String hash) {
        List<String> favorites = getFavorites();
        favorites.add(hash);
        sharedPreferences.edit().putString(FAVORITES, String.join(";", favorites)).apply();
    }
}
