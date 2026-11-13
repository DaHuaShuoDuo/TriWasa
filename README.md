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

- Frostor's Carpet Addition：同进程加载时复用公开协议 API；大末影箱终端需要匹配的 FCA `0.2.1-beta.1`
- Hana 的四改打印机（Hana 版 `litematica-printer` 分支）：启用更好的打印机功能
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
轻松放置和 Hana 的四改打印机（Hana 版 `litematica-printer` 分支）。支持铁轨形状保留、合成器锁槽和 Litematica
V2/V3 命中坐标处理；最终方块状态与放置合法性由 FCA 服务端校验。

配置总开关为 `easyPlaceV2ProtocolExtra`，默认关闭。
其下提供铁轨、墙体、observer、TNT 与活塞保护子开关，均默认关闭。
铁轨开关请求 FCA 抑制铁轨形状重算与 PP 更新，并保留动力铁轨、激活铁轨和探测铁轨的
初始 `POWERED` 状态。26.x 中动力铁轨与激活铁轨都使用 `PoweredRailBlock`，探测铁轨
使用独立的 `DetectorRailBlock`。客户端会把相邻实际铁轨与投影比较，通过 Z 通道
match mask 只保护铁轨种类与形状一致的邻轨；邻轨红石状态不参与比较，普通铁轨也没有
红石状态。
关闭墙体开关后，墙体不再发送 V2-Extra 投影状态或墙保护标志，放置与后续更新回到原版
语义。
墙体开关开启时，客户端逐方向比较实际世界与投影中的相邻墙状态，并把匹配位随 Z 通道
发送；比较忽略 `WATERLOGGED`，其他状态不符的墙不会获得邻居冻结保护。
Observer 通过独立 family 只同步 `FACING`，投影中的 `POWERED` 不会发送；服务端放置
observer 时始终从未激活状态开始。
该行为由与铁轨/墙体保护同级的 `easyPlaceObserverProtection` 控制，默认关闭；关闭后
observer 不发送 V2-Extra 状态或保护标志，放置和检测面更新恢复原版。
`easyPlaceTntProtection` 控制 TNT 保护，默认关闭；开启时阻止轻松放置造成的红石点燃，
关闭时恢复原版 TNT 激活路径。TNT 没有持久激活状态，因此不增加方块状态 adapter。
该保护请求由所有 `BlockItem` 的公共放置路径发送，包括珊瑚扇等两用放置物品。
`easyPlacePistonProtection` 与上述开关同级、默认关闭。开启时客户端比较放置目标、
六个面邻居与五个 QC 二跳候选的实际状态与投影，把匹配的 12 位掩码写入 Y 的 bit8-19，
并置 `Y_PISTON_SCHEMA`（bit20）；关闭或服务端未声明活塞能力时 Y 退回纯精度编码。
投影中的 `EXTENDED` 永不恢复，状态不一致的活塞仍执行原版更新。
关闭该铁轨开关后，客户端不发送铁轨保护标志、邻轨 match mask，也不发送动力铁轨、
激活铁轨或探测铁轨的初始激活位；轨道的形状同步与更新行为回到原有未保护路径。

