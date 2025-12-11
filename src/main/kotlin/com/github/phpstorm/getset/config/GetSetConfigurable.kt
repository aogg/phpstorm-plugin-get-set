package com.github.phpstorm.getset.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * Get/Set 配置页面
 */
class GetSetConfigurable : Configurable {
    
    private var configPanel: JPanel? = null
    private var enabledCheckBox: JBCheckBox? = null
    private var getterPatternTable: JBTable? = null
    private var setterPatternTable: JBTable? = null
    
    private var getterPatternModel: DefaultTableModel? = null
    private var setterPatternModel: DefaultTableModel? = null
    
    override fun getDisplayName(): String {
        return "Get/Set Highlighter"
    }
    
    override fun createComponent(): JComponent? {
        val configService = GetSetConfigService.getInstance()
        val config = configService.getConfig()
        
        // 创建主面板
        configPanel = JPanel(BorderLayout())
        
        // 启用复选框
        enabledCheckBox = JBCheckBox("启用 Get/Set 方法高亮", config.enabled)
        
        // 创建表格模型
        getterPatternModel = createTableModel(config.getterPatterns)
        setterPatternModel = createTableModel(config.setterPatterns)
        
        // 创建表格
        getterPatternTable = createTable(getterPatternModel!!, "Getter 模式（支持 *）")
        setterPatternTable = createTable(setterPatternModel!!, "Setter 模式（支持 *）")
        
        // 构建表单
        val enabledCheckBoxLocal = requireNotNull(enabledCheckBox) { "enabledCheckBox should be initialized" }
        val formBuilder = FormBuilder.createFormBuilder()
            .addComponent(enabledCheckBoxLocal)
            .addSeparator()
            .addLabeledComponent("Getter 方法模式:", createTablePanel(getterPatternTable!!, getterPatternModel!!))
            .addLabeledComponent("Setter 方法模式:", createTablePanel(setterPatternTable!!, setterPatternModel!!))
            .addComponentFillVertically(JPanel(), 0)
        
        configPanel!!.add(formBuilder.panel, BorderLayout.CENTER)
        
        return configPanel
    }
    
    /**
     * 创建表格模型
     */
    private fun createTableModel(items: List<String>): DefaultTableModel {
        val model = object : DefaultTableModel(arrayOf("值"), 0) {
            override fun isCellEditable(row: Int, column: Int): Boolean = column == 0
        }
        
        items.forEach { item ->
            model.addRow(arrayOf(item))
        }
        
        return model
    }
    
    /**
     * 创建表格
     */
    private fun createTable(model: DefaultTableModel, title: String): JBTable {
        val table = JBTable(model)
        table.tableHeader.reorderingAllowed = false
        table.setShowGrid(true)
        table.setPreferredScrollableViewportSize(JBUI.size(300, 150))
        return table
    }
    
    /**
     * 创建带按钮的表格面板
     */
    private fun createTablePanel(table: JBTable, model: DefaultTableModel): JPanel {
        val panel = JPanel(BorderLayout())
        
        // 添加按钮面板
        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
        
        // 添加按钮
        val addButton = JButton("添加")
        addButton.addActionListener {
            model.addRow(arrayOf(""))
            val rowCount = model.rowCount
            table.editCellAt(rowCount - 1, 0)
            table.setRowSelectionInterval(rowCount - 1, rowCount - 1)
        }
        
        val removeButton = JButton("删除")
        removeButton.addActionListener {
            val selectedRow = table.selectedRow
            if (selectedRow >= 0) {
                model.removeRow(selectedRow)
            }
        }
        
        buttonPanel.add(addButton)
        buttonPanel.add(Box.createHorizontalStrut(5))
        buttonPanel.add(removeButton)
        buttonPanel.add(Box.createHorizontalGlue())
        
        panel.add(JScrollPane(table), BorderLayout.CENTER)
        panel.add(buttonPanel, BorderLayout.SOUTH)
        
        return panel
    }
    
    /**
     * 检查配置是否已修改
     */
    override fun isModified(): Boolean {
        val configService = GetSetConfigService.getInstance()
        val currentConfig = configService.getConfig()
        
        val enabledSelected = enabledCheckBox?.isSelected ?: false
        if (enabledSelected != currentConfig.enabled) {
            return true
        }
        
        if (!listsEqual(getTableValues(getterPatternModel!!), currentConfig.getterPatterns)) {
            return true
        }
        
        if (!listsEqual(getTableValues(setterPatternModel!!), currentConfig.setterPatterns)) {
            return true
        }
        
        return false
    }
    
    /**
     * 应用配置
     */
    @Throws(ConfigurationException::class)
    override fun apply() {
        val configService = GetSetConfigService.getInstance()
        
        val newConfig = GetSetConfig(
            getterPatterns = getTableValues(getterPatternModel!!),
            setterPatterns = getTableValues(setterPatternModel!!),
            enabled = enabledCheckBox!!.isSelected
        )
        
        configService.updateConfig(newConfig)
    }
    
    /**
     * 重置配置
     */
    override fun reset() {
        val configService = GetSetConfigService.getInstance()
        val config = configService.getConfig()
        
        enabledCheckBox!!.isSelected = config.enabled
        updateTableModel(getterPatternModel!!, config.getterPatterns)
        updateTableModel(setterPatternModel!!, config.setterPatterns)
    }
    
    /**
     * 从表格获取值列表
     */
    private fun getTableValues(model: DefaultTableModel): MutableList<String> {
        val values = mutableListOf<String>()
        for (i in 0 until model.rowCount) {
            val value = model.getValueAt(i, 0) as? String ?: ""
            if (value.isNotBlank()) {
                values.add(value.trim())
            }
        }
        return values
    }
    
    /**
     * 更新表格模型
     */
    private fun updateTableModel(model: DefaultTableModel, values: List<String>) {
        model.rowCount = 0
        values.forEach { value ->
            model.addRow(arrayOf(value))
        }
    }
    
    /**
     * 比较两个列表是否相等
     */
    private fun listsEqual(list1: List<String>, list2: List<String>): Boolean {
        if (list1.size != list2.size) {
            return false
        }
        return list1.sorted() == list2.sorted()
    }
}

