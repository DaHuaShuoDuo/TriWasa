# TriWasa - 代理规则

## 项目定位

- TriWasa 是面向 Java 版 Minecraft 的 Masa 客户端附属模组。当前已实现 FCA
  V2-Extra 轻松放置、大末影箱终端、Quick Shulker、拾取过滤和多项可选客户端兼容
  修复；这份列表只记录现状，不是未来功能路线图、固定产品目标或新增功能的拒绝依据。
- 新功能可以继续加入，但必须遵守客户端边界、Masa 配置约定、可选的 FCA 依赖和双版本
  可测试性。不要为了维持当前以 FCA 适配为中心的方向，把无关功能强行塞进现有终端、
  轻松放置或配置分类。
- 当前同时支持 Minecraft `26.2` 和 `26.3`。`src/main` 是两个版本共用的源码与资源；
  `versions/<版本>/gradle.properties` 只负责版本差异。不要在版本目录复制或分叉 Java
  源码。
- TriWasa 是纯客户端模组。涉及 FCA 服务端功能时，Carpet 规则、玩家档案和大末影箱
  权威状态仍由 FCA 负责，不要把这些逻辑迁入客户端；其他独立客户端功能应按自己的
  服务端兼容边界设计，不得伪造服务端权威结果。
- 保持 mod id `triwasa`、Java 包名 `top.frostor.triwasa`、客户端入口点和现有公开
  行为稳定，除非任务明确要求迁移。
- 项目要求 Java 25。Gradle Wrapper、Fabric Loom 和 Minecraft 依赖固定为项目当前
  版本，不要为单次构建升级。

开始修改前先阅读 `README.md`、相关功能实现和测试。若存在 `.git`，先运行
`git status --short` 识别已有修改，不要覆盖或清理无关变更。

## 构建与验证

使用项目自带的 Gradle Wrapper，并确保实际执行 Gradle 的 JVM 是 Java 25。POSIX
shell 示例：

```bash
export JAVA_HOME=/path/to/jdk-25
./gradlew :26.2:build :26.3:build
```

Windows PowerShell 示例：

```powershell
$env:JAVA_HOME = '<JDK 25 installation>'
.\gradlew.bat :26.2:build :26.3:build
```

- 不要求固定 JDK 厂商或安装路径；`JAVA_HOME` 指向本机可用的 JDK 25 即可。不要用
  Java 17/21 代替 Java 25。
- 若默认 Gradle 缓存位置不可写，仅为当前环境把 `GRADLE_USER_HOME` 指向可写缓存目录；
  不要修改 Wrapper 配置来规避环境问题。
- 共享源码、资源、Mixin、版本属性或依赖变更后，必须同时构建 `26.2` 和 `26.3`。
- `:26.2:build` / `:26.3:build` 会运行 `logicTest`，覆盖轻松放置协议、大末影箱协议、
  快照、布局和动作队列。新增编译警告视为回归；排查 Gradle 弃用时可使用
  `--warning-mode all`。
- 构建产物位于 `versions/<版本>/build/libs/`。不要手工编辑 `.gradle/`、`build/`、
  `versions/*/build/`、运行目录或日志。
- 本机 GameTest（当前位于 `src/gametest`）属于开发验证，不纳入版本控制，也不进入
  CI。不得假定该目录在其他环境中存在；只有当前工作区实际包含测试类并能运行时，才可
  声明 GameTest 通过。真实菜单交互、鼠标拖拽、快捷键、Litematica/REI 兼容和客户端
  渲染仍需在开发客户端中手测；交付时区分自动逻辑测试与手动运行验证。
- 本地构建前，`libs/frostors-carpet-addition-fabric-<Minecraft 版本>-<FCA 版本>.jar`
  必须对应 `gradle.properties` 中的 `fca_version`。CI 会从 FCA 仓库构建该 API；
  手动运行时可通过 workflow input `fca_ref` 指定 FCA 分支、标签或提交。
- MaLiLib、Litematica、REI 和 Cloth Config 从公共 Maven 解析。不要提交
  `libs/*.jar` 或其他本地开发依赖。

## 版本与依赖

当前版本矩阵：

| Minecraft | Fabric Loader | Fabric API | MaLiLib | Litematica |
| --- | --- | --- | --- | --- |
| 26.2 | >= 0.19.3 | >= 0.152.1 | >= 0.29.5 | >= 0.28.6 < 0.29.0 |
| 26.3 | >= 0.19.5 | >= 0.160.5 | >= 0.30.2 | >= 0.29.1 < 0.30.0 |