客户端方块状态编解码使用显式、初始化后冻结的行为族注册表。稳定的
`EasyPlaceStateCodec.encode(...)` / `apply(...)` 作为 facade 查找 owner，具体编码
和解码位于 `easyplace/adapter` 下的 directional、attachment、rail、wall、observer、multiface、
quantity、container、sign、redstone 和 special 等适配器，通用纯状态操作集中在
`EasyPlaceStateSupport`。墙钟、悬挂牌、蜡烛等目标位置或点击面转换继续留在独立规则与
客户端放置入口中，不写入状态适配器；Mixin 只负责必要的 Litematica 调用链桥接。
所有原版墙方块将四侧连接编码为 base-3 raw，并用 Z 通道 bit26 的 schema 与 bit27
传递 `UP`；服务端继续接受旧 bit0 布局。
`WATERLOGGED` 不写入编码，仍由 FCA 服务端放置状态决定。
旧的 `LegacyEasyPlaceStateCodec` 单体映射已移除。V2-Extra Payload 标记和协议版本
仍为 1；辅助 Z 通道低 9 位继续用于 crafter slot mask，bit9/bit10 分别表示
rail/wall 更新保护请求，bit11-16 标记匹配的相邻墙方向，bit17-21 按
`NORTH,SOUTH,WEST,EAST,UP` 标记匹配的相邻铁轨方向，bit22 表示 observer 保护请求，
bit23 表示 TNT 保护请求，bit24 是 `AUXILIARY_SCHEMA` 标记，bit25 空闲。
Z 通道使用**固定原点**编码（`z = auxiliary * 2 + 2`，对齐世界 Z=0），不再以任何
`BlockPos` 的 Z 分量为锚点：Litematica 主路径、legacy `WorldUtils` 路径和 printer
适配传入的 `BlockPos` 语义不同，相对锚点会让辅助字段随命中面错一位。`AUXILIARY_SCHEMA`
是唯一合法的"Z 已编码"判据，服务端解出后剥离该位，缺少该位按无 auxiliary 处理。
握手还会声明 `easy_place_y_precision` capability 与 `y_precision_bits=8`，以及
`easy_place_piston_protection` capability 与 `piston_protection_bits=12`。服务端确认后，
客户端把命中高度相对方块底部的 8-bit 定点值写入 Y 的 bit0-7；Y 的 bit8-19 承载
12 位活塞保护掩码，bit20 是 `Y_PISTON_SCHEMA`，只有掩码非零时置位。Y 不复用 raw，
掩码为零或能力缺失时与纯精度编码逐位一致。命中位置按相对 `blockPos` 的 binary32
float 传输，Y payload 最大 `2+2*(2^21-1)=2^22`，低于精确整数上限 2^24。
X raw 与 Y 精度一律相对 packet `BlockHitResult.blockPos` 编码（不再用 `protocolPos`），
FCA 在目标变换前的 packet 级快照中解码。打印机需要点击支撑方块时，`queueAction` 决定的
packet 位置与编码锚点相差一格，框架会在入队到 `useProtocolHitModifier` 之间把 X/Y 平移
同一差值；Z 仅在 relative 布局下一起平移。

服务端确认 `easy_place_relative_aux` 后，Z 改用相对布局：低 24 位以 packet `blockPos`
为锚点，`AUXILIARY_SCHEMA`、`WALL_AUXILIARY_SCHEMA`、`WALL_UP` 三位随 X payload 的
bit8-10 一起发送。偏移 `2 + 2*payload ≤ 2^25` 且恒为偶数，binary32 在该区间 ULP 为 2，
可以逐位还原；未确认时继续使用 legacy 固定原点布局。该 Z 通道契约要求 TriWasa 与 FCA 同版本号配套使用，混用版本时铁轨、墙、observer
和 TNT 保护位会被静默丢弃。26.2/26.3 放置语义统一：两版依赖的 Litematica 不同（0.28.6 / 0.29.1），且 26.3 把
`ServerboundUseItemOnPacket` 改为 record，命中位置经 `BlockHitResult.STREAM_CODEC` 以
**相对 `blockPos` 的 float** 传输。wire 的 X/Y/Z 只承认 packet `blockPos` 一个锚点：
`BellRules.resolveProtocolTarget` 等解析出的 `protocolPos` 只用于读取投影与当前世界状态，
不得传给 `encodeX`/`encodeY`。服务端不再按命中面或 X 轴相邻锚点兜底，因此客户端必须在
HELLO 中声明 `easy_place_packet_anchor`；未声明该能力的客户端不会获得 V2-Extra，会回落到
原版 easy place，而不是静默放错状态。
多面方块的面索引顺序固定为
`DOWN, UP, NORTH, SOUTH, WEST, EAST`，藤蔓从同一顺序过滤 `DOWN` 得到
`UP, NORTH, SOUTH, WEST, EAST`。使用历史面顺序构建的客户端或服务端必须升级到
匹配版本，混用版本时多面方块和藤蔓可能按错误方向解码。旧 `main` 快照保存在双方的
`legacy` 标签中，仅用于历史兼容，不再维护。

