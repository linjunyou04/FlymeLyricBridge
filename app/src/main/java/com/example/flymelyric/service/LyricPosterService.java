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
                ch.setDescription("Ticker used for Flyme status bar lyrics");
                nm.createNotificationChannel(ch);
            }
        } catch (Exception ignored) {}
    }

    public static void updateLyric(Context ctx, String lyric) {
        if (ctx == null || lyric == null) return;
        try {
            ensureChannel(ctx);
            NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setContentTitle("♪ " + (lyric.length() <= 32 ? lyric : lyric.substring(0, 32) + "..."))
                    .setContentText(lyric)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .setTicker(lyric);

            Notification n = b.build();
            try { n.tickerText = lyric; } catch (Throwable ignored) {}
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFY_ID, n);
            LogManager.log("Posted ticker: " + lyric);
        } catch (Throwable t) {
            LogManager.log("LyricPoster error: " + t.toString());
        }
    }
}
