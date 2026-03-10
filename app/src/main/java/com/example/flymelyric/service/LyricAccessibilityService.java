package com.example.flymelyric.service;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.text.TextUtils;
import android.util.Log;

import com.example.flymelyric.LogManager;

import java.util.ArrayDeque;
import java.util.Deque;

public class LyricAccessibilityService extends AccessibilityService {
    private static final String TAG = "LyricA11ySvc";
    private long lastSent = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            final int type = event.getEventType();
            if (type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
                type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                type != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                return;
            }

            AccessibilityNodeInfo root = getRootInActiveWindow();
            if (root == null) return;

            String found = findLikelyLyricText(root);
            if (!TextUtils.isEmpty(found)) {
                long now = System.currentTimeMillis();
                if (now - lastSent > 600 && looksLikeLyric(found)) {
                    lastSent = now;
                    LogManager.log("Accessibility 捕获歌词: " + truncate(found, 200));
                    LyricPosterService.updateLyric(getApplicationContext(), found);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "onAccessibilityEvent error", t);
            LogManager.log("Accessibility error: " + t.toString());
        }
    }

    @Override
    public void onInterrupt() {
    }

    private String findLikelyLyricText(AccessibilityNodeInfo root) {
        if (root == null) return null;
        Deque<AccessibilityNodeInfo> dq = new ArrayDeque<>();
        dq.add(root);
        int nodes = 0;
        while (!dq.isEmpty() && nodes < 2000) {
            AccessibilityNodeInfo n = dq.pollFirst();
            if (n == null) continue;
            CharSequence text = n.getText();
            if (!TextUtils.isEmpty(text)) {
                String s = text.toString().trim();
                if (looksLikeLyric(s)) {
                    return s;
                }
            }
            for (int i = 0; i < n.getChildCount(); i++) {
                AccessibilityNodeInfo c = n.getChild(i);
                if (c != null) dq.addLast(c);
            }
            nodes++;
        }
        return null;
    }

    private boolean looksLikeLyric(String s) {
        if (TextUtils.isEmpty(s)) return false;
        int len = s.length();
        if (len < 2 || len > 400) return false;
        if (s.matches("^[0-9:]+$")) return false;
        return s.matches(".*[\\p{L}，。,.?!'\"].*");
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
