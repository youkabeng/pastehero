package me.phph.app.pastehero.controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import me.phph.app.pastehero.api.Cache
import me.phph.app.pastehero.api.ClipboardApi
import me.phph.app.pastehero.api.Item
import me.phph.app.pastehero.api.md5
import org.fxmisc.richtext.LineNumberFactory
import org.fxmisc.richtext.StyleClassedTextArea
import java.net.URL
import java.util.*

class EditWindowController(fxmlPath: String, val item: Item) :
    BaseController(fxmlPath), Initializable {

    @FXML
    private var editAreaScrollPane: ScrollPane? = null

    private var editArea = StyleClassedTextArea()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        setupEditArea()
    }

    private fun setupEditArea() {
        editArea.isWrapText = true
        editArea.paragraphGraphicFactory = LineNumberFactory.get(editArea)
        editArea.appendText(item.value)
        editAreaScrollPane?.content = editArea
    }

    override fun setupStage(stage: Stage) {
        stage.title = "Paste Hero - Edit"
    }

    override fun setupScene(scene: Scene) {
        scene.apply {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ESCAPE) {
                    close()
                } else if (it.isControlDown && it.code == KeyCode.S) {
                    item.updateTs = System.currentTimeMillis()
                    item.value = editArea.text.trim()
                    Cache.delete(item.md5Digest)
                    val md5Digest = md5(item.value)
                    item.md5Digest = md5Digest
                    Cache.set(item)
                    ClipboardApi.setClipboard(md5Digest)
                    close()
                } else if (it.isControlDown && it.code == KeyCode.Q) {
                    close()
                }
            }
        }
    }
}