### 更好的轻松放置

`betterEasyPlace` 默认关闭，是 TriWasa 更好的放置功能总开关，集中管理共享的
Litematica 放置链修正，不依赖 FCA 协议握手。当前会修正悬挂牌手放时的支撑方块与点击面选择，允许无侧向
支撑的墙挂悬挂牌以及无下方支撑的蜡烛通过目标位置虚拟点击放置；蜡烛仅绕过放置阶段
的支撑校验，不影响放置后的 `updateShape` 行为。`attached=true` 的天花板悬挂牌仍要求
上方为竖向锁链，依赖未满足时不会发送无效放置。Easy Place 交互期间还会抑制墙挂牌的
原版链式追加，避免重复点击向相邻方向多放。后续新增的通用手放或原版 Mixin
修正仍放在 `mixin/bettereasyplace` 包并受此开关控制。数量类方块可在首次放置后继续递增到投影数量，支持蜡烛、花床、落叶堆、海泡菜、雪层和海龟蛋，每次交互只增加一级并消耗一个物品。
发光地衣、幽匿脉络、树脂团等多面方块也会在同一位置逐面递增，直到达到投影中的完整面状态。
藤蔓同样会按缺失方向在同一位置逐面递增，并继续受有效附着面校验约束。

`easyPlaceFullBlockSelection` 是**独立开关**，不属于 `betterEasyPlace`，也不受其总开关约束，
默认关闭。开启后，轻松放置的投影射线
把火把、按钮、台阶、楼梯、栅栏、蜡烛、告示牌等不完整方块按整格 1×1×1 判定，瞄在格子空白处
也能选中目标。作用域只覆盖轻松放置的那一次投影射线：选区拖拽、取方块、验证器高亮和打印机目标
仍按真实轮廓形状判定。命中点取整格命中位置与命中面，因此依赖点击面的方块（台阶上下半、墙火把、
活板门等）在未安装 FCA 时可能出现与投影不同的朝向；安装 FCA 并由 V2-Extra 强制状态时不受影响。
整格命中**只对状态不正确的方块生效**。这里直接沿用 Litematica 在
`EasyPlaceUtils#handleEasyPlace` 中的判定：投影世界的方块状态与客户端世界的方块状态引用相等
（`==`）即为"已放对"，保持原版轮廓，不占用相邻格的点击；方块缺失、方块不同或任意
属性（含 `WATERLOGGED`）不同则视为需要放置，才扩大命中范围。该判定只比较方块状态，不比较
方块实体（如告示牌文字、容器内容）数据。

`easyPlaceSelectionOutline` 同样是**独立开关**，不属于 `betterEasyPlace`，默认关闭，
且是纯渲染选项。开启轻松放置模式时
（工具模式不为 REBUILD），原版方块选择框会画在本次点击将要落入的投影格上；它只替换渲染用的
选中框数据，不改变 `hitResult`、攻击、取方块或协议行为。框体始终跟随实际命中语义：整格命中
开启时画整格，关闭时画投影方块的真实轮廓，因此不会出现"框是整格、实际却要精确瞄准"的情况。
状态已与投影一致的方块不显示该框，同样采用上面的 Litematica 判定。

### 更好的打印机（子选项）

