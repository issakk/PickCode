# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PickCode (取件码) — Android 原生快递取件码自动提取工具。从短信中识别取件码、快递公司、取件地址。

**技术栈**: Kotlin + Jetpack Compose + Material 3 + MVVM + Hilt + Room + Ktor Client

## Build & Development

项目根目录为 `android/`，使用 Gradle 构建：

```bash
cd android
./gradlew assembleDebug      # 构建 debug APK
./gradlew assembleRelease    # 构建 release APK (已配置 R8 混淆)
./gradlew installDebug       # 安装到设备
```

- **最低版本**: Android 7.0 (API 24)
- **目标版本**: Android 14 (API 34)
- **版本号**: `android/app/build.gradle.kts` 中 `versionCode`/`versionName`，更新时同步改两处
- **无测试框架**: 项目未配置单元测试

## Architecture

### MVVM + Hilt 依赖注入

```
android/app/src/main/java/com/pickcode/v2/
├── data/
│   ├── local/          # Room 数据库：AppDatabase、DAO、Entity、Converters
│   ├── datastore/      # DataStore Preferences (AI 配置等)
│   └── repository/     # Repository 层（MatchRule、PackageCode、PackageRecord）
├── domain/
│   ├── engine/         # MatchEngine (匹配引擎)、SmsReader (短信读取)
│   └── model/          # 领域模型 (MatchRule、PackageCode、PackageRecord、AiConfig)
├── di/                 # Hilt 模块 (AppModule)
├── navigation/         # Navigation Compose 路由定义 (Routes、AppNavigation)
├── ui/
│   ├── screen/         # 页面 + ViewModel
│   │   ├── main/       # MainScreen (底部 Tab 导航容器)
│   │   ├── pickup/     # 取件码列表 + 编辑
│   │   ├── package_record/ # 包裹记录 + 编辑
│   │   └── my/         # 我的：匹配规则、AI 设置、FAQ、关于、更新日志
│   ├── components/     # 公共组件 (CodeCard、TagGrid、PlatformIcon、GradientHeader)
│   ├── theme/          # Material 3 主题 (Color、Type、Theme)
│   └── util/           # 工具类 (DateFormat、CodeFormat、Vibrate、DeepLink、LogBuffer)
├── MainActivity.kt
└── PickCodeApp.kt      # Application 入口 (@HiltAndroidApp)
```

### 三 Tab 结构

底部导航由 `MainScreen.kt` 管理，三个 Tab：

| Tab | Route | Screen | 功能 |
|-----|-------|--------|------|
| 取件码 | `pickup_list` | `PickupListScreen` | 短信读取、匹配、取件码列表展示 |
| 包裹 | `package_records` | `PackageRecordScreen` | 包裹跟踪记录 |
| 我的 | `my` | `MyScreen` | 匹配规则管理、AI 设置、FAQ、关于 |

### 核心数据流

- **Room 数据库**: `AppDatabase` 包含三张表 (`match_rules`、`package_codes`、`package_records`)
- **DataStore**: 存储 AI 配置 (`SettingsDataStore`)
- **Repository 模式**: ViewModel 通过 Repository 访问数据，Repository 封装 DAO 和 DataStore
- **Hilt 注入**: 所有 ViewModel、Repository、Database 通过 Hilt 注入

### SMS 匹配引擎

核心逻辑在 `domain/engine/MatchEngine.kt`：

- **两种匹配模式**: `start/end` 文本标记匹配 和 `regex` 正则表达式匹配
- **提取三个字段**: `code`（取件码）、`express`（快递公司）、`address`（取件地址）
- **短信读取**: `SmsReader.kt` 通过 Android ContentResolver 查询 `content://sms/inbox`
- **权限**: 需要 `READ_SMS` 和 `RECEIVE_SMS` 权限（AndroidManifest.xml 已声明）

### Navigation

- **根导航**: `AppNavigation.kt` 定义全局路由（Main、EditCode、EditPackage、MatchRules 等）
- **Tab 导航**: `MainScreen.kt` 内部嵌套 NavHost 管理三个 Tab 页面
- **路由定义**: `Routes` object 集中管理所有路由常量和构建函数

## Key Conventions

- **样式**: Material 3 Design System，主题色蓝色 (`#0052d9`)，组件使用 `MaterialTheme.colorScheme`
- **Compose**: 所有 UI 使用 Jetpack Compose，无 XML 布局
- **ViewModel**: 每个 Screen 对应一个 ViewModel，通过 `hiltViewModel()` 注入
- **协程**: 所有异步操作使用 Kotlin Coroutines + Flow
- **序列化**: Kotlinx Serialization (Ktor Client 使用)
- **触感反馈**: 使用 `Vibrate.kt` 工具类封装 `Vibrator` API
- **日期格式**: `DateFormat.kt` 提供统一的时间格式化函数

## Proguard Rules

Release 构建启用 R8 混淆，规则在 `android/app/proguard-rules.pro`。近期修复：

- Ktor Client 在 Android 上需要 `-dontwarn` 规则（SLF4J、Bouncy Castle 等）
- Room 生成的代码已通过 KSP 自动保留

## Common Issues

- **资源文件格式**: `avatar.png` 实际为 WebP 格式，AAPT2 在 release 构建时会报错，已重命名为 `.webp`
- **滚动性能**: `CodeCard` 组件在列表中滚动时可能有卡顿，已通过隔离测试定位（commit 741ee89）
