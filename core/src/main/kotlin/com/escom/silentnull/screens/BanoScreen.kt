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

class BanoScreen(
    private val game: SilentNullGame,
    private val nombreBano: String,
    private val regresoX: Float,
    private val regresoY: Float,
    private val pisoRegreso: Int = 1
) : Screen {

    // =========================
    // MUNDO DEL BANO
    // =========================
    private val worldWidth = 1500f
    private val worldHeight = 1000f

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
    // SALIDA DEL BANO
    // =========================
    private val salidaBano = CollisionBox(
        520f,
        worldHeight - 230f,
        460f,
        230f
    )

    private var moviendoArriba = false
    private var cambiandoPantalla = false

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

        // Aparece dentro del bano, cerca de la salida.
        player.setPosition(
            worldWidth / 2f,
            worldHeight - 390f
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

        update(delta)

        if (cambiandoPantalla) {
            return
        }

        ScreenUtils.clear(0.04f, 0.04f, 0.05f, 1f)

        dibujarBano()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            nombreBano,
            120f,
            worldHeight - 120f
        )

        font.draw(
            game.batch,
            "Salida",
            650f,
            worldHeight - 170f
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
    // DIBUJAR BANO
    // =========================
    private fun dibujarBano() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Piso
        shapeRenderer.color = Color(0.17f, 0.20f, 0.23f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Paredes
        shapeRenderer.color = Color(0.08f, 0.08f, 0.10f, 1f)

        // Pared superior
        shapeRenderer.rect(
            0f,
            worldHeight - 100f,
            worldWidth,
            100f
        )

        // Pared inferior
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            100f
        )

        // Pared izquierda
        shapeRenderer.rect(
            0f,
            0f,
            100f,
            worldHeight
        )

        // Pared derecha
        shapeRenderer.rect(
            worldWidth - 100f,
            0f,
            100f,
            worldHeight
        )

        // =========================
        // CABINAS
        // =========================
        shapeRenderer.color = Color(0.22f, 0.22f, 0.26f, 1f)

        shapeRenderer.rect(
            250f,
            250f,
            160f,
            220f
        )

        shapeRenderer.rect(
            520f,
            250f,
            160f,
            220f
        )

        shapeRenderer.rect(
            790f,
            250f,
            160f,
            220f
        )

        shapeRenderer.rect(
            1060f,
            250f,
            160f,
            220f
        )

        // =========================
        // LAVABOS
        // =========================
        shapeRenderer.color = Color(0.70f, 0.70f, 0.75f, 1f)

        shapeRenderer.rect(
            270f,
            150f,
            120f,
            70f
        )

        shapeRenderer.rect(
            540f,
            150f,
            120f,
            70f
        )

        shapeRenderer.rect(
            810f,
            150f,
            120f,
            70f
        )

        shapeRenderer.rect(
            1080f,
            150f,
            120f,
            70f
        )

        // =========================
        // PUERTA / SALIDA
        // =========================
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)

        shapeRenderer.rect(
            salidaBano.x + salidaBano.width / 2f - 70f,
            worldHeight - 105f,
            140f,
            70f
        )

        // Flecha de salida
        shapeRenderer.color = Color.YELLOW

        dibujarFlechaArriba(
            salidaBano.x + salidaBano.width / 2f,
            salidaBano.y - 70f,
            45f
        )

        shapeRenderer.end()
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
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        procesarInput(delta)

        player.update(delta)

        revisarSalida()

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

        moviendoArriba = false

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
    // SALIR DEL BANO
    // =========================
    private fun revisarSalida() {

        if (
            moviendoArriba
            &&
            player.collisionBox.overlaps(salidaBano)
        ) {

            cambiandoPantalla = true

            game.screen =
                if (pisoRegreso == 1) {
                    Edificio2Screen(
                        game,
                        regresoX,
                        regresoY
                    )
                } else {
                    Edificio2PisoSuperiorScreen(
                        game,
                        pisoRegreso,
                        regresoX,
                        regresoY
                    )
                }

            dispose()
        }
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

    override fun hide() {}

    override fun dispose() {

        shapeRenderer.dispose()

        font.dispose()

        player.dispose()

        btnIzq.dispose()
        btnDer.dispose()
        btnArriba.dispose()
        btnAbajo.dispose()
    }
}
