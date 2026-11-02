# TriWasa

TriWasa 是一个面向 Minecraft Java 版的 Masa 客户端附属模组，为
Frostor's Carpet Addition（FCA）的 V2-Extra 轻松放置协议提供 Litematica
客户端兼容。

> **声明：本项目的代码由 AI 辅助完成。** 需求与发布由 Frostor 负责。

## 功能

轻松放置移植选项位于 TriWasa 配置界面的“移植”分类，并保存在
`config/triwasa.json` 的 `Porting` 节点中：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `easyPlaceV2ProtocolExtra` | `true` | 启用与 FCA 兼容的 V2-Extra 客户端握手、状态编码和本地解码 |
| `easyPlaceRailBlockNoShapeUpdate` | `true` | 在 V2-Extra 状态中附带 EarlyCompat 的“铁轨不更新”兼容位；FCA 本身会保护所有 V2-Extra 铁轨形状 |
| `easyPlaceCrafterLockSlot` | `true` | 根据投影中的合成器方块实体锁定对应槽位 |

首次启动且 TriWasa 配置文件不存在时，会读取 `earlycompat.json` 中同名的三个旧选项，
完成一次性迁移。

## 辅助工具

“辅助工具”标签页用于 TriWasa 原创功能。当前包含 `quickShulkerHandRestock`：它将
Tweakeroo 式的手持物品补货扩展到背包内的潜影盒。它只跟踪玩家当前选中热栏槽位里正在
使用、数量降低的物品，不读取投影材料表，也不会让 Litematica Printer 参与物品选择。
配置保存在 `config/triwasa.json` 的 `Utilities` 节点。该总开关是父项，子配置通过
左侧 `[+]` / `[-]` 展开或折叠：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `quickShulkerHandRestock` | `false` | 启用从背包潜影盒补充当前手持物品 |
| `quickShulkerHandRestockThreshold` | `8` | 剩余数量降到该值时触发补货 |

只有背包里不存在另一组完全相同的物品时才会打开潜影盒，因此普通背包补货仍可交给
Tweakeroo 或 Litematica 处理。TriWasa 会先核对打开后的潜影盒前 27 格内容，再用一次
热栏交换把盒子内最大的同物品、同组件堆叠换到原选中槽位；界面不会闪现，超时、切换槽位、
游标非空或菜单内容不符时会安全退出并关闭自动打开的容器。

Quick Shulker 需要同时安装在客户端和服务端。该功能通过其公开客户端入口独立完成，
不依赖 `litematica-printer`，也不调用打印机的 QuickShulker 状态机。

## 拾取过滤

