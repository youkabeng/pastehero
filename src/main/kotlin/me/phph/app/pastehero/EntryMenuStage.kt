package me.phph.app.pastehero

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage

object EntryMenuStage {

    val api = Paster

    private val vBox = VBox()
    val scene = Scene(vBox)
    val stage = Stage()

    val updated = SimpleIntegerProperty(0)

    init {
        // init scene
        stage.apply {
            title = "Context Menu"
            width = 200.0   // todo calculate by entry count
            height = 600.0
        }

        updated.bind(api.updated)
        updated.addListener { _, _, _ ->
            updateEntries()
        }
    }

    private fun updateEntries() {
        vBox.children.clear()
        for (entry in api.listEntries()) {
            vBox.children.add(Button(entry.value))
        }
    }

    fun setOwner(owner: Stage) {
        stage.initOwner(owner)
    }

    fun show() {
        Platform.runLater {
            stage.show()
        }
    }

    fun hide() {
        Platform.runLater {
            stage.hide()
        }
    }

}