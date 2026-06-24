package com.escom.silentnull.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class DialogueManager {
    private val font = BitmapFont().apply { data.setScale(3f) }
    private val shapeRenderer = ShapeRenderer()

    private var isVisible = false
    private var currentText = ""

    fun show(text: String) {
        currentText = text
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }

    fun isActive(): Boolean = isVisible

    fun render(batch: SpriteBatch, hudCamera: OrthographicCamera) {
        if (!isVisible) return

        // 1. Dibujar el fondo del cuadro de diálogo
        Gdx.gl.glEnable(GL20.GL_BLEND)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        val margin = 50f
        val boxHeight = 250f
        val boxWidth = Gdx.graphics.width - (margin * 2)

        // Sombra / Fondo negro semi-transparente
        shapeRenderer.color = Color(0f, 0f, 0f, 0.8f)
        shapeRenderer.rect(margin, margin, boxWidth, boxHeight)

        // Borde blanco
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(margin, margin, boxWidth, boxHeight)
        shapeRenderer.end()

        // 2. Dibujar el texto
        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.draw(
            batch,
            currentText,
            margin + 40f,
            margin + boxHeight - 80f,
            boxWidth - 80f,
            -1,
            true
        )
        batch.end()
    }

    fun handleInput(): Boolean {
        if (isVisible && Gdx.input.justTouched()) {
            hide()
            return true
        }
        return false
    }

    fun dispose() {
        font.dispose()
        shapeRenderer.dispose()
    }
}
