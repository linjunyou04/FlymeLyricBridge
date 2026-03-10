package com.example.flymelyric;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private static File logFile;
    public static void init(Context ctx) {
        try {
            File dir = new File(ctx.getExternalFilesDir(null), "logs");
            if (!dir.exists()) dir.mkdirs();
            logFile = new File(dir, "lyric_log.txt");
        } catch (Exception ignored) {}
    }
    public static void log(String text) {
        try {
            if (logFile == null) return;
            FileWriter fw = new FileWriter(logFile, true);
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            fw.write(ts + " " + text + "\n");
            fw.close();
        } catch (Exception ignored) {}
    }
    public static File getLogFile() { return logFile; }
}
