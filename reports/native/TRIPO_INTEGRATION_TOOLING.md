# Tripo 原生接入：工具与API依据

2026-11-27。项目保持Spatial BOM6.1.9、既有Stage与SDK工具链，未升级或安装依赖。

## 导入工作流的实际结果

Codegen根Run `run-abcd8725-24a4-4b20-a968-4b35ee4efe25` 在选择后端时终止：`SPATIAL_3D_BACKEND_CAPABILITY_MISMATCH: imported models require editor`。没有产生构建成功证据。

按恢复指南转入Editor导入检查，根Run `run-40b143c7-5d95-4811-9757-97188a12a7b3` 在建立会话时终止：`EDITOR_SESSION_SAVE_FAILED`，无法连接远程prompt settings服务取得文档令牌。工具保留PID94158；未强制关闭其他项目的Editor，未伪称已打包bundle。终止Run没有可用恢复Handoff。

降级范围：不交付Editor bundle；原生应用继续采用其原有自包含GLB资源加载方式。两个模型及贴图已经完整生成、审阅和静态核验，SDK6.1本地官方动画文档明确支持glTF骨骼动画。后续是否可运行以实际编译、原生加载及设备证据为准，不用网页播放代替。

## SDK版本依据

知识图谱查询只返回6.2资料；未将其作为6.1.9可用性的依据。随后读取本机6.1官方文档 `spatial-sdk_animation_skeletal-animation.md`、`spatial-sdk_animation_animation-composition-and-playback-control.md`，以及6.1样例的SkeletalAnimationCaseEntity。

安装的 `com.pico.spatial.core:core:6.1.9` sources.jar 进一步确认公开的findSkeletonRootEntity、getAnimationResources、playAnimation、AnimationPlaybackController.pause/setTime/getDuration/close。setTime的单位为秒；官方6.1文档说明可结合pause用于静态时间预览，相关操作在主线程执行。未依赖私有或反编译API。

保持原有真实目光训练、语音引导闭眼、本地数据库和固定空间锚点。训练蝴蝶的动画时间由真实目标motionMs驱动，陪伴蝴蝶独立累计可播放时间，均须实机验收。
