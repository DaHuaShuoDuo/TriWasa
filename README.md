# TriWasa

[English](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/README_en.md) | 简体中文

TriWasa 是面向 Minecraft Java 版的 Masa 客户端附属模组，当前提供 FCA
V2-Extra 轻松放置、更好的轻松放置、更好的打印机、大末影箱终端、Quick Shulker 手持补货、
客户端拾取过滤和多项可选客户端兼容修复。

> **AI 辅助开发声明：** 部分代码、测试和文档由生成式 AI 辅助编写，并由项目
> 维护者审阅。功能设计、发布与维护责任由维护者承担。

模组 ID 为 `triwasa`。TriWasa 是纯客户端模组，不应安装到专用服务器。

## 支持版本

| Minecraft | Fabric Loader | Fabric API | MaLiLib | Litematica |
| --- | --- | --- | --- | --- |
| 26.2 | >= 0.19.3 | >= 0.152.1 | >= 0.29.5 | >= 0.28.6 < 0.29.0 |
| 26.3 | >= 0.19.5 | >= 0.160.5 | >= 0.30.2 | >= 0.29.1 < 0.30.0 |

项目要求 Java 25。必须选择与 Minecraft 版本对应的 TriWasa 构建。

## 安装与依赖

必需依赖：

- Java 25
- Fabric API
- MaLiLib
- Litematica

可选依赖：

- Frostor's Carpet Addition：同进程加载时复用公开协议 API；大末影箱终端需要匹配的 FCA `0.2.0-beta.3`
- Mod Menu：在模组列表提供 TriWasa 配置按钮
- Roughly Enough Items：启用 REI 相关界面修复
- Quick Shulker：启用手持物品自动补货

同进程安装 FCA 可复用其 Payload/Codec；未安装 FCA 时 TriWasa 继续使用线兼容
实现。TriWasa 与 EarlyCompat 不能同时安装。

连接服务器时，按需在 FCA 服务端启用：

```text
/carpet easyPlaceProtocolExtra true
/carpet bigEnderChest true
```

## 功能概览

### V2-Extra 轻松放置

TriWasa 在 Litematica 的真实放置入口编码 FCA V2-Extra 方块状态，兼容普通
轻松放置和 `litematica-printer`。支持铁轨形状保留、合成器锁槽和 Litematica
V2/V3 命中坐标处理；最终方块状态与放置合法性由 FCA 服务端校验。

配置总开关为 `easyPlaceV2ProtocolExtra`。

### 更好的轻松放置

`betterEasyPlace` 默认开启，集中管理 TriWasa 对 Litematica 手放路径的通用修正，
不依赖 FCA 协议握手。当前会修正悬挂牌手放时的支撑方块与点击面选择，并允许无侧向
支撑的墙挂悬挂牌通过目标位置虚拟点击放置。`attached=true` 的天花板悬挂牌仍要求
上方为竖向锁链，依赖未满足时不会发送无效放置。后续新增的通用手放或原版 Mixin
修正仍放在 `mixin/bettereasyplace` 包并受此开关控制。

### 更好的打印机

`betterPrinter` 默认开启，集中管理所有直接修改 `litematica-printer` 的 Mixin，
仅在安装 `litematica-printer` 时加载。当前允许无侧向支撑的墙挂悬挂牌通过打印机
原有目标位置点击路径放置，并对 `attached=true` 的天花板悬挂牌保留上方竖向锁链
依赖校验。打印机关闭时不会应用这些打印机专属修正。该功能不会改写方块后续的
`updateShape` 行为；无支撑墙挂牌仍须由服务端接受最终状态，未安装 FCA 的原版服务端
可能拒绝该放置。共享的客户端放置状态桥接在两个功能任一开启时可用。

### 原理图验证器精确选择

Litematica 原理图验证器的每个错误状态行右侧提供“精确选择”按钮。子界面列出该状态错误对应的全部方块坐标，并提供坐标、方块 ID、状态和名称搜索。点击坐标可切换其选中状态，并可在子界面中继续选择多个方块；不同错误状态组的精确选择彼此独立、可同时保留，世界高亮、注视框和验证器 HUD 会显示所有组的已选方块。子界面中的“显示全部”会选中当前组的全部位置。每行右侧提供“复制”按钮，可复制该方块的整数坐标 `x y z`。粗选状态行后，子界面内该状态的全部位置都会显示为已选；存在精选时外层状态行也会亮起，再次点击外层状态行会同时取消该组粗选和精选。

该功能仅改变验证器高亮范围，不改变错误计数、排序或“忽略该状态”的作用范围。

### 大末影箱终端

FCA 返回兼容的 protocol v3 握手后，服务端使用原版 `GENERIC_9x6` carrier（54 个只读占位槽 + 36 个玩家背包槽），TriWasa 将原版箱子 Screen 替换为自定义终端。TriWasa 不注册自定义 `MenuType`，因此集成服务器或 LAN 中的原版客户端不需要安装 TriWasa。终端支持：

