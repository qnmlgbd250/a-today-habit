# A Today Habit (今日习惯) 🌿

一个基于 Android Jetpack Compose 开发的清新、简约、功能强大的习惯追踪应用。

## ✨ 功能特性

- **简约美学**：极简主义设计，提供无压力的使用体验。
- **分次打卡**：支持设定每日目标次数（如：一天喝 8 杯水），通过圆环进度展示完成度。
- **顺时针动画**：精致的打卡填充动画，增强达成目标的成就感。
- **合并热力图**：全习惯汇总热力图，根据打卡频率呈现 5 级绿色深度变化。
- **灵活周期**：支持每天、工作日、每周（指定日期）、每月（指定日期）等多种周期设定。
- **常用图标库**：内置 20+ 精美线性图标，涵盖学习、运动、生活等多种场景。
- **习惯管理**：支持随时编辑、重命名或删除已有的习惯。

## 🛠 技术栈

- **UI 框架**：[Jetpack Compose](https://developer.android.com/jetpack/compose) (声明式 UI)
- **架构**：MVVM (ViewModel + Flow + LiveData)
- **数据库**：[Room](https://developer.android.com/training/data-storage/room) (本地持久化)
- **导航**：Compose Navigation
- **语言**：Kotlin + Kotlin Coroutines & Flow

## 🚀 构建与安装

### 环境要求
- Android Studio Ladybug (2024.2.1) 或更高版本
- JDK 17
- Android SDK 26+ (Android 8.0 及以上)

### 构建步骤
1. **克隆仓库**
   ```bash
   git clone git@github.com:qnmlgbd250/a-today-habit.git
   ```
2. **打开项目**
   使用 Android Studio 打开项目根目录。
3. **同步 Gradle**
   等待 Gradle 构建完成并下载相关依赖。
4. **运行应用**
   连接 Android 设备或模拟器，点击 **Run** 按钮。

## 📸 预览
*(由于当前为纯代码环境，请在运行后自行查看精致的 UI 效果)*

## 📄 开源协议
MIT License
