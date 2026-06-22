package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.escom.silentnull.SilentNullGame
import com.escom.silentnull.entities.Player
import com.escom.silentnull.physics.CollisionBox
import com.escom.silentnull.ui.DebugManager
import com.escom.silentnull.ui.GameButton

class SalonGobiernoScreen(
    private val game: SilentNullGame,
    private val nombreSalon: String,
    private val regresoX: Float,
    private val regresoY: Float
) : Screen {

    // =========================
    // TEXTURAS
    // =========================
    private val fondoSalon = Texture("salon_escom.png")

    // =========================
    // MUNDO
    // =========================
    private val worldWidth = 1920f
    private val worldHeight = 1280f

    // =========================
    // CÁMARAS
    // =========================
    private val camera = OrthographicCamera()

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    // =========================
    // DIBUJO
    // =========================
    private val shapeRenderer = ShapeRenderer()

    // =========================
    // TEXTO
    // =========================
    private val font = BitmapFont()

    // =========================
    // JUGADOR
    // =========================
    private val player = Player()

    // =========================
    // DEBUG TOOLS
    // =========================
    private val debugManager = DebugManager("SalonGobierno_${nombreSalon.replace(" ", "_")}", worldWidth, worldHeight)

    // =========================
    // SALIDA
    // =========================
    // La puerta en salon_escom.png está a la derecha, arriba.
    private val salidaSalon = CollisionBox(
        worldWidth - 160f,
        worldHeight * 0.65f,
        130f,
        180f
    )

    // =========================
    // ESTADOS
    // =========================
    private var moviendoDerecha = false
    private var cambiandoPantalla = false
    private var recursosLiberados = false

    // =========================
    // BOTONES
    // =========================
    private val tamanoBoton = 150f

    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton

    // =========================
    // INIT
    // =========================
    init {

        font.data.setScale(2.4f)

        btnIzq = GameButton(
            "btn_izq.png",
            0f,
            0f,
            tamanoBoton,
            tamanoBoton
        )

        btnDer = GameButton(
            "btn_der.png",
            0f,
            0f,
            tamanoBoton,
            tamanoBoton
        )

        btnArriba = GameButton(
            "btn_arriba.png",
            0f,
            0f,
            tamanoBoton,
            tamanoBoton
        )

        btnAbajo = GameButton(
            "btn_abajo.png",
            0f,
            0f,
            tamanoBoton,
            tamanoBoton
        )

        // Aparece cerca de la puerta (derecha)
        player.setPosition(
            worldWidth - 220f,
            worldHeight * 0.68f
        )

        resize(
            Gdx.graphics.width,
            Gdx.graphics.height
        )
    }

    // =========================
    // RENDER
    // =========================
    override fun render(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        update(delta)

        if (cambiandoPantalla) {
            return
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f)

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        // Dibujar Fondo
        game.batch.draw(
            fondoSalon,
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        font.draw(
            game.batch,
            nombreSalon,
            worldWidth / 2f - 100f,
            worldHeight - 50f
        )

        player.render(game.batch)

        game.batch.end()

        // =========================
        // DEBUG TOOLS
        // =========================
        debugManager.render(game.batch, camera, hudCamera, player)

        // =========================
        // HUD
        // =========================
        hudViewport.apply()

        game.batch.projectionMatrix = hudCamera.combined

        game.batch.begin()

        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)

        game.batch.end()
    }

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        val prevX = player.x
        val prevY = player.y

        procesarInput(delta)

        player.update(delta)

        // Colisión con la rejilla (Global)
        if (debugManager.checkCollision(player)) {
            player.x = prevX
            player.y = prevY
        }

        val salioDelSalon =
            revisarSalida()

        if (salioDelSalon) {
            return
        }

        player.limitarPantalla(
            worldWidth,
            worldHeight
        )

        actualizarCamara()
    }

    // =========================
    // INPUT
    // =========================
    private fun procesarInput(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        moviendoDerecha = false

        if (!Gdx.input.isTouched) {
            debugManager.procesarInput(0f, 0f, camera) // Reset dragging
            return
        }

        touchPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat(),
            0f
        )

        hudViewport.unproject(touchPosition)

        val touchX = touchPosition.x
        val touchY = touchPosition.y

        // Delegar al DebugManager
        if (debugManager.procesarInput(touchX, touchY, camera)) {
            return
        }

        if (btnIzq.isTouched(touchX, touchY)) {

            player.moverIzquierda(delta)
        }

        if (btnDer.isTouched(touchX, touchY)) {

            moviendoDerecha = true
            player.moverDerecha(delta)
        }

        if (btnArriba.isTouched(touchX, touchY)) {

            player.moverArriba(delta)
        }

        if (btnAbajo.isTouched(touchX, touchY)) {

            player.moverAbajo(delta)
        }
    }

    // =========================
    // SALIDA
    // =========================
    private fun revisarSalida(): Boolean {

        if (
            moviendoDerecha
            &&
            player.collisionBox.overlaps(salidaSalon)
        ) {

            cambiandoPantalla = true

            game.screen = EdificioGobiernoSegundoPisoScreen(
                game,
                regresoX,
                regresoY
            )

            return true
        }

        return false
    }

    // =========================
    // UPDATE
    // =========================

    // =========================
    // CÁMARA
    // =========================
    private fun actualizarCamara() {

        val playerCenterX =
            player.x + player.getWidth() / 2f

        val playerCenterY =
            player.y + player.getHeight() / 2f

        val halfViewportWidth =
            camera.viewportWidth / 2f

        val halfViewportHeight =
            camera.viewportHeight / 2f

        val minCameraX = halfViewportWidth
        val maxCameraX = worldWidth - halfViewportWidth

        val minCameraY = halfViewportHeight
        val maxCameraY = worldHeight - halfViewportHeight

        val cameraX =
            if (minCameraX > maxCameraX) {
                worldWidth / 2f
            } else {
                MathUtils.clamp(
                    playerCenterX,
                    minCameraX,
                    maxCameraX
                )
            }

        val cameraY =
            if (minCameraY > maxCameraY) {
                worldHeight / 2f
            } else {
                MathUtils.clamp(
                    playerCenterY,
                    minCameraY,
                    maxCameraY
                )
            }

        camera.position.set(
            cameraX,
            cameraY,
            0f
        )

        camera.update()
    }

    // =========================
    // BOTONES
    // =========================
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
    }

    // =========================
    // MÉTODOS OBLIGATORIOS
    // =========================
    override fun show() {}

    override fun resize(width: Int, height: Int) {

        camera.setToOrtho(
            false,
            width.toFloat(),
            height.toFloat()
        )

        camera.update()

        hudViewport.update(
            width,
            height,
            true
        )

        posicionarBotones()

        actualizarCamara()
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {

        dispose()
    }

    override fun dispose() {

        if (recursosLiberados) {
            return
        }

        shapeRenderer.dispose()

        font.dispose()

        fondoSalon.dispose()

        player.dispose()

        btnIzq.dispose()
        btnDer.dispose()
        btnArriba.dispose()
        btnAbajo.dispose()

        debugManager.dispose()

        recursosLiberados = true
    }
}
