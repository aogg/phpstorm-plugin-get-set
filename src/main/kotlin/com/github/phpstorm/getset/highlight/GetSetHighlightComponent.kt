package com.github.phpstorm.getset.highlight

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.EditorFactory

/**
 * Get/Set 高亮组件
 * 负责注册文档监听器
 */
class GetSetHighlightComponent : BaseComponent {
    
    companion object {
        private val logger = Logger.getInstance(GetSetHighlightComponent::class.java)
    }
    
    private var listener: GetSetHighlightRefreshListener? = null
    
    override fun initComponent() {
        logger.debug("初始化 GetSetHighlightComponent")
        
        // 创建监听器实例
        listener = GetSetHighlightRefreshListener()
        
        // 注册 DocumentListener 来监听文档内容变化
        val editorFactory = EditorFactory.getInstance()
        editorFactory.eventMulticaster.addDocumentListener(listener!!, ApplicationManager.getApplication())
        
        logger.debug("DocumentListener 已注册")
    }
    
    override fun disposeComponent() {
        logger.debug("销毁 GetSetHighlightComponent")
        listener?.let {
            val editorFactory = EditorFactory.getInstance()
            editorFactory.eventMulticaster.removeDocumentListener(it)
        }
        listener = null
    }
}

