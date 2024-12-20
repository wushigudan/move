# MyMove Android App

## 项目概述
MyMove 是一个基于 Android 的视频播放应用，使用 Kotlin 和 Jetpack Compose 开发。该应用支持多个视频源 API，可以自定义 API 地址，实现视频资源的获取和播放。应用采用了最新的 Android 开发技术栈，提供流畅的用户体验和丰富的功能。

## 技术栈
- **开发语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **网络请求**: Retrofit2 + OkHttp3
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow
- **视频播放**: ExoPlayer
- **本地存储**: DataStore
- **图片加载**: Coil
- **主题样式**: Material Design 3

## 项目特点
- 现代化 UI 设计，支持浅色/深色主题
- 支持自定义和切换多个视频源
- 响应式布局，完美适配各种屏幕尺寸
- 优秀的性能表现，流畅的用户体验
- 强大的搜索功能，快速找到想要的内容
- 智能的数据缓存机制，减少网络请求
- 丰富的设置选项，高度可定制

## 项目结构

```
app/
├── src/main/
│   ├── java/com/example/mymove/
│   │   ├── api/                 # API 相关代码
│   │   │   ├── MacCmsApi.kt     # API 接口定义
│   │   │   ├── RetrofitClient.kt # 网络请求客户端
│   │   │   └── ApiModels.kt     # 数据模型
│   │   ├── data/                # 数据层
│   │   │   └── SettingsDataStore.kt # 设置数据存储
│   │   ├── ui/                  # UI 相关代码
│   │   │   ├── home/           # 首页
│   │   │   ├── video/          # 视频播放
│   │   │   ├── search/         # 搜索
│   │   │   ├── settings/       # 设置界面
│   │   │   └── theme/          # 主题相关
│   │   └── MainActivity.kt      # 主活动
│   └── res/                     # 资源文件
└── build.gradle                 # 项目依赖配置
```

## 主要功能

### 1. 视频源管理
- 支持添加、删除和切换多个视频源
- 提供推荐线路，方便快速配置
- 自动验证API可用性
- 保存历史记录，方便切换

### 2. 视频浏览
- 分类浏览，支持多级分类
- 智能排序，支持多种排序方式
- 分页加载，流畅浏览大量内容
- 支持收藏功能

### 3. 视频播放
- 支持多种视频格式
- 手势控制播放进度和音量
- 画面比例调节
- 播放历史记录

### 4. 搜索功能
- 实时搜索提示
- 搜索历史记录
- 支持按类型、年份等筛选
- 结果智能排序

### 5. 个性化设置
- 主题切换（浅色/深色）
- 缓存管理
- 播放器设置
- 自定义首页布局

## 开发指南

### 1. 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- Kotlin 1.9.0 或更高版本
- Java Development Kit (JDK) 17
- Android SDK 34 (最低支持 SDK 26)
- Gradle 8.2

### 2. 构建步骤
```bash
# 1. 克隆项目
git clone https://github.com/yourusername/mymove.git

# 2. 进入项目目录
cd mymove

# 3. 构建项目
./gradlew build
```

### 3. 开发规范
- 遵循 MVVM 架构模式
- 使用 Kotlin 协程处理异步操作
- 使用 Compose 最佳实践构建 UI
- 保持代码整洁，添加必要的注释

## 贡献指南
1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 版本历史
### v1.0.0 (2024-12-20)
- 初始版本发布
- 基础功能实现
- UI 框架搭建完成

## 开源协议
本项目采用 MIT 协议开源，详见 [LICENSE](LICENSE) 文件。

## 免责声明
本应用仅供学习交流使用，严禁用于任何商业用途。用户使用本应用时，应遵守相关法律法规，不得用于任何非法用途。开发者对使用本应用产生的任何问题概不负责。

## 联系方式
如有问题或建议，欢迎提交 Issue 或 Pull Request。

## 致谢
感谢所有为这个项目做出贡献的开发者。