- 根 `gradle.properties` 管共享属性（模组/FCA 版本、group、产物前缀、Loom 版本）；
  Minecraft、Loader、Fabric API、MaLiLib、Litematica、REI 和 Cloth Config 版本由
  `versions/<版本>/gradle.properties` 定义。
- MaLiLib 和 Litematica 是运行期 `depends`，但不得打包进 TriWasa。REI 和 Cloth
  Config 仅用于可选 Mixin 编译，REI 必须保持在 `suggests`，不能变成硬依赖。
- FCA API 只是 `compileOnly` 编译依赖。FCA、Carpet、MaLiLib、Litematica、REI 和
  Cloth Config 都不得打进 TriWasa 产物。
- 增加或移除支持版本时，必须同步 `settings.gradle`、版本属性、README 版本表、
  FCA 编译输入和双版本构建验证。
- 修改运行时依赖范围后，必须检查生成的两个 `fabric.mod.json`，确认环境为 `client`
  且依赖边界没有变化。

## FCA 协议边界

- FCA 始终是协议和服务端业务状态的权威方。不要在 TriWasa 中复制大末影箱页面、
  聚合计数或最终放置合法性判定作为服务端事实。
- 连接专用服务器且客户端未安装 FCA 时，保留 TriWasa 的线兼容 Payload 回退。
- 单人游戏或集成服务器同时加载 FCA 时，必须复用 FCA 提供的 Payload、Codec 和相关
  类型，不得在同一频道注册两个不同的 Payload 类。
- Easy Place ACK 必须包含当前 `protocol_version` 和显式能力字段；缺少字段或版本不匹配
  时不得按旧协议降级为启用。
- 对 FCA 类型的引用必须隔离在独立兼容适配器中，并通过模组加载检测保护；未安装 FCA
  时不得触发可选类加载错误。
- 终端的 HELLO、能力、协议版本、快照 CRC、revision 连续性和重同步规则以 FCA 当前
  协议为准。缺少能力或握手失败时回退到 FCA 原版 6×9 菜单。
- 修改 FCA 协议时先更新并验证 FCA 公共 API，再更新 TriWasa 的 `compileOnly` 适配。
  不要用反射桥接掩盖两边 Payload/字段不一致。
- Easy Place 状态编解码以 `EasyPlaceStateCodec.encode(...)` / `apply(...)` 为稳定
  facade，由初始化后冻结的 `EasyPlaceCodecRegistry` 选择 behavior-family adapter；
  通用纯状态操作放入 `EasyPlaceStateSupport`。不要把已删除的
  `LegacyEasyPlaceStateCodec` 单体分支或每方块第二套状态表重新引回。
- `WallBlock` 四侧按 `NORTH + EAST*3 + SOUTH*9 + WEST*27` 编码为 raw，`UP` 使用
  辅助值的 bit0；编码结果通过 `EasyPlaceEncodedState(rawValue, auxiliaryValue)` 携带。
  `WATERLOGGED` 不得进入编码。无辅助状态的 adapter 默认返回 `auxiliaryValue=0`，
  且 Z 通道仍须保持合成器锁槽行为不变。
- Z 通道 bit9 和 bit10 分别表示 `easyPlaceRailProtection` 与
  `easyPlaceWallBlockNoShapeUpdate` 请求的邻域更新保护；低 9 位继续属于 crafter
  slot mask。两个子开关与 `easyPlaceV2ProtocolExtra` 父子关系同级、默认开启，并在每次
  V2-Extra 编码时写入标志，不能只在放置 wall/rail 本身时写入。
- Z 通道 bit19-24 按 `Direction.ordinal()` 标记五个铁轨保护方向（水平四向和上方）
  中实际状态与投影一致的邻轨。比较必须忽略 `WATERLOGGED`；所有带 `POWERED` 的
  铁轨都不将该状态用于邻轨保护比较。目标位置的铁轨红石状态通过 raw bit4 + Z 通道
  bit25 保护，不能参与邻轨 match mask。形状等其他状态不一致方向的 bit 不得发送，
  使该铁轨继续接受原版更新。
- Z 通道 bit11-16 按 `Direction.ordinal()` 标记六个相邻位置中哪些墙的实际状态与
  Litematica 投影状态一致。比较必须忽略 `WATERLOGGED`，其余方块种类、四侧连接和
  `UP` 必须完全一致；不一致方向的 bit 不得发送，使该墙在服务端继续接受原版邻居更新。
- 关闭 `easyPlaceWallBlockNoShapeUpdate` 时，`WallStateAdapter.encode(...)` 必须返回
  `-1`，不得发送 wall raw/UP 投影或 wall 邻域保护标志，使墙体放置回到原版状态与更新
  语义；不能只关闭 bit10 而继续恢复投影状态。
