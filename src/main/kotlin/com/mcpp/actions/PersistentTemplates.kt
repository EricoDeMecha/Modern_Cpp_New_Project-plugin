package com.mcpp.actions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "MCpp.persistent",
    storages = [Storage("mcpp.persistence.xml")]
)
class PersistentTemplates : PersistentStateComponent<PersistentTemplatesState> {

    var persistentTemplateState = PersistentTemplatesState()

    override fun getState(): PersistentTemplatesState? {
        return persistentTemplateState
    }

    override fun loadState(state: PersistentTemplatesState) {
        persistentTemplateState = state
    }
    companion object {
        @JvmStatic
        fun getInstance(): PersistentStateComponent<PersistentTemplatesState>{
            return ServiceManager.getService(PersistentTemplates::class.java)
        }
    }
}