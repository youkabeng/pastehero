package me.phph.app.pastehero.dbus

import org.freedesktop.dbus.interfaces.DBusInterface

interface DBus : DBusInterface {
    fun show()

    override fun isRemote(): Boolean {
        return false
    }

    override fun getObjectPath(): String {
        return "/pastehero"
    }
}