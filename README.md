# PickCode - 取件码

快递取件码自动提取工具。从短信中识别取件码、快递公司、取件地址，告别手动翻短信。

## 功能

- **短信自动匹配** — 读取短信，按规则提取取件码、快递公司、取件地址；匹配成功后自动回到列表顶部
- **自定义匹配规则** — 支持 start/end 文本匹配、正则表达式、短信关键词初筛
- **多取件码提取** — 一条短信里的多个取件码都会提取（文本匹配与正则两种模式都支持），按取件码 + 日期去重保存
- **AI 辅助** — 可配置 AI 接口辅助生成匹配规则
- **任意文本导入** — 其它 App「分享」一段文本到取件码（微信、浏览器都行），或手动添加，自动抽取入库
- **规则回测** — 拿最近 30 天真实短信回测当前规则，命中几条、抽出来长什么样一目了然
- **本地存储** — 所有数据存本地 Room 数据库、不上传服务器；已关闭系统云备份（`allowBackup=false`），短信只读收件箱

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
│   ├── components/ # 公共组件（CodeCard、TagGrid、AppTopBar）
│   ├── screen/     # 页面
│   │   ├── pickup/        # 取件列表 + 编辑取件码
│   │   └── my/            # 我的：匹配规则、AI 设置、FAQ、关于
│   ├── theme/      # Material 3 主题
│   └── util/       # 工具类（日期格式、取件码格式）
└── PickCodeApp.kt  # Application 入口
```

## 构建

使用 Android Studio 打开 `android/` 目录，Gradle 同步后直接运行。

```bash
cd android
./gradlew assembleDebug   # 构建 debug APK
./gradlew testDebugUnitTest # 跑单元测试（MatchEngine）
./gradlew assembleRelease  # 构建 release APK
```

最低支持 Android 7.0 (API 24)，目标版本 Android 14 (API 34)。

## 发布签名

release APK 默认用仓库里的 `android/app/debug.keystore` 签名。换正式 key 时配置 `RELEASE_KEYSTORE_PATH`（keystore 路径）、`RELEASE_KEYSTORE_PASSWORD`、`RELEASE_KEY_ALIAS`、`RELEASE_KEY_PASSWORD` 四个环境变量；GitHub Actions 用 `RELEASE_KEYSTORE_BASE64`（keystore 的 base64）等 secrets 注入。注意：换 key 后旧版本无法覆盖安装，用户需先卸载。

## 许可证

[木兰宽松许可证，第2版 (MulanPSL-2.0)](LICENSE)
