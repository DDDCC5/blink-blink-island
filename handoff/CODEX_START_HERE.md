# 给 Codex 的执行提示词（粘贴全文）

我正在开发 PICO Swan 的 PICO Spatial 应用「眨眨岛 / Blink Blink Island」。你收到的目录是素材与设计交接包，请先读 README.md、DESIGN_SPEC.md、assets/ASSET_INDEX.md、content/VOICE_SCRIPT.md 和 Blink_Blink_Island_设计与开发交接包.md，打开 prototype/index.html 查看完整交互稿。遵循我给的 PICO CLI 安装指南 https://lf3-static.bytednsdoc.com/obj/eden-cn/bvonuhmlkbps/other/pico-cli-installation-guide.md 。你要持续完成后续开发，不能把已交付的浏览器原型当作 PICO 应用。

工作顺序：
1. 检查已有工作区、node/npm/git/PICO CLI 及现有工程；查当前 CLI 帮助和 PICO 官方文档。若须安装/运行 setup，依指南先给出具体路径、region、scope、agent tool=codex、platform=spatial、完整命令和影响范围，让我批准后执行；不要擅自改我的机器或项目文件。
2. 用 Swan 真机和官方文档验证后台累计佩戴使用时长、跨应用通知与通知点击路由、眼动与眼睑开合数据、头部姿态、三种输入、PICO 用户中心资料；给我能力矩阵及可复现的最小验证程序。任何未验证能力标记待验证，不要伪造 SDK API。若功能被限制，给出确切限制和可行方案，再与我决定。
3. 先检查本包素材：SVG、WAV 是已有可导入的原始素材；概念稿不是可直接导入的 3D 模型。补做设备适用的 3D 岛屿场景、蝴蝶动画、交互态和中英配音，并把制作结果与交互原型交我审阅。获得设计确认后正式开发业务功能。
4. 建立并实现 Spatial 项目：双语、舒缓场景与音频、完整/单项三项训练、追踪质量暂停、动作判定与重试/跳过、佩戴时长提醒、通知直达、PICO 昵称头像与本地降级、离线本地记录统计与数据删除。参数按 DESIGN_SPEC.md；不可把虚拟远近物像当成真实对焦，不可把眼闭合当作力度测量。
5. 逐项真机验证并交付源代码、构建说明、安装包及验证报告。不要把原型手动 Next 按钮视为训练判定已经实现。

每一阶段报告文件位置、验证依据、已实现与待验证事项；没有真机时仍可完成可独立推进的项目与设计工作，但要明确哪些验收尚需真机。
