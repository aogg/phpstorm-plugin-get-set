package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.jetbrains.php.lang.psi.elements.Method

/**
 * Get/Set 方法检测器
 * 根据配置识别方法是否为 getter 或 setter
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
     */
    fun detectMethodType(method: Method): MethodType {
        val methodName = method.name ?: return MethodType.NONE
        
        val configService = GetSetConfigService.getInstance()
        val config = configService.getConfig()
        
        if (config.isGetterMethod(methodName)) {
            return MethodType.GETTER
        }
        
        if (config.isSetterMethod(methodName)) {
            return MethodType.SETTER
        }
        
        return MethodType.NONE
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

