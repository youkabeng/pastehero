@file:JvmName("App")

package me.phph.app.pastehero

import javafx.application.Application
import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.jnativehook.GlobalScreen
import java.awt.MouseInfo

class AppGui : Application() {

    val triggered = SimpleDoubleProperty(0.0)

    private val native = Native
    private val paster = Paster

    private var primaryStage: Stage? = null
    private var entryStage: Stage? = null

    private val entryVBox: VBox = VBox()
    private val showing: Boolean
        get() = entryStage?.isShowing ?: false

    private fun initEntryStage(): Stage {
        val stage = Stage()
        updateEntryDisplay()
        val scene = Scene(entryVBox)
        return stage.apply {
            title = "Context Menu"
            this.scene = scene
            initOwner(primaryStage)
        }
    }

    private fun updateEntryDisplay() {
        entryVBox.children.clear()
        for (entry in paster.listEntries()) {
            entryVBox.children.add(Button(entry.value))
        }
    }

    private fun showEntryList(mouseX: Int, mouseY: Int) {
        if (entryStage == null) {
            entryStage = initEntryStage().apply {
                x = mouseX * 1.0
                y = mouseY * 1.0
                onCloseRequest = EventHandler {
                    println("here")
                    hide()
                }
            }
        } else {
            updateEntryDisplay()
        }
        entryStage?.show()
    }

    override fun start(primaryStage: Stage?) {
        native.gui = this
        this.primaryStage = primaryStage

//        triggered.addListener { _, _, newVal ->
//            if (newVal == 1.0) {
//                if (!showing) {
//                    val pos = MouseInfo.getPointerInfo().location
//                    showEntryList(pos.x, pos.y)
//                }
//            }
//        }

        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(Native)

//        val root = Group()
//        val scene = Scene(root, 300.0, 200.0)
//        scene.onMouseMoved = EventHandler {
//            println("mouseX = ${it.x} mouseY = ${it.y} screenX = ${it.screenX} screenY = ${it.screenY}")
//        }

//        var i = 10
//        while (i > 0) {
//            println("mouseX = ${MouseInfo.getPointerInfo().location.x} mouseY = ${MouseInfo.getPointerInfo().location.y}")
//            Thread.sleep(1_000)
//            i--
//        }
//        val shortcutTimer = Timer()
//        shortcutTimer.schedule(object : TimerTask() {
//            override fun run() {
//                if (triggered.value == 1.0 && !showing) {
//                    val pos = MouseInfo.getPointerInfo().location
//                    showEntryList(pos.x, pos.y)
//                    triggered.value = 0.0
//                }
//                println("check")
//            }
//        }, 0, 50)

        while (true) {
            if (triggered.value == 1.0) {
                if (!showing) {
                    val pos = MouseInfo.getPointerInfo().location
                    showEntryList(pos.x, pos.y)
                    triggered.value = 0.0
                }
            }
//            if (entryStage?.isFocused != true) {
//                Platform.runLater { entryStage?.hide() }
//            }
            Thread.sleep(50)
        }

//        primaryStage?.apply {
//            title = "Clipboard Test"
//            this.scene = scene
//            show()
//        }
    }
}

fun main() {
    Application.launch(AppGui::class.java)
}