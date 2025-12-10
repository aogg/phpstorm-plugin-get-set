package com.github.phpstorm.getset.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import java.awt.Color

/**
 * Get/Set 高亮器
 * 定义高亮样式
 */
object GetSetHighlighter {
    
    /**
     * Getter 方法高亮键
     */
    val GETTER_METHOD_KEY = TextAttributesKey.createTextAttributesKey(
        "GETSET_GETTER_METHOD",
        DefaultLanguageHighlighterColors.METHOD_NAME
    )
    
    /**
     * Setter 方法高亮键
     */
    val SETTER_METHOD_KEY = TextAttributesKey.createTextAttributesKey(
        "GETSET_SETTER_METHOD",
        DefaultLanguageHighlighterColors.METHOD_NAME
    )
    
    /**
     * 获取默认的 getter 样式
     * 使用蓝色高亮
     */
    fun getDefaultGetterAttributes(): TextAttributesKey {
        return GETTER_METHOD_KEY
    }
    
    /**
     * 获取默认的 setter 样式
     * 使用绿色高亮
     */
    fun getDefaultSetterAttributes(): TextAttributesKey {
        return SETTER_METHOD_KEY
    }
}

