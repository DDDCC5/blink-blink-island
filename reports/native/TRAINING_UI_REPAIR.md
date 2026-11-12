# 训练界面与校准阻碍排查 — 2026-11-12

用户反馈能看到橙点但校准卡住，随后要求：训练面板不要显示测试/接口异常等文字；字体较小；目光指向按钮时高亮不明显。当前保持原训练判定，不通过放宽阈值或伪造眼睑能力来解决显示问题。

## 已完成源码修改

- 独立 `TrainingPanel.kt`：只展示当前动作、轮数/进度、必要操作提示；删除原始Eye/HMD状态、Flow计数、接口/测试说明、技术阈值和中断计数等开发文字。
- 眼动未获授权时明确显示“开启眼动后开始”；闭眼模式显示“暂未开放”，保留返回或跳过选择，不冒充功能已实现。
- 用户页面中的底层异常不直接输出，转为可理解的重试提示；详细问题留在日志和报告。
- 正文28sp/40sp行距；主要按钮标签28sp，至少76dp高。标准SpatialUI Button继续提供点击、hover和触觉行为。
- `IslandActionButton.kt`：独立的系统HoverGroup响应外框，目光指向时显示4dp森林色外框和1.08倍缩放；未给内置Button再叠加hover修饰符，未读取原始系统hover事件。
- 权限按钮只请求眼动，避免与通知混在一起；在真正的attachment panel内容内获取Activity宿主，并用Application Context读取当前授予状态。添加宿主与请求日志，需设备确认是否成功弹出授权。
- 用户页面移除通知测试入口；权限页改为“权限与使用说明”。诊断发送代码保留在开发源中，没有暴露在训练页。

## 依据和验证

- SDK6.1.9知识查询：`ui-api-query.json`；匹配官方spatial-hover文档、Button文档，以及公开源Button签名核对。
- 官方示例引用的 `androidx.activity.compose.LocalActivity` 在本工程现有依赖中不可用，首次编译报Unresolved reference；未升级依赖来强行匹配示例，改用现有ContextWrapper解包，并移到附件面板实际内容作用域。
- `training-ui-build-tests.log`：最后一次构建和62项单元测试通过；`training-ui-architecture.log`：架构检查通过。
- `training-ui-style.log`：1项R10错误。静态脚本把目光激活时才显示的焦点外框识别为普通内容边框；用户明确要求更明显的目光高亮，本轮保留此焦点反馈。未改校验器、未改用绕过扫描的等效绘制，也不声称样式全检查通过。

## 真机状态与未解决事项

- `alignment-package.txt`：排查时Eye权限granted=false，通知权限也为false；这是校准受阻的已知必要条件，不能据此断定唯一根因。
- 旧进程已换为12938；`alignment-stuck-current-runtime.log`仅看到启动/模型加载和闭眼模式进入记录，没有足够校准数据。尚未得到用户卡在第几个点及提示的回答。
- 在安装此UI版本前设备断开；`training-ui-install.log`、`training-ui-launch.log`报No online device。**新界面尚未安装，更未通过字号/高亮或授权真机验收。** 已请用户恢复连接。
- 恢复连接后：重新发现设备→安装当前构建→确认permission_host日志→用户点“开启眼动”→核对权限已授予及五点对齐→验证字号与目光焦点反馈。不要自动授予权限。
- 若授权成功仍卡住，再收集判定原因并复现；不能把数据缺失当闭眼、不能直接降低阈值宣称修复。

## 恢复连接后追加

用户确认重新连接并授权调试。已核对交付APK与测试构建/哈希一致；`training-ui-install.log`安装成功、`training-ui-launch.log`启动成功，进程17312。

`training-ui-runtime.log`：14:41:26启动；14:41:27权限宿主可取得（LaunchActivity），训练蝴蝶、岛屿和翼节点加载成功。`training-ui-crash.log`本次进程无crash条目。Activity可取得不等于权限弹窗成功，已请佩戴者点击“开启眼动”确认；文字及目光外框也待用户观察。

尝试空间截图时当前视野未显示目标应用，不能作为界面验收证据；该任务生成的无关截图已移除，不纳入交付包。未把进程启动日志当成当前前台可见或高亮验收通过。

## 用户反馈及点击命中修复

用户确认“文字和高亮都清楚了”；但点开启眼动没有弹窗。`permission-no-dialog.log`没有eye_permission_request记录，权限仍为false，因此尚无证据证明授权请求已到达系统，不能直接断言是系统拒绝或Activity问题。

