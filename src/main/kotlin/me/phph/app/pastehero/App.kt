@file:JvmName("App")

package me.phph.app.pastehero

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.Stage
import org.jnativehook.GlobalScreen

class AppGui : Application() {

    private val triggered = SimpleBooleanProperty(false)

    private val native = Native

    private var primaryStage: Stage? = null
    private var entryMenuStage = EntryMenuStage


    private fun bindings() {
        triggered.bind(native.triggered)
        triggered.addListener { _, _, newValue ->
            if (newValue) {
                entryMenuStage.toggleDisplay()
            }
        }
    }

    override fun start(primaryStage: Stage?) {
        entryMenuStage.setOwner(primaryStage!!);

        bindings()

        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(Native)
    }
}

fun main() {
    Platform.setImplicitExit(false)
    Application.launch(AppGui::class.java)
}