`betterPrinter` 默认关闭，是 `betterEasyPlace` 的子选项，集中管理所有直接修改
打印机的 Guide/Action 层 Mixin，仅在安装 Hana 的四改打印机（Hana 版 `litematica-printer` 分支）且
父开关开启时生效。当前允许无侧向支撑的墙挂悬挂牌通过打印机
原有目标位置点击路径放置，并对 `attached=true` 的天花板悬挂牌保留上方竖向锁链
依赖校验；蜡烛移除 Guide 前置的放置支撑门禁并进入目标位置点击路径，海龟蛋移除
`TurtleEggGuide` 人为添加的下方支撑要求，与原版放置语义一致。发光地衣、幽匿脉络、
树脂团等 `MultifaceBlock` 统一复用逐面放置逻辑；藤蔓只尝试当前缺失且有有效附着面的方向，避免对无支撑面反复尝试。花簇、落叶堆、海泡菜、雪层、蜡烛和海龟蛋等数量方块会逐级递增到目标数量，其中落叶堆复用花簇 Guide 并读取 `SegmentableBlock.AMOUNT`。打印机关闭时不会应用这些打印机专属修正。该功能不会改写方块后续的
`updateShape` 行为；无支撑墙挂牌和蜡烛仍须由服务端接受最终状态，未安装 FCA 的原版服务端
可能拒绝该放置。共享的客户端放置状态桥接由更好的轻松放置总开关统一启用。

更好的打印机只适配 Hana 的四改打印机（Hana 版 `litematica-printer` 分支），不适配
上游原始打印机。两者的 Guide、任务调度和放置确认链路不同，不能混用相应 Mixin。

### 原理图验证器精确选择

Litematica 原理图验证器的每个错误状态行右侧提供“精确选择”按钮。子界面列出该状态错误对应的全部方块坐标，并提供坐标、方块 ID、状态和名称搜索。点击坐标可切换其选中状态，并可在子界面中继续选择多个方块；不同错误状态组的精确选择彼此独立、可同时保留，世界高亮、注视框和验证器 HUD 会显示所有组的已选方块。子界面中的“显示全部”会选中当前组的全部位置。每行右侧提供“复制”按钮，可复制该方块的整数坐标 `x y z`。粗选状态行后，子界面内该状态的全部位置都会显示为已选；存在精选时外层状态行也会亮起，再次点击外层状态行会同时取消该组粗选和精选。

该功能仅改变验证器高亮范围，不改变错误计数、排序或“忽略该状态”的作用范围。
开关 `verifierPositionPicker`（默认关闭）控制错误行右侧的“精确选择”按钮；关闭后按钮隐藏，
验证器恢复原版整组高亮，已有精选也不再参与高亮合并。

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

每个 true/false 开关右侧都有一个热键位，与开关共同占据原来一行的宽度，默认全部为空键位。
绑定后按下该键即可切换对应开关，效果与点击开关一致。

### Quick Shulker 手持补货

`quickShulkerHandRestock` 可以从玩家背包中的 Quick Shulker 盒子补充当前手持物品。
功能只在右键使用后发生数量降低时补货，也支持使用后留下残留物的物品，例如
水桶或细雪桶留下空桶、蘑菇煲留下碗；仅因快捷移动或存入容器而减少数量不会触发。
功能不读取投影材料表。残留物会优先送入背包可用容量，其次送入潜影盒，均无空间时丢弃。
Quick Shulker 需要同时安装在客户端和服务端。

子选项 `quickShulkerOffhandRestock` 默认关闭。开启后副手也同样补货：副手物品因右键
使用而减少或变成残留物时，从同一个潜影盒补回同物品、同组件的一组，并把补好的那组
放回副手槽。主手需要补货时始终优先处理主手，因此开启副手补货不会延后主手补货。
副手槽在容器交互里就是 `Inventory.SLOT_OFFHAND`，换手复用与主手相同的 `SWAP`
操作，不额外引入点击序列。

### 客户端拾取过滤

`itemPickupFilterEnabled` 在客户端收到拾取结果后按黑名单或白名单整组丢弃匹配物品。
该功能不能替代服务端权限、区域保护或实体拦截。

### 投影材料统计修复

`projectionMaterialStatsFix` 修正 Litematica 材料统计对单方块多物品与多面方块的漏算。
默认关闭，开启后粉红色花簇、野花簇、枯叶堆按 1-4 段计，树脂团按附着面数计，藤蔓按
五个面计；同时消除配方 JSON 导出里同一系数的重复相乘。修改开关后需要重新生成材料
列表才会生效。

### 其他模组修复

