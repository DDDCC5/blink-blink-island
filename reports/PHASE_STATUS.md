版本更新2026-11-14：1.0.1/code2已完成根目录Debug/Release构建与108测试；APK内满幅绿色方形安装图标和空间背景已同步，版本/本地化/签名升级匹配核对通过。未执行本轮真机安装。详见 [1.0.1发布核对](native/RELEASE_1_0_1.md)。

最终2026-11-13：用户确认四角、内容完整及返回正常，并明确取消可移动。已移除所有拖动代码，恢复原左侧固定位置与静态标题，保留视口裁切修复和顶部返回；108测试、构建/样式/架构通过，替换安装启动PID17492，实际frame与root同为1383×1408。见 [最终面板修复报告](native/PANEL_VIEWPORT_AND_INPUT_FIX.md)。

最新2026-11-13：系统窗口日志定位附件2016px被裁成1440px。按实际视口适配为1383×1408，系统实际frame一致；用户确认“四角和内容完整，返回正常”。标题裸pointerInput没有收到捏合，现改原生Button输入目标+Initial事件读取，安装PID16777，拖动待验收。108项测试、构建/样式/架构通过。

最新2026-11-13：按用户要求增加标题栏捏住拖动XYZ和松手固定；四角使用实际View硬件轮廓裁切，附件输出1980×2016并补偿物理缩放。107项JVM测试/构建、样式与架构检查通过。设备重新连接后替换安装并启动PID8374；运行日志确认1980×2016、首页内容未溢出、GLB加载正常，底角和拖动仍待佩戴者验收。小球范围与重新注视继续已获用户确认。详见 [可移动面板报告](native/MOVABLE_PANEL_REBUILD.md)。

# 阶段状态 — 2026-11-12

Latest correction (2026-11-12): closer/larger left panel, one-time locked persistent placement, and fixed central training reference implemented. 75 unit tests and final build passed; replaced and launched. Wearer acceptance pending; countdown report still under investigation. See native/TRAINING_UI_REPAIR.md.

Latest manual-transition fix: user confirms butterfly countdown/experience; device eye-mode Retry did prepare3s, but Skip did not. Skip now prepares3s and PREPARING scrolls panel to the top.76 tests/build/style pass; installed/launched PID30084. Wearer verification pending. See native/TRAINING_UI_REPAIR.md.

Latest wearer acceptance: eye-movement Retry and Skip both show countdown (2026-11-12). Reported missing-countdown issue closed for these two paths. Full eye-movement routine and all-mode acceptance remain pending.

Latest restoration: BLINK independent entry restored first on home;76 tests/build/style pass; installed/launched PID30761. Automatic closure remains blocked because current public Spatial6.1.9 eye data has no eyelid/openness field. See native/BLINK_CAPABILITY_RECHECK.md.

Latest horizontal-layout fix: launch pitch/roll removed from persistent reference, yaw/eye height preserved.79 tests/final build pass, final installed/launched PID1145; wearer horizontal alignment acceptance pending. Details: native/HORIZON_LAYOUT_FIX.md.

Latest polish: wearer confirms horizontal persistent placement correct. Training language control hidden,30 neural female narration clips replaced, tangent/banked/flapping butterfly implemented.81 tests/build/style pass; installed/launched PID4236. New voice/animation wearer acceptance pending. Details native/VOICE_FLIGHT_POLISH.md.

Latest panel fix: language only WELCOME/HOME, fixed non-scrolling panel, grouped actions and2-record history pages.83 tests/build/style/architecture pass; installed/launched PID4778. Wearer full-content visibility acceptance pending. User accepts female voice and butterfly. Details native/NO_SCROLL_PANEL_FIX.md.

2026-11-13 latest: user changed butterfly/eye movement to world-gaze-only judging; head-stability/relative-eye/live-HMD scoring gates removed. Branded no-scroll panel rebuilt and real attachment height corrected.91 tests/build/style/architecture pass; final installPID29334. Natural-head-motion and new visual acceptance pending. See native/GAZE_ONLY_UI_REBUILD.md.

Latest2026-11-13 follow-up: production five-point gate removed; valid-eye readiness starts3s countdown. Full-session CTA moved above modes, Finish/Back aligned, History top Back added, flight14.3percent faster. Explicit pre-training recenter and internal text-overflow checks added.98 tests/build/style/architecture pass; final installedPID30352. Wearer acceptance pending. See native/STARTUP_LAYOUT_FOLLOWUP.md.

## 2026-11-12 训练修复与重置后重装

最新增量：用户要求重试倒计时与左右平视布局，已实现3秒准备（不判定/不计分/暂停不偷跑）、左侧面板、右侧常驻场景和中央训练。72项单元测试及最终构建通过，样式及架构检查通过；替换安装并启动最终进程25385，场景加载日志正常，本次进程crash buffer无条目。佩戴者位置/倒计时验收待确认。详见 [训练修复报告](native/TRAINING_UI_REPAIR.md)。

最新：用户已确认训练文字与高亮清楚、蝴蝶入口可进入且眼动授权弹出。设备日志确认五点对齐全部通过并进入真实蝴蝶追随；尚无完整60秒成功证据。已修复单帧头部偏移立即终止的问题：短暂偏移暂停，恢复继续，持续500ms偏移才重试；67项单元测试和构建通过，已替换安装并启动（进程23416）。当前版本首次读日志只有启动/模型加载，待佩戴者验证完整训练。详见 [训练界面修复与排查](native/TRAINING_UI_REPAIR.md)。

闭眼按用户决定等待真实开合接口；眼球运动完整轮次及系统暂停后的参考重建仍待验证/修复。没有把五点通过或计时测试作为三个模式完成。

