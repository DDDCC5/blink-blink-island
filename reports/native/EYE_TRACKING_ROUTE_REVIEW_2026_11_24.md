# 官方视线追踪文档核对与闭眼技术路线

2026-11-24。用户提供：https://developer-cn.picoxr.com/document/spatial-sdk/eye-tracking/

## 在线核对

Web文本抓取失败后，通过真实浏览器打开并阅读完整正文。页面标题“视线追踪”，版本选择显示Spatial SDK6.2.x；没有用当前在线版本冒充项目6.1.9。

该文档说明获取视线位置和方向：Full Space、EYE_TRACKING运行时权限、EyeTrackingProvider.start/stop、dataFlow或latestData，以及eyePose.position/rotation向场景root的坐标转换。示例小球随视线位姿移动，用于可视化视线方向。未给出眼睑开合、双眼闭合、blink、开合有效性或闭眼时的状态语义。

结合准确版本6.1.9公开API与本地官方源码，当前仍没有已验证的真实眼睑数据源。文档没有证明Swan硬件不能感知眼睑，也没有提供可用的Swan Kotlin眼睑接口。

## 与工程对照

- native/gradle/libs.versions.toml：BOM6.1.9；未升级。
- TrackingBridge.kt：已使用EyeTrackingProvider、dataFlow、start/stop及同类全局到root坐标转换；基本路线与文档一致。
- 蝴蝶/小球训练：目标沿独立路径运动，再计算真实视线命中。不能照搬文档“随视线移动的小球”当训练目标，否则目标始终跟着用户视线，无法证明用户追随了指定轨迹。
- DataProvider的supportState/state是数据源支持和运行状态，不能视为睁眼/闭眼状态；dataFlow没有数据、latestData不变也不具有闭眼语义。
- 当前TrackingBridge接收时刻与源timestamp的区分可作为后续追踪稳定性改进单独核实；它不能补出眼睑数据。

## 路线决定建议

| 路线 | 判断 |
|---|---|
| 保留Spatial/Kotlin + EyeTrackingProvider做目光训练 | 推荐，已有公开依据和现有工程 |
| 将眼睑识别作为独立能力，与目光姿态分开接入 | 推荐的架构边界；必须有正式Swan数据源后才能实现运行期接入 |
| 用状态变化、数据断流或方向固定推断闭眼 | 不采用，不能与失效/遮挡/丢帧等情况可靠区分，也不符合用户已确认要求 |
| 为闭眼直接升级SDK或换Unity/OpenXR | 当前证据不足，不执行迁移；其他平台的方法名并不能证明Swan可用或可与本应用并行 |
| 用语音倒计时、手动确认替代闭眼识别 | 用户已拒绝，不改变该决定 |

结论：主应用技术路线无需整体重做；应调整能力边界和开发依赖。目光训练继续沿用官方Spatial接口；闭眼训练暂不能以这份文档为依据解锁。真实闭眼验收必须等Swan正式眼睑数据来源，之后验证闭眼仍有效的连续采样、双眼状态、自然眨眼、提前睁眼、遮挡/摘下、权限拒绝和恢复。

此前118项单元测试只证明合成观测下的训练规则和回归，不能代表真实闭眼识别已完成。本次只做文档与路线审查，没有修改应用代码、安装、变更SDK或发布。