发现目光外框是按钮上方的同尺寸Hover节点，存在覆盖原生按钮输入表面的风险。已把纯视觉外框移到按钮后方，原生Button绘制/输入层在最上方，并添加仅开发日志的ui_action与授权宿主状态记录。`permission-hit-layer-build.log`构建和62项测试通过，`permission-hit-layer-install.log`替换安装成功，`permission-hit-layer-launch.log`启动成功。

已请用户重新操作，点击命中/授权弹窗效果待确认；不因源码层次调整或构建通过就宣称权限问题解决。未自动grant权限、未改系统设置。

## 恢复标准按钮与入口澄清

用户继续反馈首页无反应，并问为何训练都未开放。新取得的21955日志实际记录 `ui_action=开始完整训练`，随后进入BLINK能力阻塞；旧日志另有直接点击闭眼休息。没有证据表明这些点击进入过BUTTERFLY/GAZE。独立只读复核未确认全局输入失效或面板状态冻结，故不把高亮层、remember内容或逐帧transform断言为根因。

为恢复已知标准控件基线，移除自定义HoverGroup/外框，保持28sp大字、76dp原生Button和深绿/奶油色对比，完全使用库内hover；首页将蝴蝶与眼球运动入口放在前面，明确闭眼项暂未开放。主动进入眼动训练时请求系统眼动授权一次，仍由用户选择允许；拒绝不计完成。

`native-button-recovery-build.log`：构建及62项测试通过；`native-button-recovery-style.log`：样式0错误0警告（已无此前R10边框）；替换安装/启动日志为 `native-button-recovery-install.log` 和 `native-button-recovery-launch.log`。新进程19663加载场景及模型成功。按钮加入仅开发日志的press/release/cancel/action区分，不向训练面板显示诊断文字。已请用户只点击新首页“蝴蝶漫游”验证实际路由及授权，目前结果待确认。

闭眼模式等待真实开合接口的产品决定保持不变；不能把这一限制扩展成两个眼动模式也被平台禁用，不能把未完成开发版冒充三个模式完整交付。

## 眼动授权实际通过与校准参考修复

用户反馈“进入了，并出现授权”。`native-button-recovery-runtime.log`已记录蝴蝶/眼动入口、重新对齐、结束和返回首页的实际press/action/release；`permission-after-dialog-package.txt`确认 EYE_TRACKING granted=true、USER_SET。当前原生按钮及授权路径通过本次观察，不再把缺少授权当作唯一待解决问题。

同时用 `permissionWaitCannotCaptureAnOldHeadReference` 复现：没有眼动数据时先捕获45度旧头姿，授权后正常朝前的姿态会被错误判为头动。`permission-baseline-red.log`失败，生产代码改为眼动和头姿都新鲜有效后才建立参考；`permission-baseline-green.log`构建及**63项测试通过**。已替换安装并启动（进程20293），尚待用户验证五点对齐。

后台仅在debug包、训练期间最多20次、每3秒记录quality/head/hit布尔判定、权限与SDK供应状态/回调数，避免在面板展示技术字样；不记录眼动坐标、四元数或原始轨迹。若仍失败，再按这些判定状态定位，不能把合成测试当设备验收。

## 中心参考获取及当前阻塞

用户反馈仍卡住；`alignment-progress-runtime.log`显示有效Eye/HMD流，但head=false。中心仅在眼动首次就绪时冻结仍过早：从看菜单到抬头看目标会超过限幅。新增 `centerCanReanchorAfterLookingUpButLaterTargetsStayLocked` 先复现失败；现在只在第一中心点未通过时允许重新取舒适头姿，通过中心后锁定，后续点绝不跟随转头重取。`center-reference-green.log`64项通过，已安装进程21393。

`center-reference-runtime.log`确认15:13:24第一中心通过，随后point2持续quality=true/head=false/hit=false。用户表示“只移动眼睛，头基本不动”，因此不把问题归咎于用户转头，也不直接放宽阈值。

当前诊断版进程22582增加区分HEAD_ROTATION/HEAD_POSITION/GAZE_OFF_TARGET等失败原因和相对限幅分档（0在限幅内，1≤2倍，2≤4倍，3>4倍），无原始姿态记录、面板无技术文字。新增两项分类测试，`alignment-diagnostics-green.log`66项及构建通过；已安装，等待用户再次停留对齐页取得分类日志。`alignment-diagnostics-runtime.log`初次读取时尚未进入训练，不能声称已取得旋转或位置偏差结论。

