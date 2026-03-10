package com.example.flymelyric;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Main UI - shows current lyric, status, source app, and buttons.
 */
public class MainActivity extends AppCompatActivity {

    TextView tvApp, tvLyric, tvStatus, tvSource;
    Button btnNotif, btnA11y, btnExport, btnSettings;
    BroadcastReceiver uiReceiver;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);
        LogManager.init(this);

        tvApp = findViewById(R.id.tv_app);
        tvLyric = findViewById(R.id.tv_lyric);
        tvStatus = findViewById(R.id.tv_status);
        tvSource = findViewById(R.id.tv_source);

        btnNotif = findViewById(R.id.btn_open_notif);
        btnA11y = findViewById(R.id.btn_open_a11y);
        btnExport = findViewById(R.id.btn_export_log);
        btnSettings = findViewById(R.id.btn_settings);

        btnNotif.setOnClickListener(v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
        btnA11y.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        btnExport.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        uiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String lyric = intent.getStringExtra("lyric");
                String pkg = intent.getStringExtra("src_pkg");
                String title = intent.getStringExtra("title");
                if (lyric != null) {
                    tvLyric.setText(lyric);
                    tvStatus.setText("歌词已获取 ✔");
                } else {
                    tvLyric.setText("尚未获取歌词");
                    tvStatus.setText("等待中...");
                }
                if (pkg != null) {
                    tvApp.setText(pkg);
                } else if (title != null) {
                    tvApp.setText(title);
                }
                String src = intent.getStringExtra("source");
                if (src != null) tvSource.setText(src);
                else tvSource.setText("未知");
            }
        };

        registerReceiver(uiReceiver, new IntentFilter("com.example.flymelyric.UI_UPDATE"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(uiReceiver); } catch (Throwable ignored) {}
    }
}
