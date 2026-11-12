# 重置后重新安装 — 2026-11-12

用户已明确确认设备重置完成，并要求重新安装。本轮恢复此前保留的训练修复；不再将旧身份修复APK作为训练修复版。

## 安装前验证

- `post-reset-environment.json`：runtime通过，整体PARTIAL，公共/Spatial检查有警告；未运行setup、更新或修复环境。
- 初次重新发现设备时 `pico-cli device list --format json` 返回空数组；已请用户连接USB、重新开启重置后所需的开发者/调试选项并确认电脑授权。不从重置前的序列号推断当前连接。
- `post-reset-build-tests.log`：`testDebugUnitTest assembleDebug` 成功，**62项JVM测试全部通过**。
- 先前唯一失败的旧测试错误地要求合法移动时间仍为0；现明确区分活跃时间和终点停留，断言合法移动计入activeMs/stepMs、但targetIndex/completed不推进。未删除对误判的保护。
- 新增负例：成功对齐后，眼睛与头部旋转相同且小幅头动仍在容差内时，即使普通几何命中为true，相对眼动判据也必须拒绝。新增圆周换轮位置连续性回归。
- `post-reset-apk-badging.txt`：中文名称“眨眨岛”、英文名称“Blink Blink Island”，原品牌图标保持。
- 独立只读复核确认原两项误判已修复，未发现本次范围内新增Critical/Important阻断问题；未以代码审查冒充真机阈值验收。

## 本包范围

蝴蝶和眼球运动已接入公开SDK Eye/HMD姿态、同一root坐标转换、五点视线对齐、可见目标和动作判定。目标路径、有效移动覆盖与相对眼动修复已通过合成姿态测试；**尚未获得重置后真机训练验收**。

闭眼模式按用户要求，仍等待真实眼睑接口；不计时替代、不引入手动确认完成。整套应用尚不能作为三模式全部完成版。

## 安装结果

用户随后确认已连接并允许调试，重新发现真实Swan B3110，序列号PB311XHGKC220169G、Android16/API36、系统显示6.2.0、语言zh-Hans-CN。

- `post-reset-devices.json`、`post-reset-device-info.json`、`post-reset-device-build.txt`：重置后新取得的连接与系统信息。
- `post-reset-install.log`：安装成功。安装前已核对deliverables APK与本次通过测试的Gradle输出字节一致，且SHA与NATIVE_SHA256.json一致。
- `post-reset-launch.log`、`post-reset-pid.txt`：启动成功，进程10862。
- `post-reset-runtime.log`：14:11:21启动6.1.9原生开发版；14:11:29训练蝴蝶、岛屿GLB及左右翼节点加载成功。
- `post-reset-crash.log`：本次进程10862的crash buffer无条目，只代表该观察窗口未见崩溃。
- `post-reset-device-label.txt`：实际设备资源解析为“眨眨岛”。图标与训练视觉仍需佩戴观察，不以日志替代。

未自动授予眼动或通知权限；首次使用须由佩戴者在头显内重新确认。已请求蝴蝶模式五点对齐和运动反馈，尚未得到结果。闭眼仍不可用，不宣称三个模式均已验收。
