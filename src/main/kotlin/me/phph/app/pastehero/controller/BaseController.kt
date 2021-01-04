package me.phph.app.pastehero.controller

import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import me.phph.app.pastehero.view.ViewHelper

open class BaseController(val viewHelper: ViewHelper, val fxmlPath: String) {

    open fun createScene(parent: Parent?): Scene {
        return Scene(parent)
    }

    open fun createStage(): Stage {
        return Stage()
    }
}