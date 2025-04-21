package com.tsevaj.musicapp.utils.data;

import com.tsevaj.musicapp.utils.enums.MusicListType;
import com.tsevaj.musicapp.utils.enums.SortOption;

import lombok.Getter;

@Getter
public class SortValue {
    private final MusicListType sortOption;
    private final String data;

    public SortValue(MusicListType sortOption) {
        this(sortOption, "");
    }

    public SortValue(MusicListType sortOption, String data) {
        this.sortOption = sortOption;
        this.data = data;
    }

}
