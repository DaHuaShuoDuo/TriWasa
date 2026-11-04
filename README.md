# TriWasa

TriWasa 是一个面向 Minecraft Java 版的 Masa 客户端附属模组，为
Frostor's Carpet Addition（FCA）提供 V2-Extra 轻松放置和大末影箱终端客户端支持，
并提供 Quick Shulker 手持物品补货、客户端拾取过滤和其他客户端模组修复。

> **AI 辅助开发声明：** 本项目的部分代码、测试和文档由生成式 AI 辅助编写，并由
> 项目维护者审阅。功能设计、发布决策、最终审查和维护责任由项目维护者承担。

模组 ID 为 `triwasa`。TriWasa 是纯客户端模组，不应安装到专用服务器。
大末影箱、轻松放置协议和其他服务端功能由 FCA 服务端实现。

## 支持版本

| Minecraft | Fabric Loader | Fabric API | MaLiLib | Litematica |
| --- | --- | --- | --- | --- |
| 26.2 | >= 0.19.3 | >= 0.152.1 | >= 0.29.5 | >= 0.28.6 < 0.29.0 |
| 26.3 | >= 0.19.5 | >= 0.160.5 | >= 0.30.2 | >= 0.29.1 < 0.30.0 |

项目要求 Java 25。必须选择与 Minecraft 版本对应的 TriWasa 构建。

## 安装与依赖

