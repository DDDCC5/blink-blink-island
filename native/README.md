# 眨眨岛原生开发版

这是 Android/Kotlin PICO Spatial 应用，独立于浏览器审阅稿及能力探针。开发身份 `com.example.blinkblinkisland.dev`，入口 `.platform.LaunchActivity`，Spatial BOM **6.1.9**。当前是调试包，尚未完成产品验收。

系统名称已本地化：中文系统“眨眨岛”，英文系统“Blink Blink Island”。启动图标已用交接SVG经PICO官方App Identity Configurator生成普通图标、两层空间图标和SDF；身份修复证据见 `reports/identity/IDENTITY_FIX.md`（工作区根目录下）。

已实现双语页面、真实 GLB 岛屿及蝴蝶翼动画、环境音/配音/音量设置、本地默认身份头像、SQLite 训练记录与统计、删除确认、训练状态机与重试/跳过/退出，以及通知 start 路由。闭眼和眼动训练仍有明确能力门槛；禁止把门槛移除后直接按时间完成。

## 构建与验证

准备 JDK21、Android SDK（compileSdk35，设备 minSdk35）、网络及 Gradle 所需依赖。将自己的 SDK 路径写入本机 `local.properties` 的 `sdk.dir`，该文件不交付。使用已有工具链，不需要重新运行 PICO setup。

```sh
cd native
./gradlew testDebugUnitTest assembleDebug
```

输出 `app/build/outputs/apk/debug/app-debug.apk`。83 项 JVM 测试验证算法与 ViewModel；不替代设备精度或舒适度验收。

连接 Swan、确认设备序列号后，使用已安装官方 CLI：

```sh
pico-cli app install app/build/outputs/apk/debug/app-debug.apk --replace --device YOUR_SERIAL
pico-cli app launch com.example.blinkblinkisland.dev --activity .platform.LaunchActivity --device YOUR_SERIAL
```

权限由佩戴者在头显中确认。不要使用 `--grant-permissions` 代替用户授权。替换安装保留本地数据；不要卸载或清数据来规避问题。

```sh
./gradlew connectedDebugAndroidTest
```

两个存储仪器测试使用独立 UUID 数据库，不删除用户记录。Gradle 测试流程可能卸载被测开发包，完成后需重新安装；请安排好真机使用时间。

## 能力门槛与验收边界

- 真实 Eye/HMD Flow 姿态已接入并转换到渲染root坐标；`GazeRuntime` 先要求五点视线对齐，再使用真实几何、头动限制和相对眼动判据。开发容差尚需Swan验收；`TrackingBridge` 不伪造眼闭合数据。
- `SessionUseCase` 实现闭眼3秒×10、有效追随60秒、四向眼动次数、暂停/失败/重试/跳过等状态；合成观测测试通过，设备动作判定尚未通过。
- `GazeGeometry` 已接入运行期，计算同坐标下视线夹角、头部转角/位移及相对眼动，拒绝无效/过期/不同步数据。蝴蝶有八字运动路径，眼动目标按左右/上下/顺逆圆周路径运行。移动段须满足有效追随覆盖；圆周必须回到起点。规则为开发参数，不能冒充真机已验收的产品容差。
- `ReminderUseCase` 仅接受“已验证佩戴”输入。尚无已验证的后台累计佩戴来源，故没有启用后台提醒。15秒通知是手动诊断测试，进程终止时不可靠。
- PICO 用户中心尚未接入。当前头像/身份是本地降级，不读取系统账户冒充 PICO 资料。
- 无 raw gaze 持久化，无联网业务。记录保存在应用私有 SQLite，数据删除保留设置；关闭 Android 备份。

## 导航

- `domain/usecase/SessionUseCase.kt`：动作流程与结果。
- `domain/usecase/GazeGeometry.kt`：待校准接入的几何判定。
- `domain/usecase/ReminderUseCase.kt`：佩戴时间规则。
- `data/tracking/TrackingBridge.kt`：真实公开 SDK 数据流。
- `data/repository/SqliteIslandRepository.kt`：离线存储。
- `ui/island/IslandViewModel.kt`：页面、会话与写入重试。
- `ui/island/components/IslandScene.kt`：GLB 场景与翼动画。

完整证据见上一级 `reports/native/VALIDATION_REPORT.md`。BOM6.2.8 曾在本机旧运行时发生缺字段崩溃，不能直接升级。原始SVG/WAV来源见 `handoff/`；程序生成GLB来源见 `scripts/generate_review_3d.py`。系统 TTS 生成的中英配音仅供审阅，商业分发授权未确认。此包不能作为发布候选版。
