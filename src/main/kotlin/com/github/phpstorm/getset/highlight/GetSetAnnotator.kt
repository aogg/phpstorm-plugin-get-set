package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.github.phpstorm.getset.util.ProjectLogger
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method

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
        
        // 检查配置是否启用（合并全局配置和项目级配置）
        val configService = GetSetConfigService.getInstance()
        val project = element.project
        val config = configService.getConfig(project)
        
        if (!config.enabled) {
            return
        }
        
        // 确保 PSI 树已完全构建
        val containingClass = element.containingClass
        if (containingClass == null) {
            // PSI 树可能还未完全构建，跳过此次检测
            return
        }
        
        // 检测方法类型和匹配的属性名
        val (methodType, propertyName) = GetSetMethodDetector.detectMethodTypeWithProperty(element)
        
        // 检测逻辑保留，但不再创建 Annotation
        // 文本标签显示由 GetSetInlayHintsProvider 处理（如果 API 可用）
        if (methodType != GetSetMethodDetector.MethodType.NONE && propertyName != null) {
            ProjectLogger.debug(
                GetSetAnnotator::class.java,
                "检测到方法 ${element.name} -> 属性 $propertyName，文本标签由 InlayHintsProvider 处理"
            )
        }
    }
}

