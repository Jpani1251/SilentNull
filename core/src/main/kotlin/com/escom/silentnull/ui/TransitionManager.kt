package com.escom.silentnull.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport

class TransitionManager {
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont().apply { data.setScale(2.5f) }
    private val btnAbrir = GameButton("btn_der.png", 0f, 0f, 250f, 100f)

    private var fadeAlpha = 0f
    private var isFading = false
    private var showButton = false
    private var onFadeFinished: (() -> Unit)? = null

    fun update(delta: Float) {
        if (isFading) {
            fadeAlpha += delta * 1.5f // Velocidad del desvanecimiento
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f
                isFading = false
                onFadeFinished?.invoke()
                onFadeFinished = null
            }
        }
    }

    fun render(batch: SpriteBatch, viewport: Viewport) {
        val width = Gdx.graphics.width.toFloat()
        val height = Gdx.graphics.height.toFloat()

        if (showButton) {
            batch.projectionMatrix = viewport.camera.combined
            batch.begin()
            btnAbrir.x = width / 2f - 125f
            btnAbrir.y = height / 2f - 50f
            btnAbrir.render(batch)
            font.draw(batch, "ABRIR", btnAbrir.x + 65f, btnAbrir.y + 65f)
            batch.end()
        }

        if (fadeAlpha > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            shapeRenderer.projectionMatrix = viewport.camera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0f, 0f, fadeAlpha)
            shapeRenderer.rect(0f, 0f, width, height)
            shapeRenderer.end()
        }
    }

    fun setShowButton(show: Boolean) {
        this.showButton = show
    }

    fun startFade(callback: () -> Unit) {
        if (isFading) return // Evitar múltiples fades simultáneos
        isFading = true
        fadeAlpha = 0f
        onFadeFinished = callback
        showButton = false
    }

    fun handleInput(touchX: Float, touchY: Float): Boolean {
        if (showButton && Gdx.input.justTouched()) {
            if (btnAbrir.isTouched(touchX, touchY)) {
                return true
            }
        }
        return false
    }

    fun reset() {
        fadeAlpha = 0f
        isFading = false
        showButton = false
    }

    fun dispose() {
        shapeRenderer.dispose()
        font.dispose()
        btnAbrir.dispose()
    }
}
