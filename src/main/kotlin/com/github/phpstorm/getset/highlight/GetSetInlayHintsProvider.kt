package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.github.phpstorm.getset.util.ProjectLogger
import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.Method

/**
 * Get/Set 行内提示提供器
 * 在方法定义行上方显示匹配到的属性名标签
 */
class GetSetInlayHintsProvider : InlayHintsProvider<NoSettings> {
    
    override val key: SettingsKey<NoSettings> = SettingsKey("getset.inlay.hints")
    
    override val name: String = "Get/Set Method Property Hints"
    
    override val previewText: String? = null
    
    override val isVisibleInSettings: Boolean = true
    
    override fun createSettings(): NoSettings = NoSettings()
    
    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): javax.swing.JComponent {
                return javax.swing.JLabel("Get/Set 方法属性名标签")
            }
        }
    }
    
    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector? {
        // 只处理 PHP 文件
        if (file !is PhpFile) {
            return null
        }
        
        // 检查配置是否启用
        val configService = GetSetConfigService.getInstance()
        val project = file.project
        val config = configService.getConfig(project)
        
        if (!config.enabled) {
            return null
        }
        
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                // 只处理 PHP 方法
                if (element !is Method) {
                    return true
                }
                
                // 确保 PSI 树已完全构建
                val containingClass = element.containingClass
                if (containingClass == null) {
                    return true
                }
                
                // 检测方法类型和匹配的属性名
                val (methodType, propertyName) = GetSetMethodDetector.detectMethodTypeWithProperty(element)
                
                if (methodType != GetSetMethodDetector.MethodType.NONE && propertyName != null) {
                    // 获取方法定义行的起始偏移量
                    val methodStartOffset = element.textRange.startOffset
                    val document = editor.document
                    val lineNumber = document.getLineNumber(methodStartOffset)
                    val lineStartOffset = document.getLineStartOffset(lineNumber)
                    
                    // 创建文本标签显示
                    // 使用 smallText() 创建小文本（适合作为标签显示）
                    val presentation = factory.smallText(propertyName)
                    
                    // 使用 addInlineElement 在方法定义行首显示标签
                    // 注意：addInlineElement 在行首显示，虽然不是严格意义上的"上方"，
                    // 但会在方法定义行的开始位置显示属性名标签
                    // 这是 PhpStorm 插件，针对 PHP 方法进行标注
                    sink.addInlineElement(
                        lineStartOffset,
                        false,
                        presentation,
                        false
                    )
                    
                    ProjectLogger.debug(
                        GetSetInlayHintsProvider::class.java,
                        "添加行内提示: 方法 ${element.name} -> 属性 $propertyName 在第 $lineNumber 行"
                    )
                }
                
                return true
            }
        }
    }
}

