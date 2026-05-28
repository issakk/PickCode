# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PickCode (取件码) — 跨平台取件码记录工具，自动从短信中提取快递取件码。uni-app + Vue 3 构建，支持 Android/iOS/H5。

## Build & Development

本项目无 package.json，使用 **HBuilderX** 作为构建工具：

- **开发调试**: HBuilderX 打开项目，选择目标平台（app-plus / mp-weixin / H5），点运行
- **打包发布**: HBuilderX → 发行 → 云打包/本地打包 → APK/IPA
- **无测试框架**: 项目未配置测试

## Architecture

### 三 Tab 结构（pages.json 定义路由）

| Tab | 路径 | 功能 |
|-----|------|------|
| 取件列表 | `pages/index/index.vue` | 核心页面：短信读取、取件码匹配、列表展示 |
| 包裹记录 | `pages/packages/index.vue` | 包裹跟踪记录 |
| 我的 | `pages/my/index.vue` | 设置、匹配规则管理、FAQ |

### 核心数据流

- **无状态管理库**（无 Vuex/Pinia），所有状态通过 `uni.setStorageSync`/`uni.getStorageSync` 本地持久化
- 主要存储 key：`packageCodes`（取件码）、`packageData`（包裹）、`matchRulesList`（匹配规则）、`globalTagOptions`（标签）
- 页面间通信：uni-app `eventChannel` API（如 `editCodeInit`/`editCodeDone` 事件）

### SMS 匹配引擎（核心逻辑在 pages/index/index.vue）

通过 Android ContentResolver (`plus.android` 桥接) 直接查询 `content://sms/`。匹配规则由 `start`/`end` 文本标记组成，`extractInfoByRules()` 提取三个字段：`code`（取件码）、`express`（快递公司）、`address`（取件地址）。

### 平台条件编译

大量使用 `// #ifdef APP-PLUS` 和 `// #ifdef H5` 实现平台差异逻辑。Android 原生短信读取，H5 为降级方案。

### 权限管理

`js_sdk/wa-permission/permission.js` 封装 Android/iOS 权限 API（SMS、相机、定位等）。

## Key Conventions

- **样式**: SCSS，全局变量在 `uni.scss`，组件内 `<style lang="scss" scoped>`
- **UI**: 蓝色渐变头部（`#0052d9`）、白色卡片布局、圆角、触感反馈（`uni.vibrateShort()`）
- **版本**: `manifest.json` 中 `versionName`/`versionCode`，更新时同步改两处
- **appId**: `__UNI__6D58568`（manifest.json）

## File Map

```
components/EditDialog.vue    — 取件码编辑弹窗组件
pages/index/index.vue        — 主页：短信读取 + 匹配 + 列表（最大最复杂的文件）
pages/index/edit-code.vue    — 编辑取件码（标签、备注、已取状态）
pages/my/match-rules.vue     — 匹配规则 CRUD + 预设规则
pages/my/match-settings.vue  — 单条规则编辑器（start/end 文本模式）
js_sdk/wa-permission/        — 原生权限管理 SDK
utils/index.js               — parseTime() 时间格式化工具
```
