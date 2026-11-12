# Blink capability probe — 原生 Spatial 最小验证程序

这是真正的 Kotlin/Spatial SDK 工程，不是网页封装，也不是业务应用。当前 BOM **6.1.9**，包名 `com.example.blinkisland.capabilityprobe`，入口 `.platform.LaunchActivity`。当前已构建、安装并启动到 Swan；用户确认佩戴后面板可见；Eye/Head/Hand公开Flow接收有真机日志证据，通知中心展示及一次start点击路由已获用户/日志验证；其他通知行为及训练判定仍待验证。

## 本机复现

在本目录运行：

```sh
JAVA_HOME="/Users/chanzungsan/.pico/primer-cli/jdk/jdk-21.0.12.1+1/Contents/Home" ./gradlew :app:assembleDebug
```

APK：`app/build/outputs/apk/debug/app-debug.apk`。本机 Android SDK 路径由 CLI 生成到 `local.properties`，其他电脑应重新按官方指南配置，不复制本机路径。

当前机器有两套同名CLI，以下全部使用已核验的0.6.1绝对路径：

```sh
/Users/chanzungsan/.local/lib/nodejs/node-v22.23.2-darwin-arm64/bin/pico-cli app install \
  "/Users/chanzungsan/Documents/ChatGPT/Blink Blink Island/probes/spatial/app/build/outputs/apk/debug/app-debug.apk" \
  --replace --device PB311XHGKC220169G
/Users/chanzungsan/.local/lib/nodejs/node-v22.23.2-darwin-arm64/bin/pico-cli app launch \
  com.example.blinkisland.capabilityprobe --activity .platform.LaunchActivity --device PB311XHGKC220169G
/Users/chanzungsan/.local/lib/nodejs/node-v22.23.2-darwin-arm64/bin/pico-cli app logcat \
  --package com.example.blinkisland.capabilityprobe --device PB311XHGKC220169G \
  --tag BlinkCapabilityProbe --lines 200
```

首次安装与启动已获用户批准。上面的 replace 是只替换本测试包；不含卸载、清除数据、自动授予权限、修改系统配置。

## 真机操作步骤

1. 佩戴并唤醒Swan。确认出现“眨眨岛·能力探针”面板；全黑截图不能算通过。
2. 按“请求权限”，自行确认眼动和通知权限。拒绝权限也应记录为测试结果，不能用ADB强行授权。
3. 按“开始追踪”。观察Eye、Head、Hand、Controller的supportState/state及tick计数。tick是公开Flow的回调计数，不是训练成功数。
4. 依次做眼动、轻微头动、控制器操作和手势操作；标注发生时间。不要以零值或缺失样本推断闭眼。关闭/重开追踪以及离开Stage，核查停止清理。
5. 用手势与控制器各点击“选择测试”10次；记录输入类型、成功、漏点、误触。若系统提供dwell，再独立测试；本探针没有自定义dwell进度。
6. 点“15秒后测试通知”，切换到另一应用。观察是否出现通知、是否有三个动作。检查正文、Start/Later/Today分别回到对应route。各自重复热启动与进程被回收场景。日志里的notification_posted只说明调用成功，不证明显示成功。
7. 新增“高重要性通知测试”使用独立IMPORTANCE_HIGH通道，15秒后发送；切换其他应用观察是否主动弹出。它不修改默认通道，不证明后台调度。连续点测试只保留最后一个待发送任务，避免叠加。
8. Proximity仅作候选诊断。佩戴/摘下/遮挡传感器需对照，不把读数当作正式wear事件。当前未实现后台佩戴累计。
9. 眼睑开合与PICO用户中心API尚未确认，**本程序不伪造这两项接口**。待取得公开文档与权限后扩充。

## 代码位置

- `content/HomeStage.kt`：SpatialUI面板、公开追踪Provider、Flow计数、生命周期清理。
- `platform/ProbeActions.kt`：在实际面板Activity上请求权限；进程内15秒通知候选实验。
- `platform/LaunchActivity.kt`：冷/热启动通知路由；proximity可用性诊断。
- `ProbeState.kt`：仅内存诊断状态，无眼动/资料持久化。

不会生成任何训练成功记录。通知的Later/Today只验证路由，不执行延后或静默业务。

## 已知问题与验证界限

- CLI默认选择的6.2.8在本Swan运行时崩溃，原始日志在`../../reports/probe-crash.log`；回退6.1.9可启动。
- 初始capture是全黑；随后用户确认佩戴后面板可见。
- 启动Activity和Stage实际Activity不同，权限/通知按钮已改用实际Activity，不依赖LaunchActivity强转。
- 面板可见性已获用户确认；不据此推断所有控件、输入、通知或训练逻辑通过。
- 当前根知识selector是6.2，探针是6.1.9。本轮直接使用6.1本地文档和编译结果；未改托管配置。
- 无后台服务、无进程重启计时、无账号SDK；通知Handler不是后台调度方案。
