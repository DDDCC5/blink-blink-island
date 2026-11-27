# 眨眨岛 / Blink Blink Island

<img src="review/identity/app_icon_square_cream.png" width="160" alt="眨眨岛：浅奶油黄底的蝴蝶与岛屿图标">

这是 PICO Swan 上运行的 Android/Kotlin Spatial 原生开发版，使用 SDK BOM 6.1.9，不是浏览器原型。已实现双语界面、三维岛屿、目光跟随与训练判定、自然女声配音、蝴蝶飞行动画、十轮闭眼语音引导及离线记录统计。闭眼休息按用户最新决定采用语音引导，不检测眼睛开合。后台佩戴累计等能力仍待接入与验证，尚非正式完成版。

当前开发版：**1.0.8（versionCode 9）**，2026-11-27。新增小岛与训练目标联动：蝴蝶和小球从岛屿入场，用户确认结束后回岛；组合训练采用目标交接。137项JVM测试和Debug/Release构建通过，已在独立模拟器启动原生首页与模型；受ROM空间输入接口限制，完整动画流程及真实眼动仍待验收。Swan尚未安装本版。详见 [1.0.8开发验证记录](reports/native/RELEASE_1_0_8.md)及[版本说明](docs/releases/v1.0.8.md)。

应用源码在 `native/app/`，仓库根目录已提供云构建入口。准备 JDK21 与 Android SDK（API35）后，在根目录运行：

```sh
./gradlew --no-daemon --console=plain assembleRelease
```

输出 `app/build/outputs/apk/release/app-release-unsigned.apk`，发布签名由平台或你自己的签名配置完成。根目录 Wrapper 固定 Gradle8.13；不要使用日志中的系统Gradle8.11.1替代它。`./gradlew testDebugUnitTest` 可验证127项单元测试。原有 `cd native && ./gradlew ...` 开发入口继续可用，详见 [构建说明](native/README.md)。

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

原生探针分为 Spatial 与独立 OpenXR 两个 variant，详见 [探针说明](probes/spatial/README.md)。仅用于能力验证，不参与眨眨岛闭眼引导流程。
