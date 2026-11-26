# PICO 模拟器：闭眼能力探针测试

2026-11-24。选用CLI当前管理的 `Pico_Emulator_6_2`，bundle6.2.1，明确目标 `emulator-5556`。另一个在线模拟器未操作。原生探针BOM仍为6.1.9，正式应用未改变。本轮不验证真实眼睑。

## 结果

| 检查 | 结果与依据 |
|---|---|
| 初始混合入口启动 | 失败：PID14941抛出Activity未关联SpatialContainer；已保留crash日志 |
| 入口拆分修复 | Spatial与OpenXR成为独立variant/包；Spatial保留Stage，OpenXR使用普通Activity和VR声明，不共享句柄 |
| 修复后Spatial启动 | 成功；PID17360，UI可见，权限按钮及实验按钮均产生实际事件 |
| 视线权限 | 通过模拟器系统窗口确认；包权限状态granted=true，没有使用pm grant |
| 系统TTS | 初始化失败code=-1；改为预置PCM WAV，不依赖TTS服务 |
| 离线语音和实验流程 | 从start到finished约88秒；三次open/closed/reopen及open_look_sides顺序完整；12次音频加载/播放启动记录，无音频加载失败。未声称物理听感验收 |
| 数据流 | Eye与Head均SUPPORTED/STARTED，实验窗口每秒视线发射中位64次；这些是模拟输入 |
| OpenXR loader/instance/system/headless session | 最终PID18192查询返回均为0，销毁session/instance也返回0 |
| 面部能力 | 扩展存在，supportsFaceTracking=true；模式列表为空，Face Only未得到验证。此次该包FACE_TRACKING权限未授予，因此不能把空列表当硬件不支持的最终结论 |
| 新启动崩溃 | PID17360的crash-buffer观察没有新条目。早期PID14941崩溃仍保留，不清空历史日志 |
| 单元测试 | Spatial、OpenXR两variant各4项0失败/错误/跳过，属于同组规则在两种构建配置下运行 |
| 真机闭眼识别 | 未验证、未解锁、未计为训练完成 |

实验中的“closed”等标签是**播放提示的条件标签**，不是检测结果。本轮没有真实闭眼输入，不能用其视线数据证明闭眼会断流或不断流。

可选面部权限验证Activity流程曾等待系统窗口，未完成授权；最终默认支持查询不主动请求FACE_TRACKING，显式extra才走该流程。正式取面部数据前仍必须由佩戴者授权。

## 最终实现与安装包

`probes/spatial/app`新增 `spatial` 与 `openxr` 两个flavor，探针版本1.2-probe/code3。OpenXR包为 `com.example.blinkisland.capabilityprobe.openxr`，入口 `com.example.blinkisland.capabilityprobe.platform.OpenXrProbeActivity`；Spatial包仍是 `com.example.blinkisland.capabilityprobe`。

构建：

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/chanzungsan/Library/Android/sdk' ./gradlew -p probes/spatial --no-daemon --console=plain :app:testSpatialDebugUnitTest :app:testOpenxrDebugUnitTest :app:assembleSpatialDebug :app:assembleOpenxrDebug
```

APK分别为：
- `probes/spatial/app/build/outputs/apk/spatial/debug/app-spatial-debug.apk`
- `probes/spatial/app/build/outputs/apk/openxr/debug/app-openxr-debug.apk`

最终构建日志 `probe-emulator-final-build.log`。APK哈希及测试计数在 `emulator-eye-probe/build-results.json`。旧单variant debug路径为历史产物，不应继续安装。

## 证据

均位于 `reports/native/emulator-eye-probe/`：

- `workflow-verification.json`、`experiment-final.log`：完整条件序列、播放启动和视线计数。
- `openxr-final-query.json`、`openxr-final-runtime.log`：最终OpenXR查询结果。
- `app-info.json`、`crash-final.log`：Spatial运行和指定PID崩溃检查。
- `spatial-probe.png`：真实模拟器空间FOV截图，1920×1080。截图中诊断面板较小，不能作为正式应用文字舒适度验收。

测试后已重新启动Spatial探针供继续查看。原生项目说明位于 `probes/spatial/README.md`。

## 下一步

将两种已拆分探针安装到Swan，重新验证VR声明、supportsFaceTracking和Face Only模式；再实现/验证真实face tracker的双眼Blink权重、有效性与采样时间。最后评估与正式Spatial主应用的公开共存方式，才接入已有10轮判定。不能把模式查询成功、模拟器提示流程或软件测试视为真实闭眼识别通过。
