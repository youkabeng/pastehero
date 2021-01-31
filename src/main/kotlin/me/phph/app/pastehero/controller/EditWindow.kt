package me.phph.app.pastehero.controller

import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import me.phph.app.pastehero.api.Cache
import me.phph.app.pastehero.api.ClipboardApi
import me.phph.app.pastehero.api.Item
import me.phph.app.pastehero.api.md5
import org.fxmisc.richtext.StyleClassedTextArea

class EditWindow(item: Item) {
    private var mainStage: Stage?
    private var mainScene: Scene?
    private var editArea = StyleClassedTextArea()

    init {
        editArea.appendText(item.value)
        mainScene = Scene(editArea, 600.0, 400.0).apply {
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
                } else if (it.isControlDown && it.code == KeyCode.Z) {
                    close()
                }
            }
        }

        mainStage = Stage().apply {
            scene = mainScene
            title = "Edit Item"
        }
    }

    fun show() {
        mainStage?.show()
    }

    private fun close() {
        mainStage?.close()
    }

}