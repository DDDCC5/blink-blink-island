# 1.0.4 开发版 — 2026-11-27

Android versionName **1.0.4**，versionCode **5**。包名 `com.example.blinkblinkisland.dev`，Spatial SDK BOM 6.1.9。

## 版本说明

- 图标改为统一浅奶油黄背景（#F6F5E9），去掉外绿内黄的双重底色、内层圆盘及其阴影。
- 保留蝴蝶、闭眼弧线与岛屿，让主体更突出；方形素材铺满底色，不预制圆角矩形。
- 同步普通启动图标、应用内复用图标、PICO空间图标背景及GitHub展示素材；保留官方空间图标前景和SDF。
- 导出脚本支持已有resvg-py或macOS自带sips，无需新增环境依赖。
- 保留1.0.3的十轮闭眼语音引导、暂停与恢复倒计时、本地记录、自然插画界面和目光跟随训练。

## 验证

Debug、Release构建与117项JVM测试通过。安装包版本为1.0.4/code5。检查两种APK的打包图标，Release图像经资源优化后以解码像素校验。此轮未安装到设备，不声称启动器显示已获真机验收。

仍为开发版，后台累计佩戴提醒与PICO用户资料等能力的原有待验证状态保持。Release产物未签名，需要平台或自有签名后发布。

构建：仓库根目录 `./gradlew testDebugUnitTest assembleDebug assembleRelease`。图标来源及导出说明见 [图标报告](../identity/ICON_1_0_4.md)。
