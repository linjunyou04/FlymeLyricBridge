package com.example.flymelyric.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.flymelyric.LogManager;

public class LyricPosterService {
    private static final String CHANNEL_ID = "lyric_channel_v1";
    private static final int NOTIFY_ID = 1001;

    private static void ensureChannel(Context ctx) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Lyric Ticker", NotificationManager.IMPORTANCE_LOW);
                ch.setDescription("Ticker notifications used to bridge lyrics to Flyme status bar");
                nm.createNotificationChannel(ch);
            }
        } catch (Exception ignored) {}
    }

    public static void updateLyric(Context context, String lyric) {
        if (context == null || lyric == null) return;
        try {
            ensureChannel(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("♪ Lyric")
                    .setContentText(lyric)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setTicker(lyric);
            Notification n = builder.build();

            // 兼容旧版：尝试设置 tickerText 字段
            try {
                n.tickerText = lyric;
            } catch (Throwable ignored) {}

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFY_ID, n);
            LogManager.log("推送ticker: " + lyric);
        } catch (Throwable t) {
            LogManager.log("LyricPoster error: " + t.toString());
        }
    }
}
