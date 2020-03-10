@file:JvmName("App")

package me.phph.app.pastehero

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.jnativehook.GlobalScreen
import java.awt.MouseInfo

class AppGui : Application() {

    private val triggered = SimpleBooleanProperty(false)

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

    private fun hideEntryList() {
        Platform.runLater {
            entryStage?.hide()
        }
    }

    private fun showEntryList() {
        val pos = MouseInfo.getPointerInfo().location
        if (entryStage == null) {
            entryStage = initEntryStage().apply {
                x = pos.x * 1.0
                y = pos.y * 1.0
                onCloseRequest = EventHandler {
                    hideEntryList()
                }
            }
        } else {
            updateEntryDisplay()
        }
        Platform.runLater {
            entryStage?.show()
        }
    }

    private fun bindings() {
        triggered.bind(native.triggered)
        triggered.addListener { _, _, newValue ->
            if (newValue) {
                if (!showing) {
                    Platform.runLater {
                        showEntryList()
                    }
                } else {
                    Platform.runLater {
                        hideEntryList()
                    }
                }
            }
        }
    }

    override fun start(primaryStage: Stage?) {
        this.primaryStage = primaryStage
        bindings()

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
//        GlobalScope.launch {
//            while (true) {
//                if (triggered.value == 1.0) {
//                    if (!showing) {
//                        val pos = MouseInfo.getPointerInfo().location
//                        showEntryList(pos.x, pos.y)
//                        triggered.value = 0.0
//                    }
//                }
////            if (entryStage?.isFocused != true) {
////                Platform.runLater { entryStage?.hide() }
////            }
//                delay(10)
//            }
//        }
//        print("here2")


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