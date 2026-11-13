# 眨眨岛 / Blink Blink Island

<img src="review/identity/app_icon_square_green.png" width="160" alt="眨眨岛：绿色铺满的方形提交图标">

这是 PICO Swan 上运行的 Android/Kotlin Spatial 原生开发版，使用 SDK BOM 6.1.9，不是浏览器原型。已实现双语界面、三维岛屿、目光跟随与训练判定、自然女声配音、蝴蝶飞行动画及离线记录统计。闭眼识别、后台佩戴累计等能力仍待接入与验证，尚非正式完成版。

当前开发版：2026-11-13，108项单元测试及调试构建通过。面板保持原左侧固定位置；四角圆角、完整内容和返回入口已获佩戴者确认。蝴蝶与小球训练允许自然转头；小球扩大范围，漏看时等待、重新注视后继续。闭眼模式和佩戴累计提醒仍未开放。

应用源码在 `native/app/`，仓库根目录已提供云构建入口。准备 JDK21 与 Android SDK（API35）后，在根目录运行：

```sh
./gradlew --no-daemon --console=plain assembleRelease
```

输出 `app/build/outputs/apk/release/app-release-unsigned.apk`，发布签名由平台或你自己的签名配置完成。根目录 Wrapper 固定 Gradle8.13；不要使用日志中的系统Gradle8.11.1替代它。`./gradlew testDebugUnitTest` 可验证108项单元测试。原有 `cd native && ./gradlew ...` 开发入口继续可用，详见 [构建说明](native/README.md)。调试安装包与源码交付包保存于 GitHub Releases。

- [当前面板裁切修复与验收](reports/native/PANEL_VIEWPORT_AND_INPUT_FIX.md)
- [训练起步、可见按钮与飞行速度](reports/native/STARTUP_LAYOUT_FOLLOWUP.md)
- [目光判定与面板重构](reports/native/GAZE_ONLY_UI_REBUILD.md)
- [最新固定页式面板修复](reports/native/NO_SCROLL_PANEL_FIX.md)
- [配音与蝴蝶姿态修复](reports/native/VOICE_FLIGHT_POLISH.md)
- [闭眼接口复核与限制](reports/native/BLINK_CAPABILITY_RECHECK.md)

- [阶段报告](reports/PHASE_STATUS.md)
- [原生项目与构建说明](native/README.md)
- [原生验证报告](reports/native/VALIDATION_REPORT.md)
- [能力矩阵](reports/CAPABILITY_MATRIX.md)
- [设计审阅说明](reports/DESIGN_REVIEW.md)
- [原生能力探针](probes/spatial/README.md)
- [设计审阅入口](review/index.html)
- [原始交接包](handoff/README.md)

从此目录运行 `python3 -m http.server 8765 --bind 127.0.0.1`，然后访问 `http://127.0.0.1:8765/review/index.html`。服务只用于本机审阅；保留已有文件，若端口被占用先检查现有服务，不擅自终止其他进程。

原生探针为Kotlin/Spatial SDK 6.1.9，最终APK在`probes/spatial/app/build/outputs/apk/debug/app-debug.apk`。仅用于验证，不是眨眨岛业务应用。
