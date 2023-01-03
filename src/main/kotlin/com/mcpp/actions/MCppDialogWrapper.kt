package com.mcpp.actions

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import javax.swing.*
import javax.swing.border.Border

class MCppDialogWrapper : DialogWrapper(true) {
    val panel = JPanel(GridBagLayout())
    val name = JTextField()
    val location = JTextField()
    val browse_button = JButton("...")
    val projectTemplate = JTextField()
    val addBtn = JButton("Add")
    var listModel = DefaultListModel<String>()
    val list = JBList(listModel)

    init {
        init()
        title = "Modern Cpp New Project"
    }

    override fun createCenterPanel(): JComponent {
        val gb = GridBag()
            .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
            .setDefaultWeightX(1.0)
            .setDefaultFill(GridBagConstraints.HORIZONTAL)

        panel.preferredSize = Dimension(500, 300)

        browse_button.addActionListener{
            TODO("Implement an action listener for browsing")
        }
        addBtn.addActionListener {
            val template_text_field = projectTemplate.text
            if (!template_text_field.isEmpty()) {
                listModel.addElement(template_text_field)
                projectTemplate.text = ""
            }
        }
        list.border = CustomRenderer()
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
//        Add elements to the panel
        panel.add(label("Name"), gb.nextLine().next().weightx(0.2))
        panel.add(name, gb.next().next().weightx(0.8))
        panel.add(label("Location"), gb.nextLine().next().weightx(0.2))
        panel.add(location, gb.next().next().weightx(0.8))
        panel.add(label("Project Template"), gb.nextLine().next().weightx(0.2))
        panel.add(projectTemplate, gb.next().next().weightx(0.8))
        panel.add(addBtn, gb.next().next().weightx(0.2))
        panel.add(JLabel(""), gb.nextLine().next().weightx(0.2))
        panel.add(list, gb.next().next().weightx(0.8))
        // handle events
        projectTemplate.text = list.selectedValue
        return panel
    }

    private fun label(text: String): JComponent {
        val label = JBLabel(text)
        label.componentStyle = UIUtil.ComponentStyle.SMALL
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)
        return label
    }
}

class CustomRenderer : ListCellRenderer<String>, Border {
    private val label = JBLabel()
    var insets = Insets(10, 10, 10, 10)
    private val border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    override fun getListCellRendererComponent(
        list: JList<out String>?,
        value: String?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        label.text = value
        if (isSelected) {
            label.background = Color.LIGHT_GRAY
            label.foreground = Color.BLACK
        } else {
            label.background = Color.WHITE
            label.foreground = Color.BLACK
        }
        label.horizontalAlignment = JBLabel.CENTER
        label.border = BorderFactory.createCompoundBorder(border, label.border)
        return label
    }

    override fun paintBorder(c: Component?, g: Graphics?, x: Int, y: Int, width: Int, height: Int) {
        val g2 = g as Graphics2D
        g2.color = Color.BLACK
        g2.stroke = BasicStroke(2f)
        g2.drawRect(x, y, width - 1, height - 1)
    }

    override fun getBorderInsets(c: Component?): Insets {
        return insets
    }

    override fun isBorderOpaque(): Boolean {
        return false
    }
}