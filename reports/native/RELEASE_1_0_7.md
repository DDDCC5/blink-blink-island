# 1.0.7 / versionCode 8

2026-11-27，开发测试版，包名 `com.example.blinkblinkisland.dev`，Spatial BOM 6.1.9。已替换安装到 Swan PB3110MGL8190063B，无卸载或清除本地记录。

## 本轮修改

- 删除应用内“摆正位置”入口、回正倒计时及重置路径。保留启动时一次水平固定布局，调整视角使用系统长按回正。训练准备和重试倒计时保留。
- 蝴蝶从每33毫秒暂停寻帧改为原生渲染器连续播放；与真实目标时钟校准相位和速度，暂停、追踪丢失或目标消失时停止。
- 交付蝴蝶为7骨骼、4轨道的 NaturalWingbeat，0.72秒包含四次非对称扑翼，加入轻微翼尖滞后；身体倾角约50°，沿飞行切线转向并围绕身体纵轴倾转。
- 训练计分轨迹、目光判定和记录逻辑未因本轮修改改变。闭眼休息仍为用户选择的语音引导。

## 验证依据

- `reports/native/v1.0.7/build-final.log`：Debug、Release构建成功；127项JVM测试通过，无失败或错误。
- `reports/native/v1.0.7/design-style.log`、`architecture.log`：均通过，零警告。
- `review/tripo-storybook-2026-11-27/anatomical-rebuild/rig-verification.json`：7骨骼、4轨道、权重归一、时间递增、四元数单位长度与循环首尾一致。
- `exports/manifest.json`：蝴蝶8509三角面，岛屿36908实例三角面，原生含两只蝴蝶共53926实例三角面；模型无外部资源依赖。
- `swan-install.log` / `swan-package.txt`：安装成功，设备读回版本1.0.7/code8。
- `swan-scene.log`：两个骨骼控制器均 valid=true、duration=0.72；陪伴蝴蝶连续播放 paused=false；场景加载和首次布局锁定成功。
- 前序1.0.6在6.2模拟器的隔离SQLite测试3项通过（`reports/native/v1.0.6/storage-isolated-tests.log`）。不代表该模拟器已通过完整Spatial视觉兼容验证。

## 待验收与限制

最新飞行自然度、真实目光跟随中的暂停恢复、系统长按回正效果仍需佩戴确认，已向用户请求体验反馈。原生日志成功不等于主观体验通过。

正式 design_fidelity 检查仍因缺少完整实现映射未通过；不能宣称17状态的JSON与原生逐节点一致。Editor托管打包失败及直接GLB方案依据见 `TRIPO_INTEGRATION_TOOLING.md`。本轮没有新增付费生成，总计仍为430 credits。

参考 [Henningsson & Johansson 2021](https://pubmed.ncbi.nlm.nih.gov/33468023/) 的柔性翼和上下扑差异；动画频率为视觉参数，不声称精确生物力学仿真。

## 交付位置

- 源码：`native/`；模型及可复现绑定：`review/tripo-storybook-2026-11-27/`。
- 安装包：`deliverables/v1.0.7/BlinkBlinkIsland-1.0.7-debug.apk`。
- Release构建：`deliverables/v1.0.7/BlinkBlinkIsland-1.0.7-release-unsigned.apk`，未签名。
- 源码归档：`deliverables/Blink_Blink_Island_Native_Dev_Source.zip`；哈希：`deliverables/NATIVE_SHA256.json`。

本轮尚未提交或推送GitHub。
