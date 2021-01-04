package me.phph.app.pastehero.controller

import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.embed.swing.SwingFXUtils
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Stage
import javafx.util.Duration
import me.phph.app.pastehero.api.*
import me.phph.app.pastehero.view.ViewHelper
import org.kordamp.ikonli.javafx.FontIcon
import java.awt.MouseInfo
import java.net.URL
import java.util.*


class MainWindowController(viewHelper: ViewHelper, fxmlPath: String) :
    BaseController(viewHelper, fxmlPath), Initializable {

    @FXML
    private var searchTextField: TextField? = null

    @FXML
    private var itemsVbox: VBox? = null

    private val updated = SimpleIntegerProperty(0)
    private val searchTimer = Timer()
    private val countPerPage: Int = Configuration.getConfigurationInt(Configuration.CONF_COUNT_PER_PAGE)

    override fun initialize(url: URL?, resourceBundle: ResourceBundle?) {
        setupSearchTextField()
        setupItemsVbox()
        setupTrigger()
    }

    override fun createScene(parent: Parent?): Scene {
        return super.createScene(parent).apply {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ESCAPE) {
                    close()
                    searchTextField!!.text = ""
                    setupItemsVbox()
                } else if (it.isAltDown && it.code.isDigitKey) {
                    val children = itemsVbox!!.children
                    var index = it.code.code - '0'.toInt() - 1
                    if (index == -1) {
                        index = 9
                    }
                    if (index < children.size) {
                        pickItem(children[index].userData as String)
                    }
                } else if (it.isControlDown && it.code == KeyCode.X) {
                    findFocusedItemUserData()?.let { deleteItem(it) }
                } else if (it.isControlDown && it.code == KeyCode.E) {
                    close()
                    findFocusedItemUserData()?.let { userData ->
//                        Cache.get(userData)?.let { EditWindow(stage, it).show() }
                        println("edit is not supported for now")
                    }
                } else if (it.isControlDown && it.code == KeyCode.Q) {
                    searchTextField!!.requestFocus()
                } else if (it.isControlDown && it.code == KeyCode.R) {
                    setupItemsVbox(searchTextField!!.text)
                }
            }
        }
    }

    private fun setupSearchTextField() {
        searchTextField!!.apply {
            promptText = "Type to search"
            onKeyPressed = EventHandler { ev ->
                if (ev.code == KeyCode.ENTER) {
                    Platform.runLater {
                        setupItemsVbox(ev.source.let { it as TextField }.text)
                    }
                }
            }
            textProperty().addListener { _, _, newValue ->
                searchTimer.schedule(object : TimerTask() {
                    override fun run() {
                        Platform.runLater {
                            setupItemsVbox(newValue)
                        }
                    }
                }, 500)
            }
            focusedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    showItemOptionButtons()
                }
            }
            onMouseEntered = EventHandler { requestFocus() }
        }
    }

    private fun setupItemsVbox(searchString: String = "") {
        itemsVbox!!.apply {
            children.clear();
            var count = 0;
            val items = ClipboardApi.listItems(searchString)
            val size = items.size
            for (item in items) {
                val stackPane = createItemStackPane(item, searchString, count)
                children.add(stackPane)
                count++
                if (count == countPerPage || count == size)
                    break
            }
        }
    }

    private fun setupTrigger() {
        updated.bind(ClipboardApi.updated)
        updated.addListener { _, _, _ ->
            setupItemsVbox()
        }
    }

    private fun createItemStackPane(item: Item, searchString: String, order: Int): StackPane {
        return StackPane().apply {
            val textFlow = createItemTextFlow(item, searchString, order)
            userData = item.md5Digest
            children.add(textFlow)
            children.add(HBox().apply {
                // show variants icon
                // todo
                // use plugin to add extra functionalities
                if (item.type != ItemType.IMAGE) {
                    children.add(Button().apply {
                        graphic = FontIcon("antf-edit:16")
                        tooltip = Tooltip("edit")
                        userData = item.md5Digest
                        onAction = EventHandler { ev ->
                            val itemUserData = ev.source.let { it as Button }.userData as String
                            Cache.get(itemUserData)?.let {
                                close()
//                                hide()
//                                EditWindow(stage, it).show()
                            }
                        }
                        isFocusTraversable = false
                    })
                }
                children.add(Button().apply {
                    graphic = FontIcon("antf-delete:16:red")
                    tooltip = Tooltip("delete")
                    userData = item.md5Digest
                    onAction = EventHandler { ev ->
                        deleteItem(ev.source.let { it as Button }.userData as String)
                    }
                    isFocusTraversable = false
                })
                alignment = Pos.BOTTOM_RIGHT
                isPickOnBounds = false
                isVisible = false
            })
        }
    }

    private fun createItemTextFlow(item: Item, searchString: String, order: Int): TextFlow {
        val text = item.value
        val type = item.type
        val md5Digest = item.md5Digest

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
                ItemType.STRING,
                ItemType.FILES,
                ItemType.URL,
                ItemType.RTF,
                ItemType.JSON,
                ItemType.HTML,
                ItemType.XML -> {
                    val index =
                        if (searchString.isEmpty()) -1 else text.toLowerCase().indexOf(searchString.toLowerCase())
                    if (index >= 0) {
                        val t = Text(text.substring(0, index))
                        if (t.layoutBounds.width > getStage().width) {
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
                ItemType.IMAGE -> {
                    children.add(object : ImageView(SwingFXUtils.toFXImage(item.image!!, null)) {
                        override fun getBaselineOffset(): Double = 10.0
                    }.apply {
                        fitHeight = 100.0
                        isPreserveRatio = true
                    })
                    maxHeight = 150.0
                    Tooltip.install(
                        this,
                        Tooltip().apply {
                            graphic = ImageView(SwingFXUtils.toFXImage(item.image!!, null)).apply {
                                val imgWidth = item.image!!.width
                                val imgHeight = item.image!!.height
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
                pickItem(ev.source.let { it as TextFlow }.userData as String)
            }
            onMouseEntered = EventHandler { requestFocus() }
            onKeyPressed = EventHandler { ev ->
                if (ev.code == KeyCode.ENTER) {
                    pickItem(ev.source.let { it as TextFlow }.userData as String)
                }
            }
            focusedProperty().addListener { _, _, newValue ->
                if (newValue) {
                    showItemOptionButtons(item.md5Digest)
                }
            }
            val s = 5.0
            padding = Insets(s, s, s, s)
            userData = md5Digest
            isFocusTraversable = true
        }
    }

//    fun show() {
//        Platform.runLater {
//            val location = MouseInfo.getPointerInfo().location
//            stage.x = location.x * 1.0
//            stage.y = location.y * 1.0
//            stage.requestFocus()
//            stage.show()
//            if (stage.isMaximized) {
//                stage.isMaximized = false
//                stage.width = lastWidth
//                stage.height = lastHeight
//            }
//            stage.isIconified = true
//            stage.isIconified = false
//            searchTextField.requestFocus()
//            selectionScrollPane.vvalue = 0.0
//            updateDisplay(searchTextField.text)
//        }
//    }

    private fun readSearchText(): String {
        return searchTextField!!.text
    }

    private fun close() {
        getStage().let { viewHelper.closeStage(it) }
    }

    private fun getStage(): Stage {
        return searchTextField?.scene?.window.let { it as Stage }
    }

    private fun deleteItem(md5Digest: String) {
        itemsVbox!!.apply {
            children.first { it.userData == md5Digest }?.let {
                children.remove(it)
                Cache.delete(md5Digest)
            }
        }
    }

    private fun pickItem(md5Digest: String) {
        ClipboardApi.setClipboard(md5Digest)
        close()
        searchTextField!!.text = ""
    }

    private fun showItemOptionButtons(md5Digest: String = "") {
        itemsVbox!!.children?.forEach {
            val stackPane = it as StackPane
            val userData = stackPane.userData as String
            stackPane.children.first { it is HBox }.let {
                val node = it as HBox
                node.isVisible = md5Digest == userData
            }
        }
    }

    private fun findFocusedItemUserData(): String? {
        for (child in itemsVbox!!.children) {
            val sp = child as StackPane
            val tf = sp.children.first { it is TextFlow }
            if (tf.isFocused)
                return sp.userData as String
        }
        return null;
    }
}
