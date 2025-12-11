package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Get/Set 高亮刷新监听器
 * 监听文件打开、修改和保存事件，自动刷新高亮
 */
class GetSetHighlightRefreshListener : FileEditorManagerListener, FileDocumentManagerListener, DocumentListener {
    
    companion object {
        private val logger = Logger.getInstance(GetSetHighlightRefreshListener::class.java)
        // 防抖延迟时间（毫秒）
        private const val DEBOUNCE_DELAY_MS = 300L
    }
    
    // 用于防抖的时间戳记录
    private val lastRefreshTime = ConcurrentHashMap<VirtualFile, AtomicLong>()
    
    /**
     * 文件打开后触发
     */
    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        // 只处理 PHP 文件
        if (!isPhpFile(file)) {
            return
        }
        
        logger.debug("文件打开: ${file.path}")
        
        // 检查配置是否启用
        val configService = GetSetConfigService.getInstance()
        val config = configService.getConfig()
        if (!config.enabled) {
            logger.debug("高亮功能已禁用，跳过刷新")
            return
        }
        
        // 从 FileEditorManager 获取项目并触发重新分析
        val project = source.project
        if (!project.isDisposed) {
            // 延迟执行，确保 PSI 树已构建
            ApplicationManager.getApplication().invokeLater({
                if (!project.isDisposed) {
                    refreshHighlightingForFile(project, file)
                }
            }, project.disposed)
        }
    }
    
    /**
     * 文档内容变化时触发（DocumentListener）
     */
    override fun documentChanged(event: DocumentEvent) {
        val document = event.document
        val fileDocumentManager = FileDocumentManager.getInstance()
        val file = fileDocumentManager.getFile(document) ?: return
        
        // 只处理 PHP 文件
        if (!isPhpFile(file)) {
            return
        }
        
        logger.debug("文档修改: ${file.path}")
        
        // 检查配置是否启用
        val configService = GetSetConfigService.getInstance()
        val config = configService.getConfig()
        if (!config.enabled) {
            return
        }
        
        // 找到包含该文件的项目
        val projects = ProjectManager.getInstance().openProjects
        for (project in projects) {
            if (project.isDisposed) continue
            
            // 检查文件是否在项目中
            val projectRootManager = ProjectRootManager.getInstance(project)
            if (!projectRootManager.fileIndex.isInContent(file)) {
                continue
            }
            
            val fileEditorManager = FileEditorManager.getInstance(project)
            if (fileEditorManager.isFileOpen(file)) {
                // 使用防抖机制，避免频繁刷新
                scheduleDebouncedRefresh(project, file)
                break
            }
        }
    }
    
    /**
     * 使用防抖机制调度刷新任务
     */
    private fun scheduleDebouncedRefresh(project: Project, file: VirtualFile) {
        val now = System.currentTimeMillis()
        val lastTime = lastRefreshTime.getOrPut(file) { AtomicLong(0) }
        val timeSinceLastRefresh = now - lastTime.get()
        
        if (timeSinceLastRefresh < DEBOUNCE_DELAY_MS) {
            // 如果距离上次刷新时间太短，延迟执行
            ApplicationManager.getApplication().invokeLater({
                if (!project.isDisposed && System.currentTimeMillis() - lastTime.get() >= DEBOUNCE_DELAY_MS) {
                    lastTime.set(System.currentTimeMillis())
                    refreshHighlightingForFile(project, file)
                }
            }, project.disposed)
        } else {
            // 立即执行
            lastTime.set(now)
            ApplicationManager.getApplication().invokeLater({
                if (!project.isDisposed) {
                    refreshHighlightingForFile(project, file)
                }
            }, project.disposed)
        }
    }
    
    /**
     * 文档保存前触发
     */
    /**
     * DocumentListener 接口方法（未使用）
     */
    override fun beforeDocumentChange(event: DocumentEvent) {
        // 不需要处理
    }
    
    override fun beforeDocumentSaving(document: Document) {
        val fileDocumentManager = FileDocumentManager.getInstance()
        val file = fileDocumentManager.getFile(document) ?: return
        
        // 只处理 PHP 文件
        if (!isPhpFile(file)) {
            return
        }
        
        logger.debug("文档保存: ${file.path}")
        
        // 检查配置是否启用
        val configService = GetSetConfigService.getInstance()
        val config = configService.getConfig()
        if (!config.enabled) {
            return
        }
        
        // 找到包含该文件的项目
        val projects = ProjectManager.getInstance().openProjects
        for (project in projects) {
            if (project.isDisposed) continue
            
            // 检查文件是否在项目中
            val projectRootManager = ProjectRootManager.getInstance(project)
            if (!projectRootManager.fileIndex.isInContent(file)) {
                continue
            }
            
            val fileEditorManager = FileEditorManager.getInstance(project)
            if (fileEditorManager.isFileOpen(file)) {
                // 保存时立即刷新
                refreshHighlightingForFile(project, file)
                break
            }
        }
    }
    
    /**
     * 检查是否为 PHP 文件
     */
    private fun isPhpFile(file: VirtualFile): Boolean {
        val extension = file.extension?.lowercase() ?: return false
        return extension == "php" || extension == "phtml" || extension == "php5" || extension == "php7"
    }
    
    /**
     * 刷新指定文件的高亮
     */
    private fun refreshHighlightingForFile(project: Project, file: VirtualFile) {
        try {
            // 获取文档
            val document = FileDocumentManager.getInstance().getDocument(file) ?: run {
                logger.debug("无法获取文档: ${file.path}")
                return
            }
            
            // 获取 PSI 文件
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: run {
                logger.debug("无法获取 PSI 文件: ${file.path}")
                return
            }
            
            logger.debug("刷新高亮: ${file.path}")
            
            // 针对特定文件重新分析
            val daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project)
            daemonCodeAnalyzer.restart(psiFile)
            
        } catch (e: Exception) {
            logger.warn("刷新高亮失败: ${file.path}", e)
        }
    }
}