- 完整索引快照、CRC32 校验、连续 Delta 和自动重同步。
- 本地搜索、排序、滚动和物品列表分页。
- 取出一组/半组、快速移入背包、批量存入和丢弃。
- 槽位内使用 K/M/G 等单位压缩大数量显示，可在终端子配置中选择小/大字体（默认小字体，大字体从 1000 开始使用 3 字符紧凑缩写），悬停提示保留实际总数。
- 打开、关闭和操作提示支持静默、快捷栏上方和聊天栏三种反馈方式，默认显示在快捷栏上方。
- 能力热切换；关闭终端能力时回退到原版 6×9 菜单。
- 通过真实物品 Tooltip 上下文传递容器组件，支持潜影盒、Bundle 等内容预览插件。

FCA 通过 Carpet 规则事件推送 `bigEnderChest` 的实时启用状态。客户端可以在规则关闭时
先行完成协议握手；之后开启规则无需重连或重新打开世界，第一次打开末影箱即可使用终端。

配置总开关为 `bigEnderChestTerminalEnabled`。提示方式、终端尺寸、排序和搜索保留策略
可在 MaLiLib 配置界面中调整。

### 配置界面

默认按 `C+T` 打开 TriWasa 配置主界面，也可在“全部”页中重新绑定热键。安装
Mod Menu 后，可以直接从模组列表的配置按钮进入。

### Quick Shulker 手持补货

`quickShulkerHandRestock` 可以从玩家背包中的 Quick Shulker 盒子补充当前手持物品。
功能只在主手右键使用后发生数量降低时补货，也支持使用后留下残留物的物品，例如
水桶或细雪桶留下空桶、蘑菇煲留下碗；仅因快捷移动或存入容器而减少数量不会触发。
功能不读取投影材料表。残留物会优先送入背包可用容量，其次送入潜影盒，均无空间时丢弃。
Quick Shulker 需要同时安装在客户端和服务端。

### 客户端拾取过滤

`itemPickupFilterEnabled` 在客户端收到拾取结果后按黑名单或白名单整组丢弃匹配物品。
该功能不能替代服务端权限、区域保护或实体拦截。

### 其他模组修复

TriWasa 会在检测到对应模组时自动应用 REI 子菜单裁剪与终端交互区域修复，并阻止
Inventory Profiles Next 向大末影箱终端注入整理按钮。IPN 兼容属于自动适配，不作为
配置页中的修复项显示；这些兼容均不增加必需依赖。

## 配置

配置保存在 `config/triwasa.json`，由 MaLiLib 配置界面管理：

- **全部**：集中显示热键、所有功能配置和修复信息，便于统一搜索。
- **移植**：V2-Extra 轻松放置和拾取过滤。
- **辅助功能**：更好的轻松放置、更好的打印机、Quick Shulker 和大末影箱终端。
- **其他模组修复**：显示会修改其它模组自身表现的可选修复项。

父项左侧 `[+]` / `[-]` 只控制子项展开，不是功能总开关。配置缺失、搜索过滤和
父项关闭时仍会保留可恢复的配置状态。

## 兼容性

- TriWasa 只应安装在客户端。
- TriWasa 与 EarlyCompat 都接管轻松放置流程，不能同时安装。
- FCA 服务端负责大末影箱、协议和最终放置判定；TriWasa 不保存权威服务端状态。
- REI、Quick Shulker 和 Inventory Profiles Next 都是可选兼容对象。

## 构建

使用项目 Wrapper，并确保 Gradle 使用的 JVM 是 Java 25。`JAVA_HOME` 可使用任意
JDK 25 安装路径。

```bash
export JAVA_HOME=/path/to/jdk-25
./gradlew :26.2:build :26.3:build
```

Windows PowerShell：

```powershell
$env:JAVA_HOME = '<JDK 25 installation>'
.\gradlew.bat :26.2:build :26.3:build
```

本地构建前，按
[`libs/README.md`](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/libs/README.md)
准备与 `fca_version` 匹配的 FCA API。共享源码、资源或 Mixin 修改后必须构建
26.2 和 26.3。

PR 始终运行变更分类和 `CI Gate`；只有构建相关文件变化时才执行双版本完整构建。

### 发布构建

在 GitHub Actions 中手动运行 **Release Build**。工作流会按
`gradle.properties` 中的 `mod_version` 和 `fca_version` 检出对应的 FCA 标签，
构建 26.2、26.3 两个版本，并上传一个包含两个 JAR、`SHA256SUMS.txt` 和
`BUILD-INFO.txt` 的 `triwasa-<version>` Actions artifact，供创建 Release 使用。

## 许可证与第三方来源

TriWasa 使用
[MIT License](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/LICENSE)。

轻松放置协议和部分状态适配参考 CC0-1.0 的 EarlyCompat；可折叠父子配置交互参考
MIT 许可的 HalfMasa。其他来源见
[THIRD_PARTY_NOTICES.md](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/THIRD_PARTY_NOTICES.md)。

TriWasa 不再分发 MaLiLib、Litematica、FCA 或 Quick Shulker 的源码与二进制。
