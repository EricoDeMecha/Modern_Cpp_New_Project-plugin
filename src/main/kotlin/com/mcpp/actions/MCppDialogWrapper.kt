package com.mcpp.actions

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class MCppDialogWrapper: DialogWrapper(true) {
    val panel = JPanel(GridBagLayout())
    val name = JTextField()
    val location  = JTextField()
    val projectTemplate = JTextField()
    init {
        init()
        title = "Modern Cpp New Project"
    }
    override fun createCenterPanel(): JComponent {
        val gb  = GridBag()
            .setDefaultInsets(Insets(0,0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
            .setDefaultWeightX(1.0)
            .setDefaultFill(GridBagConstraints.HORIZONTAL)

        panel.preferredSize = Dimension(400, 500)

        panel.add(label("Name"), gb.nextLine().next().weightx(0.2))
        panel.add(name, gb.next().next().weightx(0.8))
        panel.add(label("Location"), gb.nextLine().next().weightx(0.2))
        panel.add(location, gb.next().next().weightx(0.8))
        panel.add(label("Project Template"), gb.nextLine().next().weightx(0.2))
        panel.add(projectTemplate, gb.next().next().weightx(0.8))
        panel.add(button("Add"), gb.next().next().weightx(0.8))
        return panel
    }

    private fun label(text: String): JComponent {
        val label = JBLabel(text)
        label.componentStyle = UIUtil.ComponentStyle.SMALL
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)
        return label
    }
    private fun button(text: String): JComponent {
        val m_button = JButton(text)
        m_button.setVisible(true)
        m_button.addActionListener(AddButtonListener())
        return m_button
    }
}

class AddButtonListener: ActionListener {
    override fun actionPerformed(e: ActionEvent?) {
        TODO("Check if the label is clear and if not add the path to the list ")
    }

}