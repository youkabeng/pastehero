@file:JvmName("Main")

package me.phph.app.pastehero

import javafx.application.Application
import javafx.application.Platform


fun isWindows(): Boolean {
    return System.getProperty("os.name").toLowerCase().contains("windows")
}

fun isLinux(): Boolean {
    return System.getProperty("os.name").toLowerCase().contains("linux")
}

fun main() {
    Platform.setImplicitExit(false)
    Application.launch(App::class.java)
}