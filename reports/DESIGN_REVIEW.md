# 眨眨岛 — 设计审阅 v0.1

日期：2026-11-11。**待用户确认；尚未正式开发业务功能。**

## 打开与复现

从工作区启动 `python3 -m http.server 8765 --bind 127.0.0.1`，访问 [审阅页](http://127.0.0.1:8765/review/index.html)。该网页用于审阅资源与状态，不是PICO应用。

- `review/index.html`：实际GLB三维预览、独立翼振播放/暂停、11个双语页面状态、30条配音及环境音试听。
- `review/prototype/index.html`：修复后的原始交互稿；原始ZIP内容完整保留于`handoff/`。
- 页面状态覆盖首启、首页、训练选择、闭眼、蝴蝶、眼球运动、未完成、追踪中断、结束摘要、本地记录、设置。
- 浏览器已验证：原始稿入口→未完成→重试→三项目手动推进→结束、英文切换、设置60→90分钟；无控制台报错。审阅页实际GLB加载、中文/英文状态切换、动画暂停通过浏览器观察。
- 浏览器模拟数据不用于真机能力、动作判定或统计验收。

## 本轮制作结果

### 三维

- `review/3d/island-scene.glb`：浮岛、草面、水面、三棵树、休息石与蝴蝶；是实际网格，非概念图。
- `review/3d/butterfly.glb`：独立蝴蝶、左右翼铰链、`SlowWingFlap`两秒动画。
- 编辑源：`scripts/generate_review_3d.py`。使用原创建模，无外部网格；右手系、米制、Y轴向上。材质使用线性化的交接色板。
- 节点、网格、三角面与SHA-256见`review/3d/manifest.json`。三棵树、两秒翼振、场景尺寸是本轮作者提案，不是已获确认的产品参数。
- Browser可以加载和显示动画；**尚未验证Spatial导入、动画兼容、实体交互、设备性能或舒适度**。

### 音频

- `review/audio/voice/`：15条中文 + 15条英文，24kHz / PCM16 / mono，由原配音稿生成。
- `review/audio/*_96s_review.wav`：BGM / wind / water各96秒，保留独立轨；来源是已有母素材的循环延展与电平调整。
- 生成源：`scripts/generate_review_audio.py`。技术参数、文本、音色与哈希见`review/audio/manifest.json`。
- 使用macOS本机Tingting/Samantha声音，仅交付设计试听稿；正式商业分发授权未核清，不能声称已完成发行配音。母语审听、头显响度、循环接缝和长时间重复舒适度均待确认。
- 审阅页提供三路独立音量、语音时BGM压低、停止；这些是网页试听行为，尚未接入原生混音。

## Editor真实阻塞与降级范围

已使用官方`start_editor_workflow`及当前公开Handoff启动两次受管理流程；都在模型制作前返回cancelled，没有产生模型、场景bundle或验收通过结果。

- 最新Root Run：`run-60fa8d44-0681-4c87-8b0a-fffc91168c23`。
- Child：`run-ab8cdc11-c3fa-4c93-8591-13e4209f1a05`。
- 日志：`editor-worker-errors.log`。`get_editor_docs`请求官方prompt服务失败：`https://is.snssdk.com/service/settings/v3/?app=1&caller_name=x_bird_mcp&config_key=mcp_prompts`。
- 公开结果：`editor-run-05.json`；只读恢复快照：`editor-recovery-snapshot.md`。
- 保持客户端连接后仍然取消，因此“客户端关闭导致取消”未被证实，已撤回该初步判断。prompt服务失败与取消相邻出现，尚不能单独证明取消的完整根因。
- 没有手改托管Editor工程、工作流状态或MCP配置，没有声称独立GLB是Editor验收bundle。
- `editor-asset/`中只有流程创建的空工程，应保留用于后续恢复；不要当作已完成岛屿场景。
- 本轮GLB是独立离线作者资产，用于继续设计审阅。后续需恢复官方Editor流程并验证导入/打包，或明确批准采用受支持的GLB加载方案；不能跳过原生导入验收。

## 设计与产品需要确认

1. 浮岛、色板、蝴蝶造型与两秒翼振是否接受；原生面板将使用SpatialUI/PicoTheme，浏览器CSS不是原生视觉验收。
2. 中英配音的语速和声音风格；发行配音需进一步明确授权来源。
3. 佩戴计时、闭合数据、通知、资料接口未通过能力门槛。任何功能降级须另行决定：不能默认为仅前台提醒、听觉计时替代闭眼判定或普通按钮替代dwell。
4. 正式训练仍遵守交接参数：闭眼3秒×10；蝴蝶60秒有效追视；左右/上下×10、顺逆圆周各×5。所有阈值真机标定，不能测力度或声称真实调焦。

设计确认仅表示同意视觉/交互方向，不表示能力门槛已经通过，也不代表正式业务已交付。
