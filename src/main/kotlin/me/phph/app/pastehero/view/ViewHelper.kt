package me.phph.app.pastehero.view

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import me.phph.app.pastehero.controller.BaseController
import me.phph.app.pastehero.controller.MainWindowController
import java.io.IOException

class ViewHelper {
    val activeStages: MutableList<Stage> = mutableListOf()
    var mainWindowInitialized = false
    val stagesCache: MutableMap<String, Stage> = mutableMapOf()

    val STAGE_MAIN_WINDOW = "mainWindow"

    fun showMainWindow() {
        stagesCache[STAGE_MAIN_WINDOW]?.show()
    }

    fun initMainWindow() {
        val controller = MainWindowController(this, "fxml/MainWindow.fxml")
        val stage = initializeStage(controller) ?: throw RuntimeException("failed to init main window")
        stage.let { stagesCache.put(STAGE_MAIN_WINDOW, stage) }
        mainWindowInitialized = true
    }

    private fun initializeStage(controller: BaseController): Stage? {
        val fxmlLoader = FXMLLoader(Thread.currentThread().contextClassLoader.getResource(controller.fxmlPath))
        fxmlLoader.setController(controller)
        val parent: Parent = try {
            fxmlLoader.load()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        val scene = controller.createScene(parent)
        updateStyle(scene)
        val stage = Stage()
        stage.scene = scene
        activeStages.add(stage)
        return stage
    }

    private fun updateStyle(scene: Scene) {
        scene.stylesheets.clear()
        loadStylesheet("css/TextFlow.css")?.let(scene.stylesheets::add)
        loadStylesheet("css/VBox.css")?.let(scene.stylesheets::add)
    }

    private fun loadStylesheet(path: String): String? {
        return Thread.currentThread().contextClassLoader.getResource(path)?.toExternalForm()
    }

    fun closeStage(stageToClose: Stage) {
        stageToClose.close()
        activeStages.remove(stageToClose)
    }

}