- Observer 状态由独立 `adapter/observer` family 处理。客户端只编码 `FACING` 的
  3D data value，不得编码 `POWERED`；decoder 必须把 `POWERED` 强制为 false。raw 只
  接受 `0..5`，其他值返回 `null`。
- `easyPlaceObserverProtection` 与 rail/wall 开关同级、默认开启。关闭时
  `ObserverStateAdapter.encode(...)` 必须返回 `-1`，且 `EasyPlaceUpdateFlags` 不得发送
  Z 通道 bit17，使 observer 放置和检测面更新完全回到原版语义。
- `easyPlaceTntProtection` 与 rail/wall/observer 开关同级、默认开启，并通过 Z 通道
  bit18 请求 FCA 保护 TNT。关闭时不得发送 bit18；客户端不增加 TNT 状态 adapter，
  因为 TNT 没有可恢复的激活方块状态。
- `easyPlaceRailProtection` 与 wall/observer/TNT 开关同级、默认开启，统一控制铁轨
  形状/PP 更新保护、bit9 请求、bit19-24 match mask、bit25 状态保护请求和 raw bit4
  的 `POWERED` 值。关闭时这些标志和状态位都不得发送。FCA 的 `RAIL` family 负责形状与
  邻域，`RAIL_STATE` family 只覆盖实际放置位置并保护动力、激活和探测等带红石状态
  铁轨。26.x 中动力铁轨与激活铁轨都是 `PoweredRailBlock` 的不同方块实例，探测铁轨
  使用独立 `DetectorRailBlock`；没有独立 `ActivatorRailBlock`。普通铁轨没有红石状态。
  不要引入形状/状态两个配置开关，也不要新增旧开关别名或迁移分支。
- TNT 保护的客户端请求必须位于所有 `BlockItem` 共用的 `place` 路径，并在
  `getPlacementState` 调用前发送；不得依赖目标方块 adapter，否则珊瑚扇等
  `StandingAndWallBlockItem` 会漏掉保护。
- 墙钟、悬挂牌、蜡烛等 target/click 转换属于独立 placement rule、client encoder 或
  Mixin 桥接，不得塞入状态 codec。新增或调整时保留 `EasyPlaceProtocolTest` 作为
  `logicTest` 入口，并扩展对应 family suite 与集成矩阵。
- 多面方块线值顺序必须保持显式 `DOWN, UP, NORTH, SOUTH, WEST, EAST`；藤蔓顺序从
  同一数组过滤 `DOWN` 派生，不得另手写一份。该顺序属于当前 V2-Extra v1 构建契约：
  混用使用历史面顺序的 FCA、TriWasa 或 EarlyCompat 构建会错误解码多面/藤蔓方向；
  调整顺序或增加兼容协商时必须同步两仓库、模板测试、README 和兼容性说明。
- 涉及 FCA 兼容的 PR 必须记录所使用的 `fca_ref` 或提交，并至少验证回退 Payload 与
  复用 FCA Payload 两条路径。

## Masa 配置页

- 从其他模组移植的功能必须显示在 TriWasa 的“移植”标签页中。
- TriWasa 原创功能必须显示在“辅助工具”标签页中，不得放入“移植”。
- 第三方模组修复必须显示在“其他模组修复”标签页中。
- 每个可配置功能以一个总开关作为父项；相关模式和子功能必须通过隐藏的
  `...Expanded` 布尔状态折叠，并缩进显示在父项之下。
- 父项使用左侧 `[+]` / `[-]` 展开或折叠子项。配置文件缺失、搜索过滤和父项关闭时，
  仍须保持配置可恢复，不得把隐藏状态当作功能总开关。
- 新增父项时同时维护 `TriWasaConfigs` 中的父子列表、`TriWasaConfigListWidget`
  可见性、`TriWasaConfigOptionWidget` 缩进和展开按钮、中英文翻译及 README。
- 配置默认值应与 README 表格一致。不要重新引入独立于 MaLiLib 的配置系统。

## Mixin 与客户端边界

- TriWasa 只允许客户端行为。不得添加专用服务端业务、世界持久化或服务端权限绕过。
- 新增 Mixin 必须加入 `triwasa.mixins.json` 的 `client` 列表，并在
  `TriWasaMixinPlugin` 中按 Minecraft 或可选模组条件隔离。
- 优先使用最小 `@Inject`/`@ModifyVariable` 等接入；必须改变原调用时使用
  MixinExtras `Operation` 保留原行为，避免无必要 `@Overwrite`。
