package me.phph.app.pastehero.gui

import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.SystemTray
import javafx.application.Application
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.Stage
import me.phph.app.pastehero.api.KeyListener
import org.jnativehook.GlobalScreen
import java.awt.event.ActionListener
import kotlin.system.exitProcess

class JFXApp : Application() {

    private val triggered = SimpleBooleanProperty(false)

    private val native = KeyListener

    private var primaryStage: Stage? = null
    private var entryMenuStage: EntryMenuStage? = null

    override fun start(primaryStage: Stage?) {
        this.primaryStage = primaryStage!!
        entryMenuStage = EntryMenuStage(this.primaryStage!!)

        registerNativeHook()
        initSystemTray()

        initBindings()
    }

    private fun initBindings() {
        triggered.bind(native.triggered)
        triggered.addListener { _, _, newValue ->
            if (newValue) {
                entryMenuStage?.show()
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
            menu.add(MenuItem("Exit", ActionListener {
                exitProcess(0)
            }))
        } ?: print("your system is not support system tray")
    }

    private fun registerNativeHook() {
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(KeyListener)
    }
}