# 闭眼休息：开发增量与真实数据接口门槛

2026-11-24。用户要求按原需求继续：真实闭眼3秒、睁眼，10轮；提前睁眼未完成，可重试/跳过；追踪中断不计时。不提供引导计时替代，不测闭眼力度。

## 当前结论

闭眼训练规则引擎已进一步修正并测试；**真实眼睑数据仍未接通，不能声明闭眼功能可在Swan使用**。用户此前拒绝计时替代，故继续保留能力门槛。没有解锁未开放项，没有部署虚假的完成判定，也没有声称新增SDK API。

## 本轮改动

`native/app/src/main/java/com/example/blinkblinkisland/dev/domain/usecase/SessionUseCase.kt`：

- 一轮必须先观察到有效睁眼，再观察连续闭眼至少3秒，最后观察睁眼。进入时已闭眼不计数。
- 重复时间戳不处理；逆序或超过250ms的观察间隔丢弃当前闭眼连续时长，须重新观察到睁眼。250ms沿用原引擎采样连续性上限，尚不代表未来眼睑接口设备标定值。
- 暂停/质量丢失清除本轮连续闭合与显示进度，恢复后不能把空档算作闭眼。
- 首个闭眼样本不累计此前睁眼的时间。
- 能力从阻塞变为可用、手动恢复、重试和跳过均保留3秒准备。
- 准备期间仍需真实质量输入；没有接口时保持CAPABILITY_BLOCKED。

保留原有提前睁眼未完成、10轮、退出记录和完整休息编排。蝴蝶/小球判定规则未改。注：`Observation.eyesClosed`与`Capabilities.eyelid`是本应用自定义输入协议，**不是PICO SDK字段**；本轮未向它们注入伪造的生产数据。

## 验证

新增 `native/app/src/test/java/com/example/blinkblinkisland/dev/domain/BlinkSessionSafetyTest.kt`。先复现10项专项测试中7项失败（blink-safety-red.log），修复后全项目118项测试0失败/错误/跳过，Debug构建通过（blink-safety-green.log）。测试使用合成开合观察，仅验证算法，不验证设备识别。既有闭眼测试补入明确的睁眼起点。

复现：

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/chanzungsan/Library/Android/sdk' ./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug
python3 scripts/verify_blink_eyelid_contract.py
```

本轮未安装到设备、未发布、未改SDK版本。设备上仍是此前自然插画版1.0.2；本轮源码/临时构建输出不能当作闭眼功能交付。根build输出为临时产物，其内容会随开发重建。

## 能力核实依据

| 项目 | 证据 | 结论 |
|---|---|---|
| 当前Spatial6.1.9 EyeTrackingData | 官方API与本机准确版本sources.jar公开签名 | 只有eyePose/timestamp；EyePose为position/rotation，没有眼睑开度/闭合/每眼有效性字段 |
| 本地6.2 Spatial资料 | 官方本地eye包API清单 | 同样未见开合字段，不能承诺升级SDK就能解决 |
| Unity旧眼动开度接口 | 官方PXR_EyeTracking支持列表 | 引擎/设备支持受限，不能直接移植成Swan Kotlin API |
| 新Unity GetEyeOpenness/GetEyeBlink | 本地官方Unity资料，Swan限制提示在扁平化表格内存在歧义 | 不作为Swan Kotlin支持证据；需原始正式支持说明 |
| 目光丢失/提供器STOPPED | 生命周期与数据可用性语义 | 不等于闭眼，禁止用于完成判定 |

准确版本官方来源：
- https://developer-cn.picoxr.com/spatial-api/6.1.9/trackingpack/tracking/com.pico.spatial.tracking.eye/-eye-tracking-data/index.html
- https://developer-cn.picoxr.com/spatial-api/6.1.9/trackingpack/tracking/com.pico.spatial.tracking.eye/-eye-tracking-provider/index.html
- https://developer.picoxr.com/reference/unity/client-api/PXR_EyeTracking/

本地来源：`/Users/chanzungsan/Library/PICO/sdk/6.1/agent-vault/spatial/api-reference/com.pico.spatial.tracking.eye.md`；6.2对应文件；Gradle缓存6.1.9官方tracking-sources.jar。本轮只记录公开字段及源包哈希，不分发SDK源码。可复现审计脚本与输出为 `scripts/verify_blink_eyelid_contract.py` 和 `reports/native/blink-public-contract-2026-11-24.json`。

MCP graph_stats没有返回当前workspace，按knowledge-query规则没有切换未知workspace或对未知版本执行查询。此限制不影响准确版本本地SDK与官方页面核实。

## 继续真机开发所需

PICO需提供Swan支持的正式眼睑开合接口/SDK或公开文档，至少明确：左右眼闭合/开度和有效性、采样时间语义、闭眼时数据是否继续有效、遮挡/摘下/追踪失败区分、所需权限，以及与Spatial Stage并行使用是否受支持。不发送技术支持消息；下面仅为可审阅的问题清单。

拿到接口后先实现最小独立验证：自然眨眼、连续闭眼3秒、提前睁眼、单眼闭合、传感器遮挡/摘下、权限拒绝、后台/恢复。只有双眼真实闭合且有效连续数据才计时；不能把未知状态补齐成成功。通过后再接到闭眼训练并完成中文/英文语音、完整流程和离线记录真机验收。
