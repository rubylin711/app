package com.prime.aosp.media.launcher;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.prime.mediasignencryption.SignatureAlgorithm;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


@RequiresApi(api = Build.VERSION_CODES.R)
public class MediaHelper
{
    private static final String TAG = MediaHelper.class.getSimpleName();

    private WeakReference<Context> m_ref;

    public MediaHelper(Context context) {
        m_ref = new WeakReference<>(context);
    }

    /**
     * AndroidManifest.xml
     * <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" tools:ignore="ScopedStorage" />
     * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
     * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
     * <application
     *      ...
     *      android:requestLegacyExternalStorage="true" >
     */
    public List<MediaFile> getMediaFiles() {
        Context context = m_ref.get();
        StorageManager manager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> storageVolumes = manager.getStorageVolumes();
        List<MediaFile> mediaList = new ArrayList<>();

        for (StorageVolume volume : storageVolumes) {
            // USB
            if (volume.isRemovable()) {
                FileFilter filter = File::isDirectory;
                File usb = volume.getDirectory();
                if (usb == null)
                    break;
                File[] folderList = usb.listFiles(filter);
                assert folderList != null;
                // Folder
                for (File folder : folderList) {
                    //Log.d(TAG, "readFiles: folder = " + folder.getName());
                    // check folder name
                    if (Launcher.FOLDER_AUTO_PLAY.equals(folder.getName())) {
                        File[] fileArray = folder.listFiles();
                        assert fileArray != null;
                        // check all file
                        for (File file : fileArray) {
                            // add media
                            MediaFile mediaFile = new MediaFile(file.getPath());
                            if (mediaFile.isVideo() || mediaFile.isImage()) {
                                Log.d(TAG, "readFiles: mediaFile = " + mediaFile.getName());
                                mediaList.add(mediaFile);
                            }
                        }
                        return mediaList;
                    }
                }
            }
        }
        return mediaList;
    }

    public List<MediaFile> getVerifiedList(List<MediaFile> mediaList) {
        boolean verified = false;
        int index = 0;
        String name = "";

        for (MediaFile file : mediaList) {
            index = mediaList.indexOf(file);
            name = file.getName();

            verified = SignatureAlgorithm.isVerifies(file.getPath());
            Log.d(TAG, "getVerifiedList: [" + index + "] " + name + ", verified = " + verified);

            file.validation(verified);
        }
        return mediaList;
    }

    public String[] getPlayArray(List<MediaFile> verifiedList) {
        List<String> playList = null;

        playList = new ArrayList<>();
        for (MediaFile file : verifiedList) {
            if (file.isVerified()) {
                //Log.d(TAG, "getPlayArray: path = " + file.getPath());
                playList.add(file.getPath());
            }
        }
        return playList.toArray(new String[0]);
    }

    public String[] getPlayArray() {
        List<MediaFile> mediaList = null;
        List<MediaFile> verifiedList = null;
        String[] playArray = null;

        // get media list
        mediaList = getMediaFiles();

        // verify all files
        verifiedList = getVerifiedList(mediaList);

        // init play list
        playArray = getPlayArray(verifiedList);

        return playArray;
    }
}
