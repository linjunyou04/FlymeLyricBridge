package com.example.flymelyric.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.flymelyric.LogManager;

import java.util.List;

@SuppressLint("OverrideAbstract")
public class LyricNotificationListener extends NotificationListenerService {

    private MediaSessionManager mediaSessionManager;
    private ComponentName thisComp;

    @Override
    public void onCreate() {
        super.onCreate();
        thisComp = new ComponentName(this, LyricNotificationListener.class);
        mediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
        registerMediaControllers();
        LogManager.log("NotificationListener created");
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        registerMediaControllers();
        LogManager.log("Listener connected");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            Notification n = sbn.getNotification();
            if (n == null || n.extras == null) return;
            CharSequence text = n.extras.getCharSequence("android.text");
            CharSequence big = n.extras.getCharSequence("android.bigText");
            String candidate = (big != null ? big.toString() : (text != null ? text.toString() : null));
            if (candidate != null && candidate.length() > 1 && candidate.length() < 400) {
                if (!candidate.matches("^[0-9: ]+$")) {
                    // send to UI & poster
                    Intent ui = new Intent("com.example.flymelyric.UI_UPDATE");
                    ui.putExtra("lyric", candidate);
                    ui.putExtra("src_pkg", sbn.getPackageName());
                    sendBroadcast(ui);
                    LyricPosterService.updateLyric(getApplicationContext(), candidate);
                    LogManager.log("Notification lyric: " + candidate);
                }
            }
        } catch (Throwable t) {
            LogManager.log("onNotificationPosted error: " + t.toString());
        }
    }

    private void registerMediaControllers() {
        try {
            if (mediaSessionManager == null) return;
            List<MediaController> list = mediaSessionManager.getActiveSessions(thisComp);
            if (list == null) return;
            for (MediaController c : list) {
                try {
                    MediaMetadata md = c.getMetadata();
                    String lyric = extractLyricFromMetadata(md);
                    if (lyric != null) {
                        Intent ui = new Intent("com.example.flymelyric.UI_UPDATE");
                        ui.putExtra("lyric", lyric);
                        ui.putExtra("src_pkg", c.getPackageName());
                        sendBroadcast(ui);
                        LyricPosterService.updateLyric(getApplicationContext(), lyric);
                        LogManager.log("MediaSession lyric: " + lyric);
                    }
                    c.registerCallback(new MediaController.Callback() {
                        @Override
                        public void onMetadataChanged(MediaMetadata metadata) {
                            try {
                                String l = extractLyricFromMetadata(metadata);
                                if (l != null) {
                                    Intent ui = new Intent("com.example.flymelyric.UI_UPDATE");
                                    ui.putExtra("lyric", l);
                                    ui.putExtra("src_pkg", c.getPackageName());
                                    sendBroadcast(ui);
                                    LyricPosterService.updateLyric(getApplicationContext(), l);
                                    LogManager.log("MediaSession cb lyric: " + l);
                                }
                            } catch (Throwable ignored) {}
                        }
                    });
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            LogManager.log("registerMediaControllers err: " + t.toString());
        }
    }

    private String extractLyricFromMetadata(MediaMetadata md) {
        if (md == null) return null;
        try {
            CharSequence ly = md.getText("android.media.metadata.LYRICS");
            if (ly != null) return ly.toString();
            ly = md.getText(MediaMetadata.METADATA_KEY_TITLE);
            if (ly != null && looksLikeLyric(ly.toString())) return ly.toString();
        } catch (Throwable ignored) {}
        return null;
    }

    private boolean looksLikeLyric(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.length() < 2 || s.length() > 400) return false;
        if (s.matches("^[0-9: ]+$")) return false;
        return s.matches(".*[\\p{L}，。,.?!'\"].*");
    }
}
