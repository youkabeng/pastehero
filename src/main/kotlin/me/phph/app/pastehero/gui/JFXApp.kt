package me.phph.app.pastehero.gui

import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.SystemTray
import javafx.application.Application
import javafx.beans.property.SimpleIntegerProperty
import javafx.stage.Stage
import me.phph.app.pastehero.api.KeyListener
import me.phph.app.pastehero.dbus.DBusImpl
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.jnativehook.GlobalScreen
import java.awt.event.ActionListener
import kotlin.system.exitProcess

class JFXApp : Application() {

    private val triggered = SimpleIntegerProperty(0)

    private val nativeKeyListener = KeyListener

    private val dbus = DBusImpl()

    private var primaryStage: Stage? = null
    private var mainPopupWindow: MainPopupWindow? = null

    override fun start(primaryStage: Stage?) {
        this.primaryStage = primaryStage!!
        mainPopupWindow = MainPopupWindow(this.primaryStage!!)

//        registerNativeHook()

        initSystemTray()

        initBindings()

        initDBus()
    }

    private fun initBindings() {
//        triggered.bind(native.triggered)
        triggered.bind(dbus.triggered)
        triggered.addListener { _, _, _ ->
            mainPopupWindow?.show()
        }
    }

    private fun initSystemTray() {
//        SystemTray.DEBUG = true
        SystemTray.get().apply {
            status = "Paste Hero"
            setTooltip(status)
            setImage(JFXApp::class.java.getResource("/images/icon.png"))
//            menu.add(MenuItem("Settings", ActionListener { print("show setting test") }))
            menu.add(MenuItem("Exit", ActionListener {
                exitProcess(0)
            }))
        } ?: print("your system is not support system tray")
    }

    private fun registerNativeHook() {
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(KeyListener)
    }

    private fun initDBus() {
        var conn: DBusConnection? = null
        try {
            conn = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION)
            conn?.run {
                requestBusName("me.phph.app.pastehero")
                exportObject("/pastehero", dbus)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            conn?.disconnect()
        }
    }
}
