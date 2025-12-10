package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass

/**
 * Get/Set 注解器
 * 识别并高亮 getter/setter 方法
 */
class GetSetAnnotator : Annotator {
    
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // 只处理 PHP 方法
        if (element !is Method) {
            return
        }
        
        // 检查配置是否启用
        val configService = GetSetConfigService.getInstance()
        val config = configService.getConfig()
        
        if (!config.enabled) {
            return
        }
        
        // 检测方法类型
        val methodType = GetSetMethodDetector.detectMethodType(element)
        
        when (methodType) {
            GetSetMethodDetector.MethodType.GETTER -> {
                // 高亮 getter 方法名
                val methodName = element.nameIdentifier ?: return
                holder.newAnnotation(HighlightSeverity.INFORMATION, null)
                    .range(methodName)
                    .textAttributes(GetSetHighlighter.GETTER_METHOD_KEY)
                    .create()
            }
            
            GetSetMethodDetector.MethodType.SETTER -> {
                // 高亮 setter 方法名
                val methodName = element.nameIdentifier ?: return
                holder.newAnnotation(HighlightSeverity.INFORMATION, null)
                    .range(methodName)
                    .textAttributes(GetSetHighlighter.SETTER_METHOD_KEY)
                    .create()
            }
            
            GetSetMethodDetector.MethodType.NONE -> {
                // 不做处理
            }
        }
    }
}

