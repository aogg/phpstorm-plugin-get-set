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
import com.intellij.openapi.editor.ex.util.EditorUtil
import java.awt.Font

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
                    // 计算提示对齐位置：方法所在行首个非空字符（缩进后第一列）
                    val document = editor.document
                    val methodStartOffset = element.textRange.startOffset
                    val lineNumber = document.getLineNumber(methodStartOffset)
                    val lineStartOffset = document.getLineStartOffset(lineNumber)
                    val lineEndOffset = document.getLineEndOffset(lineNumber)
                    val lineText = document.charsSequence.subSequence(lineStartOffset, lineEndOffset)
                    val firstNonSpaceOffset = lineText.indexOfFirst { !it.isWhitespace() }.let {
                        if (it >= 0) lineStartOffset + it else lineStartOffset
                    }
                    val hintDisplayOffset = (firstNonSpaceOffset)
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

                    // 计算左边距（按列转为像素）
                    val indentColumns = editor.offsetToLogicalPosition(firstNonSpaceOffset).column
                    val spaceWidthPx = EditorUtil.getSpaceWidth(Font.PLAIN, editor)
                    val paddedPresentation = factory.inset(
                        presentation,
                        left = indentColumns * spaceWidthPx,
                        right = 0,
                        top = 0,
                        down = 0
                    )
                    
                    // 在方法上方对齐到首个非空字符
                    sink.addBlockElement(
                        hintDisplayOffset,
                        true,  // 关联前置文本以使用精确水平偏移
                        true,
                        0,  // 垂直偏移（0 表示和方法行紧挨着）
                        paddedPresentation
                    )
                    
                    ProjectLogger.debug(
                        GetSetInlayHintsProvider::class.java,
                        "添加行内提示: 方法 ${element.name} -> 属性 $propertyName 在第 $lineNumber 行，偏移: $hintDisplayOffset，可导航: ${propertyElement != null}"
                    )
                }
                
                return true
            }
        }
    }
}

