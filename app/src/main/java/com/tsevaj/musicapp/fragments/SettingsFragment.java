package com.tsevaj.musicapp.fragments;

import static com.tsevaj.musicapp.utils.ApplicationConfig.BackgroundDestinationPath;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.fragments.uielements.MusicFragment;
import com.tsevaj.musicapp.utils.ApplicationConfig;
import com.tsevaj.musicapp.utils.SharedPreferencesHandler;
import com.tsevaj.musicapp.utils.files.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import top.defaults.colorpicker.ColorWheelView;

public class SettingsFragment extends MusicFragment {

    ColorWheelView colorWheel;
    EditText colorWheelText;
    CardView backgroundSetter;
    EditText songsFolder;
    NumberPicker songLengthMin;
    NumberPicker songLengthSec;
    TextView themeText;
    TextView lengthText;
    TextView folderText;
    TextView backgroundText;

    TextView saveStateText;
    CheckBox saveStateButton;

    TextView darkModeText;
    CheckBox darkModeButton;

    public static ApplicationConfig config;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View baseView = super.onCreateView(inflater, container);
        view = inflater.inflate(R.layout.settings_fragment, contentContainer, false);
        contentContainer.addView(view);

        doLayout();

        return baseView;
    }

    private void doLayout() {

        colorWheel = view.findViewById(R.id.color_wheel);
        colorWheelText = view.findViewById(R.id.settings_color_text);
        backgroundSetter = view.findViewById(R.id.settings_background);
        songsFolder = view.findViewById(R.id.settings_songs_folder_text);
        songLengthMin = view.findViewById(R.id.numpicker_minutes);
        songLengthSec = view.findViewById(R.id.numpicker_seconds);

        saveStateText = view.findViewById(R.id.state_text);
        saveStateButton = view.findViewById(R.id.save_state_button);

        darkModeText = view.findViewById(R.id.dark_mode_text);
        darkModeButton = view.findViewById(R.id.dark_mode_button);

        themeText = view.findViewById(R.id.Theme_text);
        lengthText = view.findViewById(R.id.min_length_text);
        folderText = view.findViewById(R.id.folder_text);
        backgroundText = view.findViewById(R.id.background_text);

        boolean saveState = SharedPreferencesHandler.sharedPreferences.getBoolean("SAVE_STATE", false);
        saveStateButton.setChecked(saveState);

        boolean darkMode = SharedPreferencesHandler.sharedPreferences.getBoolean("DARK_MODE", false);
        darkModeButton.setChecked(darkMode);

        setTextColors(SharedPreferencesHandler.sharedPreferences.getString("THEME_COLOR", "#FFFFFF"));

        songLengthMin.setMinValue(0);
        songLengthMin.setMaxValue(60);
        songLengthSec.setMinValue(0);
        songLengthSec.setMaxValue(60);
        songLengthSec.setValue(SharedPreferencesHandler.sharedPreferences.getInt("MIN_SIZE",0) % 60);
        songLengthMin.setValue(SharedPreferencesHandler.sharedPreferences.getInt("MIN_SIZE",60) / 60);

        colorWheel.setColor(Color.parseColor(SharedPreferencesHandler.sharedPreferences.getString("THEME_COLOR","#FFFFFF")), false);
        colorWheelText.setText(SharedPreferencesHandler.sharedPreferences.getString("THEME_COLOR","#FFFFFF"));

        colorWheel.subscribe((color, fromUser, shouldPropagate) -> {
            if (fromUser) {
                colorWheelText.setText(String.format("#%06X", (0xFFFFFF & color)));
                SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
                editor.putString("THEME_COLOR", String.format("#%06X", (0xFFFFFF & color)));
                editor.apply();
                setTextColors(String.format("#%06X", (0xFFFFFF & color)));
            }
        });

        saveStateButton.setOnClickListener(view -> {
            boolean saveState1 = SharedPreferencesHandler.sharedPreferences.getBoolean("SAVE_STATE", false);
            SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
            editor.putBoolean("SAVE_STATE", !saveState1);
            editor.apply();
        });

        darkModeButton.setOnClickListener(view -> {
            boolean darkMode1 = SharedPreferencesHandler.sharedPreferences.getBoolean("DARK_MODE", false);
            SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
            editor.putBoolean("DARK_MODE", !darkMode1);
            editor.apply();
        });

        colorWheelText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    colorWheel.setColor(Color.parseColor(String.valueOf(charSequence)), false);
                    SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
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
            SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
            editor.putInt("MIN_SIZE", i1*60+songLengthSec.getValue());
            editor.apply();
        });

        songLengthSec.setOnValueChangedListener((numberPicker1, i, i1) -> {
            SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
            editor.putInt("MIN_SIZE", songLengthMin.getValue()*60+i1);
            editor.apply();
        });

        backgroundSetter.setOnClickListener(view -> showFileChooser());

        songsFolder.setText(SharedPreferencesHandler.sharedPreferences.getString("SONG_FOLDER",""));

        songsFolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                SharedPreferences.Editor editor = SharedPreferencesHandler.sharedPreferences.edit();
                editor.putString("SONG_FOLDER", String.valueOf(charSequence));
                editor.apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void setTextColors(String color) {
        int textColor = Color.parseColor(color);
        themeText.setTextColor(textColor);
        lengthText.setTextColor(textColor);
        folderText.setTextColor(textColor);
        backgroundText.setTextColor(textColor);
        colorWheelText.setTextColor(textColor);
        songsFolder.setTextColor(textColor);
        saveStateText.setTextColor(textColor);
        darkModeText.setTextColor(textColor);
        saveStateButton.setTextColor(textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            songLengthSec.setTextColor(textColor);
            songLengthMin.setTextColor(textColor);
        }
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            fileUploadResultLauncher.launch(Intent.createChooser(intent, "Select a File to Upload"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(main.getApplication(), "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    ActivityResultLauncher<Intent> fileUploadResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    assert data != null;
                    Uri uri = data.getData();
                    try {
                        String sourcePath = Objects.requireNonNull(FileUtils.getPath(requireContext(), uri));
                        File source = new File(sourcePath);
                        copy(source,new File(BackgroundDestinationPath.getPath()+"/background"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                SettingsFragment newFragment = new SettingsFragment();
                FragmentTransaction transaction =  getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, newFragment);

                transaction.commit();
            });

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}
