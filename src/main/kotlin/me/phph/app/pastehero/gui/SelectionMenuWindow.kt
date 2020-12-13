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
import org.kordamp.ikonli.javafx.FontIcon
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
            focusedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    showEntryButton("")
                }
            }
            onMouseEntered = EventHandler { requestFocus() }
        }

        // settings for upper vbox
        upperVBox.children.add(searchTextField)

        // settings for selection vbox and scroll pane
        selectionScrollPane.apply {
            content = selectionVBox
            isFitToWidth = true
            isFitToHeight = true
        }
        VBox.setVgrow(selectionVBox, Priority.ALWAYS)
        VBox.setVgrow(selectionScrollPane, Priority.ALWAYS)

        // settings for lower vbox
        lowerVBox.children.add(selectionScrollPane)
        VBox.setVgrow(lowerVBox, Priority.ALWAYS)

        topVBox.children.addAll(upperVBox, lowerVBox)
        topVBox.prefWidthProperty().bind(stage.widthProperty())
        topVBox.prefHeightProperty().bind(stage.heightProperty())

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
                } else if (it.isControlDown && it.code == KeyCode.X) {
                    findFocusedEntryUserData()?.let { deleteEntry(it) }
                } else if (it.isControlDown && it.code == KeyCode.E) {
                    hide()
                    findFocusedEntryUserData()?.let { userData ->
                        Cache.getEntry(userData)?.let { EditWindow(stage, it).show() }
                    }
                } else if (it.isControlDown && it.code == KeyCode.Q) {
                    searchTextField.requestFocus()
                } else if (it.isControlDown && it.code == KeyCode.R) {
                    updateDisplay(searchTextField.text)
                }
            }
            loadStylesheet("css/TextFlow.css")?.let(stylesheets::add)
            loadStylesheet("css/VBox.css")?.let(stylesheets::add)
        }

        // settings for stage
        stage.apply {
            title = "Clipboard Selection Menu"
            width = 500.0
            height = 700.0
            minWidth = 100.0
            minHeight = 200.0
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
            userData = entry.md5Digest
            children.add(textFlow)
            children.add(HBox().apply {
                // show variants icon
                // todo
                // use plugin to add extra functionalities
                if (entry.type != EntryType.IMAGE) {
                    children.add(Button().apply {
                        graphic = FontIcon("antf-edit:16")
                        tooltip = Tooltip("edit")
                        userData = entry.md5Digest
                        onAction = EventHandler { ev ->
                            val entryUserData = ev.source.let { it as Button }.userData as String
                            Cache.getEntry(entryUserData)?.let {
                                hide()
                                EditWindow(stage, it).show()
                            }
                        }
                        isFocusTraversable = false
                    })
                }
                children.add(Button().apply {
                    graphic = FontIcon("antf-delete:16:red")
                    tooltip = Tooltip("delete")
                    userData = entry.md5Digest
                    onAction = EventHandler { ev ->
                        deleteEntry(ev.source.let { it as Button }.userData as String)
                    }
                    isFocusTraversable = false
                })
                alignment = Pos.BOTTOM_RIGHT
                isPickOnBounds = false
                isVisible = false
            })
        }
    }

    private fun findFocusedEntryUserData(): String? {
        for (child in selectionVBox.children) {
            val sp = child as StackPane
            val tf = sp.children.first { it is TextFlow }
            if (tf.isFocused)
                return sp.userData as String
        }
        return null;
    }

    private fun deleteEntry(md5Digest: String) {
        with(selectionVBox) {
            children.first { it.userData == md5Digest }?.let {
                children.remove(it)
                Cache.deleteEntry(md5Digest)
            }
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
            onMouseClicked = EventHandler { ev ->
                pickEntry(ev.source.let { it as TextFlow }.userData as String)
            }
            onMouseEntered = EventHandler { requestFocus() }
            onKeyPressed = EventHandler { ev ->
                if (ev.code == KeyCode.ENTER) {
                    pickEntry(ev.source.let { it as TextFlow }.userData as String)
                }
            }
            focusedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    showEntryButton(entry.md5Digest)
                }
            }
            val s = 5.0
            padding = Insets(s, s, s, s)
            prefWidthProperty().bind(stage.widthProperty())
            userData = md5Digest
            isFocusTraversable = true
        }
    }

    private fun showEntryButton(md5Digest: String) {
        for (child in selectionVBox.children) {
            val sp = child as StackPane
            val userData = sp.userData as String
            sp.children.first { it is HBox }.let {
                val node = it as HBox
                node.isVisible = md5Digest == userData
            }
        }
    }

    private fun pickEntry(md5Digest: String) {
        ClipboardApi.setClipboard(md5Digest)
        hide()
        searchTextField.text = ""
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
            updateDisplay(searchTextField.text)
        }
    }

    private fun hide() {
        Platform.runLater {
            stage.hide()
        }
    }

}