# ClipboardIME — 剪贴板搜索输入法

一款 Android 自定义输入法，可以**对剪贴板历史内容进行关键词搜索**。输入关键字即可弹出所有含该关键字的剪贴板记录，点击即可粘贴到当前输入框。

## 功能

- **剪贴板自动监听**：复制文本时自动保存到本地数据库
- **关键词搜索**：在输入法中输入关键字，实时筛选匹配的剪贴板历史
- **一键粘贴**：点击搜索结果即可将内容粘贴到当前输入框
- **本地持久化**：基于 Room 数据库，重启后记录不丢失
- **去重**：相同内容不会重复保存

## 使用方式

1. 安装应用后，进入「系统设置 > 语言和输入法 > 虚拟键盘」启用 ClipboardIME
2. 打开任意输入框，切换键盘为 ClipboardIME
3. 点击键盘右下角的 **🔍 搜索按钮** 进入搜索模式
4. 输入关键字即可看到匹配的剪贴板历史记录
5. 点击任意结果即可粘贴到输入框

## 技术栈

- **语言**: Kotlin
- **架构**: MVVM
- **数据库**: Room
- **最低支持**: Android 8.0 (API 26)
- **目标平台**: Android 14 (API 34)

## 构建

1. 使用 Android Studio 打开项目根目录
2. 等待 Gradle 同步完成
3. `Build > Make Project` 或直接运行

## 项目结构

```
app/src/main/java/com/clipboardime/
├── ClipboardIME.kt              # 核心 IME 服务
├── data/
│   ├── ClipboardEntity.kt       # Room 实体
│   ├── ClipboardDao.kt          # 数据访问对象
│   ├── ClipboardDatabase.kt     # Room 数据库
│   └── ClipboardRepository.kt   # 数据仓库
├── ui/
│   ├── KeyboardViewManager.kt   # 键盘 UI 管理器
│   └── SettingsActivity.kt      # 设置/引导页
├── viewmodel/
│   └── SearchViewModel.kt       # 搜索 ViewModel
└── util/
    └── ClipboardMonitor.kt      # 剪贴板变化监听
```

## 权限说明

- `READ_CLIPBOARD`：读取剪贴板内容（用于保存历史记录和搜索）
- `BIND_INPUT_METHOD`：注册为系统输入法

## License

MIT
