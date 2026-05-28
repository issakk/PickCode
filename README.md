# PickCode - 取件码

快递取件码自动提取工具。从短信中识别取件码、快递公司、取件地址，告别手动翻短信。

## 功能

- **短信自动匹配** — 读取短信，按规则提取取件码、快递公司、取件地址
- **自定义匹配规则** — 支持 start/end 文本匹配和正则表达式两种模式
- **包裹记录** — 跟踪包裹状态，支持标签、备注、已取/未取标记
- **平台图标识别** — 自动识别淘宝、京东、拼多多、抖音等平台来源
- **AI 辅助** — 可配置 AI 接口辅助生成匹配规则
- **本地存储** — 所有数据存本地 Room 数据库，不上传服务器

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Hilt 依赖注入 |
| 数据库 | Room |
| 网络 | Ktor Client |
| 导航 | Navigation Compose |
| 设置存储 | DataStore Preferences |

## 项目结构

```
android/app/src/main/java/com/pickcode/v2/
├── data/           # 数据层：Room DAO、Entity、Repository、DataStore
├── domain/         # 领域层：Model、MatchEngine 匹配引擎
├── di/             # Hilt 依赖注入模块
├── navigation/     # Navigation Compose 路由
├── ui/
│   ├── components/ # 公共组件（TagGrid、PlatformIcon）
│   ├── screen/     # 页面
│   │   ├── pickup/        # 取件列表 + 编辑取件码
│   │   ├── package_record/# 包裹记录 + 编辑包裹
│   │   └── my/            # 我的：匹配规则、AI 设置、FAQ、关于
│   ├── theme/      # Material 3 主题
│   └── util/       # 工具类（日期格式、震动、深链接）
└── PickCodeApp.kt  # Application 入口
```

## 构建

使用 Android Studio 打开 `android/` 目录，Gradle 同步后直接运行。

```bash
cd android
./gradlew assembleDebug   # 构建 debug APK
./gradlew assembleRelease # 构建 release APK
```

最低支持 Android 7.0 (API 24)，目标版本 Android 14 (API 34)。

## 许可证

[木兰宽松许可证，第2版 (MulanPSL-2.0)](LICENSE)
