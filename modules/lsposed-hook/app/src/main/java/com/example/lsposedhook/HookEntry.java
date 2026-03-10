package com.example.lsposedhook;

import android.app.Notification;
import android.content.Intent;
import android.media.MediaMetadata;
import android.widget.TextView;
import android.content.Context;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * LSPosed Hook - whitelist-only
 */
public class HookEntry implements IXposedHookLoadPackage {

    private static final String[] WHITELIST = new String[] {
            "com.netease.cloudmusic",
            "com.tencent.qqmusic",
            "com.spotify.music"
    };

    private static final String ACTION_APP_LYRIC = "com.example.flymelyric.ACTION_LYRIC";
    private static final String ACTION_REQ = "com.proify.lyricon.ACTION_REQUEST_LYRICS";
    private static final String LYRICON_PKG = "com.proify.lyricon";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            final String pkg = lpparam.packageName;
            if (!isWhitelisted(pkg)) return;
            XposedBridge.log("LyricHook: injected into " + pkg);

            // Hook MediaSession.setMetadata
            try {
                Class<?> ms = XposedHelpers.findClassIfExists("android.media.session.MediaSession", lpparam.classLoader);
                if (ms != null) {
                    XposedHelpers.findAndHookMethod(ms, "setMetadata", MediaMetadata.class, new XC_MethodHook() {
                        @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                Object metadata = param.args[0];
                                if (metadata == null) return;
                                CharSequence lyricCs = null;
                                try { lyricCs = (CharSequence) XposedHelpers.callMethod(metadata, "getText", "android.media.metadata.LYRICS"); } catch(Throwable ignored){}
                                if (lyricCs != null && lyricCs.length() > 0) {
                                    sendLyric(getContext(), lyricCs.toString(), pkg, null, null);
                                    return;
                                }
                                CharSequence title = (CharSequence) XposedHelpers.callMethod(metadata, "getText", "android.media.metadata.TITLE");
                                CharSequence artist = (CharSequence) XposedHelpers.callMethod(metadata, "getText", "android.media.metadata.ARTIST");
                                if (title != null && title.length() > 0) {
                                    requestLyricon(getContext(), title.toString(), artist == null ? null : artist.toString(), pkg);
                                }
                            } catch(Throwable t) { XposedBridge.log("media hook err: "+t); }
                        }
                    });
                }
            } catch(Throwable t) { XposedBridge.log("hook media failed: "+t); }

            // Hook NotificationManager.notify
            try {
                Class<?> nm = XposedHelpers.findClassIfExists("android.app.NotificationManager", lpparam.classLoader);
                if (nm != null) {
                    XposedHelpers.findAndHookMethod(nm, "notify", int.class, Notification.class, new XC_MethodHook() {
                        @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                Notification n = (Notification) param.args[1];
                                if (n == null || n.extras == null) return;
                                CharSequence big = n.extras.getCharSequence("android.bigText");
                                CharSequence text = n.extras.getCharSequence("android.text");
                                String candidate = big != null ? big.toString() : (text != null ? text.toString() : null);
                                if (candidate != null && looksLikeLyric(candidate)) {
                                    sendLyric(getContext(), candidate, pkg, null, null);
                                }
                            } catch(Throwable ignored){}
                        }
                    });
                }
            } catch(Throwable t) { XposedBridge.log("hook notify failed: "+t); }

            // Hook TextView.setText to catch lyric UI in player
            try {
                Class<?> tv = XposedHelpers.findClassIfExists("android.widget.TextView", lpparam.classLoader);
                if (tv != null) {
                    XposedHelpers.findAndHookMethod(tv, "setText", CharSequence.class, new XC_MethodHook() {
                        @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                CharSequence cs = (CharSequence) param.args[0];
                                if (cs == null) return;
                                String s = cs.toString().trim();
                                if (s.length() < 2 || s.length() > 300) return;
                                if (!looksLikeLyric(s)) return;
                                StackTraceElement[] st = Thread.currentThread().getStackTrace();
                                boolean ok = false;
                                for (StackTraceElement e : st) {
                                    if (e == null) continue;
                                    String cn = e.getClassName().toLowerCase();
                                    if (cn.contains("lyric") || cn.contains("lrc") || cn.contains("subtitle") || cn.contains("karaoke")) { ok = true; break; }
                                }
                                if (!ok) return;
                                sendLyric(getContext(), s, pkg, null, null);
                            } catch(Throwable ignored){}
                        }
                    });
                }
            } catch(Throwable t) { XposedBridge.log("hook text failed: "+t); }

        } catch(Throwable t) {
            XposedBridge.log("LyricHook error: " + t);
        }
    }

    private boolean isWhitelisted(String pkg) {
        if (pkg == null) return false;
        for (String p : WHITELIST) if (pkg.equals(p)) return true;
        return false;
    }

    private boolean looksLikeLyric(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.length() < 2 || s.length() > 400) return false;
        if (s.matches("^[0-9: ]+$")) return false;
        return s.matches(".*[\\p{L}，。,.?!'\"].*");
    }

    private void sendLyric(Context ctx, String lyric, String pkg, String title, String artist) {
        try {
            if (ctx == null || lyric == null) return;
            Intent it = new Intent(ACTION_APP_LYRIC);
            it.putExtra("lyric", lyric);
            it.putExtra("src_pkg", pkg);
            if (title != null) it.putExtra("title", title);
            if (artist != null) it.putExtra("artist", artist);
            ctx.sendBroadcast(it);
            XposedBridge.log("LyricHook: sent lyric from " + pkg);
        } catch(Throwable t) { XposedBridge.log("sendLyric err: " + t); }
    }

    private void requestLyricon(Context ctx, String title, String artist, String srcPkg) {
        try {
            if (ctx == null || title == null) return;
            Intent it = new Intent(ACTION_REQ);
            it.setPackage(LYRICON_PKG);
            it.putExtra("title", title);
            it.putExtra("artist", artist);
            it.putExtra("src_pkg", srcPkg);
            ctx.sendBroadcast(it);
            XposedBridge.log("LyricHook: requested lyricon for " + title);
        } catch(Throwable t) { XposedBridge.log("requestLyricon err: " + t); }
    }

    private Context getContext() {
        try {
            Class<?> at = Class.forName("android.app.ActivityThread");
            Object app = at.getDeclaredMethod("currentApplication").invoke(null);
            if (app instanceof Context) return (Context) app;
        } catch(Throwable ignored) {}
        return null;
    }
}
