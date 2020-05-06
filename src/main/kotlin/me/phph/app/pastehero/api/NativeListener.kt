package me.phph.app.pastehero.api

import javafx.beans.property.SimpleBooleanProperty
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener

object Native : NativeKeyListener {

    val triggered = SimpleBooleanProperty(false)

    private var isCtrlPressed = false
    private var isShiftPressed = false
    private var isAltPressed = false
    private var isSuperPressed = false

    override fun nativeKeyTyped(p0: NativeKeyEvent?) {
    }

    override fun nativeKeyPressed(p0: NativeKeyEvent?) {
        when (p0!!.keyCode) {
            0x1D -> {
                isCtrlPressed = true
            }
            0x38 -> {
                isAltPressed = true
            }
            0x2A -> {
                isShiftPressed = true
            }
            0XE5B -> {
                isSuperPressed = true
            }
        }
        triggered.value = isCtrlPressed && isAltPressed && p0.keyCode == 0x2F
//        triggered.value = isCtrlPressed
    }

    override fun nativeKeyReleased(p0: NativeKeyEvent?) {
        when (p0!!.keyCode) {
            0x1D -> {
                isCtrlPressed = false
            }
            0x38 -> {
                isAltPressed = false
            }
            0x2A -> {
                isShiftPressed = false
            }
            0XE5B -> {
                isSuperPressed = false
            }
        }
        triggered.value = false
    }
}