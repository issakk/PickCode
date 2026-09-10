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
- **单元测试**: `./gradlew testDebugUnitTest`（覆盖 `MatchEngine`，CI 会跑）
- **发布签名**: release 的 keystore 由环境变量 `RELEASE_KEYSTORE_PATH` / `RELEASE_KEYSTORE_PASSWORD` / `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD` 注入（CI 从 secrets 解码）；未配置时回落到仓库内 `app/debug.keystore`。换 key 后旧安装无法覆盖升级，用户必须先卸载

## Architecture

### MVVM + Hilt 依赖注入

```
android/app/src/main/java/com/pickcode/v2/
├── data/
│   ├── local/          # Room 数据库：AppDatabase、DAO、Entity、Converters
│   ├── datastore/      # DataStore Preferences (AI 配置等)
│   └── repository/     # Repository 层（MatchRule、PackageCode）
├── domain/
│   ├── engine/         # MatchEngine (匹配引擎)、SmsReader (短信读取)
│   └── model/          # 领域模型 (MatchRule、PackageCode)
├── di/                 # Hilt 模块 (AppModule)
├── navigation/         # Navigation Compose 路由定义 (Routes、AppNavigation)
├── ui/
│   ├── screen/         # 页面 + ViewModel
│   │   ├── main/       # MainScreen (底部 Tab 导航容器)
│   │   ├── pickup/     # 取件码列表 + 编辑
│   │   └── my/         # 我的：匹配规则、AI 设置、FAQ、关于、更新日志
│   ├── components/     # 公共组件 (CodeCard、TagGrid、GradientHeader)
│   ├── theme/          # Material 3 主题 (Color、Theme)，字体样式用 M3 默认
│   └── util/           # 工具类 (DateFormat、CodeFormat)
├── MainActivity.kt
└── PickCodeApp.kt      # Application 入口 (@HiltAndroidApp)
```

### 两 Tab 结构

底部导航由 `MainScreen.kt` 管理，两个 Tab：

| Tab | Route | Screen | 功能 |
|-----|-------|--------|------|
| 取件码 | `pickup_list` | `PickupListScreen` | 短信读取、匹配、取件码列表展示 |
| 我的 | `my` | `MyScreen` | 匹配规则管理、AI 设置、FAQ、关于 |

### 核心数据流

- **Room 数据库**: `AppDatabase` 包含两张表 (`match_rules`、`package_codes`)，当前版本 3，schema 导出到 `android/app/schemas`（改表必须加 Migration 并同步 schema）
- **DataStore**: 存储 AI 配置 (`SettingsDataStore`)
- **Repository 模式**: ViewModel 通过 Repository 访问数据，Repository 封装 DAO 和 DataStore
- **Hilt 注入**: 所有 ViewModel、Repository、Database 通过 Hilt 注入

### SMS 匹配引擎

核心逻辑在 `domain/engine/MatchEngine.kt`：

- **两种匹配模式**: `start/end` 文本标记匹配 和 `regex` 正则表达式匹配
- **提取三个字段**: `code`（取件码）、`express`（快递公司）、`address`（取件地址）
- **多取件码**: 一条短信里的多个取件码都会被提取（start/end 与 regex 都支持），入库按 `(code, date)` 唯一索引去重
- **短信读取**: `SmsReader.kt` 通过 Android ContentResolver 查询 `content://sms/inbox`
- **权限**: 只需要 `READ_SMS`（AndroidManifest.xml 已声明）

### Navigation

- **根导航**: `AppNavigation.kt` 定义全局路由（Main、EditCode、MatchRules、MatchSettings、AiSettings、Faq、About、Changelog）
- **Tab 导航**: `MainScreen.kt` 内部嵌套 NavHost 管理两个 Tab 页面
- **路由定义**: `Routes` object 集中管理所有路由常量和构建函数

## Key Conventions

- **样式**: Material 3 Design System，品牌色蓝→紫 (渐变头 `GradientHeader`)，组件一律用 `MaterialTheme.colorScheme` / `.typography` / `.shapes`，不要在页面里硬编码字号和圆角
- **顶部栏**: 所有页面统一用 `ui/components/GradientHeader.kt`（品牌渐变 + 白字，渐变铺到状态栏下），不要另写 TopAppBar 或自定义 header
- **系统栏/键盘**: MainActivity 已 `enableEdgeToEdge()`；列表页的底部间距由 `MainScreen` 的 Scaffold 负责，独立全屏页（EditCode、MatchSettings、AiSettings、About、Faq、Changelog）必须自己加 `navigationBarsPadding()`，有输入框的还要 `imePadding()`
- **Compose**: 所有 UI 使用 Jetpack Compose，无 XML 布局
- **ViewModel**: 每个 Screen 对应一个 ViewModel，通过 `hiltViewModel()` 注入
- **协程**: 所有异步操作使用 Kotlin Coroutines + Flow
- **序列化**: Kotlinx Serialization (Ktor Client 使用)
- **列表刷新**: 取件码列表是「一次性查询 + 内存缓存」，所以编辑页保存后必须靠 `PickupListScreen` 的 `LifecycleResumeEffect` 调 `viewModel.reload()`；改这块逻辑时别把刷新删了
- **日期格式**: `DateFormat.kt` 提供统一的时间格式化函数

## Proguard Rules

Release 构建启用 R8 混淆，规则在 `android/app/proguard-rules.pro`。近期修复：

- Ktor Client 在 Android 上需要 `-dontwarn` 规则（SLF4J、Bouncy Castle 等）
- Room 生成的代码已通过 KSP 自动保留

## Common Issues

- **资源文件格式**: `avatar.png` 实际为 WebP 格式，AAPT2 在 release 构建时会报错，已重命名为 `.webp`
- **滚动性能**: `CodeCard` 组件在列表中滚动时可能有卡顿，已通过隔离测试定位（commit 741ee89）
