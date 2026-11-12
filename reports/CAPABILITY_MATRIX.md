# 能力矩阵与验证证据

更新：2026-11-11（Asia/Shanghai）。所有“待验证”都不是通过，也不等于已证明平台不支持。

## 版本与设备证据

- 真机：Swan / B3110，ADB 授权可见；Android 16 / API 36。
- 系统显示版本 6.2.0，运行时属性为 `6.2.0.0-mol.20260914`，构建于 2026-09-15；见 `device-spatial-props.json`。
- CLI：明确使用 Node 安装目录内的 0.6.1。PATH 仍优先指向另一套 0.5.0，未擅自修改 PATH。
- Setup：cn / local / codex / spatial。`setup/setup.log`、`setup/doctor.json`、`setup/context-doctor.json`；两项 plugin/context 检查通过。
- 探针首次由 CLI 默认生成 BOM 6.2.8，构建成功、安装成功，启动因 `DYNAMIC_SHADOW_COMPONENT` 缺失崩溃。完整崩溃见 `probe-crash.log`。不能把 build success 当 runtime success。
- 官方本地 6.2 发布说明记录动态阴影在 2026-11-11 的 6.2.8 新增；设备运行时更早。探针回退 BOM 6.1.9 后已构建、替换安装并启动，日志存在 `lifecycle=create sdk=6.1.9`。这是探针兼容性修复，不是业务应用 SDK 已决定。
- 当前探针 BOM 声明：`../probes/spatial/gradle/libs.versions.toml`。根目录知识 selector 是 6.2，与探针 6.1 不一致；本轮探针使用本机 6.1 文档和实际编译核验，不把 6.2 知识图谱当成探针的版本依据。后续如果正式工程选 6.1，应通过官方 setup 的批准流程对齐，而不手改托管集成配置。
- 初始视野截图 `probe-swan-initial.png` 是全黑，不能当作可见UI成功证据。随后用户确认佩戴后能看到面板。
- 16:04:41–16:05:04真机日志记录Eye/Head/Hand为SUPPORTED/STARTED且Flow计数持续增长；控制器为DEVICE_NOT_SUPPORTED/PENDING。见 `probe-worn-tracking.log`。
- 16:04:50记录一次selection；用户标注为手势，单次选择成功。16:05:07记录notification_posted；用户未观察到跨应用通知。
- 通知权限已授予，系统记录id701/actions3/importance3，seen=false、airtimeMs=0、mIntercept=false。详见 `notification-appops.txt`、`probe-package-state.txt`、`notification-record.txt`。随后用户确认通知中心存在该通知，点击后显示start；日志16:08:40.877有notification_route=start，见 `probe-notification-route.log`。

## 矩阵

