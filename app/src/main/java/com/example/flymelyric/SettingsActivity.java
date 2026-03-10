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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        exportLog = findViewById(R.id.exportLog);
        exportLog.setOnClickListener(v -> export());
    }

    private void export() {
        try {
            File file = LogManager.getLogFile();
            if (file == null || !file.exists()) return;
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            startActivity(Intent.createChooser(intent, "导出日志"));
        } catch (Exception ignored) {}
    }
}