必需依赖：

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [MaLiLib](https://www.curseforge.com/minecraft/mc-mods/malilib)
- [Litematica](https://modrinth.com/mod/litematica)
- Java 25

可选依赖：

- [Frostor's Carpet Addition](https://github.com/DaHuaShuoDuo/Frostors-Carpet-Addition)：客户端与
  FCA 同进程加载时直接复用 FCA 的公开 Payload API。
- [Roughly Enough Items](https://modrinth.com/mod/rei)：启用 REI 相关界面修复。
- [Quick Shulker](https://modrinth.com/mod/quick-shulker)：启用手持物品自动补货。

连接服务器时，FCA 服务端需要启用相应的 Carpet 规则：

```text
/carpet easyPlaceProtocolExtra true
/carpet bigEnderChest true
```

TriWasa 与 EarlyCompat 都接管相同的轻松放置协议和 Litematica 放置流程，不能同时安装。

## 配置

TriWasa 的配置保存在 `config/triwasa.json`，由 MaLiLib 配置界面管理。配置按以下分类显示：

- **移植**：V2-Extra 轻松放置和拾取过滤。
- **辅助工具**：Quick Shulker 手持补货和大末影箱终端。
- **其他模组修复**：自动检测的客户端兼容修复，不提供开关。

总开关左侧的 `[+]` / `[-]` 用于展开或折叠子选项。展开状态本身不是功能开关，
即使父项关闭，配置仍会保留并可恢复。

## V2-Extra 轻松放置

TriWasa 在 Litematica 的真实放置调用点编码 FCA V2-Extra 状态，支持普通轻松放置和
`litematica-printer`。客户端完成握手后，服务端负责恢复方块状态和最终放置校验。

### 父子选项

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `easyPlaceV2ProtocolExtra` | `true` | 启用 V2-Extra 客户端握手、状态编码和本地兼容处理 |
| `easyPlaceRailBlockNoShapeUpdate` | `true` | 请求服务端保留投影中的普通轨、充能轨和探测轨形状 |
| `easyPlaceCrafterLockSlot` | `true` | 放置合成器时同步投影中的禁用槽位 |

首次创建 TriWasa 配置时，如果存在旧的 `earlycompat.json`，会迁移其中的同名选项。

FCA 本身会在 V2-Extra 握手后保护铁轨形状，因此 `easyPlaceRailBlockNoShapeUpdate`
主要保留给旧客户端与兼容工作流。

Litematica 在单人游戏中可能使用 V3 放置协议。TriWasa 会覆盖 V2 和 V3 的命中坐标编码，
并在放置入口优先识别 V2-Extra 标记，不依赖 Litematica 当前选择的协议版本。

MasaGadget 的 `fixAccurateProtocol` 可能在普通注入之前取消原方法。TriWasa 会在普通轻松
放置和 `litematica-printer` 的实际调用点再次编码 V2-Extra，因此可与该选项同时使用。

## 大末影箱终端

启用 `bigEnderChestTerminalEnabled` 后，TriWasa 会向 FCA 服务端发送协议 V2 的
`HELLO`。只有在以下条件满足时才打开自定义终端：

- 服务端返回 `accepted=true`。
- 协议版本为 `2`。
- 双方能力交集包含终端所需的快照、动作和终端菜单能力。

未完成握手的客户端继续使用 FCA 提供的原版 6×9 聚合菜单。
终端开关支持热切换：关闭后客户端会撤销终端菜单能力，并由服务端把当前界面重开为原版
6×9 聚合菜单；重新开启后会再次声明终端能力并恢复终端界面。

### 协议与同步

TriWasa 使用服务端发送的完整聚合索引，而不是逐个同步物理存储页。终端会：

- 接收分块的物品模板快照，并验证每个分块的 CRC32。
- 仅在完整快照成功后原子替换旧索引。
- 应用与当前 revision 连续的总量、模板和页面 Delta。
- 在 revision 不连续或数据损坏时请求服务端重新同步。
- 在客户端本地执行搜索、排序、滚动和物品列表分页。

连接专用服务器且客户端未安装 FCA 时，TriWasa 使用自身线兼容的 Payload。
单人游戏或集成服务器同时加载 FCA 时，TriWasa 通过 `compileOnly` 的公开 API 直接复用
FCA 服务端定义的 Payload 类型与 Codec，避免同一通道注册两个不同的 Payload 类。可选
FCA 类型引用隔离在独立适配器中，未安装 FCA 时不会加载这些类。FCA 始终是协议和服务端
业务状态的权威方。

### 界面操作

- 左键取出一组。
- 右键取出半组。
- Shift + 左键快速移入玩家背包。
- Q 丢弃一个，Ctrl + Q 丢弃一组。
- 将物品拖入终端物品区可存入。
- Shift + 左键玩家背包物品可批量存入。

`playerSlots` 和玩家快捷操作使用玩家 `Inventory` 索引 `0..35`，不是菜单槽位索引。
终端只同步玩家背包和 carried item，不同步 FCA 原版菜单的 45 个展示槽。

打开终端中的 TriWasa 设置会关闭当前容器会话。返回游戏后需要重新打开末影箱。

### 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `bigEnderChestTerminalEnabled` | `true` | 启用 FCA 协议 V2 终端 |
| `bigEnderChestTerminalStyle` | `standard` | `compact` / `standard` / `expanded` / `fullHeight` |
| `bigEnderChestSortField` | `name` | 本地按 `name` 或 `count` 排序 |
| `bigEnderChestSortDirection` | `ascending` | `ascending` 或 `descending` |
| `bigEnderChestRememberSearch` | `false` | 在客户端运行期间保留上次搜索文本 |

终端根据实际窗口高度选择尺寸，不会拉伸原始 GUI 材质。

## Quick Shulker 手持补货

该功能将 Tweakeroo 式的手持物品补货扩展到背包内的 Quick Shulker 盒子。它只跟踪玩家
当前选中热栏槽中正在使用、数量降低的物品，不读取投影材料表，也不调用 Litematica
Printer 的物品选择逻辑。

补货流程会在满足条件时打开背包中的潜影盒，核对前 27 格内容，然后通过一次热栏交换
取得最大的同物品、同组件堆叠。超时、切换槽位、游标非空或容器内容不符时会安全退出。

Quick Shulker 需要同时安装在客户端和服务端。

### 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `quickShulkerHandRestock` | `false` | 启用从背包潜影盒补充手持物品 |
| `quickShulkerHandRestockThreshold` | `8` | 剩余数量降到该值时触发补货 |

如果背包中已有可直接合并的同物品堆叠，应优先由原版、Tweakeroo 或 Litematica 完成补货，
TriWasa 只处理需要打开潜影盒的情况。

## 客户端拾取过滤

拾取过滤在客户端识别新拾取的物品，并按整组将匹配物品丢出。它适合在建造或整理时保持
背包整洁，但不是服务端掉落实体拦截。

### 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `itemPickupFilterEnabled` | `false` | 启用客户端拾取过滤 |
| `itemPickupFilterMode` | `blacklist` | `blacklist` 丢弃列表内物品；`whitelist` 丢弃列表外物品 |
| `itemPickupFilterItems` | 空 | 物品 ID 或 `#` 物品标签，每行一个 |
| `itemPickupFilterRarities` | 空 | `common`、`uncommon`、`rare`、`epic` |
| `itemPickupFilterFeedback` | `action_bar` | `silent`、`action_bar` 或 `chat` |
| `itemPickupFilterPauseWhileSneaking` | `true` | 潜行时暂停过滤 |

没有命名空间的物品 ID 默认使用 `minecraft:`。

过滤发生在客户端收到拾取结果之后。服务端可能已经完成拾取统计、事件或成就判定，
因此该功能不能替代服务端权限、区域保护或实体拦截插件。

## 其他模组修复

TriWasa 会在检测到对应模组时自动应用以下修复，不增加必需依赖：

- **REI 子菜单文本修复**：修复 REI 配置子菜单被空裁剪区域隐藏的问题，并恢复
  “显示可合成物品”筛选按钮的合成台图标。
- **REI 终端交互区域修复**：向 REI 声明大末影箱终端的完整屏幕边界，避免配方查看器
  覆盖层截获左侧按钮点击。
- **IPN 终端按钮屏蔽**：阻止 Inventory Profiles Next 将大末影箱终端当作普通可整理
  容器，从而不再向终端界面注入悬浮整理按钮；玩家背包的 IPN 功能不受影响。

这些项目在“其他模组修复”分类中仅作为说明条目显示，不需要单独开关。

## 构建

使用项目自带的 Gradle Wrapper，并将 `JAVA_HOME` 指向 Java 25。不要使用 Java 17
或 Java 21 代替。

PowerShell 示例：

```powershell
$env:JAVA_HOME = '<Java 25 installation>'
.\gradlew.bat :26.2:build :26.3:build
```

如果 Gradle 无法在默认位置创建缓存，可以仅为当前命令指定用户缓存：

```powershell
$env:GRADLE_USER_HOME = Join-Path $env:USERPROFILE '.gradle'
```

构建产物位于各版本子项目的 `build/libs`。共享源码、资源或 Mixin 修改后，必须同时构建
26.2 和 26.3。

MaLiLib 和 Litematica 从 Masa 公共 Maven 解析；开发期 FCA API、Carpet、REI 和
Cloth Config JAR 只用于编译和测试，不会打包进 TriWasa 产物。TriWasa 不把依赖 JAR
提交到版本库；首次构建前请按 [`libs/README.md`](libs/README.md) 中的文件名，自行
下载所需的本地开发依赖。

CI 使用 GitHub Actions 的 Java 25 和 26.2/26.3 构建矩阵。每个矩阵任务会先检出并构建
对应 FCA 版本，生成 TriWasa 的 `compileOnly` 协议 API；手动运行时可通过 `fca_ref`
指定需要验证的 FCA 分支、标签或提交。构建产物还会检查 FCA 类没有被误打包。

## 测试

构建会运行不启动游戏的逻辑、协议、快照、布局和动作队列测试。真实菜单交互、鼠标拖拽、
快捷键和设备环境仍需在开发客户端中人工验证。

## 项目结构

```text
src/main/java/top/frostor/triwasa
├── bigenderchest/   FCA 协议 V2 终端、菜单、快照和本地索引
├── compat/rei/      REI 客户端兼容
├── config/          MaLiLib 配置分类与父子选项
├── easyplace/       V2-Extra 握手、编码和放置接入
├── pickupfilter/    客户端拾取过滤
├── quickshulker/    Quick Shulker 手持补货
├── mixin/           Litematica、REI 和 Minecraft 客户端接入
└── TriWasa.java     客户端入口

src/main/resources   元数据、Mixin、翻译、GUI 材质和音效
src/test/java        逻辑、协议、快照、布局和动作队列测试
versions/26.2        26.2 版本属性
versions/26.3        26.3 版本属性
```

## 许可证与第三方来源

TriWasa 本身使用 [MIT License](LICENSE)。

轻松放置协议和部分状态适配参考 CC0-1.0 的 EarlyCompat；可折叠父子配置交互参考 MIT
许可的 HalfMasa。其他参考项目、许可证和来源说明见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

TriWasa 不包含或再分发 MaLiLib、Litematica、FCA 或 Quick Shulker 的源码与二进制。
