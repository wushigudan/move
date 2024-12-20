# VideoVoyage 视频漫游 🎬

> Your Personal Video Entertainment Companion
> 
> 你的私人视频娱乐伴侣

[English](#english) | [中文](#chinese)

<a name="english"></a>
## 🌟 Project Overview
VideoVoyage (formerly MyMove) is a modern, feature-rich Android video streaming application that brings entertainment to your fingertips. Built with cutting-edge technology and designed with user experience in mind, it offers a seamless way to discover, browse, and enjoy video content from multiple sources.

## ✨ Key Features
- 🎨 **Elegant Material Design 3 UI** with dynamic theming
- 🌓 **Adaptive Dark/Light Mode** for comfortable viewing
- 🔄 **Multi-Source Support** with customizable API endpoints
- 🔍 **Smart Search** with real-time suggestions
- 📱 **Responsive Layout** for all screen sizes
- 🚀 **Optimized Performance** with smart caching
- ⚡ **Fast Video Loading** and smooth playback
- 🔐 **Secure API Integration** with customizable endpoints
- 📊 **Category Management** for organized content
- ⚙️ **Rich Settings** for personalization

---

<a name="chinese"></a>
## 🌟 项目概述
VideoVoyage（原名 MyMove）是一款现代化的 Android 视频流媒体应用。采用前沿技术开发，以用户体验为核心，为用户提供从多个来源发现、浏览和欣赏视频内容的无缝体验。

## ✨ 核心特性
- 🎨 **优雅的 Material Design 3 界面**，支持动态主题
- 🌓 **自适应明暗模式**，带来舒适的观看体验
- 🔄 **多源支持**，可自定义 API 接入点
- 🔍 **智能搜索**，实时推荐
- 📱 **响应式布局**，完美适配各种屏幕
- 🚀 **性能优化**，智能缓存
- ⚡ **快速加载**，流畅播放
- 🔐 **安全的 API 集成**，可自定义接入点
- 📊 **分类管理**，内容井然有序
- ⚙️ **丰富的设置**，个性化定制

## 🛠️ 技术栈
- 📱 **平台**: Android (API Level 24+)
- 💻 **开发语言**: Kotlin
- 🎨 **UI 框架**: Jetpack Compose
- 🌐 **网络请求**: Retrofit2 + OkHttp3
- 💉 **依赖注入**: Hilt
- 🔄 **异步处理**: Kotlin Coroutines + Flow
- 🎥 **视频播放**: ExoPlayer
- 💾 **本地存储**: DataStore
- 🖼️ **图片加载**: Coil
- 🎯 **架构**: 基于 MVVM 的清洁架构

## 📁 项目结构
```
app/
├── src/main/
│   ├── java/com/example/mymove/
│   │   ├── api/                 # API 集成层
│   │   │   ├── MacCmsApi.kt     # API 接口
│   │   │   ├── RetrofitClient.kt # 网络客户端
│   │   │   └── ApiModels.kt     # 数据模型
│   │   ├── data/                # 数据层
│   │   │   └── SettingsDataStore.kt # 设置管理
│   │   ├── ui/                  # UI 组件
│   │   │   ├── home/           # 主页
│   │   │   ├── video/          # 视频播放器
│   │   │   ├── search/         # 搜索界面
│   │   │   ├── settings/       # 设置界面
│   │   │   └── theme/          # 主题配置
│   │   └── MainActivity.kt      # 主入口
│   └── res/                     # 资源文件
└── build.gradle                 # 依赖配置
```

## 🚀 快速开始

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 11 或更高版本
- Android SDK API Level 24+
- Gradle 7.0+

### 安装步骤
1. 克隆仓库：
```bash
git clone https://github.com/wushigudan/move.git
```
2. 在 Android Studio 中打开
3. 同步 Gradle 并构建项目
4. 在设备或模拟器上运行

## 🎯 核心功能

### 视频源管理
- 支持多个 API 源
- 自定义接入点配置
- 自动源验证
- 便捷的源切换

### 智能内容发现
- 高级搜索算法
- 基于分类的浏览
- 热门内容板块
- 最近观看历史

### 增强版视频播放器
- 多格式支持
- 手势控制
- 画质选择
- 播放速度控制
- 画中画模式

### 用户体验
- 直观的导航
- 流畅的动画
- 快捷操作
- 可定制界面

## 📜 免责声明 | Disclaimer

### 中文版
1. **使用目的**：本应用程序仅供个人学习和技术研究使用，不得用于任何商业目的。
2. **内容来源**：本应用不存储、制作或分发任何视频内容，所有视频内容均来自用户自行配置的第三方来源，用户对其配置的内容来源负有完全责任。
3. **法律遵从**：用户必须遵守所在地区的相关法律法规，禁止使用本应用访问任何非法或未授权的内容，用户应确保其使用行为符合内容提供方的服务条款。
4. **责任限制**：开发者不对使用本应用产生的任何直接或间接损失负责，不保证应用的持续可用性和内容的准确性，开发者保留随时修改或终止服务的权利。
5. **隐私保护**：本应用重视用户隐私，不会收集或存储用户个人信息，所有设置和配置仅保存在用户本地设备。

### English Version
1. **Purpose of Use**: This application is intended solely for personal learning and technical research purposes, not for commercial use.
2. **Content Sources**: This app does not store, produce, or distribute any video content. All video content comes from third-party sources configured by users. Users are fully responsible for their configured content sources.
3. **Legal Compliance**: Users must comply with relevant laws and regulations in their jurisdiction. Use of this app to access any illegal or unauthorized content is prohibited. Users should ensure their usage complies with content providers' terms of service.
4. **Liability Limitations**: Developers are not responsible for any direct or indirect losses from using this app. No guarantee is provided for the app's continuous availability or content accuracy. Developers reserve the right to modify or terminate services at any time.
5. **Privacy Protection**: This app values user privacy and does not collect or store personal information. All settings and configurations are saved locally on the user's device.

## 🤝 参与贡献
我们欢迎各种形式的贡献！
- 报告问题
- 提出新功能建议
- 提交代码改进

## 📝 许可证
本项目仅供教育目的使用。请确保遵守内容提供商的服务条款。

## 🌟 Star 历史

[![Star History Chart](https://api.star-history.com/svg?repos=wushigudan/move&type=Date)](https://star-history.com/#wushigudan/move&Date)

## 📧 联系我们
如有问题或建议，请提交 issue 或联系维护者。

---
由白衬衫用 ❤️ 打造 
