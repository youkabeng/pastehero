package me.phph.app.pastehero.gui

import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.SystemTray
import javafx.application.Application
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.Stage
import me.phph.app.pastehero.api.Native
import org.jnativehook.GlobalScreen
import java.awt.event.ActionListener
import kotlin.system.exitProcess

class JFXApp : Application() {

    private val triggered = SimpleBooleanProperty(false)

    private val native = Native

    private var primaryStage: Stage? = null
    private val entryMenuStage = EntryMenuStage

    override fun start(primaryStage: Stage?) {
        this.primaryStage = primaryStage!!

        registerNativeHook()
        initSystemTray()

        initEntryMenuStage()
        initBindings()
    }

    private fun initEntryMenuStage() {
        entryMenuStage.setOwner(primaryStage!!);
    }

    private fun initBindings() {
        triggered.bind(native.triggered)
        triggered.addListener { _, _, newValue ->
            if (newValue) {
                entryMenuStage.toggleDisplay()
            }
        }
    }

    private fun initSystemTray() {
        SystemTray.DEBUG = true
        SystemTray.get().apply {
            status = "Paste Hero"
            setTooltip(status)
            setImage(JFXApp::class.java.getResource("/images/icon.png"))
            menu.add(MenuItem("Settings", ActionListener { print("show setting test") }))
            menu.add(MenuItem("Exit", ActionListener { exitProcess(0) }))
        } ?: print("your system is not support system tray")
    }

    private fun registerNativeHook() {
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(Native)
    }
}