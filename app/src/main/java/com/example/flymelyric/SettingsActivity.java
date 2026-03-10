package com.example.flymelyric;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    Button exportLog;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_settings);
        exportLog = findViewById(R.id.exportLog);
        exportLog.setOnClickListener(v -> {
            try {
                File f = LogManager.getLogFile();
                if (f == null || !f.exists()) return;
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                startActivity(Intent.createChooser(i, "导出日志"));
            } catch (Exception ignored) {}
        });
    }
}
