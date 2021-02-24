package me.phph.app.pastehero.view

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import me.phph.app.pastehero.api.Item
import me.phph.app.pastehero.controller.BaseController
import me.phph.app.pastehero.controller.EditWindowController
import me.phph.app.pastehero.controller.MainWindowController
import java.io.IOException

object ViewFactory {
    private var mainWindowInitialized = false
    private val stagesCache: MutableMap<String, BaseController> = mutableMapOf()

    private const val STAGE_MAIN_WINDOW = "main_window"

    private fun setupController(controller: BaseController) {
        val fxmlLoader = FXMLLoader(Thread.currentThread().contextClassLoader.getResource(controller.fxmlPath))
        fxmlLoader.setController(controller)
        val parent: Parent = try {
            fxmlLoader.load()
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("failed to load from fxml")
        }
        controller.setupStageAndScene(parent)
        controller.setupStyles()
    }

    fun initMainWindow() {
        val controller = MainWindowController("fxml/MainWindow.fxml")
        setupController(controller)
        stagesCache[STAGE_MAIN_WINDOW] = controller
        mainWindowInitialized = true
    }

    fun showMainWindow() {
        if (!mainWindowInitialized) {
            initMainWindow()
        }
        stagesCache[STAGE_MAIN_WINDOW]?.let { it.hide();it.show() }
    }

    fun showEditWindow(item: Item) {
        val controller = EditWindowController("fxml/EditWindow.fxml", item)
        setupController(controller)
        controller.show()
    }
}