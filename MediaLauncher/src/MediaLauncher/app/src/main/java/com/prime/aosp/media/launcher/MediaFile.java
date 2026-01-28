package com.prime.aosp.media.launcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.net.URI;
import java.net.URLConnection;

public class MediaFile extends File {

    public static final int MINE_TYPE_NONE = -1;
    public static final int MINE_TYPE_IMAGE = 0;
    public static final int MINE_TYPE_AUDIO = 1;
    public static final int MINE_TYPE_VIDEO = 2;

    private boolean m_validation = false;
    private int m_mineType = MINE_TYPE_NONE;

    public MediaFile(@NonNull String pathname) {
        super(pathname);
        setMineType(new File(pathname));
    }

    public MediaFile(@Nullable String parent, @NonNull String child) {
        super(parent, child);
    }

    public MediaFile(@Nullable File parent, @NonNull String child) {
        super(parent, child);
    }

    public MediaFile(@NonNull URI uri) {
        super(uri);
    }

    public void validation(boolean validation) {
        m_validation = validation;
    }

    public boolean isVerified() {
        return m_validation;
    }

    public void setMimeType(int mineType) {
        m_mineType = mineType;
    }

    public boolean isVideo() {
        return (MINE_TYPE_VIDEO == m_mineType);
    }

    public boolean isAudio() {
        return (MINE_TYPE_AUDIO == m_mineType);
    }

    public boolean isImage() {
        return (MINE_TYPE_IMAGE == m_mineType);
    }

    public void setMineType(File file) {
        String mineType = URLConnection.guessContentTypeFromName(file.getPath());
        boolean isImage = mineType != null && mineType.startsWith("image");
        boolean isAudio = mineType != null && mineType.startsWith("audio");
        boolean isVideo = mineType != null && mineType.startsWith("video");
        if (isImage)
            m_mineType = MINE_TYPE_IMAGE;
        else if (isAudio)
            m_mineType = MINE_TYPE_AUDIO;
        else if (isVideo)
            m_mineType = MINE_TYPE_VIDEO;
        else
            m_mineType = MINE_TYPE_NONE;
    }

    public int getMineType() {
        return m_mineType;
    }
}
