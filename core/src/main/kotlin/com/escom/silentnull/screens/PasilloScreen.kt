package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
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

class PasilloScreen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    private val worldWidth = 1080f
    private val worldHeight = 1920f

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, worldWidth, worldHeight)
    }

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val pasilloTexture = Texture(Gdx.files.internal("pasillo escom.png"))
    private val font = BitmapFont().apply {
        data.setScale(3f)
    }

    private val player = Player()

    // Coordenadas exactas proporcionadas por el usuario
    private val entradasSalones = listOf(
        SalonEntry("Salón 1", 259f, 1321f),
        SalonEntry("Salón 2", 347f, 1321f),
        SalonEntry("Salón 3", 465f, 1321f),
        SalonEntry("Salón 4", 600f, 1321f)
    )

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

    data class SalonEntry(val nombre: String, val x: Float, val y: Float)

    init {
        if (spawnX != null && spawnY != null) {
            player.setPosition(spawnX, spawnY)
        } else {
            // Posición inicial cerca de la salida
            player.setPosition(400f, 200f)
        }

        // Inicializar botones
        btnIzq = GameButton("btn_izq.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnDer = GameButton("btn_der.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnArriba = GameButton("btn_arriba.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnAbajo = GameButton("btn_abajo.png", 0f, 0f, tamanoBoton, tamanoBoton)

        // El logo sirve como botón de inventario temporalmente
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

        // Dibujar botón de inventario en la esquina superior derecha
        btnInventario.render(game.batch)

        // Dibujar coordenadas para depuración
        font.draw(
            game.batch,
            "X: ${player.x.toInt()}  Y: ${player.y.toInt()}",
            0f,
            hudViewport.worldHeight - 50f,
            hudViewport.worldWidth,
            Align.center,
            false
        )

        // Dibujar la interfaz del inventario si está abierta
        inventoryUI.render(game.batch)

        game.batch.end()
    }

    private fun update(delta: Float) {
        procesarInput(delta)

        // Solo mover al jugador si el inventario está cerrado
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

            if (btnIzq.isTouched(touchPos.x, touchPos.y)) player.moverIzquierda(delta)
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

        // Transición a Pasillo Escom 2: X 824 exacto, margen en Y 935 a 1013
        if (Math.abs(currentX - 824f) < 1f && currentY >= 935f && currentY <= 1013f) {
            cambiandoPantalla = true
            game.screen = Pasillo2Screen(game)
            dispose()
            return
        }

        // Entrar a salones (Detección por proximidad al centro visual)
        for (salon in entradasSalones) {
            // Usamos player.x para que coincida con lo que el usuario ve en las coordenadas de depuración
            if (moviendoArriba &&
                Math.abs(currentX - salon.x) < 40f &&
                Math.abs(currentY - salon.y) < 50f) {

                cambiandoPantalla = true
                game.screen = SalonScreen(game, salon.nombre, player.x, player.y - 120f, 3)
                dispose()
                return
            }
        }

        // Escaleras: Coincidencia exacta con las coordenadas X e Y proporcionadas
        if (moviendoArriba &&
            currentX >= 762f && currentX <= 824f &&
            Math.abs(currentY - 1398f) < 15f) {

            cambiandoPantalla = true
            // Te lleva al segundo piso donde están los salones 201, 202...
            game.screen = Edificio2SegundoPisoScreen(game, 1000f, 1550f)
            dispose()
            return
        }

        // Salida: Coincidencia con las coordenadas de la salida
        if (moviendoAbajo &&
            currentX >= 337f && currentX <= 464f &&
            Math.abs(currentY - 150f) < 15f) {

            cambiandoPantalla = true
            game.screen = JuegoScreen(game, 1380f, 1740f)
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

        // Posicionar botón de inventario arriba a la derecha
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