- 保持 `defaultRequire: 1`。可选模组 Mixin 应由插件条件控制，不要为绕过失败静默
  降低 required。
- Litematica 放置入口同时覆盖普通轻松放置和 Hana 的四改打印机
  （Hana 版 `litematica-printer` 分支）；MasaGadget、EarlyCompat、REI、IPN 或 Quick
  Shulker 兼容不得改变 TriWasa 的核心容器语义。更好的打印机只适配该 Hana 分支，
  不为上游原始打印机复用同一套 Mixin。
- REI/Cloth Config 仅用于可选编译。终端浮空交互区通过 REI exclusion/display bounds
  标注；大末影箱实际容器仍由原版 `AbstractContainerMenu`、`Slot` 和
  `SlotPositionAccessor` 实现。
- 修改客户端菜单、Slot 坐标或协议时，必须回归普通窗口、全高度终端、背包快捷移动、
  设置切换和小窗口布局。

## Git、提交与 GitHub

- 开始任务时先运行 `git status --short`，区分并保留用户已有修改；不得覆盖、清理或
  格式化与当前任务无关的文件。
- 功能改动默认从最新 `main` 创建 `codex/<主题>` 本地分支。不要在功能分支混入无关
  提交，也不要直接修改或强推 `main`；确需改写已发布历史时必须取得用户明确同意。
- 除非用户明确要求，否则不要 push、创建 PR、启用 Auto-merge 或合并 PR。提交前确认
  暂存区只包含当前任务文件。
- 提交前按变更类型验证：
  - 源码、资源、Mixin、版本属性、Gradle、Wrapper 或 workflow：构建 26.2 和 26.3，
    并运行 `logicTest`。
  - README、docs、许可证等纯文档改动：确认所有变更路径都不属于 CI 的构建必需路径，
    不要求执行完整 Minecraft 构建；PR 已创建时仍须让 `CI Gate` 通过。
  - 涉及终端、Litematica、REI、Quick Shulker 或客户端渲染：补充 GameTest 或明确
    记录未执行的手测项。`src/gametest` 不提交、不进入 CI。
- 提交信息使用清晰的 `type: summary` 前缀，例如 `fix:`、`feat:`、`refactor:`、
  `ci:`、`docs:`。不得提交 `.gradle/`、`build/`、`versions/*/build/`、运行日志、
  `libs/*.jar`、GameTest 或 `docs/` 中的本地工作记录。
- `README.md` 与 `README_en.md` 是互为镜像的中英文用户文档。修改功能、安装、
  依赖、配置或兼容性说明时同步更新两种语言，并保留文件顶部互相跳转的绝对 GitHub
  链接；两份 README 中的仓库文件链接必须使用完整 URL。
- 用户要求创建 PR 时，PR 必须说明改动范围、自动测试、GameTest/手测结果、未验证风险
  和性能影响；涉及 FCA 兼容时注明使用的 `fca_ref` 或 FCA 提交。
- `main` 只能通过 PR 合并，必需检查为固定的 `CI Gate`，分支必须与最新 `main`
  同步。不要绕过分支保护、直接推送或临时移除必需检查。
- 只有仓库所有者提交的同仓库、非草稿 PR 才可使用 `auto-merge` 标签；Fork PR、
  未添加标签的 PR 以及修改 workflow、Gradle、Wrapper 或依赖配置的 PR 必须人工
  合并。用户未明确授权合并时，不得添加该标签。
- Auto-merge 使用 Squash。移除 `auto-merge` 标签后应确认 Auto-merge 已关闭。
- 合并 PR 后同步本地 `main`、清理已合并分支，并确认工作区干净。不得要求用户在聊天
  中发送 GitHub Token、密码或其他凭据。
- FCA 与 TriWasa 的协议改动必须按依赖顺序处理。TriWasa 的 CI 默认使用 FCA
  `main`，跨仓库验证时使用 `fca_ref` 指向对应 FCA 提交，并先确认 FCA 的公开 API
  已可用。

## 代码与交付习惯

- 沿用现有 tab 缩进、UTF-8、客户端功能分包和紧凑 Java 风格；避免无关格式化。
- 优先使用 Minecraft/Fabric/MaLiLib/Litematica 已有 API，新增依赖前说明必要性并
  确认运行期依赖边界。
- 保留 MIT 许可证、AI 辅助开发声明、第三方来源说明和模组元数据。
- 任务完成前至少确认：双版本构建通过；README、配置翻译和行为一致；FCA 未被误打包；
  涉及客户端运行期逻辑时记录手测结果。
