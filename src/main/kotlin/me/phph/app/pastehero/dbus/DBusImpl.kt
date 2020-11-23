package me.phph.app.pastehero.dbus

import javafx.beans.property.SimpleIntegerProperty

class DBusImpl : DBus {
    val triggered = SimpleIntegerProperty(0)

    override fun show() {
        triggered.value += 1
    }
}