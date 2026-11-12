# 原生开发与验证报告 — 2026-11-11

状态：已安装运行的原生开发版，未完成全产品验收。浏览器稿、能力探针和业务开发版是三个独立产物。

## 真机证据

设备 Swan B3110，序列号 PB311XHGKC220169G，Android16/API36。应用 `com.example.blinkblinkisland.dev`，Spatial BOM6.1.9。

用户先确认“场景和欢迎面板都能看到”，本轮又确认：

1. “记录正确，退出没有算完成”：首页选择闭眼休息、退出并保存，在本地记录中显示退出，未计为完成。
2. “文字清楚可读”：修复后的原生页面可读。

以上是佩戴者观察，不能扩展为所有页面、全部训练或舒适度通过。`runtime-final.log` 确认 GLB 加载和左右翼节点存在；`swan-visible.png` 是较早透明面板版，`swan-readable.png` 为全黑截图，不用作新页面视觉通过证据。

## 实现与验证层级

| 项目 | 实现 | 已验证 | 尚需验证 |
|---|---|---|---|
| 原生岛屿与蝴蝶 | 实际GLB加载、独立翼节点动画、SpatialUI面板 | 构建、加载日志、用户场景可见确认 | 动画时序、所有视角、帧率、舒适度 |
| 双语与音频 | 中英页面、音量/静音、MediaPlayer环境音/配音/音效 | 构建与代码检查 | 逐句听音、混音感受、配音授权 |
| 三项训练流程 | 状态机、重试/跳过/退出、数据失效暂停 | JVM测试 | 真实动作判定和校准；当前能力门槛阻止自动通过 |
| 几何判定 | 同坐标视线/头部姿态计算，拒绝错误样本 | 新增8项JVM测试 | 未接运行期；坐标转换、校准、设备阈值 |
| 本地记录/统计 | SQLite、唯一ID防重复、保存失败保留待重试 | JVM测试、Swan两个存储仪器测试、用户退出记录确认 | 全部结果路径及长时间使用 |
| 数据删除 | 二次确认、删除训练记录并保留设置 | 独立UUID数据库仪器测试 | 用户真实操作体验，未自动删除用户数据 |
| 佩戴提醒 | 纯规则时钟、间隔30–120、稍后10分钟、当地次日静默 | 合成“已验证佩戴”输入测试 | 后台佩戴来源、后台运行与通知展示 |
| 通知路由 | 原生开发版显式start intent；诊断按钮15秒发送 | 构建；探针中心展示+start曾实测通过 | 业务包通知、冷启动、later/today、横幅 |
| 用户资料 | 本地默认头像与身份 | 构建 | PICO正式用户中心接口、App ID与授权 |
| 三种输入 | SpatialUI手势按钮；公开追踪流 | 探针单次手势操作 | 凝视停留未实现；控制器尚未通过 |

## 自动化证据

- `delivery-build-test.log`：`testDebugUnitTest assembleDebug` 成功。合计 **39项 JVM测试，0失败**（Session16、Reminder6、ViewModel9、Geometry8）。
- `storage-instrumentation.log`：B3110 上 **2项仪器测试通过**，覆盖 SQLite 关闭重开、重复ID、防重复、删除持久化和设置保留。该次先于本轮纯几何模块，存储代码此后未修改。
- `geometry-red.log`：先编写测试时几何模块不存在，编译失败；`geometry-green-build.log` 实现后8项通过。测试输入为数学姿态，不是设备采样。
- `review-regressions-red.log`：审查发现4个行为问题后，31项中4项失败；`review-regressions-green.log` 修复后通过。
- `architecture-delivery.log`：最新架构检查通过；`design-style-delivery.log`：最新样式检查通过。
- `implementation-scan-final.log`：模板元数据和import修正后实现扫描通过；仍有工具白名单未收录公开SDK类型、组件种类较少等警告，不等于全流水线clean。
- `delivery-install.log`、`delivery-launch.log`：本轮最终APK替换安装、启动成功；`delivery-runtime.log`：16:48:04 GLB及左右翼节点加载成功；`delivery-crash-buffer.log`：本次进程18362的crash buffer无条目。只说明这次启动窗口未观察到该进程崩溃。
- `environment-recheck.json`：CLI0.6.1、runtime/common doctors通过；总状态PARTIAL，plugin doctor为0错误/1警告。无setup、升级或自动修复。

代码审查已修复：数据库写入前标记成功导致无法重试、旧终止会话覆盖通知首页路由、初始化时切语言覆盖持久化设置、退出时遗漏当前已开始动作。失败记录保留不可变快照，重试沿用同一ID，避免保存重复。

## 未通过的管理工作流检查

不能声称整个官方流水线clean：

- 新工程验证缺少历史 `.scratch/onboarding_handoff.json`；未伪造收据。
- 后续existing_module扫描发现模板Mixed Stage遗留无效immersion=100和通配import解析错误；源码已将Mixed对应值规范为0、改为明确公开import，保持style=1。
- `installed-module-validation-final.log` 的 `user_package_passed` 与 `visual-asset=true` 调用组合被收据检查拒绝。用户输入是Markdown设计规范与HTML原型，不是工具要求的无视觉JSON设计包；没有通过改标签冒充Designer验收。
- 管理式3D代码生成曾通过源码与build，`native_scene_load` 自定义管理验收项未被流程确认；后续实际运行日志与用户观察单独列为设备证据，不篡改管理状态。

## 下一阶段

1. 将同坐标目标位置、眼动与HMD姿态接入几何模块，加入显式校准和安全退出；完成头稳眼动/眼稳头动、目标偏离、重定位、失权、失焦与恢复的真机对照，才启用眼动训练通过。
2. 取得适用于Swan Spatial/Kotlin的公开眼睑与后台佩戴能力依据。未确认前不以追踪丢失代替闭眼、不以应用运行代替佩戴时间；若确无接口，再就降级方案作产品决定。
3. 验证通知展示策略、冷启动与三个动作；接入PICO用户中心并保留本地降级；补齐停留输入与控制器测试。
4. 完成音频、性能和舒适度验收、资源授权、正式身份与签名后再出发布安装包。

## 本轮交付

- `deliverables/BlinkBlinkIsland-native-dev-debug.apk`：最终调试安装包；已替换安装到Swan。
- `deliverables/Blink_Blink_Island_Native_Dev_Source.zip`：原生源码、交接资料、审阅资源、制作脚本和阶段证据；含逐文件 `SOURCE_INVENTORY.json`。
- `deliverables/NATIVE_SHA256.json`：以上两个文件的SHA-256；生成脚本 `scripts/package_native.py` 校验ZIP可读，并排除缓存、本机SDK路径文件和密钥文件。
