package me.phph.app.pastehero.gui

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import javafx.stage.StageStyle
import me.phph.app.pastehero.api.Configuration
import me.phph.app.pastehero.api.Entry
import me.phph.app.pastehero.api.EntryType
import me.phph.app.pastehero.api.PasteHero
import java.awt.MouseInfo
import java.util.*

object EntryMenuStage {

    private val searchBox = TextField()

    private val entryBox = VBox()
    private val stage = Stage()

    private val updated = SimpleIntegerProperty(0)

    private val searchTimer = Timer()

    // settings
    private val countPerPage: Int = Configuration.getConfigurationInt(Configuration.CONF_COUNT_PER_PAGE)
    private var pageNumber = 1

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
        updated.bind(PasteHero.updated)
        updated.addListener { _, _, _ ->
            Platform.runLater {
                updateDisplay()
            }
        }
        // retrieve latest entries
        updateDisplay()
    }

    private fun createScene(): Scene {
        searchBox.apply {
            id = "searchBox"
            promptText = "Search"
            onKeyPressed = EventHandler {
                if (it.code == KeyCode.ENTER) {
                    Platform.runLater {
                        updateDisplay(it.source.let { it as TextField }.text.trim())
                    }
                }
            }
            textProperty().addListener { _, _, newValue ->
                searchTimer.schedule(object : TimerTask() {
                    override fun run() {
                        Platform.runLater {
                            updateDisplay(newValue)
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
        return Button().apply {
            if (entry.type == EntryType.STRING) {
                text = createEntryAbstract(entry.value)
                textAlignment = TextAlignment.LEFT
                tooltip = Tooltip(entry.value)
                maxHeight = 100.0
            } else if (entry.type == EntryType.IMAGE) {
                val image = SwingFXUtils.toFXImage(entry.image!!, null)
                val imageView = ImageView(image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true
                graphic = imageView
                maxHeight = 200.0
            }
            maxWidth = 800.0
            alignment = Pos.CENTER_LEFT
            userData = entry.id
            onAction = EventHandler { e ->
                PasteHero.setClipboard(e.source.let { it as Button }.userData.let { it as Int })
                toggleDisplay()
            }

            // todo NoSuchMethodException setShowDelay()
//            tooltip = Tooltip(entry.value).apply {
//                showDelay = Duration.millis(50.0)
//                showDuration = Duration.minutes(1.0)
//            }
        }
    }

    private fun createEntryAbstract(value: String): String {
        val builder = StringBuilder()
        val lines = value.split("\n")
        for (line in lines) {
            builder.append(line.trim()).append(" ")
        }
        return builder.toString()
    }

    private fun updateDisplay(searchString: String = "") {
        entryBox.children.clear()
        for (entry in PasteHero.listEntries(searchString)) {
            if (searchString != "" && !entry.value.contains(searchString)) {
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