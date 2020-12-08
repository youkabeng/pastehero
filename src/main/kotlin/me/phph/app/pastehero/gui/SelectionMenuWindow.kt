package me.phph.app.pastehero.gui

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.util.Duration
import me.phph.app.pastehero.api.*
import java.awt.MouseInfo
import java.util.*

class SelectionMenuWindow(var stage: Stage) {

    private var mainScene: Scene?
    private var topVBox = VBox()
    private var upperVBox = VBox()
    private var lowerVBox = VBox()

    private var searchTextField = TextField()
    private var selectionVBox = VBox()
    private var selectionScrollPane = ScrollPane()

    private val updated = SimpleIntegerProperty(0)
    private val searchTimer = Timer()

    private val countPerPage: Int = Configuration.getConfigurationInt(Configuration.CONF_COUNT_PER_PAGE)

    private var lastWidth = 0.0
    private var lastHeight = 0.0

    init {
        // settings for search text field
        searchTextField.apply {
            promptText = "Search"
            onKeyPressed = EventHandler { ev ->
                if (ev.code == KeyCode.ENTER) {
                    Platform.runLater {
                        updateDisplay(ev.source.let { it as TextField }.text.trim())
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

        // settings for upper vbox
        upperVBox.apply {
            children.add(searchTextField)
        }

        // settings for selection vbox and scroll pane
        selectionScrollPane.apply {
            content = selectionVBox
            isFitToWidth = true
            isFitToHeight
        }

        // settings for lower vbox
        lowerVBox.apply {
            children.add(selectionScrollPane)
        }

        // bind selectionScrollPane's width and height to lowerVBox
        selectionScrollPane.apply {
            prefWidthProperty().bind(lowerVBox.widthProperty())
            prefHeightProperty().bind(lowerVBox.heightProperty())
        }
        VBox.setVgrow(selectionVBox, Priority.ALWAYS)

        // settings for top vbox
        topVBox.apply {
            children.add(upperVBox)
            children.add(lowerVBox)
            prefWidthProperty().bind(stage.widthProperty())
            prefHeightProperty().bind(stage.heightProperty())
        }
        VBox.setVgrow(lowerVBox, Priority.ALWAYS)

        mainScene = Scene(topVBox).apply {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ESCAPE) {
                    hide()
                    searchTextField.text = ""
                    updateDisplay()
                } else if (it.isAltDown && it.code.isDigitKey) {
                    val children = selectionVBox.children
                    var index = it.code.code - '0'.toInt() - 1
                    if (index == -1) {
                        index = 9
                    }
                    if (index < children.size) {
                        pickEntry(children[index].userData as String)
                    }
                }
            }
            loadStylesheet("css/TextFlow.css")?.let(stylesheets::add)
            loadStylesheet("css/VBox.css")?.let(stylesheets::add)
        }

        // settings for stage
        stage.apply {
            title = "Clipboard Selection Menu"
            width = 500.0
            height = 800.0
            minWidth = 100.0
            minHeight = 100.0
            scene = mainScene
            initStyle(StageStyle.UTILITY)
            focusedProperty().addListener { _, _, newValue ->
                if (newValue) show() else hide()
            }
            maximizedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    lastWidth = width
                    lastHeight = height
                }
            }
            onCloseRequest = EventHandler {
                hide()
            }
            isAlwaysOnTop = true
        }
        lastWidth = stage.width
        lastHeight = stage.height

        // bind to clipboard change
        updated.bind(ClipboardApi.updated)
        updated.addListener { _, _, _ ->
            updateDisplay()
        }
        // retrieve latest entries
        updateDisplay()
    }

    private fun updateDisplay(searchString: String = "") {
        selectionVBox.apply {
            children.clear()
            var count = 0
            val entries = ClipboardApi.listEntries(searchString)
            val size = entries.size
            for (entry in entries) {
//                val item = createEntryTextFlow(entry, searchString, count)
                val item = createEntry(entry, searchString, count)
                children.add(item)
                count++
                if (count == countPerPage || count == size)
                    break
            }
        }
    }

    private fun createEntry(entry: Entry, searchString: String, order: Int): StackPane {
        return StackPane().apply {
            val textFlow = createEntryTextFlow(entry, searchString, order)
            children.add(textFlow)
            children.add(HBox().apply {
                // show variants icon
                children.add(Button("TS").apply { prefWidth = 20.0;prefHeight = 20.0;isFocusTraversable = false })
                children.add(Button("TXT").apply { prefWidth = 20.0;prefHeight = 20.0;isFocusTraversable = false })
                children.add(Button("JSON").apply { prefWidth = 20.0; prefHeight = 20.0;isFocusTraversable = false })
                // show delete icon
                children.add(Button("Delete").apply { prefWidth = 20.0; prefHeight = 20.0;isFocusTraversable = false })
                alignment = Pos.BOTTOM_RIGHT
                isPickOnBounds = false
//                visibleProperty().bind(textFlow.focusedProperty())
            })
        }
    }

