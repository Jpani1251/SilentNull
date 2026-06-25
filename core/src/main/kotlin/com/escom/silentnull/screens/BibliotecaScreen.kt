package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
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

class BibliotecaScreen(
    private val game: SilentNullGame
) : Screen {

    private val worldWidth = 2200f
    private val worldHeight = 1300f

    private val camera = OrthographicCamera()

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()

    private val player = Player()
    private val debugManager = DebugManager("Biblioteca", worldWidth, worldHeight)

    private val salidaBiblioteca = CollisionBox(
        900f,
        0f,
        400f,
        220f
    )

    private var moviendoAbajo = false
    private var cambiandoPantalla = false
    private var recursosLiberados = false

    private val tamañoBoton = 150f

    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton

    init {

        font.data.setScale(2.5f)

        btnIzq = GameButton("btn_izq.png", 0f, 0f, tamañoBoton, tamañoBoton)
        btnDer = GameButton("btn_der.png", 0f, 0f, tamañoBoton, tamañoBoton)
        btnArriba = GameButton("btn_arriba.png", 0f, 0f, tamañoBoton, tamañoBoton)
        btnAbajo = GameButton("btn_abajo.png", 0f, 0f, tamañoBoton, tamañoBoton)

        player.setPosition(
            worldWidth * 0.48f,
            260f
        )

        resize(
            Gdx.graphics.width,
            Gdx.graphics.height
        )
    }

    override fun render(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        update(delta)

        if (cambiandoPantalla) {
            return
        }

        ScreenUtils.clear(0.03f, 0.04f, 0.06f, 1f)

        dibujarBiblioteca()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Biblioteca",
            120f,
            worldHeight - 120f
        )

        font.draw(
            game.batch,
            "Baja para regresar al Edificio de Gobierno",
            680f,
            180f
        )

        player.render(game.batch)

        game.batch.end()

        // =========================
        // DEBUG TOOLS
        // =========================
        debugManager.render(game.batch, camera, hudCamera, player)

        hudViewport.apply()

        game.batch.projectionMatrix = hudCamera.combined

        game.batch.begin()

        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)

        game.batch.end()

        // INVENTARIO
        game.inventoryManager.render(game.batch, hudViewport)
    }

    private fun dibujarBiblioteca() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Piso
        shapeRenderer.color = Color(0.17f, 0.20f, 0.24f, 1f)
        shapeRenderer.rect(0f, 0f, worldWidth, worldHeight)

        // Paredes
        shapeRenderer.color = Color(0.08f, 0.09f, 0.12f, 1f)
        shapeRenderer.rect(0f, worldHeight - 120f, worldWidth, 120f)
        shapeRenderer.rect(0f, 0f, worldWidth, 120f)
        shapeRenderer.rect(0f, 0f, 120f, worldHeight)
        shapeRenderer.rect(worldWidth - 120f, 0f, 120f, worldHeight)

        // Estantes
        shapeRenderer.color = Color(0.32f, 0.22f, 0.14f, 1f)

        for (i in 0 until 5) {
            shapeRenderer.rect(
                260f + i * 350f,
                780f,
                160f,
                360f
            )
        }

        for (i in 0 until 4) {
            shapeRenderer.rect(
                380f + i * 420f,
                420f,
                260f,
                100f
            )
        }

        // Mesas de lectura
        shapeRenderer.color = Color(0.44f, 0.33f, 0.20f, 1f)
        shapeRenderer.rect(720f, 250f, 220f, 100f)
        shapeRenderer.rect(1180f, 250f, 220f, 100f)

        // Flecha de salida
        shapeRenderer.color = Color.YELLOW
        dibujarFlechaAbajo(
            salidaBiblioteca.x + salidaBiblioteca.width / 2f,
            salidaBiblioteca.y + salidaBiblioteca.height + 170f,
            80f
        )

        shapeRenderer.end()
    }

    private fun dibujarFlechaAbajo(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        shapeRenderer.triangle(
            centerX,
            centerY - size,
            centerX - size,
            centerY + size,
            centerX + size,
            centerY + size
        )

        shapeRenderer.rect(
            centerX - 25f,
            centerY + size,
            50f,
            120f
        )
    }

    private fun update(delta: Float) {

        game.inventoryManager.update(delta)

        procesarInput(delta)

        if (game.inventoryManager.isVisible()) {
            return
        }

        if (cambiandoPantalla) {
            return
        }

        player.guardarPosicionAnterior()

        player.update(delta)

        // Colisión con la rejilla (Global)
        if (debugManager.checkCollision(player)) {
            player.revertirMovimiento()
        }

        val salioDeBiblioteca = revisarSalida()

        if (salioDeBiblioteca) {
            return
        }

        player.limitarPantalla(
            worldWidth,
            worldHeight
        )

        actualizarCamara()
    }

    private fun procesarInput(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        moviendoAbajo = false

        if (!Gdx.input.isTouched) {
            debugManager.procesarInput(0f, 0f, camera)
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

        // Manejar Inventario
        if (game.inventoryManager.handleInput(touchX, touchY)) {
            return
        }

        // Delegar al DebugManager
        if (debugManager.procesarInput(touchX, touchY, camera)) {
            return
        }

        if (btnIzq.isTouched(touchX, touchY)) {
            player.moverIzquierda(delta)
        }

        if (btnDer.isTouched(touchX, touchY)) {
            player.moverDerecha(delta)
        }

        if (btnArriba.isTouched(touchX, touchY)) {
            player.moverArriba(delta)
        }

        if (btnAbajo.isTouched(touchX, touchY)) {
            moviendoAbajo = true
            player.moverAbajo(delta)
        }
    }

    private fun revisarSalida(): Boolean {

        if (
            moviendoAbajo
            &&
            player.collisionBox.overlaps(salidaBiblioteca)
        ) {

            cambiandoPantalla = true

            game.screen = GobiernoScreen(game)

            return true
        }

        return false
    }

    private fun actualizarCamara() {

        val playerCenterX = player.x + player.getWidth() / 2f
        val playerCenterY = player.y + player.getHeight() / 2f

        val halfViewportWidth = camera.viewportWidth / 2f
        val halfViewportHeight = camera.viewportHeight / 2f

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

    private fun posicionarBotones() {

        val margenX = 50f
        val margenY = 50f

        btnIzq.x = margenX
        btnIzq.y = margenY + tamañoBoton

        btnDer.x = margenX + tamañoBoton * 2f
        btnDer.y = margenY + tamañoBoton

        btnArriba.x = margenX + tamañoBoton
        btnArriba.y = margenY + tamañoBoton * 2f

        btnAbajo.x = margenX + tamañoBoton
        btnAbajo.y = margenY
    }

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

        player.dispose()

        btnIzq.dispose()
        btnDer.dispose()
        btnArriba.dispose()
        btnAbajo.dispose()

        debugManager.dispose()

        recursosLiberados = true
    }
}