独立复核未发现headStable的四元数/位置数学错误或global→root转换偏离匹配官方示例。发现尚待处理的生命周期缺口：系统暂停后GazeRuntime仍保留旧基准；若重定位发生坐标跳变，第二点以后可能持续失败。匹配经验文档为draft且未注明精确build，不能直接套用其中等待时长。需结合失败分类验证，未将此可能原因冒充本次定论。


## 五点对齐通过与短暂头动修复（最新）

`alignment-diagnostics-runtime.log`（进程22582）15:26:15–19依次通过五点；15:26:19.840进入ACTIVE/PURSUIT，随后约17秒观察到有效Eye/HMD及命中。15:26:37进入INCOMPLETE；邻近诊断HEAD_ROTATION、turnBand=1、shiftBand=0。低频诊断不能还原失败瞬间的完整姿态，但源码明确任何单次headStable=false都会立即失败，这一行为已用回归测试复现。

现改为短暂头动暂停目标和计分，恢复首帧不补计时，连续偏移达到500ms才提示重试；未放宽3度/5厘米开发限幅，未用头动代替眼动。`head-adjustment-red.log`先复现，`head-adjustment-green.log`构建与67项JVM测试通过；`head-adjustment-install.log`、`head-adjustment-launch.log`确认替换安装并启动，当前进程23416。`head-adjustment-runtime.log`本次读取仅有启动和模型加载，尚无新训练操作，不能宣称60秒完成。

当前验收：用户确认按钮进入和授权弹窗；五点对齐已取得设备日志证据；60秒蝴蝶完整结果、眼球运动完整轮次仍待真机验证。闭眼继续等待真实开合接口。系统暂停后旧坐标参考重建仍有生命周期缺口，需单独修复和验证。


## 重试准备与平视左右布局（最新，2026-11-12 16:01）

用户指出点击重试必然先转头，要求倒计时及左面板/右岛屿/中央训练。按此明确指令实施局部调整，设计记录 `docs/superpowers/specs/2026-11-12-retry-and-spatial-layout-design.md`。

- Session增加PREPARING：重试后3秒，期间不判动作、不计有效训练时长；暂停/后台不消耗倒计时，结束首样本不补计时。目标回到中央并冻结，随后重新起步；没有放宽头姿阈值或自动重写校准参考。
- 面板不再训练时下移，而在共享头姿参考左侧（-1.15,0,-1.8米），保持0.75尺度并朝向用户。岛屿缩至0.45，测量现有资产包围盒后将视觉中心置于右侧（1.55,0,-2.4米），中央训练路径不变。非训练页首个有效头姿也用于平视布局。
- `retry-countdown-red.log`两项测试先复现失败；`retry-layout-build-tests.log`70项测试及APK构建通过。`retry-layout-style.log`0错误0警告；`retry-layout-architecture.log`通过。测试验证准备期间头动、暂停恢复、中央目标冻结及首帧运动计时；不替代佩戴体验。
- 已替换安装并启动，进程24231。`retry-layout-runtime.log`确认GLB和翼节点加载；场景视觉包围盒中心(1.55,1.6,-2.4)、半径(0.7794,0.5868,0.6094)米。当前进程crash buffer无条目。左右位置/文字可读性、倒计时体验与完整训练结果待佩戴者确认。
- 管理场景流程根run-1fd90052-e2ac-440f-b276-7754d780a5a7已提交代码，最终validate-delivery为blocked；独立Gradle构建通过不冒充管理流程全验收通过。响应见retry-layout-scene-*.json。

闭眼能力限制及系统暂停后旧坐标参考重建缺口保持待办；本轮不宣称三个模式已完成。


### 复核追加：继续也有准备期

只读复核指出左侧点击“继续”同样需要回正时间。已添加普通眼动暂停恢复的3秒PREPARING，并通过preparationRestartsPath=false保留原目标、路径时间及已累计训练时长；重试才复位中央/本轮时间。`resume-countdown-red.log`复现旧问题，最终`retry-layout-build-tests.log`72项测试全部通过。最终替换安装/启动见`retry-layout-final-install.log`、`retry-layout-final-launch.log`，进程25385；佩戴效果待用户确认。


## 2026-11-12 fixed placement and readable near panel (latest)

