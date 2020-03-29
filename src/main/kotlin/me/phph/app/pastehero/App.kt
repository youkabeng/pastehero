@file:JvmName("App")

package me.phph.app.pastehero

import javafx.application.Application
import javafx.application.Platform
import me.phph.app.pastehero.gui.JFXApp


fun main() {
    Platform.setImplicitExit(false)
    Application.launch(JFXApp::class.java)
}