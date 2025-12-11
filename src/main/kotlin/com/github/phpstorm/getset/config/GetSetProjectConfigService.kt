package com.github.phpstorm.getset.config

import com.github.phpstorm.getset.util.ProjectLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import java.io.File

/**
 * 项目级配置服务
 * 读取项目目录下的 .plugin/get-set-highlighter/config.json 配置文件
 */
object GetSetProjectConfigService {
    
    private const val PLUGIN_DIR_NAME = ".plugin"
    private const val PLUGIN_NAME = "get-set-highlighter"
    private const val CONFIG_FILE_NAME = "config.json"
    
    /**
     * 获取项目级配置
     * @param project 项目实例，如果为 null 则尝试获取当前打开的项目
     * @return 项目级配置，如果文件不存在或解析失败则返回 null
     */
    fun getProjectConfig(project: Project? = null): GetSetProjectConfig? {
        val targetProject = project ?: getCurrentProject() ?: return null
        
        try {
            val configFile = getConfigFile(targetProject) ?: return null
            
            if (!configFile.exists() || !configFile.isFile) {
                ProjectLogger.debug(
                    GetSetProjectConfigService::class.java,
                    "项目级配置文件不存在: ${configFile.absolutePath}"
                )
                return null
            }
            
            val jsonContent = configFile.readText(Charsets.UTF_8)
            if (jsonContent.isBlank()) {
                ProjectLogger.debug(
                    GetSetProjectConfigService::class.java,
                    "项目级配置文件为空: ${configFile.absolutePath}"
                )
                return null
            }
            
            val config = parseConfig(jsonContent)
            
            ProjectLogger.info(
                GetSetProjectConfigService::class.java,
                "成功加载项目级配置: ${configFile.absolutePath}, getterPatterns=${config.getterPatterns}, setterPatterns=${config.setterPatterns}, enabled=${config.enabled}"
            )
            
            return config
        } catch (e: Exception) {
            ProjectLogger.warn(
                GetSetProjectConfigService::class.java,
                "读取项目级配置失败: ${e.message}",
                e
            )
            return null
        }
    }
    
    /**
     * 获取配置文件路径
     */
    private fun getConfigFile(project: Project): File? {
        try {
            val basePath = project.basePath ?: return null
            val projectDir = File(basePath)
            if (!projectDir.exists() || !projectDir.isDirectory) {
                return null
            }
            
            // 路径: 项目目录/.plugin/get-set-highlighter/config.json
            val pluginDir = File(projectDir, PLUGIN_DIR_NAME)
            val pluginNameDir = File(pluginDir, PLUGIN_NAME)
            val configFile = File(pluginNameDir, CONFIG_FILE_NAME)
            
            return configFile
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * 解析 JSON 配置（简单的 JSON 解析，支持基本格式）
     */
    private fun parseConfig(jsonContent: String): GetSetProjectConfig {
        val getterPatterns = mutableListOf<String>()
        val setterPatterns = mutableListOf<String>()
        var enabled: Boolean? = null
        
        // 移除空白字符和换行符（但保留字符串值中的内容），简化解析
        // 注意：这里移除所有空白字符，对于简单的配置格式（模式字符串通常不含空格）是安全的
        val cleaned = jsonContent.replace("\\s".toRegex(), "")
        
        // 解析 getterPatterns 数组
        val getterPattern = "\"getterPatterns\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex(RegexOption.IGNORE_CASE)
        val getterMatch = getterPattern.find(cleaned)
        if (getterMatch != null) {
            val arrayContent = getterMatch.groupValues[1]
            // 提取数组中的字符串值
            val stringPattern = "\"([^\"]+)\"".toRegex()
            stringPattern.findAll(arrayContent).forEach { match ->
                val pattern = match.groupValues[1]
                if (pattern.isNotBlank()) {
                    getterPatterns.add(pattern.trim())
                }
            }
        }
        
        // 解析 setterPatterns 数组
        val setterPattern = "\"setterPatterns\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex(RegexOption.IGNORE_CASE)
        val setterMatch = setterPattern.find(cleaned)
        if (setterMatch != null) {
            val arrayContent = setterMatch.groupValues[1]
            // 提取数组中的字符串值
            val stringPattern = "\"([^\"]+)\"".toRegex()
            stringPattern.findAll(arrayContent).forEach { match ->
                val pattern = match.groupValues[1]
                if (pattern.isNotBlank()) {
                    setterPatterns.add(pattern.trim())
                }
            }
        }
        
        // 解析 enabled 布尔值
        val enabledPattern = "\"enabled\"\\s*:\\s*(true|false)".toRegex(RegexOption.IGNORE_CASE)
        val enabledMatch = enabledPattern.find(cleaned)
        if (enabledMatch != null) {
            enabled = enabledMatch.groupValues[1].lowercase() == "true"
        }
        
        return GetSetProjectConfig(
            getterPatterns = getterPatterns,
            setterPatterns = setterPatterns,
            enabled = enabled
        )
    }
    
    /**
     * 获取当前项目
     */
    private fun getCurrentProject(): Project? {
        return try {
            val projects = ProjectManager.getInstance().openProjects
            if (projects.isNotEmpty()) {
                // 优先返回第一个打开的项目
                projects.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 项目级配置数据类
 */
data class GetSetProjectConfig(
    /**
     * Getter 方法通配模式列表
     */
    val getterPatterns: MutableList<String> = mutableListOf(),
    
    /**
     * Setter 方法通配模式列表
     */
    val setterPatterns: MutableList<String> = mutableListOf(),
    
    /**
     * 是否启用高亮（null 表示使用全局配置）
     */
    val enabled: Boolean? = null
)