| 能力 | 官方依据 / 已有实现 | 实际结果 | 最小复现与验收 | 限制、下一步 |
|---|---|---|---|---|
| 后台累计佩戴时长 | 已查 Spatial 6.1/6.2 公开跟踪资料，尚未找到可信后台 wear-state 契约。探针只检查 Android proximity 是否存在，不将其定义为佩戴 | **待验证** | 前台佩戴60秒→摘下30秒→佩戴60秒；另在其他应用重复；应累计约120秒，摘下不计；重启、休眠、杀进程单独测试 | 目前没有后台累计实现；proximity、Activity生命周期或屏幕亮灭均不能单独证明佩戴。若无法取得公开wear事件，需PICO确认受支持方案；仅前台计时属于功能降级，必须由用户决定 |
| 跨应用系统通知 | 使用公开 Android NotificationChannel/NotificationManager 的**验证候选**；不声称这就是 Swan 官方跨应用通知方案 | **通知中心展示通过；主动横幅待验证** | 在探针里授权通知→点15秒测试→切换其他应用；观察是否实际展示；勿扰、拒绝权限另测 | 默认重要性通道未观察到主动横幅，但用户已在通知中心找到并点击。已增加独立HIGH通道候选测试；进程内Handler不证明后台调度可靠性 |
| 通知点击路由、三个动作 | 显式 immutable PendingIntent，start/later/today 路由，LaunchActivity onCreate/onNewIntent 记录 | **本次start路由通过；冷启动及其他动作待验证** | 先热启动点正文与三个动作，再杀进程后点击已存在通知；日志与面板应显示对应 route | 动作展示是否获系统允许未证实；later/today 在此只回显路由，没有真正延后或当日静默业务 |
| 凝视姿态 | EyeTrackingProvider + runtime EYE_TRACKING；官方文档限制 Full Space | **真机公开数据流接收通过；轨迹与判定待验证** | 授权后启动，观察 Eye supportState/state、回调计数；移视线、撤销权限、切出Full Space分别记录 | 数据质量、丢失容差、准确度和实际轨迹尚未标定；只有回调次数不证明眼动判定正确 |
| 眼睑开合 | 已核对 EyeTrackingData，仅见 eyePose 与 timestamp；没有公开闭合字段证据 | **待验证，关键门槛** | 必须先取得正式公开接口与签名，再做自然眨眼、完整闭眼3秒、提前睁眼、追踪丢失对照 | 不把数据缺失当闭眼，不把闭眼当力度。若没有开合接口，可提议“听觉引导、未自动验证”，但这是降级，尚未获产品确认 |
| 头部姿态 | HMDTrackingProvider 有官方文档；实际 6.1.9 import/start/dataFlow 编译通过 | **真机公开数据流接收通过；姿态准确性待验证** | 头稳眼动与眼稳头动对照；记录HMD回调、有效姿态与变化；再标定容差 | 头部内部timestamp不可访问，改用公开Flow计数；未制定未经标定的通过阈值 |
| 手势输入 | HandTrackingProvider 及 SpatialUI 按钮 | **流接收及单次手势选择通过；可靠性待验证** | 仅用手势选择同一测试按钮10次，记录成功/误触；另测遮挡 | 回调并不等于手势选择体验通过；实际类型由观察者标注 |
| 控制器输入 | ControllerTrackingProvider 及 SpatialUI 按钮 | **当前DEVICE_NOT_SUPPORTED/PENDING；原因待验证** | 同一按钮控制器10次，失联恢复；记录成功/误触 | 不能从系统合成点击推断来源；探针日志明确 source_requires_observer_label |
| 凝视停留选择 | 尚未实现自定义停留进度，探针可用于观察系统是否提供dwell | **待验证 / 未实现自定义dwell** | 无手势/控制器时进入、保持、移出目标；测进度、取消与误触 | 不把 gaze ray 或普通按钮的可点击性当成 dwell 已实现 |
| PICO昵称头像 | 尚未确认适用于 Spatial/Kotlin、该账户与发布身份的公开SDK调用链 | **待验证 / 未接入** | 待官方接口与开发者App ID配置确认后，测授权、拒绝、离线和头像失败 | 不用Android系统账户信息冒充PICO用户中心；可提议本地昵称/默认头像，正式逻辑待确认 |

## 依据文件与官方链接

- [EyeTrackingProvider 官方 API（6.1.9）](https://developer-cn.picoxr.com/spatial-api/6.1.9/trackingpack/tracking/com.pico.spatial.tracking.eye/-eye-tracking-provider/index.html)
- [Android 通知创建文档](https://developer.android.com/develop/ui/views/notifications/build-notification)：候选探测路径，不能据此推断Swan系统实际展示行为。
- 本机版本匹配资料根：`/Users/chanzungsan/Library/PICO/sdk/6.1/agent-vault/spatial/`。
- `documentation/spatial-sdk_tracking_eye-tracking.md`、`spatial-sdk_tracking_hmd-tracking.md`、`spatial-sdk_tracking_use-dataprovider.md`。
- `api-reference/com.pico.spatial.tracking.eye.md`、`com.pico.spatial.tracking.hand.md`、`com.pico.spatial.tracking.controller.md`。
- 新版差异依据：`/Users/chanzungsan/Library/PICO/sdk/6.2/agent-vault/spatial/documentation/spatial-sdk_release-notes.md`。

## 结论边界

本轮通过了环境配置、探针构建/安装/进程启动；用户观察确认面板可见；真机公开Eye/Head/Hand数据流接收已取得日志证据。佩戴计时、闭合状态、通知交互、三种输入完整体验、资料读取均未通过产品验收。发布说明与设备属性可以解释兼容问题，但不是官方完整兼容表；6.1.9的选择仍须通过本机完整探针验证。

## 高重要性通知追加实验

在独立通道 `blink-capability-probe-high` 使用 `IMPORTANCE_HIGH=4`（未改变原默认通道）。16:10:44.434 日志确认发送，系统存在id702/actions3/importance4的记录，mIsInterruptive=true，但seen=false、airtimeMs=0、mIntercept=false。用户反馈“本次通知中心也没有”。本次展示实验未通过；不能据此抹去此前默认通道通知中心+start点击通过的证据，也不能扩大为所有Swan通知接口不支持。

证据：`probe-high-notification-runtime.log`、`notification-high-record.txt`。仅提高Android通道重要性不足以保证在本机这次展示；原因仍待PICO OS通知策略与正式接口核查。没有修改系统勿扰、全局开关、通知监听权限或固件。
