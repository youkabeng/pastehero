package me.phph.app.pastehero

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.awt.MouseInfo
import java.util.*

object EntryMenuStage {

    private val api = Paster

    private val searchBox = TextField()

    private val entryBox = VBox()
    private val stage = Stage()

    private val updated = SimpleIntegerProperty(0)

    private val searchTimer = Timer()

    init {
        // init scene
        stage.apply {
            title = "Context Menu"
            width = 250.0   // todo calculate by entry count
            minWidth = 100.0
            maxWidth = 800.0
            height = 400.0
            minHeight = 200.0
            maxHeight = 800.0
            scene = createScene()
            initStyle(StageStyle.DECORATED)
            focusedProperty().addListener { _, _, newValue ->
                if (!newValue) {
                    toggleDisplay()
                }
            }
        }
        updated.bind(api.updated)
        updated.addListener { _, _, _ ->
            Platform.runLater {
                updateEntries()
            }
        }
    }

    private fun createScene(): Scene {
        searchBox.apply {
            id = "searchBox"
            promptText = "Search"
            onKeyPressed = EventHandler {
                if (it.code == KeyCode.ENTER) {
                    Platform.runLater {
                        updateEntries(it.source.let { it as TextField }.text.trim())
                    }
                }
            }
            textProperty().addListener { _, _, newValue ->
                searchTimer.schedule(object : TimerTask() {
                    override fun run() {
                        Platform.runLater {
                            updateEntries(newValue)
                        }
                    }
                }, 500)
            }
        }
        val vBoxTop = VBox(searchBox)
        val vBox = VBox(vBoxTop, entryBox)
        return Scene(vBox).apply {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ESCAPE) {
                    hide()
                }
            }
        }
    }

    private fun createEntryButton(entry: Entry): Button {
        return Button(entry.value).apply {
            userData = entry.id
            onAction = EventHandler { e ->
                api.setClipboardEntry(e.source.let { it as Button }.userData.let { it as Int })
                toggleDisplay()
            }
            maxHeight = 50.0
            maxWidth = 800.0

            tooltip = Tooltip(entry.value)

            // todo NoSuchMethodException setShowDelay()
//            tooltip = Tooltip(entry.value).apply {
//                showDelay = Duration.millis(50.0)
//                showDuration = Duration.minutes(1.0)
//            }
        }
    }

    private fun updateEntries(searchStr: String = "") {
        entryBox.children.clear()
        for (entry in api.listEntries()) {
            if (searchStr != "" && !entry.value.contains(searchStr)) {
                continue
            }
            entryBox.children.add(createEntryButton(entry))
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