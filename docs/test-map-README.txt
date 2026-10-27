BloomSystem 1.21.1 · 命令方块测试地图
========================================

【安装】
1. Minecraft 1.21.1 Fabric，Java 21。
2. mods 放入：Fabric API、Veil 4.5.2、Cardinal Components API 6.1.3（base/entity）、YACL 3.6.6、BloomSystem 0.1.0 jar。
3. 把本目录下的 `TestWorld` 整个文件夹放进 `.minecraft/saves/`。
4. 单人游戏 → 选择世界「TestWorld」进入（创造模式、允许作弊、和平、不刷怪、时间锁定白天）。

【地图布局】
- 黑色 Blackstone 平台 + 金色法阵地板 + 中央 lodestone。
- 南侧（z=5）三个拉杆：
  - 04 Trigger ritual：触发一次棱镜法阵（效果落在你脚下）。
  - 05 Viewpoint：传送到观看位并看向中央。
  - 06 Trigger ritual：再次触发法阵。
- 北侧（z=-5）三个拉杆：
  - 01 Trigger ritual：触发一次棱镜法阵。
  - 02 Set midnight：把时间设置为午夜（更容易看清 bloom）。
  - 03 Set daytime：恢复白天。
- 每个拉杆下方是命令方块，旁边/后方有木牌说明。
- 玩家出生点在世界中心附近（spawnRadius=0），面朝南侧拉杆。

【测试步骤】
1. 进入世界，右键南侧任意「Trigger ritual」拉杆。
2. 观察：脚下出现多层旋转法阵；屏幕左上出现彩虹 HUD、阶段文字与进度条。
3. 右键「Set midnight」拉杆，再触发一次，观察 Veil bloom 与色散后处理在暗环境下的表现。
4. 按 B（或输入 `/bloomconfig`）打开 YACL 配置：
   - 总开关 / Bloom 强度 / 色散强度 / HUD 发光文字
   - 保存后立即生效，配置写入 `config/bloomsystem.properties`
5. 也可直接命令：`/bloom trigger @p prism_ritual`（需要 OP/作弊）。

【测试点】
- 世界层：法阵环、径向光带、旋转刃面、bloom shard 输出。
- 屏幕层：彩虹 HUD、发光文字、三角渐变面板、阶段/进度条。
- 后处理：Veil chromatic pipeline（色差/色相滚动/暗角）。
- 配置层：YACL 面板与属性文件持久化。
- 数据层：CCA PlayerComponent 已注册；服务端只下发最小 payload（技能 ID/位置/时间戳）。

【已知限制】
- 当前碎片仍是 CPU 几何，GPU 实例化棱镜碎片尚未实现。
- Sodium + Iris 组合未在本机实测。
- 客户端崩溃时先检查：Veil 4.5.2、CCA 6.1.3、YACL 3.6.6 是否与 1.21.1 匹配；日志看 `logs/latest.log`。

【开发重建】
- 起服务端：`gradlew runServer`（RCON 25575 / bloomdev）。
- 重建：`powershell -ExecutionPolicy Bypass -File scripts\build-bloomsystem-testmap.ps1`
- 停服后复制 `run\world` → `run\saves\TestWorld`。
