package com.mcpp.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class CreateProjectAction : AnAction(){
    override fun actionPerformed(e: AnActionEvent) {
        if(MCppDialogWrapper().showAndGet()){
            Messages.showMessageDialog("User pressed Ok", "Res", Messages.getInformationIcon())
        }
    }
}