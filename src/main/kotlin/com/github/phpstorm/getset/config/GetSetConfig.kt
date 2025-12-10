package com.github.phpstorm.getset.config

/**
 * Get/Set 方法高亮配置数据类
 */
data class GetSetConfig(
    /**
     * Getter 方法前缀列表（如：get, is, has）
     */
    val getterPrefixes: MutableList<String> = mutableListOf("get", "is", "has"),
    
    /**
     * Setter 方法前缀列表（如：set）
     */
    val setterPrefixes: MutableList<String> = mutableListOf("set"),
    
    /**
     * Getter 方法后缀列表
     */
    val getterSuffixes: MutableList<String> = mutableListOf(),
    
    /**
     * Setter 方法后缀列表
     */
    val setterSuffixes: MutableList<String> = mutableListOf(),
    
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
        
        // 检查前缀
        val matchesPrefix = getterPrefixes.any { prefix ->
            methodName.startsWith(prefix, ignoreCase = true) &&
            methodName.length > prefix.length &&
            methodName[prefix.length].isUpperCase()
        }
        
        // 检查后缀
        val matchesSuffix = getterSuffixes.isEmpty() || getterSuffixes.any { suffix ->
            methodName.endsWith(suffix, ignoreCase = true) &&
            methodName.length > suffix.length
        }
        
        return matchesPrefix && matchesSuffix
    }
    
    /**
     * 检查方法名是否匹配 setter 规则
     */
    fun isSetterMethod(methodName: String): Boolean {
        if (!enabled) return false
        
        // 检查前缀
        val matchesPrefix = setterPrefixes.any { prefix ->
            methodName.startsWith(prefix, ignoreCase = true) &&
            methodName.length > prefix.length &&
            methodName[prefix.length].isUpperCase()
        }
        
        // 检查后缀
        val matchesSuffix = setterSuffixes.isEmpty() || setterSuffixes.any { suffix ->
            methodName.endsWith(suffix, ignoreCase = true) &&
            methodName.length > suffix.length
        }
        
        return matchesPrefix && matchesSuffix
    }
}

