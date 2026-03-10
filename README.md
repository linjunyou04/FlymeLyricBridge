# FlymeLyricBridge

将歌词桥接到 Flyme 状态栏的示例应用（使用 NotificationListenerService / MediaSessionManager / AccessibilityService）。

## 功能

- 抓取 MediaSession metadata、通知中的歌词
- 可选使用 AccessibilityService 从屏幕抓取歌词（适配视频/嵌入式播放器）
- 将歌词以含 `ticker` 的通知方式发布，Flyme 状态栏会将其显示为滚动歌词
- 日志记录与导出功能
- GitHub Actions 自动构建 Debug APK

## 如何使用（对小白友好）

1. Clone 仓库并 push 到你的 GitHub（或直接在 GitHub 上创建新仓库并上传所有文件）

2. 打开 `Actions` -> 选择 `Android CI`，第一次构建会触发。或本地编译：

```bash
./gradlew assembleDebug
```

生成的 APK 在 `app/build/outputs/apk/debug/app-debug.apk`。

3. 安装 APK 到手机：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

4. 在手机上打开应用，依次：
   - 点击 "打开通知访问设置" -> 在系统设置中允许本应用的通知访问（Notification access）
   - 点击 "打开辅助功能设置" -> 在系统设置中找到本应用并打开辅助功能（如果需要抓屏歌词）

5. 回到应用并播放音乐/视频，观察 Flyme 状态栏是否显示滚动歌词

6. 如需调试，进入设置页面点击 "导出日志" 将 `lyric_log.txt` 导出方便查看。

## 注意事项

- AccessibilityService 能读取屏幕文本，请在可信设备/环境下授予。
- 我们不 Hook system_server/SystemUI，不修改系统文件；仅在应用进程内工作。
- 若需要 LSPosed 白名单 Hook 模块（仅对特定包注入以获取内部歌词），请在 `modules/lsposed-helper/` 中新增并自行审计安全性（可选）。

## 项目结构

```
FlymeLyricBridge/
├─ .github/
│  └─ workflows/
│     └─ build.yml          # GitHub Actions 自动构建配置
├─ app/
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ java/com/example/flymelyric/
│  │  │  │  ├─ MainActivity.java
│  │  │  │  ├─ SettingsActivity.java
│  │  │  │  └─ LogManager.java
│  │  │  └─ java/com/example/flymelyric/service/
│  │  │     ├─ LyricPosterService.java
│  │  │     ├─ LyricNotificationListener.java
│  │  │     └─ LyricAccessibilityService.java
│  │  │  ├─ res/
│  │  │  │  ├─ layout/
│  │  │  │  │  ├─ activity_main.xml
│  │  │  │  │  └─ activity_settings.xml
│  │  │  │  ├─ xml/
│  │  │  │  │  └─ accessibility_service_config.xml
│  │  │  │  └─ values/
│  │  │  │     └─ strings.xml
│  │  └─ AndroidManifest.xml
│  └─ build.gradle
├─ build.gradle
├─ settings.gradle
├─ gradle.properties
└─ README.md
```

## 如何把这个仓库放到 GitHub（一步步，适合小白）

1. 在 GitHub 上新建仓库 `FlymeLyricBridge`（私有或公开都行）。

2. 在本地把文件整理好（按上面目录结构）。

3. 打开终端（或 Git GUI），执行：

```bash
git init
git add .
git commit -m "Initial commit - FlymeLyricBridge"
git remote add origin https://github.com/<your-username>/FlymeLyricBridge.git
git push -u origin main
```

4. Push 完成后，进入仓库的 Actions 页签，会看到 Android CI 自动开始构建（第一次可能需要等待几分钟）。

5. 构建完成后，在 Actions -> latest workflow run -> Artifacts 下载 `lyricbridge-apk`，或在仓库里自托管下载链接。

## 在 GitHub Actions 上如果构建失败的常见原因与排查

- **gradle 版本与 com.android.tools.build:gradle 不匹配** — 我在模板里使用 8.1.1 和 JDK17，通常 OK。若失败，Actions 日志会给出错误行，按日志提示调整 gradle 插件版本或 compileSdk。

- **资源/包名不一致** — 若你替换了 `applicationId` 或 `package`，请同步修改 `AndroidManifest.xml` 与 Java 包声明。

- **网络依赖问题** — Actions 默认可连网；如果依赖私有库需配置凭证。

## 常见问题

### Q: 为什么状态栏没有显示歌词？

A: 请检查以下几点：
1. 确保已授予通知访问权限
2. 确保正在播放的音乐应用支持 MediaSession 或通知歌词
3. 检查日志文件查看是否有捕获到歌词

### Q: AccessibilityService 有什么用？

A: 某些音乐/视频应用的歌词不在通知中显示，而是直接在屏幕上渲染。AccessibilityService 可以读取屏幕上的文本内容，从而捕获这些歌词。

### Q: 支持哪些音乐应用？

A: 理论上支持所有使用 MediaSession 标准接口的应用，如：
- 网易云音乐
- QQ音乐
- 酷狗音乐
- Spotify
- 以及其他遵循 Android MediaSession 标准的应用

## 许可证

MIT License
