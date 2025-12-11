package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.github.phpstorm.getset.util.ProjectLogger
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
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
        
        // 确保 PSI 树已完全构建
        val containingClass = element.containingClass
        if (containingClass == null) {
            // PSI 树可能还未完全构建，跳过此次检测
            return
        }
        
        // 检测方法类型
        val methodType = GetSetMethodDetector.detectMethodType(element)
        
        when (methodType) {
            GetSetMethodDetector.MethodType.GETTER -> {
                // 高亮 getter 方法名 - 使用浅绿色加粗
                val nameRange: TextRange = element.nameIdentifier?.textRange ?: element.textRange
                val textAttributes = GetSetHighlighter.getGetterTextAttributes()
                
                // 使用 WEAK_WARNING 优先级，确保高亮可见且不会被覆盖
                // 同时使用 enforcedTextAttributes 强制应用颜色
                holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Getter method")
                    .range(nameRange)
                    .textAttributes(GetSetHighlighter.GETTER_METHOD_KEY)
                    .enforcedTextAttributes(textAttributes)
                    .create()
                
                ProjectLogger.info(GetSetAnnotator::class.java, "高亮创建: GETTER 方法 ${element.name} 在 ${element.containingFile?.name}")
            }
            
            GetSetMethodDetector.MethodType.SETTER -> {
                // 高亮 setter 方法名 - 使用浅绿色加粗
                val nameRange: TextRange = element.nameIdentifier?.textRange ?: element.textRange
                val textAttributes = GetSetHighlighter.getSetterTextAttributes()
                
                // 使用 WEAK_WARNING 优先级，确保高亮可见且不会被覆盖
                // 同时使用 enforcedTextAttributes 强制应用颜色
                holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Setter method")
                    .range(nameRange)
                    .textAttributes(GetSetHighlighter.SETTER_METHOD_KEY)
                    .enforcedTextAttributes(textAttributes)
                    .create()
                
                ProjectLogger.info(GetSetAnnotator::class.java, "高亮创建: SETTER 方法 ${element.name} 在 ${element.containingFile?.name}")
            }
            
            GetSetMethodDetector.MethodType.NONE -> {
                // 不做处理
            }
        }
    }
}

