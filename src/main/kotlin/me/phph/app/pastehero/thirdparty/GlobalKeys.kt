package me.phph.app.pastehero.thirdparty

import com.tulskiy.keymaster.common.Provider
import javafx.application.Platform
import me.phph.app.pastehero.api.Configuration
import me.phph.app.pastehero.view.ViewFactory
import javax.swing.KeyStroke

fun registerGlobalKeys() {
    val triggerShortcut = Configuration.getConfiguration(Configuration.CONF_TRIGGER_SHORTCUT).replace(",", " ")
    val provider = Provider.getCurrentProvider(false)
    provider.register(KeyStroke.getKeyStroke(triggerShortcut)) {
        Platform.runLater {
            ViewFactory.showMainWindow()
        }
    }
}