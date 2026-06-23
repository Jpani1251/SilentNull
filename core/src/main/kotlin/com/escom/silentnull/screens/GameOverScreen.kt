package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.escom.silentnull.SilentNullGame

class GameOverScreen(
    private val game: SilentNullGame
) : Screen {

    private val camera = OrthographicCamera()
    private val viewport = ScreenViewport(camera)

    private val shapeRenderer = ShapeRenderer()

    private val titleFont = BitmapFont()
    private val messageFont = BitmapFont()
    private val buttonFont = BitmapFont()

    private val titleLayout = GlyphLayout()
    private val messageLayout = GlyphLayout()
    private val buttonLayout = GlyphLayout()

    private val touchPosition = Vector3()
    private val returnButton = Rectangle()

    private var changingScreen = false

    init {
        titleFont.data.setScale(5f)
        messageFont.data.setScale(2.5f)
        buttonFont.data.setScale(2.2f)
    }

    override fun render(delta: Float) {
        processInput()

        if (changingScreen) {
            return
        }

        ScreenUtils.clear(0.025f, 0.025f, 0.035f, 1f)

        viewport.apply()

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color(0.08f, 0.01f, 0.015f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            viewport.worldWidth,
            viewport.worldHeight
        )

        val panelWidth = (viewport.worldWidth * 0.78f).coerceAtMost(1050f)
        val panelHeight = (viewport.worldHeight * 0.68f).coerceAtMost(720f)
        val panelX = (viewport.worldWidth - panelWidth) / 2f
        val panelY = (viewport.worldHeight - panelHeight) / 2f

        shapeRenderer.color = Color(0.12f, 0.12f, 0.15f, 1f)
        shapeRenderer.rect(
            panelX,
            panelY,
            panelWidth,
            panelHeight
        )

        shapeRenderer.color = Color(0.65f, 0.04f, 0.07f, 1f)
        shapeRenderer.rect(
            panelX,
            panelY + panelHeight - 18f,
            panelWidth,
            18f
        )

        shapeRenderer.color = Color(0.52f, 0.05f, 0.08f, 1f)
        shapeRenderer.rect(
            returnButton.x,
            returnButton.y,
            returnButton.width,
            returnButton.height
        )

        shapeRenderer.color = Color(0.82f, 0.12f, 0.15f, 1f)
        shapeRenderer.rect(
            returnButton.x + 6f,
            returnButton.y + 6f,
            returnButton.width - 12f,
            returnButton.height - 12f
        )

        shapeRenderer.end()

        game.batch.projectionMatrix = camera.combined
        game.batch.begin()

        titleLayout.setText(titleFont, "HAS MUERTO")
        titleFont.color = Color(0.95f, 0.12f, 0.15f, 1f)
        titleFont.draw(
            game.batch,
            titleLayout,
            (viewport.worldWidth - titleLayout.width) / 2f,
            panelY + panelHeight - 145f
        )

        messageLayout.setText(messageFont, "Has perdido el juego")
        messageFont.color = Color.WHITE
        messageFont.draw(
            game.batch,
            messageLayout,
            (viewport.worldWidth - messageLayout.width) / 2f,
            panelY + panelHeight / 2f + 55f
        )

        buttonLayout.setText(buttonFont, "VOLVER AL INICIO")
        buttonFont.color = Color.WHITE
        buttonFont.draw(
            game.batch,
            buttonLayout,
            returnButton.x + (returnButton.width - buttonLayout.width) / 2f,
            returnButton.y + (returnButton.height + buttonLayout.height) / 2f
        )

        game.batch.end()
    }

    private fun processInput() {
        val keyboardPressed =
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)

        var buttonPressed = false

        if (Gdx.input.justTouched()) {
            touchPosition.set(
                Gdx.input.x.toFloat(),
                Gdx.input.y.toFloat(),
                0f
            )

            viewport.unproject(touchPosition)

            buttonPressed = returnButton.contains(
                touchPosition.x,
                touchPosition.y
            )
        }

        if (keyboardPressed || buttonPressed) {
            returnToStart()
        }
    }

    private fun returnToStart() {
        if (changingScreen) {
            return
        }

        changingScreen = true
        game.screen = JuegoScreen(game)
        dispose()
    }

    private fun positionButton(uiScale: Float) {
        val availableWidth =
            (viewport.worldWidth - 40f).coerceAtLeast(200f)

        val buttonWidth =
            (viewport.worldWidth * 0.55f)
                .coerceAtMost(760f * uiScale)
                .coerceAtMost(availableWidth)

        val buttonHeight =
            (125f * uiScale)
                .coerceAtLeast(80f)
                .coerceAtMost(viewport.worldHeight * 0.20f)

        returnButton.set(
            (viewport.worldWidth - buttonWidth) / 2f,
            viewport.worldHeight * 0.20f,
            buttonWidth,
            buttonHeight
        )
    }

    override fun show() {}

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)

        val widthScale = width.toFloat() / 1920f
        val heightScale = height.toFloat() / 1080f
        val uiScale = minOf(widthScale, heightScale)
            .coerceIn(0.60f, 1.20f)

        titleFont.data.setScale(5f * uiScale)
        messageFont.data.setScale(2.5f * uiScale)
        buttonFont.data.setScale(2.2f * uiScale)

        positionButton(uiScale)
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        shapeRenderer.dispose()
        titleFont.dispose()
        messageFont.dispose()
        buttonFont.dispose()
    }
}
