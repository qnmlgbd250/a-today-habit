# AGENTS.md — 项目规则

本文件是 AI 编码助手（ZCode / Claude / Cursor 等）在本仓库工作时的规则与上下文说明。

## 项目概览

「小日常」（A Today Habit）— 一款极简习惯打卡 Android 应用。

- **包名 / applicationId**：`com.today.habit`（Gradle rootProject 名为 `ConstantTrack`，勿混淆）
- **minSdk 26 / target & compileSdk 35**，JVM target 11
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
├── MainActivity.kt          # 入口 + 导航宿主
├── data/
│   ├── entity/              # Room 实体（Habit, CheckInRecord）
│   ├── dao/                 # Room DAO
│   ├── AppDatabase.kt       # Room 数据库单例
│   ├── HabitRepository.kt   # 数据仓库
│   └── SettingsManager.kt   # SharedPreferences 设置
├── ui/
│   ├── screen/              # HomeScreen / ManageHabitsScreen / StatsScreen
│   ├── component/           # 通用组件（底部导航、图标、音效等）
│   ├── theme/               # 颜色、主题、字体
│   └── viewmodel/           # HabitViewModel（唯一的 ViewModel）
```

## 编码规范

1. **Kotlin 代码风格**：遵循 `kotlin.code.style=official`；与周围代码保持一致的注释密度与命名习惯。
2. **注释语言**：代码注释使用中文（现有代码即如此）。
3. **Compose**：UI 一律使用 Material3；新增可复用组件放 `ui/component/`，页面放 `ui/screen/`。
4. **状态管理**：优先沿用现有模式——ViewModel 中 `mutableStateOf` + `State<T>` 暴露、仓库层 Flow 转 LiveData。
5. **依赖管理**：所有依赖与版本号必须加到 `gradle/libs.versions.toml`，不要在 `build.gradle.kts` 里硬编码版本。
6. **字符串资源**：App 名称等用户可见文案放 `res/values/strings.xml`；界面内文案目前直接写在 Compose 代码中（中文），新增时保持一致。

## 数据库改动规则

- `AppDatabase` 当前 `version = 3`（Room 2.8.4，`fallbackToDestructiveMigration(dropAllTables = true)`） 且使用 `fallbackToDestructiveMigration()`（会清数据）。
- **修改任何 Room 实体（Habit / CheckInRecord）时必须同步递增 database version**，并在提交说明中明确提醒"升级会清除本地数据"。
- 尽量通过加字段 + 默认值的方式做向后兼容，避免删列/改列名。

## 版本发布规则

- 每次**发版提交必须同步修改** `app/build.gradle.kts` 中的 `versionCode`（+1）和 `versionName`。
- **Commit message 格式**：`vX.Y.Z: 中文变更描述`，例如 `v1.0.39: 日期选择栏显示月/日`。
- 日常开发提交也使用简洁的中文描述。

## 其他注意事项

- 仓库根目录的 `search_results*.csv` 是历史遗留文件，**不要引用、不要模仿、也不要随意删除**。
- 不要提交 `local.properties`、`build/`、`.idea/` 缓存等内容（已配置 .gitignore）。
- Release 构建开启了 minify + shrinkResources，新增反射/Gson 序列化的类如遇混淆问题需检查 `proguard-rules.pro`。
