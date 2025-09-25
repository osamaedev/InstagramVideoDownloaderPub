package com.instagram.video.downloader.mediaplayer;


/**
 * This interface is used only for some states to avoid
 * the issue of the player being focused after displaying
 * the video, it's the replacement of v out event in vlc.
 */
public interface CustomPlayerState {
    void onStateChange(int state);
}
