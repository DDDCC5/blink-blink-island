# 眨眨岛 / Blink Blink Island

这是 PICO Swan 上运行的 Android/Kotlin Spatial 原生开发版，使用 SDK BOM 6.1.9，不是浏览器原型。已实现双语界面、三维岛屿、眼动校准与训练判定、自然女声配音、蝴蝶飞行动画及离线记录统计。闭眼识别、后台佩戴累计等能力仍待接入与验证，尚非正式完成版。

当前备份：2026-11-12，83项单元测试及调试构建通过。最新界面采用固定页式面板与历史记录分页；该轮真机显示验收待确认。女声音色、蝴蝶姿态及水平固定布局已得到佩戴者确认。

正式工程在 `native/`。准备 JDK21 与 Android SDK 后，按 [原生构建说明](native/README.md) 配置本机 SDK 路径并运行 `./gradlew testDebugUnitTest assembleDebug`。调试安装包作为 GitHub Release 附件保存；构建缓存、本机配置及原始设备日志不纳入版本控制。

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
