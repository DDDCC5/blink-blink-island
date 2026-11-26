# Swan 面部追踪实机验证 — 2026-11-26

## 本次结论

独立 OpenXR 探针 1.3-probe-openxr / code 4 已替换安装并运行。用户已完成系统边界确认并允许面部追踪，进程确认权限为 granted。自有 OpenXR instance、system、headless session 创建及 xrBeginSession 均返回 XR_SUCCESS；随后面部模式列表为空，请求公开 DEFAULT 模式的 xrCreateFaceTrackerBD 返回 -8（XR_ERROR_FEATURE_UNSUPPORTED），输出 tracker 仍为 NULL。未取得真实眼睑数据，闭眼训练未通过验收。

这证明本次系统/权限/会话/应用配置下，DEFAULT 追踪器无法创建；不证明所有 Swan 硬件或其他系统版本永久不支持，也不把 headless 结果等同于有渲染会话结果。待进一步由 PICO 确认 Swan OS 6.3.0 的 DEFAULT 模式开放条件、应用资格、会话类型要求及 Spatial 互操作方式。扩展存在与 supportsFaceTracking=true 不能单独作为开放训练依据。

## 能力矩阵

| 检查 | 真机结果 | 依据 |
|---|---|---|
| 替换安装和启动 | 通过 | session-probe-install.log / session-probe-launch.log |
| 面部权限 | 已授予 | face-package.txt、face-authorized-query.log、session-probe-runtime.log |
| XR_BD_facial_simulation 扩展 | 已公布 | session-probe-result.json |
| 系统 supportsFaceTracking | true | 同上，仅能力声明 |
| 自有 headless session 启动 | 通过 | begin_headless_session=0 |
| 支持模式枚举 | 成功但为空 | count=0、modes=[] |
| DEFAULT 面部追踪器 | 当前不可用 | create_default_face_tracker=-8、handle=false |
| 双眼 Blink 权重读取 | 未验证 | 未成功创建 tracker，未调用采样 |
| 睁眼/闭眼对照及十轮训练 | 待验证 | 无真实输入，不能开放自动判定 |
| 与正式 Spatial 应用共存 | 待验证 | 探针为独立应用，不操作 Spatial 私有句柄 |

设备 B3110 Swan，系统 ro.build.display.id=6.3.0。设备标识、APK SHA256、单测明细存于 swan-verify-2026-11-26/install-status.json。4 项探针 JVM 单测通过；单测不证明设备传感器可用。

## 可复现步骤

在工程根目录执行：

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/chanzungsan/Library/Android/sdk' ./gradlew -p probes/spatial --no-daemon --console=plain :app:testOpenxrDebugUnitTest :app:assembleOpenxrDebug
pico-cli app install probes/spatial/app/build/outputs/apk/openxr/debug/app-openxr-debug.apk --replace --device PB3110MGL8190063B
pico-cli app launch com.example.blinkisland.capabilityprobe.openxr --activity com.example.blinkisland.capabilityprobe.platform.OpenXrProbeActivity --device PB3110MGL8190063B
pico-cli log --device PB3110MGL8190063B --tag BlinkCapabilityProbe --lines 30
```

首次权限请求由 Activity 的 request_face_permission=true 参数触发，需用户在系统窗口选择。此次已授权，无需再次请求。脚本未绕过边界、修改系统权限或替换正式应用。日志仅记录能力、返回码、有效性和计数，不保存面部权重或视线坐标。

## 公开接口依据

- [xrCreateFaceTrackerBD](https://registry.khronos.org/OpenXR/specs/1.1/man/html/xrCreateFaceTrackerBD.html)：不支持所请求模式时返回 XR_ERROR_FEATURE_UNSUPPORTED。
- [xrEnumerateFacialSimulationModesBD](https://registry.khronos.org/OpenXR/specs/1.1/man/html/xrEnumerateFacialSimulationModesBD.html)：按当前 session 枚举模式。
- [XR_MND_headless](https://registry.khronos.org/OpenXR/specs/1.1/man/html/XR_MND_headless.html)：本次运行时公布的公开 headless 扩展。

## 文件

- 源码：probes/spatial/app/src/main/cpp/openxr_support.cpp
- 独立入口：probes/spatial/app/src/main/java/com/example/blinkisland/capabilityprobe/platform/OpenXrProbeActivity.kt
- 构建、安装、运行及解析后的完整结果：reports/native/swan-verify-2026-11-26/
- 本次未改正式应用的训练能力开关；没有用视线消失替代闭眼。
