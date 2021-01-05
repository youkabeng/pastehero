package me.phph.app.pastehero.controller

import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

open class BaseController(val fxmlPath: String) {
    var stage: Stage? = null
    var scene: Scene? = null

    open fun show() {
        stage?.show()
    }

    open fun hide() {
        stage?.hide()
    }

    open fun close() {
        stage?.close()
    }

    fun setupStageAndScene(parent: Parent) {
        scene = Scene(parent)
        setupScene(scene!!)
        stage = Stage()
        stage!!.let { it.scene = scene; setupStage(it) }
    }

    open fun setupScene(scene: Scene) {
    }

    open fun setupStage(stage: Stage) {
    }

    open fun setupStyles() {
    }

    private fun updateStyle(scene: Scene) {

    }

    private fun loadStylesheet(path: String): String? {
        return Thread.currentThread().contextClassLoader.getResource(path)?.toExternalForm()
    }
}