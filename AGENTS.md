# AGENTS.md — 项目规则

> 通用规范：本文件不写任何本机绝对路径。各电脑的 JDK / SDK 安装位置不同，命令中统一用占位符：
> - `$JAVA_HOME`：本机 Android Studio 自带 jbr（各机器自行配好环境变量）
> - `$ANDROID_SDK`：本机 Android SDK 根目录（即 `local.properties` 中 `sdk.dir` 的值）
>
> 所有命令均在 Git Bash + 仓库根目录执行（Windows 环境）。

## 版本发布规则

- 每次**发版提交必须同步修改** `app/build.gradle.kts` 中的 `versionCode`（+1）和 `versionName`。
- **Commit message 格式**：`vX.Y.Z: 中文变更描述`，例如 `v1.0.39: 日期选择栏显示月/日`。
- 日常开发提交也使用简洁的中文描述。

## 发版流程

1. **升版本**：`app/build.gradle.kts` 中 `versionCode` +1、`versionName` 递增，按上面的格式提交。

2. **构建已签名的 release 包**（签名由 `app/build.gradle.kts` 的 `signingConfigs.release` 自动完成，无需手动 zipalign / apksigner）：
   ```bash
   ./gradlew assembleRelease
   ```
   产物即成品：`app/build/outputs/apk/release/app-release.apk`（已 zipalign + 发行版签名，开箱即用）。
   如需中文名留档：`cp app/build/outputs/apk/release/app-release.apk "app/build/outputs/apk/release/小日常-<版本>.apk"`
   可选校验签名：`$ANDROID_SDK/build-tools/<版本>/apksigner.bat verify --print-certs app/build/outputs/apk/release/app-release.apk`，证书 DN 应为 `CN=XiaoRiChang`，SHA-256 见下节。

3. **发布到云剪贴板**（房间地址：http://8.148.25.234:5000/r/sky ，内容 20 天有效）：**只上传 APK 文件，不发送发版说明文本**。
   ```bash
   # 上传 APK（multipart，字段名固定为 file；文件名必须用纯 ASCII，中文文件名服务端会报“请选择文件”）
   curl -X POST "http://8.148.25.234:5000/api/files?room=sky" \
     -F "file=@app/build/outputs/apk/release/app-release.apk;filename=xiaorichang-<版本>.apk"
   ```
   - 上传成功返回 HTTP 201；

### 发行版签名（证书与配置全量记录，多电脑通用）

- 不使用 debug keystore。发行版 keystore 随仓库提交：`keystore/release.keystore`；构建配置只用仓库相对路径，任何电脑 checkout 后零配置即可打出同签名包。
- 证书信息：
  - 别名：`xiaorichang`
  - DN：`CN=XiaoRiChang, OU=App, O=XiaoRiChang, L=Beijing, ST=Beijing, C=CN`
  - SHA-256：`8E:F3:BF:7E:23:FB:22:E2:1D:76:8F:5A:AD:93:F2:8D:5B:1B:7E:C0:0F:65:04:61:51:67:1F:23:B4:F4:E1:88`
  - 有效期：25 年（约至 2051 年）
- 签名密码（`app/build.gradle.kts` 从 `keystore/release.properties` 自动读取，此处同步记录一份）：
  - `storePassword=WPWb2Pl2EaolOvMfq4gH`
  - `keyAlias=xiaorichang`
  - `keyPassword=WPWb2Pl2EaolOvMfq4gH`
- ⚠️ keystore + 密码已随仓库提交，有仓库权限的人都能打出同签名包：不要公开仓库，不要分享给不可信的人。
- ⚠️ 不要在本文件写入任何本机绝对路径；新电脑只需配好 `local.properties` 的 `sdk.dir` 和 `JAVA_HOME`，其余全走仓库相对路径。
- 证书历史：曾用 debug 证书（`775daca9...`）发版，现发行版证书为 `8ef3bf7e...`，老用户需卸载重装一次（数据清空）。发版说明里必须如实注明，不得写"覆盖安装无需卸载旧版"。

## 自动发版规则（每次更新代码都自动发）

- 每次完成用户确认的代码更新后，**自动走完上面的发版流程并发布到云剪贴板**，无需用户再下指令（除非遇到 403 需要房间密码）。
- 发版提交与版本 bump 合并为一个提交，message 沿用 `vX.Y.Z: 中文变更描述` 格式。
