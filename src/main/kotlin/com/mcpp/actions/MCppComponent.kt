package com.mcpp.actions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "MCppComponent",
    storages = [Storage("MCpp_component.xml")]
)
class MCppComponent: PersistentStateComponent<MCppComponent.MyState> {
    data class MyState(var values:  MutableSet<String> = mutableSetOf())
    private var myState = MyState()

    override fun getState(): MyState {
        return myState
    }

    override fun loadState(state: MyState) {
        myState = state
    }

    fun addValue(value: String){
        myState.values.add(value)
    }

    fun getValue(i: Int): String {
        return myState.values.elementAt(i)
    }
    fun removeValue(value: String)
    {
        myState.values.remove(value)
    }
}