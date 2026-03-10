package com.example.flymelyric.service;

import android.content.ComponentName;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.flymelyric.LogManager;

import java.util.List;

public class LyricNotificationListener extends NotificationListenerService {
    private MediaSessionManager mediaSessionManager;
    private ComponentName thisComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        thisComponent = new ComponentName(this, LyricNotificationListener.class);
        try {
            mediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
            registerMediaControllers();
            LogManager.log("NotificationListener created");
        } catch (Exception e) {
            LogManager.log("NotificationListener create error: " + e.toString());
        }
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
            // 尝试从通知 extras 中提取歌词
            CharSequence text = sbn.getNotification().extras.getCharSequence("android.text");
            if (text == null) text = sbn.getNotification().extras.getCharSequence("android.subText");
            if (text != null) {
                String s = text.toString();
                if (looksLikeLyric(s)) {
                    LogManager.log("Notification lyric: " + s);
                    LyricPosterService.updateLyric(getApplicationContext(), s);
                }
            }
        } catch (Throwable t) {
            LogManager.log("onNotificationPosted error: " + t.toString());
        }
    }

    private void registerMediaControllers() {
        try {
            if (mediaSessionManager == null) return;
            List<MediaController> controllers = mediaSessionManager.getActiveSessions(thisComponent);
            if (controllers == null) return;

            for (MediaController controller : controllers) {
                try {
                    MediaMetadata meta = controller.getMetadata();
                    String lyric = extractLyricFromMetadata(meta);
                    if (lyric != null) deliverLyric(lyric);

                    controller.registerCallback(new MediaController.Callback() {
                        @Override
                        public void onMetadataChanged(MediaMetadata metadata) {
                            try {
                                String l = extractLyricFromMetadata(metadata);
                                if (l != null) {
                                    LogManager.log("MediaSession callback lyric: " + l);
                                    deliverLyric(l);
                                }
                            } catch (Throwable ignored) {}
                        }
                    });
                } catch (Throwable ignoreInner) {}
            }

            // 优先扫描可能来自蓝牙/车机的 controllers（简单 heuristics）
            for (MediaController c : controllers) {
                try {
                    String pkg = c.getPackageName();
                    if (pkg != null && pkg.toLowerCase().contains("bluetooth")) {
                        MediaMetadata m = c.getMetadata();
                        String l = extractLyricFromMetadata(m);
                        if (l != null) deliverLyric(l);
                    }
                } catch (Throwable ignore) {}
            }
        } catch (SecurityException se) {
            LogManager.log("No permission to get active sessions");
        } catch (Throwable t) {
            LogManager.log("registerMediaControllers exception: " + t.toString());
        }
    }

    private String extractLyricFromMetadata(MediaMetadata meta) {
        if (meta == null) return null;
        try {
            // 常见 key 名称尝试
            CharSequence ly = meta.getText("android.media.metadata.LYRICS");
            if (ly != null) return ly.toString();

            // 一些播放器会把歌词放在 extras 或 title/subtitle
            ly = meta.getText(MediaMetadata.METADATA_KEY_TITLE);
            if (ly != null && looksLikeLyric(ly.toString())) return ly.toString();

            ly = meta.getText(MediaMetadata.METADATA_KEY_ALBUM);
            if (ly != null && looksLikeLyric(ly.toString())) return ly.toString();
        } catch (Throwable ignored) {}
        return null;
    }

    private void deliverLyric(String lyric) {
        if (lyric == null || lyric.trim().isEmpty()) return;
        LyricPosterService.updateLyric(getApplicationContext(), lyric);
    }

    private boolean looksLikeLyric(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.length() < 2 || s.length() > 400) return false;
        // 简单规则：不可为纯数字或时间戳
        if (s.matches("^[0-9:]+$")) return false;
        return s.matches(".*[\\p{L}，。,.?!'\"].*");
    }
}
