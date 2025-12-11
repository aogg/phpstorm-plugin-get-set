package com.github.phpstorm.getset.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font

/**
 * Get/Set 高亮器
 * 定义高亮样式
 */
object GetSetHighlighter {
    
    /**
     * Getter 方法高亮键
     * 使用浅绿色 (#90EE90) 加粗
     */
    val GETTER_METHOD_KEY = TextAttributesKey.createTextAttributesKey(
        "GETSET_GETTER_METHOD",
        DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
    )
    
    /**
     * Setter 方法高亮键
     * 使用浅绿色 (#90EE90) 加粗
     */
    val SETTER_METHOD_KEY = TextAttributesKey.createTextAttributesKey(
        "GETSET_SETTER_METHOD",
        DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
    )
    
    /**
     * 获取 getter 方法的文本属性
     * 使用浅绿色加粗
     */
    fun getGetterTextAttributes(): TextAttributes {
        return TextAttributes(
            Color(0x90EE90), // 浅绿色前景色 (#90EE90)
            null, // 无背景色
            null, // 无效果颜色
            null, // 无边框
            Font.BOLD // 粗体
        )
    }
    
    /**
     * 获取 setter 方法的文本属性
     * 使用浅绿色加粗
     */
    fun getSetterTextAttributes(): TextAttributes {
        return TextAttributes(
            Color(0x90EE90), // 浅绿色前景色 (#90EE90)
            null, // 无背景色
            null, // 无效果颜色
            null, // 无边框
            Font.BOLD // 粗体
        )
    }
}

