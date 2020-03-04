package me.phph.app.pastehero

import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener

object Native : NativeKeyListener {

    var gui: AppGui? = null

    var isCtrlPressed = false
    var isShiftPressed = false
    var isAltPressed = false
    var isSuperPressed = false

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
        if (isCtrlPressed && isAltPressed && p0.keyCode == 0x2F) {
            gui?.triggered?.value = 1.0
        }
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
    }
}