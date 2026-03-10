package com.example.flymelyric;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button btnSettings;
    Button btnOpenNotif;
    Button btnOpenA11y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogManager.init(this);

        btnSettings = findViewById(R.id.btn_settings);
        btnOpenNotif = findViewById(R.id.btn_open_notif);
        btnOpenA11y = findViewById(R.id.btn_open_a11y);

        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // 打开通知访问设置页，用户手动授予 Notification Listener 权限
        btnOpenNotif.setOnClickListener(v -> startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));

        // 打开辅助功能设置页，用户手动开启我们的 Accessibility 服务
        btnOpenA11y.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
    }
}
