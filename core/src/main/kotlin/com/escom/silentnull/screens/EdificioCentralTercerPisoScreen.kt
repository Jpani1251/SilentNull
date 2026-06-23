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
import com.escom.silentnull.ui.GameButton

class EdificioCentralTercerPisoScreen(
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
    // PASILLO
    // =========================
    private val corridorY = 500f
    private val corridorHeight = 500f

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

    // =========================
    // CONEXION IZQUIERDA
    // EDIFICIO 2 - TERCER PISO
    // =========================
    private val salidaEdificio2TercerPiso = CollisionBox(
        0f,
        corridorY + 80f,
        420f,
        corridorHeight - 160f
    )

    // =========================
    // CONEXION DERECHA
    // EDIFICIO 1 - TERCER PISO
    // =========================
    private val salidaEdificio1TercerPiso = CollisionBox(
        worldWidth - 420f,
        corridorY + 80f,
        420f,
        corridorHeight - 160f
    )

    // =========================
    // ESCALERAS PARA BAJAR
    // =========================
    private val escalerasWidth = 380f
    private val escalerasHeight = 260f

    private val escalerasX =
        worldWidth / 2f - escalerasWidth / 2f

    private val escalerasY = 170f

    private val entradaEscalerasBajar = CollisionBox(
        escalerasX - 70f,
        escalerasY,
        escalerasWidth + 140f,
        escalerasHeight + 220f
    )

    // =========================
    // ESTADOS
    // =========================
    private var moviendoIzquierda = false
    private var moviendoDerecha = false
    private var moviendoAbajo = false

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

        player.setPosition(
            spawnX ?: worldWidth / 2f,
            spawnY ?: corridorY + corridorHeight / 2f
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
            0.04f,
            0.04f,
            0.05f,
            1f
        )

        dibujarTercerPiso()

        game.batch.projectionMatrix =
            camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio Central - Tercer piso",
            120f,
            worldHeight - 110f
        )

        font.draw(
            game.batch,
            "Edificio 2 - Tercer piso",
            120f,
            corridorY + corridorHeight / 2f + 35f
        )

        font.draw(
            game.batch,
            "Edificio 1 - Tercer piso",
            worldWidth - 470f,
            corridorY + corridorHeight / 2f + 35f
        )

        font.draw(
            game.batch,
            "Escaleras al segundo piso",
            escalerasX - 10f,
            escalerasY + escalerasHeight + 105f
        )

        player.render(game.batch)

        game.batch.end()

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
    }

    // =========================
    // DIBUJO
    // =========================
    private fun dibujarTercerPiso() {

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        // Fondo general
        shapeRenderer.color = Color(
            0.20f,
            0.20f,
            0.24f,
            1f
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Pasillo horizontal
        shapeRenderer.color = Color(
            0.34f,
            0.34f,
            0.39f,
            1f
        )

        shapeRenderer.rect(
            wallSize,
            corridorY,
            worldWidth - wallSize * 2f,
            corridorHeight
        )

        // Camino hacia las escaleras
        shapeRenderer.color = Color(
            0.32f,
            0.32f,
            0.37f,
            1f
        )

        shapeRenderer.rect(
            worldWidth / 2f - 300f,
            120f,
            600f,
            corridorY - 120f
        )

        // Conexion izquierda
        shapeRenderer.color = Color(
            0.37f,
            0.37f,
            0.42f,
            1f
        )

        shapeRenderer.rect(
            wallSize,
            corridorY + 80f,
            500f,
            corridorHeight - 160f
        )

        // Conexion derecha
        shapeRenderer.rect(
            worldWidth - 600f,
            corridorY + 80f,
            500f,
            corridorHeight - 160f
        )

        // Linea central
        shapeRenderer.color = Color(
            0.42f,
            0.42f,
            0.47f,
            1f
        )

        shapeRenderer.rect(
            wallSize,
            corridorY + corridorHeight / 2f - 7f,
            worldWidth - wallSize * 2f,
            14f
        )

        // Paredes exteriores
        shapeRenderer.color = Color(
            0.08f,
            0.08f,
            0.11f,
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

        // Puerta izquierda
        shapeRenderer.color = Color(
            0.55f,
            0.38f,
            0.20f,
            1f
        )

        shapeRenderer.rect(
            wallSize,
            corridorY + corridorHeight / 2f - 130f,
            100f,
            260f
        )

        // Puerta derecha
        shapeRenderer.rect(
            worldWidth - wallSize - 100f,
            corridorY + corridorHeight / 2f - 130f,
            100f,
            260f
        )

        // Escaleras
        dibujarEscaleras(
            escalerasX,
            escalerasY,
            escalerasWidth,
            escalerasHeight
        )

        // Flechas
        shapeRenderer.color = Color.YELLOW

        // Ir al tercer piso del Edificio 2
        dibujarFlechaIzquierda(
            380f,
            corridorY + corridorHeight / 2f,
            45f
        )

        // Ir al tercer piso del Edificio 1
        dibujarFlechaDerecha(
            worldWidth - 380f,
            corridorY + corridorHeight / 2f,
            45f
        )

        // Bajar al segundo piso
        dibujarFlechaAbajo(
            escalerasX + escalerasWidth / 2f,
            escalerasY + escalerasHeight + 85f,
            45f
        )

        shapeRenderer.end()
    }

    // =========================
    // ESCALERAS
    // =========================
    private fun dibujarEscaleras(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        shapeRenderer.color = Color(
            0.24f,
            0.24f,
            0.29f,
            1f
        )

        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color = Color(
            0.12f,
            0.12f,
            0.16f,
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
    }

    // =========================
    // ACCESOS
    // =========================
    private fun revisarAccesos(): Boolean {

        // Ir al tercer piso del Edificio 2
        if (
            moviendoIzquierda &&
            player.collisionBox.overlaps(
                salidaEdificio2TercerPiso
            )
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2PisoSuperiorScreen(
                game,
                3,
                1355f,
                2800f
            )

            return true
        }

        // Ir al tercer piso del Edificio 1
        if (
            moviendoDerecha &&
            player.collisionBox.overlaps(
                salidaEdificio1TercerPiso
            )
        ) {

            cambiandoPantalla = true

            game.screen = Edificio1PisoSuperiorScreen(
                game,
                3,
                850f,
                2800f
            )

            return true
        }

        // Bajar al segundo piso del Edificio Central
        if (
            moviendoAbajo &&
            player.collisionBox.overlaps(
                entradaEscalerasBajar
            )
        ) {

            cambiandoPantalla = true

            game.screen =
                EdificioCentralSegundoPisoScreen(
                    game,
                    worldWidth / 2f,
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

        if (cambiandoPantalla) {
            return
        }

        player.guardarPosicionAnterior()

        procesarInput(delta)

        player.update(delta)

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

        moviendoIzquierda = false
        moviendoDerecha = false
        moviendoAbajo = false

        if (!Gdx.input.isTouched) {
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

        if (btnIzq.isTouched(touchX, touchY)) {

            moviendoIzquierda = true
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

            moviendoAbajo = true
            player.moverAbajo(delta)
        }
    }

    // =========================
    // FLECHAS
    // =========================
    private fun dibujarFlechaIzquierda(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth = size * 0.45f
        val bodyLength = size * 1.4f

        shapeRenderer.triangle(
            centerX - size,
            centerY,
            centerX + size,
            centerY + size,
            centerX + size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX + size,
            centerY - bodyWidth / 2f,
            bodyLength,
            bodyWidth
        )
    }

    private fun dibujarFlechaDerecha(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth = size * 0.45f
        val bodyLength = size * 1.4f

        shapeRenderer.triangle(
            centerX + size,
            centerY,
            centerX - size,
            centerY + size,
            centerX - size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX - size - bodyLength,
            centerY - bodyWidth / 2f,
            bodyLength,
            bodyWidth
        )
    }

    private fun dibujarFlechaAbajo(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth = size * 0.45f
        val bodyLength = size * 1.4f

        shapeRenderer.triangle(
            centerX,
            centerY - size,
            centerX - size,
            centerY + size,
            centerX + size,
            centerY + size
        )

        shapeRenderer.rect(
            centerX - bodyWidth / 2f,
            centerY + size,
            bodyWidth,
            bodyLength
        )
    }

    // =========================
    // CAMARA
    // =========================
    private fun actualizarCamara() {

        val centerX =
            player.x + player.getWidth() / 2f

        val centerY =
            player.y + player.getHeight() / 2f

        val halfWidth =
            camera.viewportWidth / 2f

        val halfHeight =
            camera.viewportHeight / 2f

        val cameraX =
            if (halfWidth > worldWidth - halfWidth) {

                worldWidth / 2f

            } else {

                MathUtils.clamp(
                    centerX,
                    halfWidth,
                    worldWidth - halfWidth
                )
            }

        val cameraY =
            if (halfHeight > worldHeight - halfHeight) {

                worldHeight / 2f

            } else {

                MathUtils.clamp(
                    centerY,
                    halfHeight,
                    worldHeight - halfHeight
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

        recursosLiberados = true
    }
}
