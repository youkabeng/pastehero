package me.phph.app.pastehero.thirdparty

import me.phph.app.pastehero.App
import me.phph.app.pastehero.api.Storage
import java.awt.*
import kotlin.system.exitProcess

fun initSystemTray() {
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

private fun createImage(): Image {
    return Toolkit.getDefaultToolkit().createImage(App::class.java.getResource("/images/icon.png"))
}
