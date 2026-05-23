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
import com.escom.silentnull.physics.CollisionBox
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

    // ESCALERAS: X 755 - 824 Y 1087 (Aumentamos altura para asegurar detección)
    private val areaEscaleras = CollisionBox(755f, 1080f, 69f, 100f)

    // SALIDA: X 337 - 464 Y 150 (Aumentamos altura para asegurar detección)
    private val areaSalida = CollisionBox(337f, 100f, 127f, 100f)

    private var moviendoArriba = false
    private var moviendoAbajo = false
    private var cambiandoPantalla = false

    private val tamanoBoton = 150f
    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton

    data class SalonEntry(val nombre: String, val x: Float, val y: Float)

    init {
        if (spawnX != null && spawnY != null) {
            player.setPosition(spawnX, spawnY)
        } else {
            // Posición inicial cerca de la salida
            player.setPosition(400f, 200f)
        }
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
        game.batch.end()
    }

    private fun update(delta: Float) {
        procesarInput(delta)
        player.update(delta)
        player.limitarPantalla(worldWidth, worldHeight)
        revisarAccesos()
        actualizarCamara()
    }

    private fun procesarInput(delta: Float) {
        moviendoArriba = false
        moviendoAbajo = false

        if (Gdx.input.isTouched) {
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
        val pBox = player.collisionBox

        // Entrar a salones (Detección por proximidad al centro)
        for (salon in entradasSalones) {
            val playerCenterX = player.x + (player.getWidth() / 2f)
            if (moviendoArriba &&
                Math.abs(playerCenterX - salon.x) < 55f &&
                Math.abs(player.y - salon.y) < 70f) {

                cambiandoPantalla = true
                game.screen = SalonScreen(game, salon.nombre, player.x, player.y - 120f, 3)
                dispose()
                return
            }
        }

        // Escaleras: Usamos la caja de colisión completa del jugador para mayor facilidad
        if (moviendoArriba && pBox.overlaps(areaEscaleras)) {
            cambiandoPantalla = true
            // Spawneamos en el pasillo del segundo piso
            game.screen = Edificio2SegundoPisoScreen(game, 1000f, 1550f)
            dispose()
            return
        }

        // Salida: Usamos la caja de colisión completa del jugador
        if (moviendoAbajo && pBox.overlaps(areaSalida)) {
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
        val margin = 50f
        btnIzq = GameButton("btn_izq.png", margin, margin, tamanoBoton, tamanoBoton)
        btnDer = GameButton("btn_der.png", margin + tamanoBoton + 20f, margin, tamanoBoton, tamanoBoton)
        btnArriba = GameButton("btn_arriba.png", Gdx.graphics.width - margin - tamanoBoton, margin + tamanoBoton + 20f, tamanoBoton, tamanoBoton)
        btnAbajo = GameButton("btn_abajo.png", Gdx.graphics.width - margin - tamanoBoton, margin, tamanoBoton, tamanoBoton)
    }

    override fun show() {}
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
    }
}
