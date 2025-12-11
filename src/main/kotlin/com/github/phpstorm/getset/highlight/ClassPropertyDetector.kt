package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.util.ProjectLogger
import com.jetbrains.php.lang.psi.elements.Field
import com.jetbrains.php.lang.psi.elements.PhpClass
import java.util.regex.Pattern

/**
 * 类属性检测器
 * 检测类中是否存在指定的属性（包括字段和 @property 注释）
 */
object ClassPropertyDetector {
    
    /**
     * 检查类中是否存在指定属性
     * 支持驼峰命名和蛇形命名
     * 
     * @param phpClass PHP 类
     * @param propertyName 属性名（支持驼峰和蛇形）
     * @return 如果类中存在该属性则返回 true
     */
    fun hasProperty(phpClass: PhpClass?, propertyName: String): Boolean {
        if (phpClass == null || propertyName.isBlank()) {
            ProjectLogger.debug(
                ClassPropertyDetector::class.java,
                "hasProperty: phpClass 为空或 propertyName 为空: $propertyName"
            )
            return false
        }
        
        // 生成可能的属性名变体（驼峰和蛇形）
        val propertyVariants = generatePropertyVariants(propertyName)
        ProjectLogger.debug(
            ClassPropertyDetector::class.java,
            "hasProperty: 查找属性 $propertyName, 变体: $propertyVariants, 在类 ${phpClass.name}"
        )
        
        // 检查类的字段
        val fields = phpClass.fields
        ProjectLogger.debug(
            ClassPropertyDetector::class.java,
            "hasProperty: 类 ${phpClass.name} 共有 ${fields.size} 个字段"
        )
        
        for (field in fields) {
            val fieldName = field.name ?: continue
            // 移除 $ 符号
            val cleanFieldName = fieldName.removePrefix("$")
            
            ProjectLogger.debug(
                ClassPropertyDetector::class.java,
                "hasProperty: 检查字段: $cleanFieldName"
            )
            
            if (propertyVariants.any { variant -> 
                variant.equals(cleanFieldName, ignoreCase = true) 
            }) {
                ProjectLogger.info(
                    ClassPropertyDetector::class.java,
                    "找到字段属性: $cleanFieldName (匹配: $propertyName) 在类 ${phpClass.name}"
                )
                return true
            }
        }
        
        // 检查 PHPDoc 中的 @property 注释
        val docComment = phpClass.docComment
        if (docComment != null) {
            val docText = docComment.text
            val propertyNames = extractPropertyNamesFromDoc(docText)
            
            for (docPropertyName in propertyNames) {
                if (propertyVariants.any { variant -> 
                    variant.equals(docPropertyName, ignoreCase = true) 
                }) {
                    ProjectLogger.info(
                        ClassPropertyDetector::class.java,
                        "找到 @property 属性: $docPropertyName (匹配: $propertyName) 在类 ${phpClass.name}"
                    )
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * 生成属性名的可能变体（驼峰和蛇形）
     * 例如: "cacheKey" -> ["cacheKey", "cache_key"]
     */
    private fun generatePropertyVariants(propertyName: String): Set<String> {
        val variants = mutableSetOf<String>()
        
        // 原始名称
        variants.add(propertyName)
        
        // 转换为蛇形命名
        val snakeCase = camelToSnake(propertyName)
        if (snakeCase != propertyName) {
            variants.add(snakeCase)
        }
        
        // 转换为驼峰命名
        val camelCase = snakeToCamel(propertyName)
        if (camelCase != propertyName) {
            variants.add(camelCase)
        }
        
        return variants
    }
    
    /**
     * 驼峰转蛇形: cacheKey -> cache_key
     */
    private fun camelToSnake(str: String): String {
        return str.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
    }
    
    /**
     * 蛇形转驼峰: cache_key -> cacheKey
     */
    private fun snakeToCamel(str: String): String {
        val parts = str.split("_")
        if (parts.size <= 1) return str
        
        return parts[0] + parts.drop(1).joinToString("") { 
            it.replaceFirstChar { char -> char.uppercaseChar() }
        }
    }
    
    /**
     * 从 PHPDoc 注释中提取 @property 属性名
     * 支持格式: @property Type $propertyName
     */
    private fun extractPropertyNamesFromDoc(docText: String): List<String> {
        val propertyNames = mutableListOf<String>()
        
        // 匹配 @property Type $propertyName 格式
        val pattern = Pattern.compile(
            "@property\\s+[^\\$]+\\$([a-zA-Z_][a-zA-Z0-9_]*)",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = pattern.matcher(docText)
        
        while (matcher.find()) {
            val propertyName = matcher.group(1)
            if (propertyName != null) {
                propertyNames.add(propertyName)
            }
        }
        
        return propertyNames
    }
}

