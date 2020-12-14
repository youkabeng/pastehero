package me.phph.app.pastehero.gui

import com.tulskiy.keymaster.common.Provider
import javafx.application.Application
import javafx.beans.property.SimpleIntegerProperty
import javafx.stage.Stage
import me.phph.app.pastehero.api.Storage
import java.awt.*
import javax.swing.KeyStroke
import kotlin.system.exitProcess

class App : Application() {

    private val triggered = SimpleIntegerProperty(0)

    private var appStage: Stage? = null
    private var selectionMenuWindow: SelectionMenuWindow? = null

    override fun start(primaryStage: Stage?) {
        primaryStage?.let {
            appStage = primaryStage
            selectionMenuWindow = SelectionMenuWindow(primaryStage)
            initSystemTray()
            initBindings()
            registerGlobalKeys()
        }
    }

    private fun registerGlobalKeys() {
        val provider = Provider.getCurrentProvider(false)
        provider.register(KeyStroke.getKeyStroke("control shift V"), { selectionMenuWindow?.show() })
    }

    private fun initBindings() {
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

}