    private fun createEntryTextFlow(entry: Entry, searchString: String, order: Int): TextFlow {
        val text = entry.value
        val type = entry.type
        val md5Digest = entry.md5Digest

        return TextFlow().apply {
            if (order < 10) {
                var v = order + 1
                if (v == 10) {
                    v = 0
                }
                children.addAll(
                    Text("["),
                    Text("$v").apply { fill = Color.RED; style = "-fx-underline: true" },
                    Text("]"),
                    Text("[$type] ")
                )
            }
            when (type) {
                EntryType.STRING,
                EntryType.FILES,
                EntryType.URL,
                EntryType.RTF,
                EntryType.JSON,
                EntryType.HTML,
                EntryType.XML -> {
                    val index =
                        if (searchString.isEmpty()) -1 else text.toLowerCase().indexOf(searchString.toLowerCase())
                    if (index >= 0) {
                        val t = Text(text.substring(0, index))
                        if (t.layoutBounds.width > stage.width) {
                            // too long
                            // need to make summary
                            children.add(Text("..."))
                        } else {
                            children.add(Text(text.substring(0, index)))
                        }
                        children.add(Text(text.substring(index, index + searchString.length)).apply {
                            fill = Color.RED
                        })
                    }
                    val lengthLimit = 200
                    if (index == -1) {
                        if (text.length > lengthLimit) {
                            children.add(Text(text.substring(0, lengthLimit) + "..."))
                        } else {
                            children.add(Text(text))
                        }
                    } else {
                        val partText = text.substring(index + searchString.length)
                        if (partText.length > lengthLimit) {
                            children.add(Text(partText.substring(0, lengthLimit) + "..."))
                        } else {
                            children.add(Text(partText))
                        }
                    }
                    Tooltip.install(
                        this,
                        Tooltip(if (text.length > lengthLimit) text.substring(0, lengthLimit) else text).apply {
                            showDelay = Duration.millis(50.0)
                            showDuration = Duration.minutes(1.0)
                        })
                    maxHeight = 200.0
                }
                EntryType.IMAGE -> {
                    children.add(object : ImageView(SwingFXUtils.toFXImage(entry.image!!, null)) {
                        override fun getBaselineOffset(): Double = 10.0
                    }.apply {
                        fitHeight = 100.0
                        isPreserveRatio = true
                    })
                    maxHeight = 150.0
                    Tooltip.install(
                        this,
                        Tooltip().apply {
                            graphic = ImageView(SwingFXUtils.toFXImage(entry.image!!, null)).apply {
                                val imgWidth = entry.image!!.width
                                val imgHeight = entry.image!!.height
                                if (imgWidth > imgHeight) {
                                    fitWidth = 900.0
                                } else {
                                    fitHeight = 900.0
                                }
                                isPreserveRatio = true
                            }
                            showDelay = Duration.millis(300.0)
                            showDuration = Duration.minutes(1.0)
                        }
                    )
                }
            }

            val s = 5.0
            padding = Insets(s, s, s, s)
            prefWidthProperty().bind(stage.widthProperty())
            onMouseClicked = EventHandler { ev ->
                pickEntry(ev.source.let { it as TextFlow }.userData as String)
            }
            onMouseEntered = EventHandler { requestFocus() }
            onKeyPressed = EventHandler { ev ->
                if (ev.code == KeyCode.ENTER) {
                    pickEntry(ev.source.let { it as TextFlow }.userData as String)
                }
            }
            userData = md5Digest
            isFocusTraversable = true
        }
    }

    private fun pickEntry(md5Digest: String) {
        ClipboardApi.setClipboard(md5Digest)
        hide()
        searchTextField.text = ""
        updateDisplay()
    }

    fun show() {
        Platform.runLater {
            val location = MouseInfo.getPointerInfo().location
            stage.x = location.x * 1.0
            stage.y = location.y * 1.0
            stage.requestFocus()
            stage.show()
            if (stage.isMaximized) {
                stage.isMaximized = false
                stage.width = lastWidth
                stage.height = lastHeight
            }
            stage.isIconified = true
            stage.isIconified = false
            searchTextField.requestFocus()
            selectionScrollPane.vvalue = 0.0
        }
    }

    private fun hide() {
        Platform.runLater {
            stage.hide()
        }
    }

}