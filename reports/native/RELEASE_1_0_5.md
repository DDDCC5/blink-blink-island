# 1.0.5 开发与验证记录 — 2026-11-27

versionName **1.0.5**，versionCode **6**。包名 com.example.blinkblinkisland.dev，SDK BOM 6.1.9。用户同意下一版实施快速休息、节奏选择、七天统计和精简重复配音。本次未推送GitHub。

## 已实现

- 首页增加快速休息，约2–3分钟：3轮闭眼语音引导、30秒有效蝴蝶跟随、水平/垂直/顺时针/逆时针各1轮。实际用时随目光跟随和暂停变化。完整练习保留原10轮闭眼、60秒蝴蝶及10/10/5/5轮眼动。
- 开始前设置页可选择短/完整练习、舒缓/标准。舒缓使用标准75%的路径时钟速度和80%的横纵幅度，深度不变；标准保留原参数，不重新启用头部陀螺仪限制。记住上次组合计划和节奏，训练内不显示切换入口。
- 记录页提供七天统计：今天与近七天次数、有效练习分钟数、七日条形趋势。按开始时间的本地日历日期归类，排除零练习时长、七天之前及未来日期的记录。部分退出但实际练习过的记录计入次数，完成状态仍保留退出。引导单项、目光单项、组合计划分开计数，不拆猜旧组合记录中的动作份额。
- 闭眼首轮使用原完整说明，后续轮次使用简短“轻轻闭上眼睛 / Gently close your eyes”。同一套已确认的离线女声、同样的音量处理。中文2.1472秒、英文1.8733秒；原睁眼语音继续复用。语音完成后才保持3秒，暂停/后台/继续准备等保护保留。
- 设置数据库从v1增量升级至v2，仅追加pace和last_plan；旧记录、语言、音量和删除语义保留，无清库迁移。

## 界面与空间结构

按既有设计作局部界面修改，沿用PicoTheme、SpatialUI按钮和进度条、奶油黄图标、自然插画与固定1100×1120面板。新增页仍在原面板中切换，保留四角圆角、不滚动、返回入口和固定场景。未增加空间容器、移动面板或新3D素材；未声称全量设计JSON保真验证。

## 验证依据

- Debug、Release构建通过，127项JVM测试零失败。覆盖原有训练判定、新短计划、舒缓路径、时区/日期边界、分类、空记录、准备页与偏好保存。
- emulator-5556上3项Android存储测试通过，其中旧v1数据库迁移后保留记录与语言，并验证新偏好跨重建持久化。
- 模拟器原生界面实际操作：中英文准备页、舒缓短练习启动、闭眼1/3到3/3自动推进、暂停与恢复3秒倒计时、退出保存为EXITED、历史入口、七天统计及返回可见。退出记录显示完成2轮且整体为退出，统计含有效练习0.5分钟。没有用手动Next当动作判定。
- 中英文新设置页内容完整，最终英文统计页已用深绿进度条改善对比度。一次SUMMARY页面切换的瞬时边界日志随后恢复为true；稳定截图内容完整。
- SpatialUI局部风格检查0错误0警告。截图为真实模拟器空间渲染，未用网页替代。
- Swan已替换安装并启动1.0.5/code6，设备日志确认首页和GLB加载。旧记录与设置通过增量迁移读取；不卸载或清数据。

## 待佩戴确认

新增舒缓节奏的舒适度、两种语言简短提示的听感、真机新增页可读性，以及短组合完整眼动段的端到端体验仍待用户确认。模拟器和合成规则测试不能代替这些验收。

## 文件

源码在 native/app/src/main/java/com/example/blinkblinkisland/dev：SessionOptions.kt、SessionUseCase.kt、GazeRuntime.kt、WeeklyStatsUseCase.kt、SqliteIslandRepository.kt、IslandViewModel.kt、SessionPanels.kt。配音源稿 content/VOICE_GUIDED_SHORT.md，生成器 scripts/generate_natural_voice.py，审阅音频与波形/模型校验记录 review/audio/guided-short/。

本机构建、安装、测试、运行日志、APK SHA256及模拟器截图位于 reports/native/v1.0.5/。调试包为 app/build/outputs/apk/debug/app-debug.apk；Release为 app/build/outputs/apk/release/app-release-unsigned.apk，需要发布签名。

根目录复现：`./gradlew testDebugUnitTest assembleDebug assembleRelease`。存储测试APK由 `./gradlew assembleDebugAndroidTest` 生成；只在指定模拟器运行Instrumentation，未向真实用户库写入合成测试记录。
