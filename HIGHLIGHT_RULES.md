# 高亮规则说明

## 默认规则

### Getter 方法
- `get*` - 匹配所有以 `get` 开头的方法
- `is*` - 匹配所有以 `is` 开头的方法
- `has*` - 匹配所有以 `has` 开头的方法

### Setter 方法
- `set*` - 匹配所有以 `set` 开头的方法

## 示例

### 基础示例
```php
class User {
    private $name;
    private $email;
    private $active;
    
    // ✅ 高亮：匹配 get*
    public function getName() { return $this->name; }
    
    // ✅ 高亮：匹配 set*
    public function setName($name) { $this->name = $name; }
    
    // ✅ 高亮：匹配 is*
    public function isActive() { return $this->active; }
    
    // ✅ 高亮：匹配 has*
    public function hasPermission() { return $this->hasPermission; }
}
```

### 通配符示例
```php
class Cache {
    private $cacheKey;
    private $cacheAttr;
    private $key;
    
    // ✅ 高亮：getCacheKey() 匹配 get*，提取属性 cacheKey
    public function getCacheKey() { return $this->cacheKey; }
    
    // ✅ 高亮：getCacheAttr() 匹配 get*，提取属性 cacheAttr
    public function getCacheAttr() { return $this->cacheAttr; }
    
    // ✅ 高亮：getCacheLinuxPath() 匹配 get*，提取属性 cacheLinuxPath
    public function getCacheLinuxPath() { return $this->cacheLinuxPath; }
}
```

### 自定义模式示例
如果配置了 `getCache*` 模式：
```php
class Cache {
    private $key;
    private $linuxPath;
    
    // ✅ 高亮：getCacheKey() 匹配 getCache*，提取属性 key
    public function getCacheKey() { return $this->key; }
    
    // ✅ 高亮：getCacheLinuxPath() 匹配 getCache*，提取属性 linuxPath
    public function getCacheLinuxPath() { return $this->linuxPath; }
}
```

如果配置了 `get*Attr` 模式：
```php
class User {
    private $cache;
    
    // ✅ 高亮：getCacheAttr() 匹配 get*Attr，提取属性 cache
    public function getCacheAttr() { return $this->cache; }
}
```

## 注意事项

- 只有方法名匹配规则**且类中存在对应属性**时才会高亮
- 匹配不区分大小写
- 支持 `*` 通配符匹配任意字符

