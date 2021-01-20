@file:JvmName("Main")

package me.phph.app.pastehero

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.stage.Stage
import me.phph.app.pastehero.thirdparty.initSystemTray
import me.phph.app.pastehero.thirdparty.registerGlobalKeys
import me.phph.app.pastehero.view.ViewFactory

class App : Application() {
    private val triggered = SimpleIntegerProperty(0)

    override fun start(primaryStage: Stage?) {
        ViewFactory.initMainWindow()
        initSystemTray()
        initBindings()
        registerGlobalKeys()
    }

    private fun initBindings() {
        triggered.addListener { _, _, _ ->
            Platform.runLater {
                ViewFactory.showMainWindow()
            }
        }
    }
}

fun main() {
    Platform.setImplicitExit(false)
    Application.launch(App::class.java)
}