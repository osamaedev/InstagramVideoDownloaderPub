package com.instagram.video.downloader.mediaplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    private Context mAppContext;
    private SharedPreferences mSharedPreferences;

    public static final int PV_PLAYER__Auto = 0;
    public static final int PV_PLAYER__AndroidMediaPlayer = 1;
    public static final int PV_PLAYER__IjkMediaPlayer = 2;
    public static final int PV_PLAYER__IjkExoMediaPlayer = 3;

    public Settings(Context context) {
        mAppContext = context.getApplicationContext();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mAppContext);
    }

    public boolean getEnableBackgroundPlay() {
        String key = "pref_key_enable_background_play";
        return mSharedPreferences.getBoolean(key, false);
    }

    public int getPlayer() {
        String key = "pref_key_player";
        String value = mSharedPreferences.getString(key, "");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean getUsingMediaCodec() {
//        String key = "pref_key_using_media_codec";
//        return mSharedPreferences.getBoolean(key, false);
        return true;
    }

    public boolean getUsingMediaCodecAutoRotate() {
//        String key = "pref_key_using_media_codec_auto_rotate";
//        return mSharedPreferences.getBoolean(key, false);
        return true;
    }

    public boolean getMediaCodecHandleResolutionChange() {
//        String key = "pref_key_media_codec_handle_resolution_change";
//        return mSharedPreferences.getBoolean(key, false);
        return true;
    }

    public boolean getUsingOpenSLES() {
//        String key = "pref_key_using_opensl_es";
//        return mSharedPreferences.getBoolean(key, false);
        return true;
    }

    public String getPixelFormat() {
        String key = "pref_key_pixel_format";
        return mSharedPreferences.getString(key, "");
    }

    public boolean getEnableNoView() {
        String key = "pref_key_enable_no_view";
        return mSharedPreferences.getBoolean(key, false);
    }

    public boolean getEnableSurfaceView() {
//        String key = "pref_key_enable_surface_view";
//        return mSharedPreferences.getBoolean(key, false);
        return true;
    }

    public boolean getEnableTextureView() {
//        String key = "pref_key_enable_texture_view";
//        return mSharedPreferences.getBoolean(key, false);
        return true;
    }

    public boolean getEnableDetachedSurfaceTextureView() {
//        String key = "pref_key_enable_detached_surface_texture";
//        return mSharedPreferences.getBoolean(key, false);
        return true;
    }

    public boolean getUsingMediaDataSource() {
        String key = "pref_key_using_mediadatasource";
        return mSharedPreferences.getBoolean(key, false);
    }

    public String getLastDirectory() {
        String key = "pref_key_last_directory";
        return mSharedPreferences.getString(key, "/");
    }

    public void setLastDirectory(String path) {
        String key = "pref_key_last_directory";
        mSharedPreferences.edit().putString(key, path).apply();
    }
}
