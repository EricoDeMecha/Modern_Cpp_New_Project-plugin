package com.mcpp.actions

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * M cpp component
 *
 * @constructor Create empty M cpp component
 */
@State(
    name = "MCppComponent",
    storages = [Storage("MCpp_component.xml")]
)
class MCppComponent: PersistentStateComponent<MCppComponent.MyState> {
    /**
     * My state
     *
     * @property values
     * @constructor Create empty My state
     */
    data class MyState(var values:  MutableSet<String> = mutableSetOf())
    private var myState = MyState()

    override fun getState(): MyState {
        return myState
    }

    override fun loadState(state: MyState) {
        myState = state
    }

    /**
     * Add value
     *
     * @param value
     */
    fun addValue(value: String){
        myState.values.add(value)
    }

    /**
     * Get value
     *
     * @param i
     * @return
     */
    fun getValue(i: Int): String {
        return myState.values.elementAt(i)
    }

    /**
     * Remove value
     *
     * @param value
     */
    fun removeValue(value: String)
    {
        myState.values.remove(value)
    }

    /**
     * Get last
     *
     * @return
     */
    fun getLast(): String {
        return myState.values.last()
    }
}