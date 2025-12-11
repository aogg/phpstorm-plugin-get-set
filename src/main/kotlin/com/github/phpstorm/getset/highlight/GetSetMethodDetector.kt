package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.github.phpstorm.getset.util.ProjectLogger
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass

/**
 * Get/Set 方法检测器
 * 根据配置识别方法是否为 getter 或 setter，并检查类中是否存在对应属性
 */
object GetSetMethodDetector {
    
    /**
     * 检测方法类型
     */
    enum class MethodType {
        GETTER,
        SETTER,
        NONE
    }
    
    /**
     * 检测方法类型
     * 只有当方法名匹配配置规则且类中存在对应属性时才返回 GETTER/SETTER
     */
    fun detectMethodType(method: Method): MethodType {
        val methodName = method.name ?: return MethodType.NONE
        
        val configService = GetSetConfigService.getInstance()
        val project = method.project
        val config = configService.getConfig(project)
        
        // 获取方法所在的类
        val containingClass = method.containingClass
        
        // 如果类不存在，说明 PSI 树可能还未完全构建，暂时跳过
        if (containingClass == null) {
            return MethodType.NONE
        }
        
        // 检查 getter 方法
        if (config.isGetterMethod(methodName)) {
            ProjectLogger.debug(
                GetSetMethodDetector::class.java,
                "方法匹配 getter 规则: $methodName, 模式列表: ${config.getterPatterns}"
            )
            
            // 遍历所有模式，找到第一个匹配且能找到对应属性的模式
            for (pattern in config.getterPatterns) {
                val propertyName = extractPropertyName(methodName, pattern)
                if (propertyName != null) {
                    ProjectLogger.debug(
                        GetSetMethodDetector::class.java,
                        "模式 $pattern 提取到属性名: $propertyName, 开始检查类中是否存在该属性"
                    )
                    
                    // 检查类中是否存在该属性
                    if (ClassPropertyDetector.hasProperty(containingClass, propertyName)) {
                        ProjectLogger.info(
                            GetSetMethodDetector::class.java,
                            "方法检测: $methodName -> GETTER (模式: $pattern, 属性: $propertyName)"
                        )
                        return MethodType.GETTER
                    } else {
                        ProjectLogger.debug(
                            GetSetMethodDetector::class.java,
                            "模式 $pattern 提取的属性 $propertyName 在类中不存在，继续尝试下一个模式"
                        )
                    }
                }
            }
            
            ProjectLogger.debug(
                GetSetMethodDetector::class.java,
                "方法检测: $methodName 匹配规则但所有模式都无法找到对应属性"
            )
        }
        
        // 检查 setter 方法
        if (config.isSetterMethod(methodName)) {
            ProjectLogger.debug(
                GetSetMethodDetector::class.java,
                "方法匹配 setter 规则: $methodName, 模式列表: ${config.setterPatterns}"
            )
            
            // 遍历所有模式，找到第一个匹配且能找到对应属性的模式
            for (pattern in config.setterPatterns) {
                val propertyName = extractPropertyName(methodName, pattern)
                if (propertyName != null) {
                    ProjectLogger.debug(
                        GetSetMethodDetector::class.java,
                        "模式 $pattern 提取到属性名: $propertyName, 开始检查类中是否存在该属性"
                    )
                    
                    // 检查类中是否存在该属性
                    if (ClassPropertyDetector.hasProperty(containingClass, propertyName)) {
                        ProjectLogger.info(
                            GetSetMethodDetector::class.java,
                            "方法检测: $methodName -> SETTER (模式: $pattern, 属性: $propertyName)"
                        )
                        return MethodType.SETTER
                    } else {
                        ProjectLogger.debug(
                            GetSetMethodDetector::class.java,
                            "模式 $pattern 提取的属性 $propertyName 在类中不存在，继续尝试下一个模式"
                        )
                    }
                }
            }
            
            ProjectLogger.debug(
                GetSetMethodDetector::class.java,
                "方法检测: $methodName 匹配规则但所有模式都无法找到对应属性"
            )
        }
        
        return MethodType.NONE
    }
    
    /**
     * 从 getter 方法名提取属性名
     * 支持多种模式: get*, getCache*, get*Attr
     */
    private fun extractPropertyNameFromGetter(
        methodName: String,
        patterns: List<String>
    ): String? {
        for (pattern in patterns) {
            val propertyName = extractPropertyName(methodName, pattern)
            if (propertyName != null) {
                return propertyName
            }
        }
        return null
    }
    
    /**
     * 从 setter 方法名提取属性名
     * 支持多种模式: set*
     */
    private fun extractPropertyNameFromSetter(
        methodName: String,
        patterns: List<String>
    ): String? {
        for (pattern in patterns) {
            val propertyName = extractPropertyName(methodName, pattern)
            if (propertyName != null) {
                return propertyName
            }
        }
        return null
    }
    
    /**
     * 从方法名中根据模式提取属性名
     * 
     * 示例:
     * - getCacheKey() + get* -> cacheKey
     * - getCacheKey() + getCache* -> key
     * - getCacheAttr() + get*Attr -> cache
     * - getCacheLinuxPath() + getCache* -> linuxPath
     */
    private fun extractPropertyName(methodName: String, pattern: String): String? {
        // 将模式转换为正则表达式
        val regexPattern = pattern
            .replace(".", "\\.")
            .replace("*", "(.*)")
            .toRegex(RegexOption.IGNORE_CASE)
        
        val matchResult = regexPattern.matchEntire(methodName) ?: return null
        
        // 提取匹配的部分（* 对应的内容）
        val groups = matchResult.groupValues
        if (groups.size < 2) return null
        
        // 获取第一个捕获组（* 匹配的内容）
        val extracted = groups[1]
        
        if (extracted.isBlank()) return null
        
        // 转换为属性名格式（首字母小写）
        val propertyName = extracted.replaceFirstChar { it.lowercaseChar() }
        
        ProjectLogger.debug(
            GetSetMethodDetector::class.java,
            "属性名提取: 方法名=$methodName, 模式=$pattern, 提取=$extracted, 属性名=$propertyName"
        )
        
        return propertyName
    }
    
    /**
     * 检查是否为 getter 方法
     */
    fun isGetterMethod(method: Method): Boolean {
        return detectMethodType(method) == MethodType.GETTER
    }
    
    /**
     * 检查是否为 setter 方法
     */
    fun isSetterMethod(method: Method): Boolean {
        return detectMethodType(method) == MethodType.SETTER
    }
}

