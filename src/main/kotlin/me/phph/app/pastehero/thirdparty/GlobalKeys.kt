package me.phph.app.pastehero.thirdparty

import com.tulskiy.keymaster.common.Provider
import javafx.application.Platform
import me.phph.app.pastehero.view.ViewFactory
import javax.swing.KeyStroke

fun registerGlobalKeys() {
    val provider = Provider.getCurrentProvider(false)
    provider.register(KeyStroke.getKeyStroke("control shift V")) {
        Platform.runLater {
            ViewFactory.showMainWindow()
        }
    }
}