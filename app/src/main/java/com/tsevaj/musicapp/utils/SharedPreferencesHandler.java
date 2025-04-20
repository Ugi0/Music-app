package com.tsevaj.musicapp.utils;

import android.content.SharedPreferences;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.utils.data.MusicItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SharedPreferencesHandler {
    public static SharedPreferences sharedPreferences;

    public enum SharedPreferenceKeys {
        FAVORITES,
        PLAYLISTS
    }

    //TODO handle favorites and playlists through this class
    public SharedPreferencesHandler(MainActivity main) {
        sharedPreferences = main.getSharedPreferences("SAVEDATA", 0);
    }

    public String getConfig() {
        return sharedPreferences.getString("CONFIG", "");
    }

    public List<MusicItem> getFavorites() {
        return MainActivity.wholeSongList.stream().filter(e -> Arrays.asList(sharedPreferences.getString(SharedPreferenceKeys.FAVORITES.name(), "").split(";")).contains(e.getHash())).collect(Collectors.toList());
    }

    private List<String> getFavoritesHashes() {
        return Arrays.asList(sharedPreferences.getString(SharedPreferenceKeys.FAVORITES.name(), "").split(";"));
    }

    public boolean songInPlayList(String playList, MusicItem song) {
        return getPlayList(playList).contains(song);
    }

    public void removeFromFavorites(String hash) {
        List<String> favorites = getFavoritesHashes();
        favorites.remove(hash);
        sharedPreferences.edit().putString(SharedPreferenceKeys.FAVORITES.name(), String.join(";", favorites)).apply();
    }

    public List<String> getPlayLists() {
        return Arrays.asList(sharedPreferences.getString(SharedPreferenceKeys.PLAYLISTS.name(), "").split(";"));
    }

    public void addPlaylist(String name) {
        List<String> playlists = getPlayLists();
        playlists.add(name);
        sharedPreferences.edit().putString(SharedPreferenceKeys.PLAYLISTS.name(), String.join(",", playlists)).apply();
    }

    public void removePlayList(String name) {
        List<String> playlists = getPlayLists();
        playlists.remove(name);
        sharedPreferences.edit().putString(SharedPreferenceKeys.PLAYLISTS.name(), String.join(",", playlists)).apply();
    }

    public void addToFavorites(String hash) {
        List<String> favorites = getFavoritesHashes();
        favorites.add(hash);
        sharedPreferences.edit().putString(SharedPreferenceKeys.FAVORITES.name(), String.join(";", favorites)).apply();
    }

    public List<MusicItem> getPlayList(String title) {
        return MainActivity.wholeSongList.stream().filter(e -> Arrays.asList(sharedPreferences.getString(String.format("%s_%s", SharedPreferenceKeys.PLAYLISTS.name(), title), "").split(";")).contains(e.getHash())).collect(Collectors.toList());
    }
}
