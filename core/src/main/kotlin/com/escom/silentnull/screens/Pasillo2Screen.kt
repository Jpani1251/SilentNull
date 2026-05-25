package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.escom.silentnull.SilentNullGame
import com.escom.silentnull.entities.Player
import com.escom.silentnull.inventory.InventoryUI
import com.escom.silentnull.ui.GameButton

class Pasillo2Screen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    private val worldWidth = 1080f
    private val worldHeight = 1440f // Reducido de 1920 a 1440 para comprimir la imagen verticalmente

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, worldWidth, worldHeight)
    }

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val pasilloTexture = Texture(Gdx.files.internal("pasillo escom 2.png"))
    private val font = BitmapFont().apply {
        data.setScale(3f)
    }

    private val player = Player()

    private var moviendoIzquierda = false
    private var moviendoArriba = false
    private var moviendoAbajo = false
    private var cambiandoPantalla = false

    private val tamanoBoton = 150f
    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton
    private lateinit var btnInventario: GameButton

    private lateinit var inventoryUI: InventoryUI

    init {
        if (spawnX != null && spawnY != null) {
            player.setPosition(spawnX, spawnY)
        } else {
            // Posicionamiento inicial solicitado: X 369, Y 486
            player.setPosition(369f, 486f)
        }

        btnIzq = GameButton("btn_izq.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnDer = GameButton("btn_der.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnArriba = GameButton("btn_arriba.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnAbajo = GameButton("btn_abajo.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnInventario = GameButton("logo.png", 0f, 0f, tamanoBoton, tamanoBoton)

        inventoryUI = InventoryUI(player, font)

        posicionarBotones()
    }

    override fun render(delta: Float) {
        update(delta)
        if (cambiandoPantalla) return

        ScreenUtils.clear(0f, 0f, 0f, 1f)

        game.batch.projectionMatrix = camera.combined
        game.batch.begin()
        game.batch.draw(pasilloTexture, 0f, 0f, worldWidth, worldHeight)
        player.render(game.batch)
        game.batch.end()

        hudViewport.apply()
        game.batch.projectionMatrix = hudCamera.combined
        game.batch.begin()
        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)
        btnInventario.render(game.batch)

        font.draw(
            game.batch,
            "X: ${player.x.toInt()}  Y: ${player.y.toInt()}",
            0f,
            hudViewport.worldHeight - 50f,
            hudViewport.worldWidth,
            Align.center,
            false
        )

        inventoryUI.render(game.batch)
        game.batch.end()
    }

    private fun update(delta: Float) {
        procesarInput(delta)
        if (!inventoryUI.isOpen()) {
            player.update(delta)
            player.limitarPantalla(worldWidth, worldHeight)
            revisarAccesos()
        }
        actualizarCamara()
    }

    private fun procesarInput(delta: Float) {
        moviendoArriba = false
        moviendoAbajo = false

        if (Gdx.input.justTouched()) {
            val touchPos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            hudViewport.unproject(touchPos)
            if (btnInventario.isTouched(touchPos.x, touchPos.y)) {
                inventoryUI.toggle()
            }
        }

        if (Gdx.input.isTouched && !inventoryUI.isOpen()) {
            val touchPos = Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            hudViewport.unproject(touchPos)

            if (btnIzq.isTouched(touchPos.x, touchPos.y)) {
                player.moverIzquierda(delta)
                moviendoIzquierda = true
            }
            if (btnDer.isTouched(touchPos.x, touchPos.y)) player.moverDerecha(delta)
            if (btnArriba.isTouched(touchPos.x, touchPos.y)) {
                player.moverArriba(delta)
                moviendoArriba = true
            }
            if (btnAbajo.isTouched(touchPos.x, touchPos.y)) {
                player.moverAbajo(delta)
                moviendoAbajo = true
            }
        }
    }

    private fun revisarAccesos() {
        val currentX = player.x
        val currentY = player.y

        // Regresar al pasillo 1: X 0, Y entre 486 y 537
        if (moviendoIzquierda && currentX <= 10f && currentY >= 486f && currentY <= 537f) {
            cambiandoPantalla = true
            // Al regresar del segundo pasillo, aparecemos en X: 419, Y: 978 en el Pasillo 1
            game.screen = PasilloScreen(game, 419f, 978f)
            dispose()
            return
        }
    }

    private fun actualizarCamara() {
        camera.position.x = worldWidth / 2f
        camera.position.y = player.y
        camera.position.y = camera.position.y.coerceIn(camera.viewportHeight / 2f, worldHeight - camera.viewportHeight / 2f)
        camera.update()
    }

    private fun posicionarBotones() {
        val margenX = 50f
        val margenY = 50f
        btnIzq.x = margenX
        btnIzq.y = margenY + tamanoBoton
        btnDer.x = margenX + tamanoBoton * 2f
        btnDer.y = margenY + tamanoBoton
        btnArriba.x = margenX + tamanoBoton
        btnArriba.y = margenY + tamanoBoton * 2f
        btnAbajo.x = margenX + tamanoBoton
        btnAbajo.y = margenY
        btnInventario.x = Gdx.graphics.width - tamanoBoton - 50f
        btnInventario.y = Gdx.graphics.height - tamanoBoton - 50f
    }

    override fun show() {
        game.playBackgroundMusic()
    }
    override fun resize(width: Int, height: Int) {
        hudViewport.update(width, height, true)
        val aspectRatio = width.toFloat() / height.toFloat()
        camera.viewportWidth = worldWidth
        camera.viewportHeight = worldWidth / aspectRatio
        camera.update()
        posicionarBotones()
    }
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        pasilloTexture.dispose()
        font.dispose()
        btnIzq.dispose()
        btnDer.dispose()
        btnArriba.dispose()
        btnAbajo.dispose()
        btnInventario.dispose()
        inventoryUI.dispose()
    }
}
