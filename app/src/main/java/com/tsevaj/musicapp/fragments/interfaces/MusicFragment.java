package com.tsevaj.musicapp.fragments.interfaces;

import static com.tsevaj.musicapp.utils.ApplicationConfig.BackgroundDestinationPath;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.tsevaj.musicapp.MainActivity;
import com.tsevaj.musicapp.R;
import com.tsevaj.musicapp.utils.data.MusicItem;

import java.io.File;
import java.util.List;
import java.util.Objects;

public abstract class MusicFragment extends Fragment {

    protected MainActivity main;
    protected View view;

    public MusicFragment(MainActivity main) {
        this.main = main;
    }

    protected void onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, int viewInt) {
        view = inflater.inflate(viewInt, container, false);
        setBackground(view, getResources());
        MainActivity.currentFragment = this;
    }

    public void setBackground(View view, Resources resources) {
        if (new File(BackgroundDestinationPath+"/background").exists()) {
            view.setBackground(new BitmapDrawable(resources, BitmapFactory.decodeFile(BackgroundDestinationPath+"/background")));
        }
        else {
            view.setBackground(ResourcesCompat.getDrawable(resources, R.drawable.background, null));
        }
    }
}
