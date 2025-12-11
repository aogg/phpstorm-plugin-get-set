package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.github.phpstorm.getset.util.ProjectLogger
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.Method
import java.awt.Color
import java.awt.Font

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
                // 高亮 getter 方法名 - 使用浅绿色加粗
                val nameRange: TextRange = element.nameIdentifier?.textRange ?: element.textRange
                val textAttributes = TextAttributes(
                    Color(0x90EE90), // 浅绿色前景色 (#90EE90)
                    null, // 无背景色
                    null, // 无效果颜色
                    null, // 无边框
                    Font.BOLD // 粗体
                )
                // 使用 enforcedTextAttributes 来强制应用颜色
                holder.newAnnotation(HighlightSeverity.INFORMATION, "Getter method")
                    .range(nameRange)
                    .enforcedTextAttributes(textAttributes)
                    .create()
                
                ProjectLogger.info(GetSetAnnotator::class.java, "高亮创建: GETTER 方法 ${element.name} 在 ${element.containingFile?.name}")
            }
            
            GetSetMethodDetector.MethodType.SETTER -> {
                // 高亮 setter 方法名 - 使用浅绿色加粗
                val nameRange: TextRange = element.nameIdentifier?.textRange ?: element.textRange
                val textAttributes = TextAttributes(
                    Color(0x90EE90), // 浅绿色前景色 (#90EE90)
                    null, // 无背景色
                    null, // 无效果颜色
                    null, // 无边框
                    Font.BOLD // 粗体
                )
                // 使用 enforcedTextAttributes 来强制应用颜色
                holder.newAnnotation(HighlightSeverity.INFORMATION, "Setter method")
                    .range(nameRange)
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

