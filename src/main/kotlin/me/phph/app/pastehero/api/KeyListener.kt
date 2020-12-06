package me.phph.app.pastehero.api

import javafx.beans.property.SimpleIntegerProperty
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener

object KeyListener : NativeKeyListener {
    val triggered = SimpleIntegerProperty(0)
    private val configuration = Configuration

    private val triggeredKeys = mutableSetOf<String>()

    private fun isTriggered(): Boolean {
        return triggeredKeys.size == configuration.triggerKeys.size && triggeredKeys.containsAll(configuration.triggerKeys)
    }

    override fun nativeKeyTyped(p0: NativeKeyEvent?) {
    }

    override fun nativeKeyPressed(p0: NativeKeyEvent?) {
        val keyText = NativeKeyEvent.getKeyText(p0!!.keyCode).toLowerCase()
        triggeredKeys.add(keyText.toLowerCase());
        if (isTriggered()) {
            triggered.value += 1
        }
    }

    override fun nativeKeyReleased(p0: NativeKeyEvent?) {
        val keyText = NativeKeyEvent.getKeyText(p0!!.keyCode).toLowerCase()
        triggeredKeys.remove(keyText)
        triggered.value += 1
    }
}