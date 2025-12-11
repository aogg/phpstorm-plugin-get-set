package com.github.phpstorm.getset.config

/**
 * Get/Set 方法高亮配置数据类
 */
data class GetSetConfig(
    /**
     * Getter 方法通配模式列表（支持 *，例如：get*、get*Attr、getCache*）
     */
    val getterPatterns: MutableList<String> = mutableListOf("get*", "is*", "has*"),
    
    /**
     * Setter 方法通配模式列表（支持 *，例如：set*）
     */
    val setterPatterns: MutableList<String> = mutableListOf("set*"),
    
    /**
     * 是否启用高亮
     */
    val enabled: Boolean = true
) {
    /**
     * 创建默认配置
     */
    companion object {
        fun default(): GetSetConfig {
            return GetSetConfig()
        }
    }
    
    /**
     * 检查方法名是否匹配 getter 规则
     */
    fun isGetterMethod(methodName: String): Boolean {
        if (!enabled) return false
        
        return matchesPattern(methodName, getterPatterns)
    }
    
    /**
     * 检查方法名是否匹配 setter 规则
     */
    fun isSetterMethod(methodName: String): Boolean {
        if (!enabled) return false
        
        return matchesPattern(methodName, setterPatterns)
    }
    
    /**
     * 通用通配符匹配：支持 * 匹配任意长度字符（大小写不敏感）
     */
    private fun matchesPattern(methodName: String, patterns: List<String>): Boolean {
        if (patterns.isEmpty()) return false
        
        return patterns.any { pattern ->
            val regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .toRegex(RegexOption.IGNORE_CASE)
            regex.matches(methodName)
        }
    }
}