Wearer rejected the previous distance and reported movement on training entry. Device logs retry-feedback-runtime.log and retry-feedback-all-app.log confirm persistent transforms were rewritten from target.reference on entry/calibration. ScenePlacementUseCase now captures one fresh valid initial head pose, and panel/island transforms are written once, independently of calibration. Panel offset changed from (-1.15,0,-1.8)m to (-0.95,0,-1.2)m: distance2.13m to1.53m, scale0.75 to1.0. Body/button text28sp to32sp, button minimum84dp.

Read-only review caught a related target-occlusion risk: entering while facing the left panel previously anchored training forward behind that panel. Scene reference is now passed through Frame to GazeRuntime. Target origin stays at the fixed scene center; head-stability baseline can settle independently during center calibration without moving target/persistent entities. New tests cover locked placement, stale data, and entry from a38.4degree side heading.

fixed-layout-red.log and fixed-center-red.log record missing feature before implementation; fixed-center-build-tests.log:75 tests,0 failures/errors, assembleDebug passed. fixed-layout-style.log PASS,0 warnings. Final install/launch logs fixed-center-install.log and fixed-center-launch.log confirm replacement, preserving app data. Wearer readability and fixed placement acceptance still pending.

Managed revision first failed with SPATIAL_CODEGEN_GRADLE_WRAPPER_MISSING because inherited workspace was absent. Recovery used explicit existing native path; code submitted and built, final delivery validator still blocked. Responses saved in fixed-layout-scene-*.json; do not claim full managed workflow success.

Countdown feedback remains open: captured logs contain no Retry action or PREPARING phase. Asked wearer to distinguish missing countdown, immediate failure, or alignment blockage. No unsupported root-cause claim or completion claim.


## 2026-11-12 eye-movement manual transitions (latest)

Wearer reports butterfly countdown present and experience basically complete, but eye-movement retry countdown seemed absent. Scoped PID28135 log gaze-retry-feedback-runtime.log confirms HORIZONTAL round4 Retry at16:42:02.833, PREPARING02.862, ACTIVE05.910 (3.048s); subsequent rounds progressed to7. It also confirms repeated Skip immediately entered ACTIVE and failed about0.55s later while head remained away from neutral. Do not say the observed Retry omitted preparation; its visual visibility remains wearer feedback.

Session.skip now gives the next admitted non-BLINK round a3s preparation, independently of Retry/Resume which already prepare. This covers manual selection at the left panel without changing calibration/geometry limits. IslandContent uses one retained scroll state and returns to top on PREPARING so the countdown is visible; prior scroll occlusion was a risk, not proven by logs. Added bounded countdown second diagnostics (no raw gaze). Persistent layout and central target fixes preserved.

New skip preparation regression failed first in gaze-manual-transition-red.log. gaze-manual-transition-build-tests.log:76 tests,0 failures/errors; assembleDebug passed. Circle fixture now explicitly consumes preparation before testing the circle closure condition. Style verifier PASS0 errors/warnings. Installed and launched PID30084, logs gaze-manual-transition-install/launch.log. Updated Retry/Skip on-device wearer verification pending. Butterfly user feedback is recorded as experience feedback, not full acceptance of all modes.

Review follow-up: preparation scroll effect also keys on step index, covering repeated Skip while already PREPARING. Final76-test/build rerun passed; replaced/launched PID30526 (gaze-manual-transition-final-install/launch.log). Earlier PID30084 launch observation is historical.


## Wearer acceptance: eye-movement Retry and Skip countdown

2026-11-12: wearer explicitly confirmed both Retry and Skip display the countdown in eye-movement training. This closes the reported missing-countdown issue for these two operations. No inference of complete horizontal/vertical/circular session acceptance or all-mode completion.


## Close-and-rest navigation restored

BLINK restored as first home action, existing unavailable-state view retained until verified eyelid sensing exists. Source/build/style passed and replacement installed/launched (PID30761). Exact current SDK eye fields and cross-platform limits documented in BLINK_CAPABILITY_RECHECK.md. No tracking-loss-as-closure or timer-only substitute.


## Horizontal-layout correction (latest)

Launch pitch/roll had been included in the fixed room reference, which could lock in a slanted layout. ScenePlacementUseCase now preserves yaw/eye height only; panel and scenery share horizontal Y and upright orientation, still locked once.78 tests/build pass; installed/launched PID32156. Wearer verification pending. See HORIZON_LAYOUT_FIX.md.
