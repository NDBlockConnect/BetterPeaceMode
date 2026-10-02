<p style="font-size:28px;text-align:center;"><b>BetterPeaceMode</b></p>

> 为 Minecraft Java 版增加两个全新的和平取向游戏模式：**更好的和平（Better Peace）** 与 **真正的和平（Real Peace）**。

> [!NOTE]
> 面向 Minecraft **1.21.11**（Fabric）。两个模式都是在原版难度体系之上叠加规则：不修改难度选择界面，也不修改存档格式，因此存档带不带本模组都能打开。

---

## 1 引言

原版和平模式会彻底移除敌对生物，而原版简单/困难又放任敌对生物随意开战。BetterPeaceMode 在这两个极端之间补上两个可选模式：

| 模式 | 使用的难度 | 敌对刷怪 | 谁能主动开战 |
| :--- | :--- | :--- | :--- |
| `vanilla` | 不干预 | 原版 | 原版 |
| `better_peace` | 和平 | 无 | 谁都不能（友好生物互不攻击） |
| `real_peace` | 困难 | 正常，困难刷怪密度 | 谁都不能，直到被挑衅 |

## 2 模式

### 2.1 更好的和平

原版和平模式，再加一条规则：**友好生物之间不再互相攻击**。狐狸不再猎杀鸡、狼不再猎杀绵羊，其他动物之间的捕食行为也一并被压制。玩家与生物之间的冲突不受影响，世界依然是大家熟悉的那种和平模式。

### 2.2 真正的和平

在"更好的和平"基础上，把敌对生物放回来：

- 敌对生物正常刷新，刷怪密度为困难档，世界因此保持"困难难度那样热闹"的状态。
- **任何敌对实体都不主动开战。** 僵尸、骷髅、苦力怕、蜘蛛、末影人、猪灵在未被挑衅时无视玩家、村民、动物以及彼此。
- 末影龙与凋灵除外：它们保持敌对。
- **挑衅会产生单体对单体的仇恨。** 任何实体一旦真正被打中，就会记住是谁打的，并且只对该实体进行有限时间的反击（默认 600 刻）。因此攻击任何实体都会引发战斗。
- **任何实体都会反击。** 敌对生物本来就会战斗；牛、羊、鸡、村民等所有和平生物会在第一次被挑衅时获得反击能力。自带攻击力数值的生物沿用原值，其余按体型分配（小 1 / 中 2 / 大 3），并受 `retaliationDamageMultiplier` 缩放。
- **被挑衅的实体会呼叫增援。** 默认在 16 格内呼叫 7 个同类型增援，加上呼叫者共 8 个成员。只要团内还有任一成员存活，就会按 10-30 秒的随机间隔继续补兵，补兵半径为初始半径的两倍，补兵成员随机获得速度 II-V 与力量 I-III。当团内成员全部阵亡时，停止呼叫并解散。
- 下界生物离开下界后保持平静且不变异：猪灵不会变成僵尸猪灵，疣猪兽不会变成僵尸疣猪兽；在下界之外它们不会主动开战，但被攻击时依然会反击。
- 困难难度的僵尸增援被关闭，保证战斗始终是单体对单体。
- 玩家保留和平模式的回血速率（受伤时每秒 +1 生命值与 +1 饱和度），这是单纯困难难度不会提供的。
- 不走 AI 目标选择的攻击也被覆盖：史莱姆、河豚等"碰到就伤害"的接触伤害，在未挑衅时会被拒绝。

## 3 配置

首次启动会生成 `config/betterpeacemode.json`：

```json
{
  "gameMode": "VANILLA",
  "friendlyPeace": false,
  "realPeace": false,
  "keepBossHostile": true,
  "hostilesIgnoreEachOther": true,
  "crossDimensionCalm": true,
  "aggroDurationTicks": 600,
  "universalRetaliation": true,
  "retaliationDamageMultiplier": 1.0,
  "reinforcementsEnabled": true,
  "reinforcementCount": 7,
  "reinforcementRadius": 16.0,
  "autoReinforce": true,
  "autoReinforceMaxSeconds": 30,
  "autoReinforceRadiusMultiplier": 2.0
}
```

| 字段 | 含义 |
| :--- | :--- |
| `gameMode` | `VANILLA` / `BETTER_PEACE` / `REAL_PEACE`，这是切换模式的常规方式。 |
| `friendlyPeace` / `realPeace` | 细粒度开关，便于整合包组合规则。 |
| `keepBossHostile` | 在"真正的和平"下保持末影龙与凋灵敌对。 |
| `hostilesIgnoreEachOther` | 让敌对生物之间也不主动开战；关闭后仍然保护玩家、村民与动物。 |
| `crossDimensionCalm` | 下界生物离开下界后保持平静且不变异。 |
| `aggroDurationTicks` | 单体仇恨的持续时间，范围 20-24000。 |
| `universalRetaliation` | 让任何被挑衅的实体都能反击，包括原本没有战斗 AI 的和平生物。 |
| `retaliationDamageMultiplier` | 缩放被挑衅实体造成的伤害。 |
| `reinforcementCount` | 每次呼叫的增援数量；仇恨团上限为该值 +1（含呼叫者）。 |
| `reinforcementRadius` | 初始呼叫半径（格）。 |
| `autoReinforce` | 团内仍有存活成员时持续补兵。 |
| `autoReinforceMaxSeconds` | 自动补兵随机间隔的上限（最小 10 秒）。 |
| `autoReinforceRadiusMultiplier` | 自动补兵半径相对初始半径的倍数。 |

## 4 指令

需要权限等级 2（管理员）及以上，单机存档与服务器 OP 均可使用。

| 指令 | 作用 |
| :--- | :--- |
| `/betterpeace status` | 打印当前模式、难度与规则开关。 |
| `/betterpeace mode <vanilla\|better_peace\|real_peace>` | 切换模式并落盘。 |
| `/betterpeace reload` | 从磁盘重新读取配置。 |
| `/betterpeace save` | 将内存中的配置写回磁盘。 |

单机下也可用模组按键（默认 `B`）打开图形化配置界面；专用服务器上该界面只改本地配置，运营者请使用指令或服务器配置文件。

## 5 实现方式

模式体现为一小组原版切入点，而不是重写生物 AI：

- 目标获取（`Mob#setTarget`，以及重写了它的四个子类——狐狸、苦力怕、末影人、僵尸猪灵）被拦截，使生物根本无法开始追击。
- 伤害结算（`LivingEntity#hurtServer`）被拦截，使友好生物之间的攻击无法命中，并双向记录单体仇恨。
- 每秒一次的服务器扫描重新校验存活目标、固定当前模式的难度、阻止下界生物变异，并把开销限制为固定预算。
- 僵尸增援被压为 0，使战斗无法升级成群殴。

## 6 构建

```powershell
$env:JAVA_HOME = "<一个 JDK 25>"    # Loom 1.18 需要 JDK 25 运行
.\gradlew.bat build                 # 模组本身仍以 Java 21 为目标
```

产物：`build/libs/BetterPeaceMode-v26.0-Alpha.3-JE-1.21.11-Fabric.jar`。

## 7 兼容性

- Minecraft 1.21.11，Fabric Loader 0.19.3+ 与 Fabric API（指令接口使用 Fabric 的指令回调）。
- 客户端与专用服务器均可用；规则从通用入口点装载。
- 可与其他模组共存：补丁只针对众所周知的少量原版方法，且当模式为 `vanilla` 时全部为空操作。

---

License: Apache-2.0

GitHub@NDBlockConnect
