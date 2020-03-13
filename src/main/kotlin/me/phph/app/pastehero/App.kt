@file:JvmName("App")

package me.phph.app.pastehero

import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.SystemTray
import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.Stage
import org.jnativehook.GlobalScreen
import java.awt.event.ActionListener
import kotlin.system.exitProcess

class PasteHero : Application() {

    private val triggered = SimpleBooleanProperty(false)

    private val native = Native

    private var primaryStage: Stage? = null
    private val entryMenuStage = EntryMenuStage


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

        Platform.setImplicitExit(false)

        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(Native)


        SystemTray.DEBUG = true
        SystemTray.get().apply {
            status = "Paste Hero"
            setTooltip(status)
            setImage(PasteHero::class.java.getResource("/images/icon.png"))
            menu.add(MenuItem("Settings", ActionListener { print("show setting test") }))
            menu.add(MenuItem("Exit", ActionListener { exitProcess(0) }))
        } ?: print("your system is not support system tray")
    }
}

fun main() {
    Application.launch(PasteHero::class.java)
}