“移植”标签页中的 `itemPickupFilterEnabled` 提供一个客户端物品过滤功能，改编自
[Item Pickup Filter](https://github.com/dev-jcwd/Item-Pickup-Filter)。该总开关是父项，
其他选项通过左侧 `[+]` / `[-]` 展开或折叠，并由 MaLiLib 配置页管理。选项保存在
`config/triwasa.json` 的 `ItemPickupFilter` 节点：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `itemPickupFilterEnabled` | `false` | 拾取过滤总开关 |
| `itemPickupFilterMode` | `blacklist` | `blacklist` 丢弃列表内物品，`whitelist` 丢弃列表外物品 |
| `itemPickupFilterItems` | 空 | 物品 id 或 `#` 物品标签列表 |
| `itemPickupFilterRarities` | 空 | `common` / `uncommon` / `rare` / `epic` 稀有度列表 |
| `itemPickupFilterFeedback` | `action_bar` | `silent` / `action_bar` / `chat` 丢弃提示 |
| `itemPickupFilterPauseWhileSneaking` | `true` | 按住潜行键时暂时允许正常拾取 |

客户端会在槽位同步和拾取通知到达时记录过滤物品，不再每 tick 扫描整个背包。整批网络
包处理完成后、客户端 tick 和画面渲染开始前，所有本次涉及的匹配堆叠会按整组批量丢出。
投掷期间只向服务器临时发送一次向下看的旋转，客户端实际视角不会被改变，也不会播放
自动丢弃产生的挥手动画；物品会落在脚边而不是沿目视方向飞远。

该功能不能阻止服务端真正拾取物品。服务端仍会先完成拾取统计和事件触发，随后才接收
客户端的丢出操作；它属于客户端视觉黑名单，而不是服务端掉落实体拦截。

## 支持的版本

| Minecraft | Fabric Loader | Fabric API | MaLiLib | Litematica |
| --- | --- | --- | --- | --- |
| 26.2 | >= 0.19.3 | >= 0.152.1 | >= 0.29.5 | >= 0.28.6 < 0.29.0 |
| 26.3 | >= 0.19.5 | >= 0.160.5 | >= 0.29.99-dev | >= 0.28.99-dev |

对应产物名为 `triwasa-fabric-<Minecraft 版本>`。

## 依赖

- [MaLiLib](https://www.curseforge.com/minecraft/mc-mods/malilib)（必需）
- [Litematica](https://modrinth.com/mod/litematica)（必需）
- [Roughly Enough Items](https://modrinth.com/mod/rei)（可选，用于“其他模组修复”）
- [Quick Shulker](https://modrinth.com/mod/quick-shulker)（可选，仅用于手持物品自动补货）
- [Fabric API](https://modrinth.com/mod/fabric-api)（必需）
- Java 25

MaLiLib 和 Litematica 仅作为开发期依赖放在 `libs/`，不会被包含进 TriWasa
的产物 JAR。EarlyCompat 与 TriWasa 都接管同一协议通道和 Litematica 轻松放置流程，
因此不能同时安装。

FCA 服务端必须启用 `easyPlaceProtocolExtra`。TriWasa 客户端只有在配置项启用且
收到 FCA 握手确认后才使用 V2-Extra。

FCA 的握手确认和能力广播包含协议版本及能力列表，TriWasa 会校验显式
`easy_place_v2_extra` 能力。缺少新字段的旧版确认仍按兼容模式接受。

V2-Extra 的扩展状态使用空闲位同步蛋糕咬数、铜傀儡姿态、铜灯开关/通电状态和门上下半状态；旧客户端
未设置这些位时分别保持原版默认值，因而不会破坏已有协议的线格式兼容性。

双箱子的 `CHEST_TYPE` 由 FCA 服务端按原版邻接关系归一化，TriWasa 只编码朝向，
不把 Servux 的半箱类型直接作为终态广播。V2-Extra 使用 `bit6` 标记显式类型存在，
并用 `bit4-5` 编码 `SINGLE/LEFT/RIGHT`，从而精确区分相邻的大箱与小箱。

Litematica 在单人/集成服务器中可能自动选择 V3。TriWasa 会同时覆盖 V2 和 V3 的
命中坐标编码，并在 `PlacementHandler` 的版本分发入口优先识别 V2-Extra marker，
因此不依赖 Litematica 当前选择的具体协议版本。

MasaGadget 的 `fixAccurateProtocol` 会在 Litematica
`EasyPlaceUtils.applyCarpetProtocolHitVec()` 的 `HEAD` 处自行取消并返回结果，使普通
`RETURN` 注入无法执行。TriWasa 因此在普通轻松放置和 `litematica-printer` 的实际
调用点再次编码 V2-Extra，可与该选项同时开启。

## 其他模组修复

TriWasa 的“其他模组修复”选项卡只列出修复项目名称，不提供开关：

- **REI 子菜单文本修复**：修复 REI 配置子菜单被空 scissor 区域完全裁剪的问题，并恢复
  “显示可合成物品”筛选按钮缺失的合成台图标。

该修复仅在检测到 REI 时应用，不修改 REI 配置，也不会成为 TriWasa 的运行期必需依赖。
该项目本身没有额外配置，因此在界面中保持为单个示意条目。

“移植”中的 `easyPlaceV2ProtocolExtra` 和 `itemPickupFilterEnabled` 是移植功能的
父项；“辅助工具”中的 `quickShulkerHandRestock` 是 TriWasa 原创功能的父项。铁轨形状
保护、合成器槽位锁定、补货阈值以及拾取过滤的子选项分别缩进显示在对应父项之下，并通过
左侧 `[+]` / `[-]` 展开或折叠。

## 构建

```powershell
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-25'
$env:GRADLE_USER_HOME = Join-Path $env:USERPROFILE '.gradle'
.\gradlew.bat :26.2:build :26.3:build
```

产物位于 `versions/<版本>/build/libs/`。

## 项目结构

```
.
├── common.gradle                         # 各版本共享的构建脚本
├── gradle.properties                     # 全局属性
├── libs/                                 # 未发布版本的开发期依赖
├── settings.gradle                       # 26.2 / 26.3 子项目
├── src/main/java/top/frostor/triwasa
│   ├── TriWasa.java                      # 客户端入口
│   ├── compat/rei/                       # REI 修复共享资源
│   ├── config/                           # MaLiLib 移植/辅助工具/修复界面
│   ├── easyplace/                        # 握手、协议和状态编解码
│   ├── pickupfilter/                     # 客户端拾取过滤与待丢弃队列
│   ├── quickshulker/                     # 快捷潜影盒手持物品补货
│   └── mixin/                            # Litematica / REI / Minecraft 客户端接入
├── src/main/resources
│   ├── assets/triwasa/lang/              # 模组名称与描述
│   ├── triwasa.mixins.json
│   └── fabric.mod.json                   # 模组元数据与 Masa 依赖
├── src/test/java                         # 协议与槽位掩码测试
└── versions/
    ├── 26.2/gradle.properties
    └── 26.3/gradle.properties
```

## 许可证

[MIT](LICENSE)。TriWasa 不包含或再分发 MaLiLib、Litematica 源码。

协议与方块状态适配源自 CC0-1.0 的 EarlyCompat，详见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

可折叠父子配置交互改写自 MIT 许可的 HalfMasa，详见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
