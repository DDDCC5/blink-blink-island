# 眨眨岛能力探针

探针与正式应用分开；不生成训练完成记录。

## 两种运行方式

- `spatial`：`com.example.blinkisland.capabilityprobe`，Spatial Stage中验证视线更新与离线语音对照流程。
- `openxr`：`com.example.blinkisland.capabilityprobe.openxr`，普通Android Activity创建自有OpenXR instance/headless session，查询面部支持与模式后结束。使用官方Loader1.1.65。VR应用标记仅在此variant，不能混入Spatial入口，否则可能出现“Activity is not associated with a SpatialContainer”。

主应用保持原有Spatial结构与SDK版本。模拟器结果不等于真实眼睑识别通过。

## 构建

在仓库根目录使用现有JDK与Android SDK：

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/chanzungsan/Library/Android/sdk' ./gradlew -p probes/spatial --no-daemon --console=plain :app:testSpatialDebugUnitTest :app:testOpenxrDebugUnitTest :app:assembleSpatialDebug :app:assembleOpenxrDebug
```

输出：

- `app/build/outputs/apk/spatial/debug/app-spatial-debug.apk`
- `app/build/outputs/apk/openxr/debug/app-openxr-debug.apk`

路径相对此目录；旧单variant `outputs/apk/debug` 是历史产物，应使用上面两个明确路径。

## 运行

先检查设备列表并选定真实返回的ID，将下列YOUR_SERIAL替换为它。所有后续设备命令使用同一ID，不会自动选择另一个设备。

```sh
pico-cli app install probes/spatial/app/build/outputs/apk/spatial/debug/app-spatial-debug.apk --replace --device YOUR_SERIAL
pico-cli app launch com.example.blinkisland.capabilityprobe --activity .platform.LaunchActivity --device YOUR_SERIAL
pico-cli app install probes/spatial/app/build/outputs/apk/openxr/debug/app-openxr-debug.apk --replace --device YOUR_SERIAL
pico-cli app launch com.example.blinkisland.capabilityprobe.openxr --activity com.example.blinkisland.capabilityprobe.platform.OpenXrProbeActivity --device YOUR_SERIAL
pico-cli log --device YOUR_SERIAL --tag BlinkCapabilityProbe --lines 160
```

OpenXR探针1.3会创建并启动自己的headless会话；获得用户授权后尝试公开DEFAULT面部追踪模式。通过标准Activity extra `request_face_permission=true` 请求FACE_TRACKING运行时权限，仍需用户在设备上确认，不自动授予。只有模式、句柄均有效时才读取一次有效性与有限数值计数，不保存面部权重。Swan实测DEFAULT返回不支持，详见 `reports/native/SWAN_FACE_VALIDATION_2026_11_26.md`（仓库根目录）。

正式应用1.0.3的闭眼休息已按用户决定改为纯语音引导，不依赖本探针或面部追踪。

## 信号实验

Spatial探针中请求视线权限后，选择“闭眼信号对照实验”。预置离线语音提示三次睁眼/闭眼/重新睁眼及睁眼转向，约88秒。条件标签是指令，不是真值；真机实验结束后需佩戴者确认是否遵循提示。程序只统计真实发射次数、源时间戳重复/逆序、非有限值、姿态未变化及到达间隔，不保存视线坐标，也不推断闭眼。

`probe_voice/` 是仅供诊断的中文离线提示（本机已安装Tingting声线生成、PCM WAV），不替换主应用已确认的自然女声。使用MediaPlayer播放，不依赖设备TTS引擎。

## 已知边界

模拟器可验证启动、权限按钮、数据流与播放调度；真实眼睑准确性、Face Only权重和Spatial主应用共存仍需Swan。模拟器的一次查询中supportsFaceTracking=true，但模式列表为空，不能据此宣称真实闭眼可用。详见仓库 `reports/native/EMULATOR_EYE_PROBE_2026_11_24.md`。
