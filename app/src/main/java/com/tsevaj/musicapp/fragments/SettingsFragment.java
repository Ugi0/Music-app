package com.tsevaj.musicapp.fragments;

import static androidx.core.content.ContextCompat.getColorStateList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import top.defaults.colorpicker.ColorWheelView;

public class SettingsFragment extends Fragment {

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

    MainActivity main;

    public SettingsFragment(MainActivity main) {
        this.main = main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ll = inflater.inflate(R.layout.settings_fragment, container, false);

        main.setBackground(ll, getResources());

        SharedPreferences settings = requireActivity().getSharedPreferences("SAVEDATA", 0);

        colorWheel = ll.findViewById(R.id.color_wheel);
        colorWheelText = ll.findViewById(R.id.settings_color_text);
        backgroundSetter = ll.findViewById(R.id.settings_background);
        songsFolder = ll.findViewById(R.id.settings_songs_folder_text);
        songLengthMin = ll.findViewById(R.id.numpicker_minutes);
        songLengthSec = ll.findViewById(R.id.numpicker_seconds);

        saveStateText = ll.findViewById(R.id.state_text);
        saveStateButton = ll.findViewById(R.id.save_state_button);

        darkModeText = ll.findViewById(R.id.dark_mode_text);
        darkModeButton = ll.findViewById(R.id.dark_mode_button);

        themeText = ll.findViewById(R.id.Theme_text);
        lengthText = ll.findViewById(R.id.min_length_text);
        folderText = ll.findViewById(R.id.folder_text);
        backgroundText = ll.findViewById(R.id.background_text);

        boolean saveState = requireContext().getSharedPreferences("SAVEDATA", 0).getBoolean("SAVE_STATE", false);
        saveStateButton.setChecked(saveState);

        boolean darkMode = requireContext().getSharedPreferences("SAVEDATA", 0).getBoolean("DARK_MODE", false);
        darkModeButton.setChecked(darkMode);

        setTextColors(requireContext().getSharedPreferences("SAVEDATA", 0).getString("THEME_COLOR", "#FFFFFF"));
        main.setClickable();

        songLengthMin.setMinValue(0);
        songLengthMin.setMaxValue(60);
        songLengthSec.setMinValue(0);
        songLengthSec.setMaxValue(60);
        songLengthSec.setValue(settings.getInt("MIN_SIZE",0) % 60);
        songLengthMin.setValue(settings.getInt("MIN_SIZE",60) / 60);

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

        saveStateButton.setOnClickListener(view -> {
            boolean saveState1 = requireContext().getSharedPreferences("SAVEDATA", 0).getBoolean("SAVE_STATE", false);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("SAVE_STATE", !saveState1);
            editor.apply();
        });

        darkModeButton.setOnClickListener(view -> {
            boolean darkMode1 = requireContext().getSharedPreferences("SAVEDATA", 0).getBoolean("DARK_MODE", false);
            SharedPreferences.Editor editor = settings.edit();
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
        lengthText.setTextColor(textColor);
        folderText.setTextColor(textColor);
        backgroundText.setTextColor(textColor);
        colorWheelText.setTextColor(textColor);
        songsFolder.setTextColor(textColor);
        saveStateText.setTextColor(textColor);
        darkModeText.setTextColor(textColor);
        saveStateButton.setTextColor(textColor);

        songLengthSec.setTextColor(textColor);
        songLengthMin.setTextColor(textColor);
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            fileUploadResultLauncher.launch(Intent.createChooser(intent, "Select a File to Upload"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "Please install a File Manager.",
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
                        copy(source,new File(main.BackgroundDestinationPath.getPath()+"/background"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                SettingsFragment newFragment = new SettingsFragment(main);
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
