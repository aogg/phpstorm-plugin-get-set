package com.github.phpstorm.getset.highlight

import com.github.phpstorm.getset.config.GetSetConfigService
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.VirtualFile

/**
 * Get/Set 高亮刷新监听器
 * 监听文件打开事件，自动刷新高亮
 */
class GetSetHighlightRefreshListener : FileEditorManagerListener {
    
    /**
     * 文件打开后触发
     */
    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
        // 只处理 PHP 文件
        if (!isPhpFile(file)) {
            return
        }
        
        // 检查配置是否启用
        val configService = GetSetConfigService.getInstance()
        val config = configService.getConfig()
        if (!config.enabled) {
            return
        }
        
        // 从 FileEditorManager 获取项目并触发重新分析
        val project = source.project
        if (!project.isDisposed) {
            DaemonCodeAnalyzer.getInstance(project).restart()
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
     * 刷新高亮
     */
    private fun refreshHighlighting(file: VirtualFile) {
        // 遍历所有打开的项目，找到包含该文件的项目
        val projects = ProjectManager.getInstance().openProjects
        for (project in projects) {
            if (project.isDisposed) continue
            
            val fileEditorManager = FileEditorManager.getInstance(project)
            if (fileEditorManager.isFileOpen(file)) {
                // 使用 DaemonCodeAnalyzer 重新分析文件
                DaemonCodeAnalyzer.getInstance(project).restart()
                break
            }
        }
    }
}

