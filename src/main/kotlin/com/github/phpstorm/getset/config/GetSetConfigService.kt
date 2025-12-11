package com.github.phpstorm.getset.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Get/Set 配置服务
 * 负责配置的持久化和管理
 */
@State(
    name = "GetSetConfigService",
    storages = [Storage("getset-highlighter.xml")]
)
@Service(Service.Level.APP)
class GetSetConfigService : PersistentStateComponent<GetSetConfigService> {
    
    private var config = GetSetConfig.default()
    
    /**
     * 获取当前配置（合并全局配置和项目级配置）
     * @param project 项目实例，如果提供则合并项目级配置
     * @return 合并后的配置
     */
    fun getConfig(project: Project? = null): GetSetConfig {
        val globalConfig = config.copy(
            getterPatterns = config.getterPatterns.toMutableList(),
            setterPatterns = config.setterPatterns.toMutableList()
        )
        
        // 如果没有提供项目，或者项目级配置不存在，直接返回全局配置
        val projectConfig = if (project != null) {
            GetSetProjectConfigService.getProjectConfig(project)
        } else {
            null
        }
        
        if (projectConfig == null) {
            return globalConfig
        }
        
        // 合并配置：项目级配置的模式追加到全局配置（去重）
        val mergedGetterPatterns = (globalConfig.getterPatterns + projectConfig.getterPatterns)
            .distinct()
            .toMutableList()
        
        val mergedSetterPatterns = (globalConfig.setterPatterns + projectConfig.setterPatterns)
            .distinct()
            .toMutableList()
        
        // enabled 字段：如果项目级配置存在，使用项目级配置的值
        val mergedEnabled = projectConfig.enabled ?: globalConfig.enabled
        
        return GetSetConfig(
            getterPatterns = mergedGetterPatterns,
            setterPatterns = mergedSetterPatterns,
            enabled = mergedEnabled
        )
    }
    
    /**
     * 更新配置
     * 通过属性设置器更新，触发 PersistentStateComponent 的自动保存机制
     */
    fun updateConfig(newConfig: GetSetConfig) {
        // 使用属性设置器更新，这样 IntelliJ Platform 才能检测到变化并自动保存
        getterPatterns = newConfig.getterPatterns.joinToString(",")
        setterPatterns = newConfig.setterPatterns.joinToString(",")
        enabled = newConfig.enabled
    }
    
    /**
     * 获取服务实例
     */
    companion object {
        fun getInstance(): GetSetConfigService {
            return ApplicationManager.getApplication().getService(GetSetConfigService::class.java)
        }
    }
    
    // 持久化相关方法
    override fun getState(): GetSetConfigService {
        return this
    }
    
    override fun loadState(state: GetSetConfigService) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    // 序列化配置数据
    var getterPatterns: String
        get() = config.getterPatterns.joinToString(",")
        set(value) {
            config = config.copy(
                getterPatterns = if (value.isBlank()) mutableListOf() else value.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            )
        }
    
    var setterPatterns: String
        get() = config.setterPatterns.joinToString(",")
        set(value) {
            config = config.copy(
                setterPatterns = if (value.isBlank()) mutableListOf() else value.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            )
        }
    
    var enabled: Boolean
        get() = config.enabled
        set(value) {
            config = config.copy(enabled = value)
        }
}

