package com.example.flymelyric;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LyricReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String a = intent.getAction();
        if (a == null) return;
        if ("com.example.flymelyric.ACTION_LYRIC".equals(a) || "com.proify.lyricon.ACTION_RESPONSE_LYRICS".equals(a)) {
            String lyric = intent.getStringExtra("lyric");
            if (lyric != null && !lyric.trim().isEmpty()) {
                com.example.flymelyric.service.LyricPosterService.updateLyric(context, lyric);
                com.example.flymelyric.LogManager.log("Receiver got lyric: " + lyric);
                // Also send local broadcast to update UI
                Intent ui = new Intent("com.example.flymelyric.UI_UPDATE");
                ui.putExtra("lyric", lyric);
                ui.putExtra("src_pkg", intent.getStringExtra("src_pkg"));
                ui.putExtra("title", intent.getStringExtra("title"));
                ui.putExtra("artist", intent.getStringExtra("artist"));
                context.sendBroadcast(ui);
            }
        }
    }
}
