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

class SalonScreen(
    private val game: SilentNullGame,
    private val nombreSalon: String,
    private val regresoX: Float,
    private val regresoY: Float,
    private val pisoRegreso: Int = 1
) : Screen {

    // =========================
    // MUNDO DEL SALON
    // =========================
    private val worldWidth = 1800f
    private val worldHeight = 1300f

    // =========================
    // CAMARAS
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
    // SALIDA DEL SALON
    // =========================
    private val salidaSalon = CollisionBox(
        worldWidth - 260f,
        worldHeight * 0.38f,
        260f,
        420f
    )

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

        // Aparece cerca de la puerta del salon.
        player.setPosition(
            worldWidth - 420f,
            worldHeight * 0.46f
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

        ScreenUtils.clear(0.04f, 0.04f, 0.05f, 1f)

        dibujarSalon()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            nombreSalon,
            120f,
            worldHeight - 120f
        )

        font.draw(
            game.batch,
            "Salida",
            worldWidth - 260f,
            worldHeight * 0.38f + 470f
        )

        player.render(game.batch)

        game.batch.end()

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
    // DIBUJAR SALON
    // =========================
    private fun dibujarSalon() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Piso
        shapeRenderer.color = Color(0.18f, 0.22f, 0.27f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Paredes
        shapeRenderer.color = Color(0.08f, 0.08f, 0.11f, 1f)

        shapeRenderer.rect(
            0f,
            worldHeight - 110f,
            worldWidth,
            110f
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            110f
        )

        shapeRenderer.rect(
            0f,
            0f,
            110f,
            worldHeight
        )

        shapeRenderer.rect(
            worldWidth - 110f,
            0f,
            110f,
            worldHeight
        )

        // Pizarron
        shapeRenderer.color = Color(0.07f, 0.10f, 0.13f, 1f)
        shapeRenderer.rect(
            260f,
            worldHeight - 250f,
            720f,
            70f
        )

        // Escritorio del profesor
        shapeRenderer.color = Color(0.46f, 0.32f, 0.18f, 1f)
        shapeRenderer.rect(
            1130f,
            worldHeight - 310f,
            260f,
            120f
        )

        // Bancas 4x4
        dibujarBancas()

        // Puerta
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            worldWidth - 130f,
            worldHeight * 0.45f,
            80f,
            150f
        )

        // Flecha de salida
        shapeRenderer.color = Color.YELLOW

        dibujarFlechaDerecha(
            worldWidth - 300f,
            salidaSalon.y + salidaSalon.height / 2f,
            45f
        )

        shapeRenderer.end()
    }

    // =========================
    // DIBUJAR BANCAS
    // =========================
    private fun dibujarBancas() {

        val startX = 330f
        val startY = 330f

        val deskWidth = 130f
        val deskHeight = 80f

        val gapX = 210f
        val gapY = 150f

        for (fila in 0 until 4) {

            for (columna in 0 until 4) {

                val x = startX + columna * gapX
                val y = startY + fila * gapY

                // Mesa
                shapeRenderer.color = Color(0.42f, 0.30f, 0.18f, 1f)
                shapeRenderer.rect(
                    x,
                    y,
                    deskWidth,
                    deskHeight
                )

                // Silla
                shapeRenderer.color = Color(0.12f, 0.13f, 0.16f, 1f)
                shapeRenderer.rect(
                    x + 35f,
                    y - 55f,
                    60f,
                    45f
                )
            }
        }
    }

    // =========================
    // FLECHA DERECHA
    // =========================
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

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        procesarInput(delta)

        player.update(delta)

        val salioDelSalon = revisarSalida()

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
    // SALIR DEL SALON
    // =========================
    private fun revisarSalida(): Boolean {

        if (
            moviendoDerecha
            &&
            player.collisionBox.overlaps(salidaSalon)
        ) {

            cambiandoPantalla = true

            game.screen =
                when (pisoRegreso) {

                    1 -> {
                        Edificio2Screen(
                            game,
                            regresoX,
                            regresoY
                        )
                    }

                    2 -> {
                        Edificio2SegundoPisoScreen(
                            game,
                            regresoX,
                            regresoY
                        )
                    }

                    else -> {
                        Edificio2PisoSuperiorScreen(
                            game,
                            pisoRegreso,
                            regresoX,
                            regresoY
                        )
                    }
                }

            return true
        }

        return false
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
    // POSICIONAR BOTONES
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
    // METODOS OBLIGATORIOS
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

        player.dispose()

        btnIzq.dispose()
        btnDer.dispose()
        btnArriba.dispose()
        btnAbajo.dispose()

        recursosLiberados = true
    }
}
