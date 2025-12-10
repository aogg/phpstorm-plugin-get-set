package com.github.phpstorm.getset.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

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
        DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
    )
    
    /**
     * Setter 方法高亮键
     */
    val SETTER_METHOD_KEY = TextAttributesKey.createTextAttributesKey(
        "GETSET_SETTER_METHOD",
        DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
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

