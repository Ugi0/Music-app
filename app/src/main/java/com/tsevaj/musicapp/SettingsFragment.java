package com.tsevaj.musicapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Set;

import top.defaults.colorpicker.ColorObserver;
import top.defaults.colorpicker.ColorWheelPalette;
import top.defaults.colorpicker.ColorWheelView;

public class SettingsFragment extends Fragment {
    private static final int FILE_SELECT_CODE = 0;
    public static final String destination = Environment.getExternalStorageDirectory()+"/"+"Android/data/com.tsevaj.musicapp/files/Pictures/background";

    ColorWheelView colorWheel;
    EditText colorWheelText;
    NumberPicker numberPicker;
    CardView backgroundSetter;
    EditText songsFolder;
    NumberPicker songLengthMin;
    NumberPicker songLengthSec;
    TextView themeText;
    TextView loopingText;
    TextView lengthText;
    TextView folderText;
    TextView backgroundText;

    MainActivity main;

    public SettingsFragment(MainActivity main) {
        this.main = main;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ll = inflater.inflate(R.layout.settings_fragment, container, false);

        MainActivity.setBackground(ll, getResources());

        SharedPreferences settings = requireActivity().getSharedPreferences("SAVEDATA", 0);

        colorWheel = ll.findViewById(R.id.color_wheel);
        colorWheelText = ll.findViewById(R.id.settings_color_text);
        numberPicker = ll.findViewById(R.id.settings_looping_size_number);
        backgroundSetter = ll.findViewById(R.id.settings_background);
        songsFolder = ll.findViewById(R.id.settings_songs_folder_text);
        songLengthMin = ll.findViewById(R.id.numpicker_minutes);
        songLengthSec = ll.findViewById(R.id.numpicker_seconds);

        themeText = ll.findViewById(R.id.Theme_text);
        loopingText = ll.findViewById(R.id.looping_text);
        lengthText = ll.findViewById(R.id.min_length_text);
        folderText = ll.findViewById(R.id.folder_text);
        backgroundText = ll.findViewById(R.id.background_text);

        setTextColors(requireContext().getSharedPreferences("SAVEDATA", 0).getString("THEME_COLOR", "#FFFFFF"));
        main.setClickable();

        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(1);
        numberPicker.setValue(settings.getInt("LOOPING_SIZE", 20));

        songLengthMin.setMinValue(0);
        songLengthMin.setMaxValue(60);
        songLengthSec.setMinValue(0);
        songLengthSec.setMaxValue(60);
        songLengthSec.setValue(settings.getInt("MIN_SIZE",0) % 60);
        songLengthMin.setValue(settings.getInt("MIN_SIZE",120) / 60);

        colorWheel.setColor(Color.parseColor(settings.getString("THEME_COLOR","#FFFFFF")), false);
        colorWheelText.setText(settings.getString("THEME_COLOR","#FFFFFF"));

        colorWheel.subscribe((color, fromUser, shouldPropagate) -> {
            if (fromUser) {
                colorWheelText.setText(String.format("#%06X", (0xFFFFFF & color)));
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("THEME_COLOR", String.format("#%06X", (0xFFFFFF & color)));
                editor.apply();
                setTextColors(String.format("#%06X", (0xFFFFFF & color)));
            }
        });

        colorWheelText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    colorWheel.setColor(Color.parseColor(String.valueOf(charSequence)), false);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("THEME_COLOR", String.valueOf(charSequence));
                    editor.apply();
                    setTextColors(String.valueOf(charSequence));
                }
                catch (Exception ignored) {}
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        songLengthMin.setOnValueChangedListener((numberPicker12, i, i1) -> {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("MIN_SIZE", i1*60+songLengthSec.getValue());
            editor.apply();
        });

        songLengthSec.setOnValueChangedListener((numberPicker1, i, i1) -> {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("MIN_SIZE", songLengthMin.getValue()*60+i1);
            editor.apply();
        });

        numberPicker.setOnValueChangedListener((numberPicker13, i, i1) -> {
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("LOOPING_SIZE", i1);
            editor.apply();
        });

        backgroundSetter.setOnClickListener(view -> showFileChooser());

        songsFolder.setText(settings.getString("SONG_FOLDER",""));

        songsFolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("SONG_FOLDER", String.valueOf(charSequence));
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        MainActivity.currentFragment = this;
        return ll;
    }

    public void setTextColors(String color) {
        int textColor = Color.parseColor(color);
        themeText.setTextColor(textColor);
        loopingText.setTextColor(textColor);
        lengthText.setTextColor(textColor);
        folderText.setTextColor(textColor);
        backgroundText.setTextColor(textColor);
        colorWheelText.setTextColor(textColor);
        songsFolder.setTextColor(textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            songLengthSec.setTextColor(textColor);
            songLengthMin.setTextColor(textColor);
            numberPicker.setTextColor(textColor);
        }
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == -1) {
                Uri uri = data.getData();
                try {
                    copy(Environment.getExternalStorageDirectory() + "/" + uri.getPath().split(":")[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void copy(String source) throws IOException {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            src = new FileInputStream(source).getChannel();
            dst = new FileOutputStream(destination).getChannel();
            dst.transferFrom(src, 0, src.size());
        } finally {
            if (src != null) {
                src.close();
            }
            if (dst != null) {
                dst.close();
            }
        }
        SettingsFragment newFragment = new SettingsFragment(main);
        FragmentTransaction transaction =  getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);

        transaction.commit();
    }
}
