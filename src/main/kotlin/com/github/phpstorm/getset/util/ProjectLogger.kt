package com.github.phpstorm.getset.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * 项目目录日志工具类
 * 将日志写入到项目目录的 .idea/plugin/Get-Set-Method-Highlighter/logs/ 目录
 */
object ProjectLogger {
    
    private const val PLUGIN_NAME = "Get-Set-Method-Highlighter"
    private const val LOG_DIR_NAME = "logs"
    private const val LOG_FILE_PREFIX = "getset-highlighter"
    private const val DATE_FORMAT = "yyyy-MM-dd"
    
    // 降级到系统日志的 Logger
    private val fallbackLogger = Logger.getInstance(ProjectLogger::class.java)
    
    // 每个项目的日志文件写入器缓存
    private val projectWriters = mutableMapOf<String, FileWriter>()
    private val writerLock = ReentrantLock()
    
    // 日期格式化器
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
    
    /**
     * 记录 INFO 级别日志
     */
    fun info(clazz: Class<*>, message: String) {
        log(LogLevel.INFO, clazz, message)
    }
    
    /**
     * 记录 WARN 级别日志
     */
    fun warn(clazz: Class<*>, message: String) {
        log(LogLevel.WARN, clazz, message)
    }
    
    /**
     * 记录 WARN 级别日志（带异常）
     */
    fun warn(clazz: Class<*>, message: String, throwable: Throwable) {
        log(LogLevel.WARN, clazz, "$message\n${throwable.stackTraceToString()}")
    }
    
    /**
     * 记录 DEBUG 级别日志
     */
    fun debug(clazz: Class<*>, message: String) {
        log(LogLevel.DEBUG, clazz, message)
    }
    
    /**
     * 记录日志
     */
    private fun log(level: LogLevel, clazz: Class<*>, message: String) {
        try {
            val project = getCurrentProject()
            if (project != null) {
                val logFile = getLogFile(project)
                if (logFile != null) {
                    writeToFile(logFile, level, clazz, message)
                    return
                }
            }
            
            // 降级到系统日志
            when (level) {
                LogLevel.INFO -> fallbackLogger.info("[$clazz.simpleName] $message")
                LogLevel.WARN -> fallbackLogger.warn("[$clazz.simpleName] $message")
                LogLevel.DEBUG -> fallbackLogger.debug("[$clazz.simpleName] $message")
            }
        } catch (e: Exception) {
            // 如果写入失败，降级到系统日志
            try {
                fallbackLogger.warn("无法写入项目日志，使用系统日志: ${e.message}")
                when (level) {
                    LogLevel.INFO -> fallbackLogger.info("[$clazz.simpleName] $message")
                    LogLevel.WARN -> fallbackLogger.warn("[$clazz.simpleName] $message")
                    LogLevel.DEBUG -> fallbackLogger.debug("[$clazz.simpleName] $message")
                }
            } catch (ex: Exception) {
                // 最后的降级，静默失败
            }
        }
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
    
    /**
     * 获取日志文件
     */
    private fun getLogFile(project: Project): File? {
        try {
            val basePath = project.basePath ?: return null
            val projectDir = File(basePath)
            if (!projectDir.exists() || !projectDir.isDirectory) {
                return null
            }
            
            // 创建目录结构: .idea/plugin/Get-Set-Method-Highlighter/logs/
            val ideaDir = File(projectDir, ".idea")
            if (!ideaDir.exists()) {
                ideaDir.mkdirs()
            }
            
            val pluginDir = File(ideaDir, "plugin")
            if (!pluginDir.exists()) {
                pluginDir.mkdirs()
            }
            
            val pluginNameDir = File(pluginDir, PLUGIN_NAME)
            if (!pluginNameDir.exists()) {
                pluginNameDir.mkdirs()
            }
            
            val logsDir = File(pluginNameDir, LOG_DIR_NAME)
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            
            // 生成日志文件名: getset-highlighter-YYYY-MM-DD.log
            val dateStr = dateFormat.format(Date())
            val logFileName = "$LOG_FILE_PREFIX-$dateStr.log"
            val logFile = File(logsDir, logFileName)
            
            // 如果文件不存在，创建它
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            
            return logFile
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * 写入日志到文件
     */
    private fun writeToFile(logFile: File, level: LogLevel, clazz: Class<*>, message: String) {
        writerLock.lock()
        try {
            val writer = projectWriters.getOrPut(logFile.absolutePath) {
                FileWriter(logFile, true) // append mode
            }
            
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val logLine = "[$timestamp] [$level] [${clazz.simpleName}] - $message\n"
            
            writer.append(logLine)
            writer.flush()
        } catch (e: IOException) {
            // 如果写入失败，关闭并移除写入器
            try {
                projectWriters[logFile.absolutePath]?.close()
            } catch (ex: Exception) {
                // 忽略关闭异常
            }
            projectWriters.remove(logFile.absolutePath)
            throw e
        } finally {
            writerLock.unlock()
        }
    }
    
    /**
     * 关闭所有日志文件写入器
     */
    fun closeAll() {
        writerLock.lock()
        try {
            projectWriters.values.forEach { writer ->
                try {
                    writer.close()
                } catch (e: Exception) {
                    // 忽略关闭异常
                }
            }
            projectWriters.clear()
        } finally {
            writerLock.unlock()
        }
    }
    
    /**
     * 日志级别枚举
     */
    private enum class LogLevel {
        INFO,
        WARN,
        DEBUG
    }
}

