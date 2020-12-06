@file:JvmName("Main")

package me.phph.app.pastehero

import javafx.application.Application
import javafx.application.Platform
import me.phph.app.pastehero.gui.App


fun main() {
    Platform.setImplicitExit(false)
    Application.launch(App::class.java)
}