# 1.0.8 / versionCode 9 · 小岛训练联动

2026-11-27，开发包 `com.example.blinkblinkisland.dev`，Spatial SDK6.1.9。未升级SDK或安装新工具链，未新增Tripo积分消耗。公开版本标签为 `v1.0.8`，发布摘要见 `docs/releases/v1.0.8.md`。

## 已实现

- 蝴蝶从固定岛屿中的陪伴位置沿曲线飞入中央；小球从岛上花丛位置沿轻缓弧线送出。
- 入场使用三秒准备时间，不累计有效练习。原有计分轨迹、目光规则保留；暂停和重试不会触发回岛。
- 训练自动完成后，目标停留在面前，结果页提供“结束休息，送它回岛”。用户明确结束或返回选择后，目标才从当前显示位置回岛。
- 中途结束仍记录为退出，保存与动画分开；重复结束不会重置回程或重复保存，后台时间不直接跳过动画。
- 完整休息的类型切换单独交接：旧目标先回岛，再送出新目标，交接不计分、不额外创建记录。岛上蝴蝶参与训练时隐藏陪伴副本。

## 已验证

- `reports/native/v1.0.8/build-final-2.log`：Debug/Release构建成功；16套、137项JVM测试，失败和错误均为0。
- 新测试覆盖入场不计分、自动完成等待确认、重复结束、暂停恢复、后台间隔、类型交接、入场中跳过、交接中到终态，以及一次记录保存。
- `architecture.log`、`design-style.log`：均PASS，零警告。最终只读复核未发现剩余P1/P2。
- 用户要求先在模拟器测试：在独立 `Pico_Emulator_6_1` / `emulator-5558` 成功安装并启动1.0.8；进程3320，原生首页、岛屿、两个0.72秒骨骼控制器及首次固定布局均加载成功。HOME文字与按钮fit探针全部为true。证据为 `emulator-5558-scene.log`、`emulator-5558-info.json`。
- 普通显示截图 `emulator-display.png` 实际包含原生面板和岛屿；左面板部分位于当前摄像机视野外，不能据此认定内容裁切。该ROM不支持空间截图，未伪称spatial模式成功。

## 未通过或待验证

实际飞入、回岛、暂停后的动画衔接尚未完成端到端模拟器验收。该ROM的空间查询返回 `ROM_BACKEND_UNAVAILABLE`（缺少 `uiautomator spatial`），摄像机接口缺少 `/system_ext/bin/pvrtracking_inject`；运行日志还出现 `EyeTrackingProvider: create Eye tracking provider failed`，本轮没有可用于真实训练的有效眼动证据。不能可靠自动操作空间训练和注入姿态。未用二维ADB坐标点击替代空间交互，也未修改应用添加绕过判定的测试入口。

另一个旧实例 `emulator-5554` 的启动请求停留在系统 PendingLaunchItem，未作为本次成功运行证据。未操作其他项目的前台模拟器。

137项测试使用可控观测和时钟验证业务逻辑，不代表真实目光或主观视觉验收通过。Swan当前离线，按用户指示暂不安装真机；真实眼动、入场/回岛自然度和完整佩戴流程仍待验收。

正式design_fidelity仍缺少完整实现映射，本次不宣称该门禁通过。设计IR已同步入场、回岛及结果页结束动作，540节点、19状态，通过Schema。

## 文件与复现

- 设计：`docs/superpowers/specs/2026-11-27-island-training-handoff-design.md`。
- 实现：`native/app/src/main/java/com/example/blinkblinkisland/dev/domain/usecase/TargetJourneyUseCase.kt`，以及IslandViewModel、IslandScene、TrainingPanel、SummaryPanel。
- 测试：TargetJourneyUseCaseTest、IslandViewModelTest。
- 安装包：`deliverables/v1.0.8/BlinkBlinkIsland-1.0.8-debug.apk`。
- Release：`deliverables/v1.0.8/BlinkBlinkIsland-1.0.8-release-unsigned.apk`，未签名。
- 源码归档：`deliverables/Blink_Blink_Island_Native_Dev_Source.zip`；哈希见 `deliverables/NATIVE_SHA256.json`。

构建使用既有JDK21，运行 `./native/gradlew -p native :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease`。动态验收顺序：选择蝴蝶→入场→暂停/继续→结束回岛；选择眼球运动重复；再测试完整休息的类型交接和结束页等待确认。执行动态验收需要可操作的模拟器ROM或Swan，不应把手动Next算作目光判定。