## 当前状态（原生开发版，覆盖下方历史状态）

名称与图标已修复：官方App Identity Configurator写入品牌图标及空间图层/SDF，中文系统仅“眨眨岛”、英文系统仅“Blink Blink Island”。替换安装成功，用户确认Swan“图标和中文名称都正确”；英文资源APK核对通过、英文系统桌面尚未实测。详见 [身份修复报告](identity/IDENTITY_FIX.md)。

用户要求“那继续开发啊”后，已推进 `native/` 原生业务工程，并安装到 Swan。用户确认场景和欢迎面板可见、文字清楚可读，以及“退出”记录保存正确且未计为“完成”。

- 已实现：双语SpatialUI页面、实际GLB岛屿和翼动画、音频设置、本地默认身份、SQLite记录统计与删除确认、训练状态机、保存失败重试、start通知路由。
- 已接运行期：注视角度/头部姿态判定、五点对齐、蝴蝶路径与眼球运动路径；五点对齐已有设备证据，完整模式尚待验收。
- 验证：当前67项JVM测试；此前2项Swan存储仪器测试通过；最新构建成功。详见 [原生验证报告](native/VALIDATION_REPORT.md) 和 [构建说明](../native/README.md)。
- 仍待：眼睑公开接口、后台累计佩戴来源、真机坐标校准和动作判定、通知展示/冷启动/其他动作、PICO资料、停留输入与控制器、音频授权及性能舒适度验收。当前不是完整应用交付。

以下内容保留为探针及设计阶段的历史记录，其中“尚未实现”的业务工程等状态已由上方更新。

## 已实现并取得证据

1. 按批准的绝对CLI路径完成cn/local/codex/spatial setup；plugin.doctor及project.context.doctor均为ok。日志在`setup/`，未运行doctor --fix、未改PATH或shell文件。
2. 解压33个交接文件到`handoff/`并记录原始entry与SHA-256；不执行包内生成脚本。`handoff-inventory.json`。
3. 复现原型SyntaxError并在`review/prototype/`修复嵌套单引号；原文件不动。`prototype-syntax-check.txt`，浏览器流程检查已执行。
4. 官方CLI生成真正Kotlin Spatial能力探针；最终BOM6.1.9构建通过、安装通过、进程启动有日志。`probe-build-6.1.9.log`、`probe-install-final.log`、`probe-launch-final.log`、`probe-runtime-final.log`。
5. 三维GLB与独立左右翼动画、33份审阅音频和双语审阅页已生成。参数、哈希见`../review/3d/manifest.json`和`../review/audio/manifest.json`。

## 已失败或受阻，未伪装成功

- CLI默认BOM6.2.8探针启动崩溃：缺DYNAMIC_SHADOW_COMPONENT，见`probe-crash.log`。不兼容APK在`builds/probe-sdk-6.2.8-incompatible.apk`，请勿作为安装交付使用。
- 第一次探针实现访问文档描述但实际internal的timestamp，编译失败；已改公开Flow计数，见`probe-build-internal-timestamp-failure.log`。
- Swan初始空间截图全黑；随后用户确认佩戴后面板可见，已记为用户观察通过。
- 新日志确认Eye/Head/Hand公开Flow持续收到数据；这是流接收通过，不是轨迹准确性或训练判定通过。控制器DEVICE_NOT_SUPPORTED/PENDING，原因待查。
- 手势单次选择：日志selection=1，用户确认手势输入；多次可靠性待测。
- 通知发送调用成功、权限授予、系统记录存在，但用户未见通知，seen=false/airtimeMs=0。随后用户确认通知中心有通知，点击后显示start，且日志notification_route=start；通知中心展示和本次start路由通过，主动横幅仍待验证。
- Editor两次受管理流程cancelled，未产出模型或bundle。已保存运行响应和worker日志；独立GLB不冒充Editor产物。
- 根知识selector6.2与最终探针6.1.9不匹配；使用已安装6.1文档及实际编译核对，不手改setup管理文件。

## 尚未实现 / 未验收

- 正式业务工程、训练状态机和传感器动作判定。
- 原生本地统计/离线保存/删除、双语与混音业务、PICO昵称头像与降级。
- 后台累计佩戴、跨应用提醒、10分钟稍后与设备本地次日静默。
- 眼睑开合API、追踪质量阈值、头动容差、三输入完整体验。
- 设备适用性、帧率、舒适度、原生资源导入、正式配音授权。

这些项受用户明确的“设计确认后正式开发”及真实设备能力门槛约束，不以浏览器Next、普通计时或API可编译代替。

## 当前交付导航

- `../review/index.html`：设计审阅入口。
- `CAPABILITY_MATRIX.md`：逐能力依据、结果、复现、限制。
- `DESIGN_REVIEW.md`：素材来源、待审设计与Editor阻塞。
- `../probes/spatial/README.md`：原生探针构建、安装、操作及验收边界。

当前探针已获用户批准安装/启动。后续需要用户通知/输入测试反馈与设计确认；不自动改变设备权限或强行执行空间点击。

## 后续真机追加结论

- 用户确认面板可见、手势选择成功；Eye/Head/Hand公开流持续接收。
- 默认通知：通知中心展示及本次start点击路由通过，日志16:08:40.877。
- 新增HIGH通道：构建/安装/发送成功，系统记录存在，但用户未在横幅或通知中心看到；当前测试未通过，原因待查。
- 不把“系统有记录”当作“用户收到提醒”。后台佩戴累计、闭合数据和主动横幅保证仍是未解决门槛。
