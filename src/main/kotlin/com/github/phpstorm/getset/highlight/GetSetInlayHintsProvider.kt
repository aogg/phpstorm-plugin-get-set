package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.github.phpstorm.getset.util.ProjectLogger
import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
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
                    
                    // 查找对应的属性元素（字段或 @property 注释）
                    val propertyElement = ClassPropertyDetector.findProperty(containingClass, propertyName)
                    
                    // 创建文本标签，如果找到属性则添加点击跳转功能
                    val presentation = if (propertyElement != null) {
                        // 创建可点击的文本，使用 referenceOnHover 添加导航功能
                        val textPresentation = factory.smallText(propertyName)
                        factory.referenceOnHover(textPresentation) { _, _ ->
                            // 点击时导航到属性
                            val virtualFile = propertyElement.containingFile?.virtualFile
                            if (virtualFile != null) {
                                val offset = propertyElement.textRange.startOffset
                                val targetProject = propertyElement.project
                                val descriptor = OpenFileDescriptor(targetProject, virtualFile, offset)
                                FileEditorManager.getInstance(targetProject).openTextEditor(descriptor, true)
                            }
                        }
                    } else {
                        // 如果找不到属性，创建普通文本
                        factory.smallText(propertyName)
                    }
                    
                    // 使用 addBlockElement 在方法上方显示标签（和无用法提示同一行）
                    // showAbove = true 表示显示在方法上方
                    sink.addBlockElement(
                        methodStartOffset,
                        false,
                        true,  // showAbove = true，显示在方法上方
                        0,
                        presentation
                    )
                    
                    ProjectLogger.debug(
                        GetSetInlayHintsProvider::class.java,
                        "添加行内提示: 方法 ${element.name} -> 属性 $propertyName 在第 $lineNumber 行，可导航: ${propertyElement != null}"
                    )
                }
                
                return true
            }
        }
    }
}

