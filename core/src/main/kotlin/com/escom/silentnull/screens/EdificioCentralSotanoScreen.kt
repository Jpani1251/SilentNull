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

class EdificioCentralSotanoScreen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    // =========================
    // MUNDO
    // =========================
    private val worldWidth = 2200f
    private val worldHeight = 1500f

    private val wallSize = 100f

    // =========================
    // CAMARAS
    // =========================
    private val camera = OrthographicCamera()

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    // =========================
    // RECURSOS
    // =========================
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()
    private val player = Player()
    private val debugManager = DebugManager("EdificioCentralSotano", worldWidth, worldHeight)

    // =========================
    // ESCALERAS PARA SUBIR
    // AHORA ESTAN ARRIBA
    // =========================
    private val escalerasWidth = 380f
    private val escalerasHeight = 260f

    private val escalerasX =
        worldWidth / 2f - escalerasWidth / 2f

    private val escalerasY =
        worldHeight - wallSize - escalerasHeight - 40f

    /*
     * Zona de acceso frente a las escaleras.
     * Se extiende hacia abajo para que sea sencillo activarlas.
     */
    private val entradaEscalerasSubir = CollisionBox(
        escalerasX - 70f,
        escalerasY - 180f,
        escalerasWidth + 140f,
        escalerasHeight + 180f
    )

    // =========================
    // ESTADOS
    // =========================
    private var moviendoArriba = false

    private var cambiandoPantalla = false
    private var recursosLiberados = false

    private var tiempoBloqueoAccesos = 0.45f

    // =========================
    // BOTONES
    // =========================
    private val tamanoBoton = 150f

    private val btnIzq = GameButton(
        "btn_izq.png",
        0f,
        0f,
        tamanoBoton,
        tamanoBoton
    )

    private val btnDer = GameButton(
        "btn_der.png",
        0f,
        0f,
        tamanoBoton,
        tamanoBoton
    )

    private val btnArriba = GameButton(
        "btn_arriba.png",
        0f,
        0f,
        tamanoBoton,
        tamanoBoton
    )

    private val btnAbajo = GameButton(
        "btn_abajo.png",
        0f,
        0f,
        tamanoBoton,
        tamanoBoton
    )

    // =========================
    // INIT
    // =========================
    init {

        font.data.setScale(2.2f)

        /*
         * El jugador aparece en la parte inferior-central,
         * dejando espacio suficiente para caminar hacia las
         * escaleras ubicadas arriba.
         */
        player.setPosition(
            spawnX ?: worldWidth / 2f,
            spawnY ?: 420f
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

        ScreenUtils.clear(
            0.025f,
            0.025f,
            0.035f,
            1f
        )

        dibujarSotano()

        game.batch.projectionMatrix =
            camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio Central - Sotano",
            120f,
            worldHeight - 110f
        )

        font.draw(
            game.batch,
            "Escaleras a planta baja",
            escalerasX - 10f,
            escalerasY - 90f
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

        game.batch.projectionMatrix =
            hudCamera.combined

        game.batch.begin()

        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)

        game.batch.end()

        // INVENTARIO
        game.inventoryManager.render(game.batch, hudViewport)
    }

    // =========================
    // DIBUJAR SOTANO
    // =========================
    private fun dibujarSotano() {

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        // Fondo oscuro
        shapeRenderer.color = Color(
            0.08f,
            0.08f,
            0.11f,
            1f
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Área libre para caminar
        shapeRenderer.color = Color(
            0.24f,
            0.24f,
            0.29f,
            1f
        )

        shapeRenderer.rect(
            wallSize,
            wallSize,
            worldWidth - wallSize * 2f,
            worldHeight - wallSize * 2f
        )

        // Área central ligeramente más clara
        shapeRenderer.color = Color(
            0.28f,
            0.28f,
            0.33f,
            1f
        )

        shapeRenderer.rect(
            350f,
            250f,
            worldWidth - 700f,
            worldHeight - 500f
        )

        // Camino vertical hacia las escaleras
        shapeRenderer.color = Color(
            0.32f,
            0.32f,
            0.37f,
            1f
        )

        shapeRenderer.rect(
            worldWidth / 2f - 220f,
            250f,
            440f,
            escalerasY - 250f
        )

        // Línea central decorativa
        shapeRenderer.color = Color(
            0.37f,
            0.37f,
            0.42f,
            1f
        )

        shapeRenderer.rect(
            worldWidth / 2f - 7f,
            250f,
            14f,
            escalerasY - 250f
        )

        // Paredes
        shapeRenderer.color = Color(
            0.035f,
            0.035f,
            0.05f,
            1f
        )

        shapeRenderer.rect(
            0f,
            worldHeight - wallSize,
            worldWidth,
            wallSize
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            wallSize
        )

        shapeRenderer.rect(
            0f,
            0f,
            wallSize,
            worldHeight
        )

        shapeRenderer.rect(
            worldWidth - wallSize,
            0f,
            wallSize,
            worldHeight
        )

        // Escaleras en la parte superior
        dibujarEscaleras(
            escalerasX,
            escalerasY,
            escalerasWidth,
            escalerasHeight
        )

        // Flecha de subida
        shapeRenderer.color = Color.YELLOW

        dibujarFlechaArriba(
            escalerasX + escalerasWidth / 2f,
            escalerasY - 90f,
            45f
        )

        shapeRenderer.end()
    }

    // =========================
    // DIBUJAR ESCALERAS
    // =========================
    private fun dibujarEscaleras(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        // Base
        shapeRenderer.color = Color(
            0.18f,
            0.18f,
            0.23f,
            1f
        )

        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        // Escalones
        shapeRenderer.color = Color(
            0.08f,
            0.08f,
            0.11f,
            1f
        )

        var stepY = y + 28f

        for (i in 0 until 8) {

            shapeRenderer.rect(
                x + 40f,
                stepY,
                width - 80f,
                18f
            )

            stepY += 28f
        }

        // Bordes laterales
        shapeRenderer.color = Color(
            0.05f,
            0.05f,
            0.07f,
            1f
        )

        shapeRenderer.rect(
            x,
            y,
            25f,
            height
        )

        shapeRenderer.rect(
            x + width - 25f,
            y,
            25f,
            height
        )
    }

    // =========================
    // ACCESOS
    // =========================
    private fun revisarAccesos(): Boolean {

        /*
         * Para salir del sotano:
         * el jugador camina hacia arriba y presiona ARRIBA
         * frente a las escaleras.
         */
        if (
            moviendoArriba
            &&
            player.collisionBox.overlaps(
                entradaEscalerasSubir
            )
        ) {

            cambiandoPantalla = true

            /*
             * Regresa cerca de las escaleras del sotano
             * en la planta baja, pero sin aparecer directamente
             * encima de la zona de activacion.
             */
            game.screen = EdificioCentralScreen(
                game,
                1480f,
                650f
            )

            return true
        }

        return false
    }

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        game.inventoryManager.update(delta)

        if (game.inventoryManager.isVisible()) {
            return
        }

        if (cambiandoPantalla) {
            return
        }

        player.guardarPosicionAnterior()

        procesarInput(delta)

        player.update(delta)

        // Colisión con la rejilla (Global)
        if (debugManager.checkCollision(player)) {
            player.revertirMovimiento()
        }

        if (tiempoBloqueoAccesos > 0f) {

            tiempoBloqueoAccesos -= delta

        } else if (revisarAccesos()) {

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

        moviendoArriba = false

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

            moviendoArriba = true
            player.moverArriba(delta)
        }

        if (btnAbajo.isTouched(touchX, touchY)) {

            player.moverAbajo(delta)
        }
    }

    // =========================
    // FLECHA ARRIBA
    // =========================
    private fun dibujarFlechaArriba(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth = size * 0.45f
        val bodyLength = size * 1.4f

        shapeRenderer.triangle(
            centerX,
            centerY + size,
            centerX - size,
            centerY - size,
            centerX + size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX - bodyWidth / 2f,
            centerY - size - bodyLength,
            bodyWidth,
            bodyLength
        )
    }

    // =========================
    // CAMARA
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

        val minCameraX =
            halfViewportWidth

        val maxCameraX =
            worldWidth - halfViewportWidth

        val minCameraY =
            halfViewportHeight

        val maxCameraY =
            worldHeight - halfViewportHeight

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
    // SCREEN
    // =========================
    override fun show() {}

    override fun resize(
        width: Int,
        height: Int
    ) {

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
