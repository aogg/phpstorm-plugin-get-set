package com.github.phpstorm.getset.config

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
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
     * 获取当前配置
     */
    fun getConfig(): GetSetConfig {
        return config.copy(
            getterPatterns = config.getterPatterns.toMutableList(),
            setterPatterns = config.setterPatterns.toMutableList()
        )
    }
    
    /**
     * 更新配置
     */
    fun updateConfig(newConfig: GetSetConfig) {
        config = newConfig.copy(
            getterPatterns = newConfig.getterPatterns.toMutableList(),
            setterPatterns = newConfig.setterPatterns.toMutableList()
        )
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

