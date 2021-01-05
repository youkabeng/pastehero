package me.phph.app.pastehero.controller

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import me.phph.app.pastehero.api.Cache
import me.phph.app.pastehero.api.ClipboardApi
import me.phph.app.pastehero.api.Item
import me.phph.app.pastehero.api.md5
import org.fxmisc.richtext.StyleClassedTextArea

class EditWindow(var rootStage: Stage, entry: Item) {
    private var mainStage: Stage?
    private var mainScene: Scene?
    private var editArea = StyleClassedTextArea()

    init {
        editArea.appendText(entry.value)
        mainScene = Scene(editArea, 600.0, 400.0).apply {
            addEventHandler(KeyEvent.KEY_PRESSED) {
                if (it.code == KeyCode.ESCAPE) {
                    close()
                } else if (it.isControlDown && it.code == KeyCode.S) {
                    entry.updateTs = System.currentTimeMillis()
                    entry.value = editArea.text.trim()
                    Cache.delete(entry.md5Digest)
                    val md5Digest = md5(entry.value)
                    entry.md5Digest = md5Digest
                    Cache.set(entry)
                    ClipboardApi.setClipboard(md5Digest)
                    close()
                } else if (it.isControlDown && it.code == KeyCode.Z) {
                    close()
                }
            }
        }

        mainStage = Stage().apply {
            initOwner(rootStage)
            scene = mainScene
            title = "Edit demo"
        }
    }

    fun show() {
        Platform.runLater {
            mainStage?.show()
        }
    }

    fun close() {
        Platform.runLater {
            mainStage?.close()
        }
    }

}