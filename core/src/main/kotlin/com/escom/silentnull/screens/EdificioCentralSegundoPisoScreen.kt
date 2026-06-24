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

class EdificioCentralSegundoPisoScreen(
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
    private val debugManager = DebugManager("EdificioCentral2P", worldWidth, worldHeight)

    // =========================
    // CONEXION IZQUIERDA
    // EDIFICIO 2 SEGUNDO PISO
    // =========================
    private val salidaEdificio2SegundoPiso = CollisionBox(
        0f,
        corridorY + 80f,
        420f,
        corridorHeight - 160f
    )

    // =========================
    // CONEXION DERECHA
    // EDIFICIO 1 SEGUNDO PISO
    // =========================
    private val salidaEdificio1SegundoPiso = CollisionBox(
        worldWidth - 420f,
        corridorY + 80f,
        420f,
        corridorHeight - 160f
    )

    // =========================
    // ESCALERAS PARA BAJAR A PB
    // IZQUIERDA
    // =========================
    private val escaleraBajarX = 700f
    private val escaleraBajarY = 170f
    private val escaleraBajarWidth = 360f
    private val escaleraBajarHeight = 260f

    private val entradaEscaleraBajar = CollisionBox(
        escaleraBajarX - 60f,
        escaleraBajarY,
        escaleraBajarWidth + 120f,
        escaleraBajarHeight + 200f
    )

    // =========================
    // ESCALERAS PARA SUBIR A 3P
    // DERECHA
    // =========================
    private val escaleraSubirX = 1240f
    private val escaleraSubirY = 170f
    private val escaleraSubirWidth = 360f
    private val escaleraSubirHeight = 260f

    private val entradaEscaleraSubir = CollisionBox(
        escaleraSubirX - 60f,
        escaleraSubirY,
        escaleraSubirWidth + 120f,
        escaleraSubirHeight + 200f
    )

    // =========================
    // ESTADOS
    // =========================
    private var moviendoIzquierda = false
    private var moviendoDerecha = false
    private var moviendoAbajo = false

    private var cambiandoPantalla = false
    private var recursosLiberados = false

    private var tiempoBloqueoAccesos = 0.4f

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

        dibujarEdificioCentralSegundoPiso()

        game.batch.projectionMatrix =
            camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio Central - Segundo piso",
            120f,
            worldHeight - 110f
        )

        font.draw(
            game.batch,
            "Edificio 2 - Segundo piso",
            120f,
            corridorY + corridorHeight / 2f + 35f
        )

        font.draw(
            game.batch,
            "Edificio 1 - Segundo piso",
            worldWidth - 470f,
            corridorY + corridorHeight / 2f + 35f
        )

        font.draw(
            game.batch,
            "Escaleras a planta baja",
            escaleraBajarX - 20f,
            escaleraBajarY + escaleraBajarHeight + 100f
        )

        font.draw(
            game.batch,
            "Escaleras a tercer piso",
            escaleraSubirX,
            escaleraSubirY + escaleraSubirHeight + 100f
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
    }

    // =========================
    // DIBUJO
    // =========================
    private fun dibujarEdificioCentralSegundoPiso() {

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

        // Zona inferior para acceder a escaleras
        shapeRenderer.color = Color(
            0.32f,
            0.32f,
            0.37f,
            1f
        )

        shapeRenderer.rect(
            500f,
            120f,
            worldWidth - 1000f,
            corridorY - 120f
        )

        // Conexión izquierda
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

        // Conexión derecha
        shapeRenderer.rect(
            worldWidth - 600f,
            corridorY + 80f,
            500f,
            corridorHeight - 160f
        )

        // Línea central
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

        // Paredes
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

        // Escalera para bajar
        dibujarEscaleras(
            escaleraBajarX,
            escaleraBajarY,
            escaleraBajarWidth,
            escaleraBajarHeight
        )

        // Escalera para subir
        dibujarEscaleras(
            escaleraSubirX,
            escaleraSubirY,
            escaleraSubirWidth,
            escaleraSubirHeight
        )

        // Flechas
        shapeRenderer.color = Color.YELLOW

        // Edificio 2
        dibujarFlechaIzquierda(
            380f,
            corridorY + corridorHeight / 2f,
            45f
        )

        // Edificio 1
        dibujarFlechaDerecha(
            worldWidth - 380f,
            corridorY + corridorHeight / 2f,
            45f
        )

        // Bajar a planta baja
        dibujarFlechaAbajo(
            escaleraBajarX + escaleraBajarWidth / 2f,
            escaleraBajarY + escaleraBajarHeight + 75f,
            45f
        )

        // Subir a tercer piso
        dibujarFlechaAbajo(
            escaleraSubirX + escaleraSubirWidth / 2f,
            escaleraSubirY + escaleraSubirHeight + 75f,
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

        // Ir al segundo piso del Edificio 2
        if (
            moviendoIzquierda &&
            player.collisionBox.overlaps(
                salidaEdificio2SegundoPiso
            )
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2SegundoPisoScreen(
                game,
                1355f,
                2800f
            )

            return true
        }

        // Ir al segundo piso del Edificio 1
        if (
            moviendoDerecha &&
            player.collisionBox.overlaps(
                salidaEdificio1SegundoPiso
            )
        ) {

            cambiandoPantalla = true

            game.screen = Edificio1SegundoPisoScreen(
                game,
                850f,
                3550f
            )

            return true
        }

        // Bajar a planta baja
        if (
            moviendoAbajo &&
            player.collisionBox.overlaps(
                entradaEscaleraBajar
            )
        ) {

            cambiandoPantalla = true

            game.screen = EdificioCentralScreen(
                game,
                escaleraBajarX + escaleraBajarWidth / 2f,
                650f
            )

            return true
        }

        // Subir al tercer piso
        if (
            moviendoAbajo &&
            player.collisionBox.overlaps(
                entradaEscaleraSubir
            )
        ) {

            cambiandoPantalla = true

            game.screen = EdificioCentralTercerPisoScreen(
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

        moviendoIzquierda = false
        moviendoDerecha = false
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

        // Delegar al DebugManager
        if (debugManager.procesarInput(touchX, touchY, camera)) {
            return
        }

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

        debugManager.dispose()

        recursosLiberados = true
    }
}
