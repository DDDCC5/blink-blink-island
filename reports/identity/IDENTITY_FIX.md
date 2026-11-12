# 应用名称与图标修复 — 2026-11-11

## 原因与修复

原APK的 `app_name` 是硬编码的“眨眨岛 / Blink Blink Island”，没有中文资源覆盖。Manifest的图标指向原生模板PNG，PICO空间图层/SDF也仍为模板。此前未完成品牌图标接入，这是交付遗漏；但“桌面不显示”的具体系统行为仍需佩戴者确认，不能仅由模板资源推断原因。

已复用 `handoff/assets/brand/app_icon.svg`，保留蝴蝶、闭眼与岛屿图形。`scripts/prepare_identity_layers.py` 生成两个1024px背景/前景PNG：背景改为PICO规范的圆形不透明底；前景移除原SVG自带阴影和半透明样式，由系统提供空间效果。

通过 Android Studio 官方 **PICO App Identity Configurator → Manual → Apply** 写入两个空间图层、对应SDF、普通兼容图标。工具显示 `3D Icon successfully generated and applied to the project.`；2D/3D预览均为眨眨岛品牌图形。没有使用AI自动拆图或伪造SDF。

名称配置：默认及 `values-en` 为 `Blink Blink Island`；`values-zh` 和 `values-b+zh+Hant` 为 `眨眨岛`。额外的Hant覆盖确保香港、台湾中文系统不回退到英文。启动Activity沿用application标签；诊断通知标题也改为读取同一本地化资源。

## 验证依据

- `before-badging.txt`：修复前APK所有语言均显示双语名称。
- `build.log`：构建和39项JVM测试通过；`build-final.log`：补齐Hant后的最终构建通过。
- `after-badging.txt`：最终APK的en/en-US回退及各英文变体为英文名，zh/zh-CN/zh-HK/zh-TW/zh-Hant均为中文名；图标资源可解析。
- `apk-identity-verification.json`：最终APK五份图标PNG均为1024×1024，字节及SHA与官方工具写入的源资源完全一致，语言名称断言通过。
- `install.log`：替换安装至 Swan B3110 成功，未卸载、未清除数据、未改变系统语言或权限。
- `device-label.txt`：当前系统为zh-Hans-CN；实际设备资源查询返回 **眨眨岛**。命令：`pico-cli shell 'cmd overlay lookup com.example.blinkblinkisland.dev com.example.blinkblinkisland.dev:string/app_name' --device PB311XHGKC220169G`。

用户随后确认 **“图标和中文名称都正确”**：当前Swan应用列表中的品牌图标和中文名称真机观察通过。

英文资源已通过APK核对，未擅自切换整台Swan的语言；英文系统桌面显示尚未实测。本轮修复只涉及应用身份与通知标题，不改变训练完成判定。
