# 1.0.4 图标优化 — 2026-11-27

用户反馈旧图标外绿、内黄，底色重复。先预览单一薄荷绿底后，用户选择浅奶油黄方案。本次修改原SVG，去掉内层圆盘及阴影，采用铺满方形画布的统一浅奶油黄（#F6F5E9）。蝴蝶、闭眼弧线与岛屿的几何、颜色保持一致，不增加圆角矩形底板。

同步更新普通启动图标、应用内复用图标、提交图标PNG和PICO空间图标背景层。空间图标前景和官方Configurator生成的两张SDF逐字节保持不变；系统启动器最终外形仍由PICO系统决定。

版本为1.0.4 / versionCode 5。Debug和Release构建通过。普通及背景图源尺寸均为1024×1024；Debug直接校验PNG字节，Release经aapt优化后解码为RGBA像素比对，验证新素材确实进入安装包。验证JSON在本机 reports/identity/icon-1.0.4-verification.json。

源码：review/identity/app_icon_square_cream.svg、icon_background.svg；green历史文件名作为打包兼容别名同步更新，内容为新奶油黄方案。
导出：scripts/export_launcher_identity.py，使用已有resvg-py或macOS自带sips，无需安装新依赖。prepare_identity_layers.py也移除了旧圆盘，避免后续生成时恢复旧背景。

本次没有安装到设备；启动器实际显示尚需设备确认。训练逻辑与音频未修改。GitHub版本说明见 reports/native/RELEASE_1_0_4.md。
