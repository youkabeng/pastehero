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
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import me.phph.app.pastehero.api.ClipboardApi
import me.phph.app.pastehero.api.Configuration
import me.phph.app.pastehero.api.Entry
import me.phph.app.pastehero.api.EntryType
import java.awt.MouseInfo
import java.awt.image.BufferedImage
import java.util.*


class EntryMenuStage(var stage: Stage) {

    private val searchBox = TextField()

    private val entryBox = VBox()

    private val updated = SimpleIntegerProperty(0)

    private val searchTimer = Timer()

    // settings
    private val countPerPage: Int = Configuration.getConfigurationInt(Configuration.CONF_COUNT_PER_PAGE)
    private var pageNumber = 1

    init {
        // init scene
        stage.apply {
            title = "Context Menu"
            width = 500.0   // todo calculate by entry count
            minWidth = 100.0
            maxWidth = 800.0
            height = 800.0
            minHeight = 200.0
            maxHeight = 800.0
            scene = createScene()
            initStyle(StageStyle.UTILITY)
//            initModality(Modality.WINDOW_MODAL)
            focusedProperty().addListener { _, _, newValue ->
                if (newValue)
                    show()
                else
                    hide()
            }
            // todo find a better solution
            maximizedProperty().addListener { _, _, newValue ->
                if (newValue)
                    isMaximized = false
            }
            onCloseRequest = EventHandler {
                hide()
            }
            isAlwaysOnTop = true
        }
        updated.bind(ClipboardApi.updated)
        updated.addListener { _, _, _ ->
            updateDisplay()
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
//                val image = convertToFxImage(entry.image)
                val imageView = ImageView(image)
                imageView.fitHeight = 100.0
                imageView.isPreserveRatio = true
                graphic = imageView
                maxHeight = 300.0
            }
            maxWidth = 800.0
            alignment = Pos.CENTER_LEFT
            userData = entry.md5Digest
            onAction = EventHandler { e ->
                ClipboardApi.setClipboard(e.source.let { it as Button }.userData.let { it as String })
                hide()
            }
            tooltip = Tooltip(entry.value).apply {
                showDelay = Duration.millis(50.0)
                showDuration = Duration.minutes(1.0)
            }
        }
    }

    private fun convertToFxImage(image: BufferedImage): Image {
        val wr = WritableImage(image.width, image.height)
        val pw = wr.pixelWriter
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                pw.setArgb(x, y, image.getRGB(x, y))
            }
        }
        return wr
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
        for (entry in ClipboardApi.listEntries(searchString)) {
            if (searchString != "" && !entry.value.contains(searchString)) {
                continue
            }
            entryBox.children.add(createEntryButton(entry))
        }
    }

    fun setOwner(owner: Stage) {
        stage.initOwner(owner)
    }

    fun show() {
        Platform.runLater {
            val location = MouseInfo.getPointerInfo().location
            stage.x = location.x * 1.0
            stage.y = location.y * 1.0
            stage.requestFocus()
            stage.show()
            stage.isIconified = true
            stage.isIconified = false
            searchBox.requestFocus()
        }
    }

    private fun hide() {
        Platform.runLater {
            stage.hide()
        }
    }

}