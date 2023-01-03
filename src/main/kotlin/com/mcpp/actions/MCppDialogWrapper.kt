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


class MCppDialogWrapper : DialogWrapper(true) {
    val panel = JPanel(GridBagLayout())
    val name = JTextField()
    val browse_folder = TextFieldWithBrowseButton()
    val projectTemplate = JTextField()
    val addBtn = JButton("Add")
    var listModel = DefaultListModel<String>()
    val list = JBList(listModel)
    val location_info_label = InformationLabel("")
    var selectedTemplate: String? = null
    init {
        init()
        title = "Modern Cpp New Project"
        /*Get the persistent storage*/
        val mCppComponent = ServiceManager.getService(MCppComponent::class.java)
        mCppComponent.addValue("https://github.com/cpp-best-practices/cmake_conan_boilerplate_template.git")
        mCppComponent.addValue("https://github.com/filipdutescu/modern-cpp-template.git")
        for(element in mCppComponent.getState()?.values!!){
            listModel.addElement(element)
        }
        /*Change focus to the add button*/
        addBtn.isDefaultCapable = true
        projectTemplate.addActionListener{
            addBtn.requestFocusInWindow()
        }
    }

    override fun createActions(): Array<Action> {
        val actions = super.createActions()
        actions[0].putValue(Action.NAME, "Create")
        return actions
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
        name.document.addDocumentListener(object: DocumentListener{
            override fun insertUpdate(e: DocumentEvent?) {
                updateLocationField()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                updateLocationField()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                updateLocationField()
            }
            fun updateLocationField(){
                location_info_label.text  =  browse_folder.textField.text + "/" + name.text
            }
        })
        browse_folder.addBrowseFolderListener(TextBrowseFolderListener(FileChooserDescriptor(
            false,
            true,
            false,
            false,
            false,
            false
        )))

        browse_folder.textField.document.addDocumentListener(object: DocumentListener{
            override fun insertUpdate(e: DocumentEvent?) {
                updateLocationField()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                updateLocationField()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                updateLocationField()
            }
            fun updateLocationField(){
                location_info_label.text  =  browse_folder.textField.text + "/" + name.text
            }
        })

        projectTemplate.document.addDocumentListener(object: DocumentListener{
            override fun insertUpdate(e: DocumentEvent?) {
                updateSelectedTemplate()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                updateSelectedTemplate()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                updateSelectedTemplate()
            }
            fun updateSelectedTemplate(){
                selectedTemplate = projectTemplate.text
            }
        })

        list.border = CustomRenderer()
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION

        list.addListSelectionListener( object: ListSelectionListener {
            override fun valueChanged(e: ListSelectionEvent?) {
                val selectedValue = list.selectedValue as String
                projectTemplate.text = selectedValue
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
class InformationLabel(text: String): JLabel(text){
    init {
        font  = font.deriveFont(Font.ITALIC)
    }

    override fun paintComponent(g: Graphics?) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = Color(0, 0, 0, 128)
        super.paintComponent(g)
    }
}