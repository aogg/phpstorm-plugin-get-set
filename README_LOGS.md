# 日志查看指南

## 日志位置

插件的日志会记录到**项目目录**的 `.idea/plugin/Get-Set-Method-Highlighter/logs/` 目录中。

### 日志文件路径

日志文件位于项目根目录下的：
```
项目根目录/.idea/plugin/Get-Set-Method-Highlighter/logs/getset-highlighter-YYYY-MM-DD.log
```

例如：
- 如果项目在 `D:\project\my-app\`，日志文件会在：
  `D:\project\my-app\.idea\plugin\Get-Set-Method-Highlighter\logs\getset-highlighter-2025-12-11.log`

### 日志文件命名

- 日志文件按日期命名：`getset-highlighter-YYYY-MM-DD.log`
- 每天会创建一个新的日志文件
- 日志文件会自动创建，无需手动创建

### 查看日志

1. **直接在 IDE 中查看**：
   - 在项目视图中，展开 `.idea/plugin/Get-Set-Method-Highlighter/logs/` 目录
   - 双击日志文件即可在 IDE 中打开查看

2. **使用文件管理器查看**：
   - 打开项目根目录
   - 进入 `.idea/plugin/Get-Set-Method-Highlighter/logs/` 目录
   - 使用文本编辑器打开日志文件

3. **实时查看日志**：
   - 使用支持文件监控的编辑器（如 VS Code、Notepad++）可以实时看到日志更新
   - 在 IDE 中打开日志文件后，文件会自动刷新显示新内容

### 降级机制

如果无法创建项目日志目录（例如项目路径不存在），插件会自动降级到 PhpStorm 的系统日志：
- **Windows**: `%USERPROFILE%\.PhpStorm<version>\system\log\idea.log`
- **macOS**: `~/Library/Logs/PhpStorm<version>/idea.log`
- **Linux**: `~/.PhpStorm<version>/system/log/idea.log`

## 日志级别

插件使用以下日志级别：

- **INFO**: 关键操作（文件打开、修改、保存、高亮刷新、方法检测等）
- **WARN**: 警告信息（无法获取文档、PSI 文件等）
- **DEBUG**: 详细调试信息（需要启用 DEBUG 级别才能看到）

## 启用 DEBUG 级别日志

如果需要查看更详细的 DEBUG 日志：

1. 在 PhpStorm 中，打开 `Help` -> `Edit Custom Properties...`
2. 添加以下配置：
   ```
   # 启用 DEBUG 日志
   idea.log.debug=true
   ```
3. 或者针对特定类启用：
   ```
   # 启用特定类的 DEBUG 日志
   idea.log.debug.categories=com.github.phpstorm.getset
   ```
4. 重启 PhpStorm

## 日志内容示例

插件会记录以下信息：

```
INFO - 初始化 GetSetHighlightComponent
INFO - DocumentListener 已注册
INFO - 文件打开: D:\project\src\Test.php
INFO - 方法检测: getName -> GETTER
INFO - 高亮创建: GETTER 方法 getName 在 Test.php
INFO - 文档修改: D:\project\src\Test.php
INFO - 刷新高亮: D:\project\src\Test.php
INFO - 文档保存: D:\project\src\Test.php
```

## 日志格式

日志文件中的每一行格式如下：
```
[YYYY-MM-DD HH:mm:ss.SSS] [级别] [类名] - 消息内容
```

示例：
```
[2025-12-11 10:30:45.123] [INFO] [GetSetHighlightComponent] - 初始化 GetSetHighlightComponent
[2025-12-11 10:30:45.456] [INFO] [GetSetHighlightComponent] - DocumentListener 已注册
[2025-12-11 10:31:20.789] [INFO] [GetSetHighlightRefreshListener] - 文件打开: D:\project\src\Test.php
[2025-12-11 10:31:21.012] [INFO] [GetSetMethodDetector] - 方法检测: getName -> GETTER
[2025-12-11 10:31:21.345] [INFO] [GetSetAnnotator] - 高亮创建: GETTER 方法 getName 在 Test.php
```

## 过滤日志

在日志文件中搜索以下关键词可以快速找到插件相关的日志：
- `GetSetHighlightComponent`
- `GetSetHighlightRefreshListener`
- `GetSetAnnotator`
- `GetSetMethodDetector`
- `高亮创建`
- `文档修改`
- `刷新高亮`
- `文件打开`
- `文档保存`

## 注意事项

1. **日志目录会自动创建**：首次使用插件时，会自动创建 `.idea/plugin/Get-Set-Method-Highlighter/logs/` 目录
2. **日志文件会持续追加**：同一天的日志会追加到同一个文件中
3. **建议将日志目录加入 .gitignore**：日志文件通常不需要提交到版本控制
4. **日志文件大小**：如果日志文件过大，可以手动删除旧文件

