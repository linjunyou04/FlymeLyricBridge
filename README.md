# FlymeLyricBridge

将歌词桥接到 Flyme 状态栏的完整解决方案。

## 包含组件

- **主 App**: FlymeLyricBridge (显示当前歌词/状态/来源，发布 ticker)
- **LSPosed 模块**: 注入播放器进程抓取歌词 / 请求 lyricon

## 快速安装

### 1. 构建 APK

在仓库根运行（若尚未存在 gradle wrapper）：
```bash
scripts/fix-wrapper-and-push.sh
```

构建两个 APK：
```bash
./gradlew :app:assembleDebug :modules:lsposed-hook:app:assembleDebug
```

### 2. 安装

```bash
# 主 App
adb install -r app/build/outputs/apk/debug/app-debug.apk

# LSPosed module
adb install -r modules/lsposed-hook/app/build/outputs/apk/debug/app-debug.apk
```

### 3. 配置

1. 在设备上安装/启用 LSPosed
2. 启用该模块并只勾选目标播放器包（如 com.netease.cloudmusic）
3. 重启播放器
4. 若使用 `lyricon` 外部 App：安装 proify/lyricon APK（确保包名匹配），模块会向其请求歌词

## 权限

- 授予「通知访问」用于读取通知 / MediaSession（主 App）
- （可选）授予辅助功能以作为后备抓取

## 日志

日志位于： `Android/data/com.example.flymelyric/files/logs/lyric_log.txt`

## 支持的播放器（LSPosed 模块白名单）

- 网易云音乐 (com.netease.cloudmusic)
- QQ音乐 (com.tencent.qqmusic)
- Spotify (com.spotify.music)

可在 `HookEntry.java` 中添加更多包名。

## 项目结构

```
FlymeLyricBridge/
├── app/                          # 主 App
│   ├── src/main/java/...
│   └── build.gradle
├── modules/
│   └── lsposed-hook/             # LSPosed 模块
│       ├── app/
│       │   ├── src/main/java/com/example/lsposedhook/HookEntry.java
│       │   └── src/main/assets/xposed_init
│       ├── build.gradle
│       └── settings.gradle
├── scripts/
│   └── fix-wrapper-and-push.sh   # 自动生成 gradle wrapper
├── .github/workflows/
│   └── android.yml               # GitHub Actions 自动构建
├── build.gradle
├── settings.gradle
└── README.md
```

## GitHub Actions 自动构建

每次 push 到 main 分支，GitHub Actions 会自动构建两个 APK：
- `lyricbridge-apk`: 主 App
- `lsposed-module-apk`: LSPosed 模块

## 许可证

MIT License
