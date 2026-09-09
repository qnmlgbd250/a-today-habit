# AGENTS.md — 项目规则

本文件是 AI 编码助手（ZCode / Claude / Cursor 等）在本仓库工作时的规则与上下文说明。

## 项目概览

「小日常」（A Today Habit）— 一款极简习惯打卡 Android 应用。

- **包名 / applicationId**：`com.today.habit`（Gradle rootProject 名为 `ConstantTrack`，勿混淆）
- **mineSdk**，JVM target 11
- **UI 语言为中文**，面向用户的所有文案使用中文

## 技术栈

| 组件 | 技术 |
|------|------|
| UI | Jetpack Compose (BOM 2026.08.00，Compose 1.12) + Material3 |
| 架构 | MVVM（ViewModel + State/LiveData）；主布局为 Box，NavHost 内容通过 `layerBackdrop` 录制为背景层 |
| 底栏 | 悬浮液态玻璃底栏，基于 [backdrop](https://github.com/Kyant0/AndroidLiquidGlass) 库（`io.github.kyant0:backdrop`，见 `ui/component/BottomNavigationBar.kt`） |
| 数据库 | Room 2.8.4（KSP 编译） |
| 导航 | Navigation Compose |
| 语言 | Kotlin 2.4.10、Coroutines & Flow |
| 序列化 | Gson（备份/恢复） |
| 构建 | AGP 9.4.0 + Gradle 9.6.0（AGP 9 已内置 Kotlin 支持，**不要**再引入 `org.jetbrains.kotlin.android` 插件），依赖统一走 `gradle/libs.versions.toml` |

## 常用命令

```bash
# 编译检查（最快验证方式）
./gradlew compileDebugKotlin

# 完整构建
./gradlew build

# 安装到设备/模拟器
./gradlew installDebug
```

- Windows 环境下使用 `gradlew.bat` 或 `./gradlew`（Git Bash）。
- 本项目无单测覆盖要求，改动后至少跑通 `compileDebugKotlin`。
- 本机未配置 `JAVA_HOME` 与 `local.properties`（已 gitignore），命令行构建需先指定：
  ```bash
  JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew compileDebugKotlin
  ```
  `local.properties` 内容为 `sdk.dir=C:/Users/ttt/AppData/Local/Android/Sdk`。

## 目录结构

```
app/src/main/java/com/today/habit/
├── MainActivity.kt          # 入口 + 导航宿主（iOS 风格转场定义于此）
├── data/
│   ├── entity/              # Room 实体（Habit, CheckInRecord）
│   ├── dao/                 # Room DAO
│   ├── AppDatabase.kt       # Room 数据库单例
│   ├── HabitRepository.kt   # 数据仓库
│   └── SettingsManager.kt   # SharedPreferences 设置
├── ui/
│   ├── screen/              # HomeScreen / StatsScreen / ManageHabitsScreen / IconPickerScreen
│   ├── component/           # 通用组件（玻璃底栏、SFIcons 图标注册表、HabitIcons、音效等）
│   ├── theme/               # 颜色、主题、字体
│   └── viewmodel/           # HabitViewModel（唯一的 ViewModel）
```

## 图标规范

- 全局图标统一使用 **SF Symbols**（来源 sfsymbols.rakibulism.space，已获授权），**禁止引入 Material Icons**。
- 图标以 VectorDrawable 形式放在 `res/drawable/sf_<名称>.xml`。生成规范：统一 1000×1000 viewport，字形最长边缩放至 750（75% 光学尺寸）并居中——**不要**直接用符号墨迹边界当 viewport（会导致非方形图标被拉伸、大小不一致）。
- 通过 `ui/component/SFIcons.kt` 注册表引用：`SFIcons.res("sun.max")` 返回 drawable id，`SFIcons.label()` 返回中文名。新增图标需同时加 drawable 和注册表条目。
- 习惯图标：`Habit.icon` 字段直接存 SF Symbols 名称；历史遗留 key（"Sunny" 等）由 `HabitIcons.LegacyMap` 自动映射，勿删。
- 全 App **禁止使用 Dialog/AlertDialog**：新建与编辑习惯用 `HabitEditScreen`（路由 `habit_edit/new` 或 `habit_edit/{id}`），删除确认用 `DeleteHabitScreen`（两步确认：第一次点击只进入"确认删除"状态），图标选择用 `IconPickerScreen`。跨页回传统一走 `savedStateHandle` 的 `picked_icon` 键。

## 导航转场规范

- **全局统一侧滑转场**：在 `MainActivity` 的 `NavHost` 上定义（新页面从右推入、返回滑出、底层页面 1/4 位移），标签页互切也不例外，页面级不要单独覆盖。
- 新增页面无需再写转场参数，直接加 `composable` 即可。
- **勿开启** `android:enableOnBackInvokedCallback`：开启后手势返回会先播放系统预测性返回的整窗缩小+淡出预览动画，与应用内侧滑转场叠加，观感割裂（已因此回退过一次）。应用内返回动画由 NavHost 的 pop 转场负责。
- 页面内跨导航需要保留的状态用 `rememberSaveable`（如弹窗草稿、编辑目标 id）。

## 编码规范

1. **Kotlin 代码风格**：遵循 `kotlin.code.style=official`；与周围代码保持一致的注释密度与命名习惯。
2. **注释语言**：代码注释使用中文（现有代码即如此）。
3. **Compose**：UI 一律使用 Material3；新增可复用组件放 `ui/component/`，页面放 `ui/screen/`。
4. **状态管理**：优先沿用现有模式——ViewModel 中 `mutableStateOf` + `State<T>` 暴露、仓库层 Flow 转 LiveData。
5. **依赖管理**：所有依赖与版本号必须加到 `gradle/libs.versions.toml`，不要在 `build.gradle.kts` 里硬编码版本。
6. **字符串资源**：App 名称等用户可见文案放 `res/values/strings.xml`；界面内文案目前直接写在 Compose 代码中（中文），新增时保持一致。

## 数据库改动规则

- `AppDatabase` 当前 `version = 3`（Room 2.8.4，`fallbackToDestructiveMigration(dropAllTables = true)`，升级清空本地数据）。
- **修改任何 Room 实体（Habit / CheckInRecord）时必须同步递增 database version**，并在提交说明中明确提醒"升级会清除本地数据"。
- 尽量通过加字段 + 默认值的方式做向后兼容，避免删列/改列名。

## 版本发布规则

- 每次**发版提交必须同步修改** `app/build.gradle.kts` 中的 `versionCode`（+1）和 `versionName`。
- **Commit message 格式**：`vX.Y.Z: 中文变更描述`，例如 `v1.0.39: 日期选择栏显示月/日`。
- 日常开发提交也使用简洁的中文描述。

## 发版流程

以下命令均在 Git Bash 中执行（Windows 环境）。

1. **升版本**：`app/build.gradle.kts` 中 `versionCode` +1、`versionName` 递增，按上面的格式提交。

2. **构建 release**（依赖要求 compileSdk ≥ 37，AGP 报错信息里会提示）：
   ```bash
   JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew assembleRelease
   ```

3. **对齐 + 签名**（沿用用户 debug keystore，证书 SHA-256 为 `775daca9...`，与历史版本一致，用户可覆盖安装）：
   ```bash
   BT="C:\Users\ttt\AppData\Local\Android\Sdk\build-tools\36.0.0"
   OUT="app\build\outputs\apk\release"
   "$BT/zipalign.exe" -f 4 "$OUT/app-release-unsigned.apk" "$OUT/aligned.apk"
   "$BT/apksigner.bat" sign \
     --ks "C:\Users\ttt\.android\debug.keystore" \
     --ks-pass pass:android --ks-key-alias androiddebugkey \
     --out "$OUT/小日常-<版本>.apk" "$OUT/aligned.apk"
   # 校验：应输出 CN=Android Debug，SHA-256 指纹 775daca9...
   "$BT/apksigner.bat" verify --print-certs "$OUT/小日常-<版本>.apk"
   ```

4. **发布到云剪贴板**（房间地址：http://8.148.25.234:5000/r/sky ，内容 20 天有效）：
   ```bash
   # 上传 APK（multipart，字段名固定为 file）
   curl -X POST "http://8.148.25.234:5000/api/files?room=sky" \
     -F "file=@app/build/outputs/apk/release/小日常-<版本>.apk"

   # 发送发版说明文本（JSON）
   curl -X POST "http://8.148.25.234:5000/api/items?room=sky" \
     -H "Content-Type: application/json" \
     -d '{"content": "【小日常 vX.Y.Z 发版】..."}'
   ```
   - 上传成功返回 HTTP 201；若返回 403 说明房间后来设置了密码，需向用户索取 `X-Room-Password` 请求头的值。
   - 发版说明文本中写明版本号、versionCode、更新内容，并注明"覆盖安装无需卸载旧版"（证书不一致时如实改写，见下）。
   - ⚠️ 云剪贴板**不支持中文文件名**（返回 400 `"请选择文件"`）：APK 先复制为英文名（如 `xiaorichang-<版本>.apk`）再上传，发版说明里注明对应中文名。
   - ⚠️ 发版说明含中文时**不要用 `-d` 内联 JSON**（返回 400 `"内容不能为空"`）：先把 JSON 写文件，再用 `--data-binary @文件` 发送（文件放仓库内用相对路径，`curl` 读系统 `/tmp` 会失败）。

## 自动发版规则（每次更新代码都自动发）

- AI 每次完成用户确认的代码更新后，**自动走完上面的发版流程并发布到云剪贴板**，无需用户再下指令（除非遇到 403 需要房间密码，或证书不一致需用户决策，见下）。
- 发版提交与版本 bump 合并为一个提交，message 沿用 `vX.Y.Z: 中文变更描述` 格式。

### 本机（linsh）环境差异

上面发版流程里的 `C:\Users\ttt\...` 路径是 ttt 机器的，在 linsh 本机按下表替换：

| 项目 | linsh 本机值 |
|------|--------------|
| SDK | `C:/Android/Sdk`（`local.properties` 写 `sdk.dir=C:/Android/Sdk`，已 gitignore） |
| build-tools | `C:/Android/Sdk/build-tools/36.0.0` |
| debug keystore | `C:/Users/linsh/.android/debug.keystore`（密码 `android`，别名 `androiddebugkey`） |
| `~/.gradle/gradle.properties` | 配了 `127.0.0.1:7890` 代理；该代理未启动时构建前临时删掉代理行，构建完立即恢复 |
| `apksigner.bat` | 经 Git Bash 调用输出会被吞，改用 `java -jar <build-tools>/lib/apksigner.jar` |
| `android-37.0` 平台 | 公有仓库元数据最高只到 android-36，需用 canary 通道安装：`sdkmanager --channel=3 "platforms;android-37.0"`（直接调 java 主类，见下；装一次即可） |

```bash
# canary 通道安装平台（Git Bash，直接调 java，绕开 .bat）
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
cd /c/Android/Sdk/cmdline-tools/latest
"$JAVA_HOME/bin/java" -Dcom.android.sdklib.toolsdir="$PWD" -cp "lib/*" \
  com.android.sdklib.tool.sdkmanager.SdkManagerCli --channel=3 "platforms;android-37.0"
```

### 签名证书警告（重要）

- linsh 本机 debug keystore 的 SHA-256 是 `9d2a8fa2...`，与历史版本 `775daca9...` **不一致**，签出的包老用户无法覆盖安装（需卸载重装，数据清空）。
- 发版说明里必须如实注明证书变化，不得写"覆盖安装无需卸载旧版"。
- 要恢复覆盖安装，需用户提供原 `775daca9` 证书的 keystore 文件，放到本机后再按原流程签名；拿到之前每次发版都要重复本警告。

## UI 风格规范（iOS 化，去 Android 化）

- **禁止引入 Material 风格控件**：输入框用 `IOSFormTextField`、分段选择用 `IOSSegmentedControl`、滑块用 `IOSSlider`、筛选胶囊用 `IOSPill`、提示用 `IOSToast`（禁用 `android.widget.Toast`）、弹出菜单用 `IOSMenuCard`/`IOSMenuItem`（液态玻璃卡片，需传入 backdrop），均在 `ui/component/IOSUi.kt`。新增 iOS 组件也放这里。
- 全局水波纹已在 `Theme.kt` 通过 `LocalIndication provides NoIndication` 关闭（iOS 无 ripple），勿移除。
- 顶栏统一 `CenterAlignedTopAppBar`（居中标题 + 左返回箭头 + 右动作），背景与页面同色。

## 其他注意事项

- 仓库根目录的 `search_results*.csv` 是历史遗留文件，**不要引用、不要模仿、也不要随意删除**。
- 不要提交 `local.properties`、`build/`、`.idea/` 缓存等内容（已配置 .gitignore）。
- Release 构建开启了 minify + shrinkResources，新增反射/Gson 序列化的类如遇混淆问题需检查 `proguard-rules.pro`。
