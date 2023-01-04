package com.mcpp.actions


import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import javax.swing.*
import javax.swing.border.Border
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.text.AbstractDocument


/**
 * M cpp dialog wrapper
 *
 * @constructor Create empty M cpp dialog wrapper
 */
class MCppDialogWrapper : DialogWrapper(true) {
    val panel = JPanel(GridBagLayout())
    val name = JTextField()
    val browse_folder = TextFieldWithBrowseButton()
    val projectTemplate = JTextField()
    val addBtn = JButton("+")
    val removeBtn = JButton("-")
    var listModel = DefaultListModel<String>()
    val list = JBList(listModel)
    val location_info_label = InformationLabel("")
    var selectedTemplate: String? = null
    val m_ok = this.okAction

    init {
        init()
        title = "Modern Cpp New Project"
        /*Get the persistent storage*/
        val mCppComponent = ServiceManager.getService(MCppComponent::class.java)
        /*        mCppComponent.addValue("https://github.com/cpp-best-practices/cmake_conan_boilerplate_template.git")
                mCppComponent.addValue("https://github.com/filipdutescu/modern-cpp-template.git")*/
        for (element in mCppComponent.getState().values) {
            listModel.addElement(element)
        }
        /*Change focus to the add button*/
        addBtn.isDefaultCapable = true
        projectTemplate.addActionListener {
            addBtn.requestFocusInWindow()
        }
        m_ok.isEnabled = false
    }

    override fun createActions(): Array<Action> {
        val actions = super.createActions()
        actions[0].putValue(Action.NAME, "Create")
        return actions
    }

    /**
     * Add update func
     *
     * @param updateFunc
     * @receiver
     */
    fun AbstractDocument.addUpdateFunc(updateFunc: () -> Unit) {
        val documentListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                updateFunc()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                updateFunc()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                updateFunc()
            }
        }
        addDocumentListener(documentListener)
    }

    override fun createCenterPanel(): JComponent {
        val gb = GridBag()
            .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
            .setDefaultWeightX(1.0)
            .setDefaultFill(GridBagConstraints.HORIZONTAL)

        panel.preferredSize = Dimension(500, 300)
//        panel.preferredSize = Dimension(preferredSize.width*2, preferredSize.height*2)

        addBtn.addActionListener {
            val template_text_field = projectTemplate.text
            if (!template_text_field.isEmpty()) {
                listModel.addElement(template_text_field)
                val mCppComponent = ServiceManager.getService(MCppComponent::class.java)
                mCppComponent.addValue(template_text_field)
                projectTemplate.text = ""
            }
        }
        removeBtn.addActionListener {
            val mCppComponent = ServiceManager.getService(MCppComponent::class.java)
            val selectedIndex = list.selectedIndex
            if (selectedIndex != -1) {
                mCppComponent.removeValue(listModel.getElementAt(selectedIndex))
                listModel.removeElementAt(list.selectedIndex)
                projectTemplate.text = ""
            }
        }
        (name.document as AbstractDocument).addUpdateFunc { locationFieldUpdate() }
        browse_folder.addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(
                    false,
                    true,
                    false,
                    false,
                    false,
                    false
                )
            )
        )

        (browse_folder.textField.document as AbstractDocument).addUpdateFunc { locationFieldUpdate() }

        (projectTemplate.document as AbstractDocument).addUpdateFunc { templateFieldUpdate() }

        list.border = CustomRenderer()
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION

        list.addListSelectionListener(object : ListSelectionListener {
            override fun valueChanged(e: ListSelectionEvent?) {
                val selected_index = list.selectedIndex
                if (selected_index != -1) {
                    projectTemplate.text = listModel.elementAt(selected_index)
                }
            }
        })
//        Add elements to the panel
        panel.add(label("Name"), gb.nextLine().next().weightx(0.2))
        panel.add(name, gb.next().next().weightx(0.8))
        panel.add(label("Location"), gb.nextLine().next().weightx(0.2))
        panel.add(browse_folder, gb.next().next().weightx(0.8))
        panel.add(JLabel(""), gb.nextLine().next().weightx(0.2))
        panel.add(location_info_label, gb.next().next().weightx(0.2))
        panel.add(label("Project Template"), gb.nextLine().next().weightx(0.2))
        panel.add(projectTemplate, gb.next().next().weightx(0.8))
        panel.add(addBtn, gb.next().next().weightx(0.2))
        panel.add(JLabel(""), gb.nextLine().next().weightx(0.2))
        panel.add(JLabel(""), gb.next().next().weightx(0.2))
        panel.add(removeBtn, gb.next().next().weightx(0.2))
        panel.add(JLabel(""), gb.nextLine().next().weightx(0.2))
        panel.add(list, gb.next().next().weightx(0.8))

        return panel
    }

    private fun label(text: String): JComponent {
        val label = JBLabel(text)
        label.componentStyle = UIUtil.ComponentStyle.SMALL
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)
        return label
    }

    /**
     * Location field update
     *
     */
    fun locationFieldUpdate() {
        if (browse_folder.textField.text.isEmpty()) {
            location_info_label.text = "~/" + name.text
        } else {
            location_info_label.text = browse_folder.textField.text + "/" + name.text
        }
        m_ok.isEnabled = !name.text.isEmpty() && !browse_folder.text.isEmpty() && !projectTemplate.text.isEmpty()
    }

    /**
     * Template field update
     *
     */
    fun templateFieldUpdate() {
        m_ok.isEnabled = !name.text.isEmpty() && !browse_folder.text.isEmpty() && !projectTemplate.text.isEmpty()
        selectedTemplate = projectTemplate.text
    }
}

/**
 * Custom renderer
 *
 * @constructor Create empty Custom renderer
 */
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

/**
 * Information label
 *
 * @constructor
 *
 * @param text
 */
class InformationLabel(text: String) : JLabel(text) {
    init {
        font = font.deriveFont(Font.ITALIC)
    }

    override fun paintComponent(g: Graphics?) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = Color(0, 0, 0, 128)
        super.paintComponent(g)
    }
}