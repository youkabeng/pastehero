package me.phph.app.pastehero.gui

import com.sun.javafx.PlatformUtil
import javafx.application.Application
import javafx.beans.property.SimpleIntegerProperty
import javafx.stage.Stage
import me.phph.app.pastehero.api.KeyListener
import me.phph.app.pastehero.api.Storage
import me.phph.app.pastehero.dbus.DBusImpl
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.exceptions.DBusException
import org.jnativehook.GlobalScreen
import java.awt.*
import kotlin.system.exitProcess

class App : Application() {

    private val triggered = SimpleIntegerProperty(0)

    private val nativeKeyListener = KeyListener

    private val dbus = DBusImpl()

    private var appStage: Stage? = null
    private var selectionMenuWindow: SelectionMenuWindow? = null

    override fun start(primaryStage: Stage?) {
        primaryStage?.let {
            appStage = primaryStage
            selectionMenuWindow = SelectionMenuWindow(primaryStage)
            initSystemTray()
            initBindings()
        }
    }

    private fun initBindings() {
        if (PlatformUtil.isLinux()) {
            try {
                initDBus()
            } catch (e: DBusException) {
                registerNativeHook()
            }
        } else {
            registerNativeHook()
        }
        triggered.addListener { _, _, _ ->
            selectionMenuWindow?.show()
        }
    }

    private fun createImage(): Image {
        return Toolkit.getDefaultToolkit().createImage(App::class.java.getResource("/images/icon.png"))
    }

    private fun initSystemTray() {
        if (SystemTray.isSupported()) {
            val systemTray = SystemTray.getSystemTray()
            val popup = PopupMenu()
            popup.add(MenuItem("Quit").apply {
                addActionListener {
                    Storage.cleanup()
                    exitProcess(0)
                }
            })
            val trayIcon = TrayIcon(createImage())
            trayIcon.popupMenu = popup
            trayIcon.isImageAutoSize = true
            systemTray.add(trayIcon)
        }

//        SystemTray.get().apply {
//            status = "Paste Hero"
//            setTooltip(status)
//            setImage(App::class.java.getResource("/images/icon.png"))
////            menu.add(MenuItem("Settings", ActionListener { print("show setting test") }))
//            menu.add(MenuItem("Quit", ActionListener {
//                exitProcess(0)
//            }))
//        } ?: print("your system is not support system tray")
    }

    private fun registerNativeHook() {
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(KeyListener)
        triggered.bind(nativeKeyListener.triggered)
    }

    private fun initDBus() {
        DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION)?.run {
            requestBusName("me.phph.app.pastehero")
            exportObject("/pastehero", dbus)
        }
        triggered.bind(dbus.triggered);
    }
}