TriWasa 会在检测到对应模组时自动应用 REI 子菜单裁剪与终端交互区域修复，并阻止
Inventory Profiles Next 向大末影箱终端注入整理按钮。IPN 兼容属于自动适配，不作为
配置页中的修复项显示；这些兼容均不增加必需依赖。

## 配置

配置保存在 `config/triwasa.json`，由 MaLiLib 配置界面管理：

- **全部**：集中显示热键、所有功能配置和修复信息，便于统一搜索。
- **投影辅助**：所有改变 Litematica 行为的功能，包括 V2-Extra 轻松放置、更好的轻松放置
  （更好的打印机、不完整方块整格选中、投影目标选择框）、投影材料统计修复和验证器精确选择。
- **辅助功能**：与 Litematica 无关的功能，包括拾取过滤、Quick Shulker 和大末影箱终端。
- **其他模组修复**：显示会修改其它模组自身表现的可选修复项。

原「移植」页的两个主开关在配置界面中直接标注了来源：

- `easyPlaceV2ProtocolExtra`：移植自 [EarlyCompat](https://github.com/chenjunfu2/EarlyCompat)（chenjunfu2，CC0-1.0）。
- `itemPickupFilterEnabled`：移植自
  [Item Pickup Filter](https://github.com/dev-jcwd/Item-Pickup-Filter)（jcwd，MIT）。

完整来源与许可说明见 `THIRD_PARTY_NOTICES.md`。

父项左侧 `[+]` / `[-]` 只控制子项展开，不是功能总开关。配置缺失、搜索过滤和
父项关闭时仍会保留可恢复的配置状态。

## 兼容性

- TriWasa 只应安装在客户端。
- TriWasa 与 EarlyCompat 都接管轻松放置流程，不能同时安装。
- FCA 服务端负责大末影箱、协议和最终放置判定；TriWasa 不保存权威服务端状态。
- 更好的打印机只支持 Hana 的四改打印机（Hana 版 `litematica-printer` 分支），不支持
  上游原始 `litematica-printer`。
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
`logicTest` 会执行以 `EasyPlaceProtocolTest` 为稳定入口的协议与状态测试；该入口
聚合按 wire、方向、附着、轨道、多面、数量、容器、悬挂牌、特殊方块和 printer/config
等行为族拆分的测试。

PR 始终运行变更分类和 `CI Gate`；只有构建相关文件变化时才执行双版本完整构建。

### 发布构建

发布以 GitHub Release 为入口。创建并发布标签为 `v<mod_version>` 的 Release 后，
`Release Build` 会校验 `mod_version`，按 `fca_version` 检出匹配的 FCA 标签，
构建并测试 26.2、26.3 两个版本，再分别发布到 Modrinth 和 CurseForge。平台版本号
使用 `<mod_version>+<Minecraft 版本>`，GitHub Release 正文会作为更新日志。

手动运行 **Release Build** 仍可只生成包含两个 JAR、`SHA256SUMS.txt` 和
`BUILD-INFO.txt` 的 `triwasa-<version>` Actions artifact。需要重试已有 Release 时，
输入完整 `release_tag` 并启用 `publish`。

首次启用前需要创建对应的 Modrinth 与 CurseForge 项目，并在 GitHub 仓库设置中配置：

- Actions variables：`MODRINTH_PROJECT_ID`、`CURSEFORGE_PROJECT_ID`
- Actions secrets：`MODRINTH_TOKEN`、`CURSEFORGE_TOKEN`

令牌不会写入仓库。TriWasa 发布应使用与 `fca_version` 对应的 FCA Release，确保
两个仓库的协议版本匹配。

## 许可证与第三方来源

TriWasa 使用
[MIT License](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/LICENSE)。

轻松放置协议和部分状态适配参考 CC0-1.0 的 EarlyCompat；可折叠父子配置交互参考
MIT 许可的 HalfMasa。其他来源见
[THIRD_PARTY_NOTICES.md](https://github.com/DaHuaShuoDuo/TriWasa/blob/main/THIRD_PARTY_NOTICES.md)。

TriWasa 不再分发 MaLiLib、Litematica、FCA 或 Quick Shulker 的源码与二进制。
