# TriWasa

TriWasa 是一个面向 Minecraft Java 版的 Masa 客户端附属模组，为
Frostor's Carpet Addition（FCA）的 V2-Extra 轻松放置协议提供 Litematica
客户端兼容。

> **声明：本项目的代码由 AI 辅助完成。** 需求与发布由 Frostor 负责。

## 功能

三个选项位于 TriWasa 配置界面的“移植”分类，并保存在 `config/triwasa.json`
的 `Porting` 节点中：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `easyPlaceV2ProtocolExtra` | `true` | 启用与 FCA 兼容的 V2-Extra 客户端握手、状态编码和本地解码 |
| `easyPlaceRailBlockNoShapeUpdate` | `true` | 在 V2-Extra 状态中附带 EarlyCompat 的“铁轨不更新”兼容位；FCA 本身会保护所有 V2-Extra 铁轨形状 |
| `easyPlaceCrafterLockSlot` | `true` | 根据投影中的合成器方块实体锁定对应槽位 |

首次启动且 TriWasa 配置文件不存在时，会读取 `earlycompat.json` 中同名的三个旧选项，
完成一次性迁移。

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

“移植”中的 `easyPlaceV2ProtocolExtra` 是轻松放置母功能；铁轨形状保护和合成器槽位
锁定作为其子项，通过左侧 `[+]` / `[-]` 展开或折叠。

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
│   ├── config/                           # MaLiLib “移植”/“其他模组修复”界面
│   ├── easyplace/                        # 握手、协议和状态编解码
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
