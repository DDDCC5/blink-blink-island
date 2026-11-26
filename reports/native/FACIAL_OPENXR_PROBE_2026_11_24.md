# Swan 闭眼识别：OpenXR 面部输入候选与真机探针

2026-11-24。此报告记录新的公开路线及实际验证进度，不代表闭眼训练已交付。

## 新路线

保留主应用Spatial/Kotlin，先在独立能力探针通过官方Khronos Android OpenXR Loader与JNI验证 `XR_BD_facial_simulation`。它提供左右Eye Blink权重、上脸有效性、数据时间。只考虑DEFAULT（Face Only）模式；音频生成/预测表情不能用于实际闭眼判定。

公开来源：
- https://registry.khronos.org/OpenXR/specs/1.1/man/html/XrFacialSimulationDataBD.html
- https://registry.khronos.org/OpenXR/specs/1.1/man/html/XrFacialSimulationModeBD.html
- https://registry.khronos.org/OpenXR/specs/1.1/man/html/xrEnumerateFacialSimulationModesBD.html
- https://github.com/Pico-Developer/PICO-Unity-SDK/blob/main/XR/Runtime/Scripts/Features/PXR_FacialSimulation.cs
- https://github.com/Pico-Developer/PICO-Unity-SDK/blob/main/XR/Runtime/Scripts/OpenXRFeatures/Features/PICO/FacialSimulationFeature.cs

## 实际取得的真机证据

Swan/B3110（PB3110MGL8190063B），探针PID28942：loader初始化、扩展数量与列表查询返回0。87项公开扩展中确实包含 `XR_BD_facial_simulation` 和 `XR_MND_headless`。结构化结果：`swan-openxr-support-2026-11-24.json`；原始日志：`openxr-support-probe-runtime.log`。

第一次创建探针自己的OpenXR instance返回-1；同PID运行时日志明确 `no vr tag in the application`。这不是face extension不存在的证据。精确日志：`openxr-support-create-detail.log`。

随后核对PICO官方 `XR/Editor/PXR_BuildProcessor.cs`：OpenXR应用插入 `pvr.app.type=vr`；面部功能声明 `com.picovr.permission.FACE_TRACKING` 及 `picovr.software.face_tracking=false/true`。已将对应声明与通用IMMERSIVE_HMD分类加入独立探针，未加入主应用。

Swan随后断开USB，目前仅两个模拟器在线；修正后的探针尚未替换安装或取得新结果。因此 **supportsFaceTracking、Face Only模式与实际双眼权重仍待验证**。此前的扩展枚举成功不能代替它们。

## 当前探针已实现

`probes/spatial/app/src/main/cpp/openxr_support.cpp`：

1. 公开loader初始化与两阶段扩展枚举。
2. 若face扩展存在，创建探针自有instance，查询HMD system与supportsFaceTracking。
3. 若系统支持且公开headless扩展存在，创建自己的headless session，只枚举支持模式，记录是否有DEFAULT。
4. 按路径销毁自有session/instance。不读取Spatial Engine私有句柄，不创建face tracker或采集面部权重。

`OpenXrSupport.kt`记录查询返回值及面部权限状态。探针新增 `org.khronos.openxr:openxr_loader_for_android:1.1.65`，用本机已存在NDK28.2.13676358与CMake3.22.1构建。主应用BOM6.1.9未改；没有执行CLI setup或工具升级。官方AAR合并OpenXR权限/broker查询，源依赖为Maven Central。

## 视线断流对照实验

独立探针另有语音提示的3次睁眼/闭眼/重新睁眼与睁眼转向实验；只记录每秒发射数量、重复/逆序源时间戳、非有限位姿数量、姿态未变化计数与数据间隔，不保存视线坐标。

`EyeSignalExperiment.kt`中的条件标签是**语音要求的动作，不是观测真值**。用户先前反馈“没有语音或无法开始”，日志没有成功实验开始事件，因此没有产生可用的闭眼对照数据，也没有推断闭眼。已修正实验按钮权限判断使用Context和Activity回调，增加语音准备与语言结果日志；修正后待重新佩戴验证。

## 构建与复现

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/chanzungsan/Library/Android/sdk' ./gradlew -p probes/spatial --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug
pico-cli app install probes/spatial/app/build/outputs/apk/debug/app-debug.apk --replace --device PB3110MGL8190063B
pico-cli app launch com.example.blinkisland.capabilityprobe --activity .platform.LaunchActivity --device PB3110MGL8190063B
pico-cli log --device PB3110MGL8190063B --tag BlinkCapabilityProbe --lines 120
```

现有3项计数窗口测试验证重复、逆序、无数据及跨窗口连续性；不验证SDK或设备准确性。界面规范检查0错误、4项既有结构警告。最终构建证据 `openxr-support-probe-final-build.log`。

## 接下来的验收顺序

- 修正后创建instance成功 → supportsFaceTracking查询 → DEFAULT模式枚举。
- 若可用，先确认必要权限，由用户在头显内授权，再建立face tracker读取真实有效数据；此步尚未实现/验证。
- 对比自然眨眼、3秒闭眼、提前睁眼、单眼闭合、追踪失败及摘下；只有双眼真实闭合且有效连续数据才计时，未知状态暂停。
- 验证独立OpenXR输入能否与主应用Spatial Stage共存，或是否需要官方互操作方案；不能直接假定可集成。
- 最后接入既有10轮状态机、自然女声、重试倒计时、离线记录并逐项真机验收。

禁止仅因运行时支持扩展就解锁训练，也禁止以模拟器结果冒充Swan结果。

## 后续模拟器修订

原混合Spatial/VR入口在模拟器崩溃，已拆为两个独立variant和包；旧单variant安装命令被替代。最新结果与路径见 `EMULATOR_EYE_PROBE_2026_11_24.md` 和 `probes/spatial/README.md`。离线PCM配音已替换探针系统TTS。模拟器查询走通，但真实Face Only与Blink数据仍未验证。

## 2026-11-26 真机续验

已授权并成功启动 headless session；模式为空，DEFAULT tracker 返回 XR_ERROR_FEATURE_UNSUPPORTED。最新复现命令与精确结论见 `SWAN_FACE_VALIDATION_2026_11_26.md`。本文早期“仅查询不创建tracker”描述只适用于旧探针。
