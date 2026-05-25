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

class EdificioGobiernoSegundoPisoScreen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    // =========================
    // MUNDO
    // =========================
    private val worldWidth = 1800f
    private val worldHeight = 1200f

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
    // CONEXIÓN DE REGRESO AL EDIFICIO 2
    // =========================
    private val salidaEdificio2 = CollisionBox(
        0f,
        worldHeight / 2f - 170f,
        260f,
        340f
    )

    // =========================
    // ESTADOS
    // =========================
    private var moviendoIzquierda = false
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

        font.data.setScale(2.2f)

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

        // Aparece entrando desde el Edificio 2.
        player.setPosition(
            spawnX ?: 280f,
            spawnY ?: worldHeight / 2f
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

        dibujarEdificioGobiernoSegundoPiso()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio de Gobierno - Segundo piso",
            120f,
            worldHeight - 120f
        )

        font.draw(
            game.batch,
            "Regresar a Edificio 2",
            130f,
            worldHeight / 2f + 230f
        )

        font.draw(
            game.batch,
            "Zona superior del edificio de gobierno",
            560f,
            worldHeight / 2f + 40f
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
    // DIBUJAR ESCENARIO
    // =========================
    private fun dibujarEdificioGobiernoSegundoPiso() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Piso general
        shapeRenderer.color = Color(0.23f, 0.23f, 0.27f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Pasillo principal
        shapeRenderer.color = Color(0.33f, 0.33f, 0.38f, 1f)
        shapeRenderer.rect(
            180f,
            250f,
            worldWidth - 360f,
            700f
        )

        // Paredes exteriores
        shapeRenderer.color = Color(0.08f, 0.08f, 0.11f, 1f)

        shapeRenderer.rect(
            0f,
            worldHeight - 100f,
            worldWidth,
            100f
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            100f
        )

        shapeRenderer.rect(
            0f,
            0f,
            100f,
            worldHeight
        )

        shapeRenderer.rect(
            worldWidth - 100f,
            0f,
            100f,
            worldHeight
        )

        // Entrada desde Edificio 2
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            100f,
            worldHeight / 2f - 90f,
            100f,
            180f
        )

        // Área superior provisional
        shapeRenderer.color = Color(0.18f, 0.25f, 0.32f, 1f)
        shapeRenderer.rect(
            600f,
            520f,
            580f,
            260f
        )

        // Detalle tipo oficina/sala
        shapeRenderer.color = Color(0.08f, 0.10f, 0.14f, 1f)
        shapeRenderer.rect(
            680f,
            650f,
            420f,
            55f
        )

        // Flecha de regreso hacia Edificio 2
        shapeRenderer.color = Color.YELLOW

        dibujarFlechaIzquierda(
            300f,
            worldHeight / 2f,
            45f
        )

        shapeRenderer.end()
    }

    // =========================
    // REVISAR ACCESOS
    // =========================
    private fun revisarAccesos() {

        if (
            moviendoIzquierda
            &&
            player.collisionBox.overlaps(salidaEdificio2)
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2SegundoPisoScreen(
                game,
                930f + 850f / 2f,
                420f + 260f / 2f
            )

            dispose()

            return
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

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        procesarInput(delta)

        player.update(delta)

        revisarAccesos()

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

        moviendoIzquierda = false

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
