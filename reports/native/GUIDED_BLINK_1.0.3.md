# 1.0.3 闭眼休息：十轮语音引导

2026-11-26 用户明确决定不再检测闭眼，直接跟随语音闭眼、睁眼；此决定替代此前必须真实检测的验收要求。原设计包保留为历史输入。

## 局部修改

继承自然插画界面、DefaultStage、固定左面板、中央训练区、右岛屿、四圆角与不滚动要求。使用 spatial-design-to-app 的 incremental_patch 路径恢复首页现有闭眼区域的单项入口，修改训练页状态文字与记录标签。不改容器、空间布局、场景素材或其他训练的目光判定。

进入后先准备3秒，然后女声播报闭眼指引，播完保持3秒，再播报睁眼指引，播完放松2秒，循环10轮。复用已确认的自然女声，中英文分别播放。中文约150秒，英文约140秒，包含实际音频和调度时间。静音时使用相同音频的无声播放时序配合文字。语音加载或播放失败会暂停。未播放暗示检测成功的“已闭眼”旧素材。

用户暂停或退到后台会停止计时和语音，继续时先倒计时3秒，重新引导当前轮。闭眼项不启动眼动流，不申请眼动或面部权限。传感器观测不会使该项通过、失败或计时；初始头姿仍用于既有空间摆位。

## 记录含义

播完十轮记为引导流程完成，中途结束记为退出。记录类型为 BLINK_GUIDED 或 FULL_GUIDED；历史页标明语音引导，结果页说明不检测眼睛开合。旧记录不改写，无数据库结构变更。

## 验证结果

- 改动前新测试失败：原实现被能力门槛拦截。
- 改动后 testDebugUnitTest、assembleDebug 通过，117项测试零失败。
- 覆盖十轮、语音完成后才计时、保持与放松时长、传感器丢失、暂停恢复、过期回调、时间跳变、跳过与退出、完整休息转入眼动门槛，以及独立记录类型。
- SpatialUI局部风格检查通过，0错误0警告；未进行全量设计JSON保真比对。
- Swan PB3110MGL8190063B 已替换安装并启动1.0.3/code4，首页已记录 panel_fit 项为true，GLB加载成功。
- 佩戴后的语音与流程：2026-11-26 用户反馈“可以了”，接受当前版本；未把该反馈扩大为所有场景的完整验收。

## 文件与复现

源码位于 native/app/src/main/java/com/example/blinkblinkisland/dev：domain/usecase/SessionUseCase.kt 管理流程，data/audio/AudioGuide.kt 管理语音生命周期，ui/island/IslandScreen.kt 与 IslandViewModel.kt 编排前台事件，components/TrainingPanel.kt、IslandPages.kt 显示训练及记录。

在工程根目录执行：

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ANDROID_HOME='/Users/chanzungsan/Library/Android/sdk' ./gradlew --no-daemon --console=plain :app:testDebugUnitTest :app:assembleDebug
pico-cli app install app/build/outputs/apk/debug/app-debug.apk --replace --device PB3110MGL8190063B
pico-cli app launch com.example.blinkblinkisland.dev --activity .platform.LaunchActivity --device PB3110MGL8190063B
```

APK：app/build/outputs/apk/debug/app-debug.apk。构建、测试、安装、运行日志及校验值保存在本机 reports/native/guided-blink-2026-11-26/。原始日志不提交公开仓；GitHub版本说明见 RELEASE_1_0_3.md。
