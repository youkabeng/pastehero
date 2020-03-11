package me.phph.app.pastehero

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.awt.MouseInfo

object EntryMenuStage {

    private val api = Paster

    private val vBox = VBox()
    private val stage = Stage()

    private val updated = SimpleIntegerProperty(0)

    init {
        // init scene
        stage.apply {
            title = "Context Menu"
            width = 300.0   // todo calculate by entry count
            height = 600.0
            scene = Scene(vBox)
            initStyle(StageStyle.UNDECORATED)
        }

        stage.focusedProperty().addListener { _, _, newValue ->
            if (!newValue) {
                toggleDisplay()
            }
        }

        updated.bind(api.updated)
        updated.addListener { _, _, _ ->
            Platform.runLater {
                updateEntries()
            }
        }
    }

    private fun updateEntries() {
        vBox.children.clear()
        for (entry in api.listEntries()) {
            vBox.children.add(Button(entry.value).apply {
                userData = entry.id
                onAction = EventHandler { e ->
                    api.setClipboardEntry(e.source.let { it as Button }.userData.let { it as Int })
                    toggleDisplay()
                }
            })
        }
    }

    fun setOwner(owner: Stage) {
        stage.initOwner(owner)
    }

    fun toggleDisplay() {
        if (stage.isShowing) {
            hide()
        } else {
            show()
        }
    }

    private fun show() {
        Platform.runLater {
            val location = MouseInfo.getPointerInfo().location
            stage.x = location.x * 1.0
            stage.y = location.y * 1.0
            stage.show()
        }
    }

    private fun hide() {
        Platform.runLater {
            stage.hide()
        }
    }

}