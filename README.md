# PhpStorm Get/Set 方法高亮插件

自动高亮 PHP 类中的 getter 和 setter 方法，支持自定义多个前缀和后缀。

## 功能特性

- ✅ 自动识别并高亮 getter/setter 方法
- ✅ 支持自定义多个前缀和后缀
- ✅ 可配置的高亮样式
- ✅ 实时更新，无需重启 IDE
- ✅ 支持启用/禁用功能

## 安装

1. 克隆或下载此项目
2. 使用 Gradle 构建插件：
   ```bash
   ./gradlew buildPlugin
   ```
3. 在 PhpStorm 中安装插件：
   - File → Settings → Plugins → Install Plugin from Disk
   - 选择 `build/distributions/phpstorm-plugin-get-set-1.0.0.zip`

## 配置

1. 打开 PhpStorm 设置：File → Settings → Editor → Get/Set Highlighter
2. 配置选项：
   - **启用 Get/Set 方法高亮**：开启或关闭高亮功能
   - **Getter 方法前缀**：添加或删除 getter 方法的前缀（默认：get, is, has）
   - **Setter 方法前缀**：添加或删除 setter 方法的前缀（默认：set）
   - **Getter 方法后缀**：添加或删除 getter 方法的后缀（默认：无）
   - **Setter 方法后缀**：添加或删除 setter 方法的后缀（默认：无）

## 使用方法

配置完成后，插件会自动识别并高亮符合规则的方法。例如：

```php
class User {
    private $name;
    private $email;
    
    // 这些方法会被高亮
    public function getName() { return $this->name; }
    public function setName($name) { $this->name = $name; }
    public function isActive() { return $this->active; }
    public function hasPermission() { return $this->permission; }
}
```

## 开发

### 项目结构

```
src/main/kotlin/
  ├── config/
  │   ├── GetSetConfig.kt          # 配置数据类
  │   ├── GetSetConfigService.kt   # 配置服务
  │   └── GetSetConfigurable.kt    # 配置页面
  ├── highlight/
  │   ├── GetSetAnnotator.kt       # 注解器
  │   ├── GetSetHighlighter.kt    # 高亮样式
  │   └── GetSetMethodDetector.kt  # 方法检测器
```

### 构建

```bash
# 构建插件
./gradlew buildPlugin

# 运行测试
./gradlew test

# 运行插件（需要配置 IDE）
./gradlew runIde
```

## 许可证

MIT License

