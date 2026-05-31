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
import com.escom.silentnull.ui.GameButton

class EdificioGobiernoScreen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    // =========================
    // MUNDO
    // =========================
    private val worldWidth = 1800f
    private val worldHeight = 1400f

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
    // ZONAS IMPORTANTES
    // =========================

    // Salida hacia Edificio 2
    private val salidaEdificio2X = 0f
    private val salidaEdificio2Y = worldHeight / 2f - 230f
    private val salidaEdificio2Width = 420f
    private val salidaEdificio2Height = 460f

    // Escaleras centradas y más abajo,
    // pegadas a la parte inferior del pasillo.
    private val escaleraX = worldWidth / 2f - 170f
    private val escaleraY = 210f
    private val escaleraWidth = 340f
    private val escaleraHeight = 240f

    // =========================
    // ESTADOS
    // =========================
    private var moviendoIzquierda = false
    private var moviendoAbajo = false
    private var cambiandoPantalla = false

    private var tiempoBloqueoAccesos = 0.35f

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

        // Aparece entrando desde Edificio 2
        player.setPosition(
            spawnX ?: 430f,
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

        dibujarEdificioGobiernoPlantaBaja()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio de Gobierno - Planta baja",
            120f,
            worldHeight - 120f
        )

        font.draw(
            game.batch,
            "Pasillo hacia Edificio 2",
            120f,
            worldHeight / 2f + 295f
        )

        font.draw(
            game.batch,
            "Escaleras a segundo piso",
            escaleraX - 35f,
            escaleraY + escaleraHeight + 95f
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
    private fun dibujarEdificioGobiernoPlantaBaja() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Fondo general
        shapeRenderer.color = Color(0.23f, 0.23f, 0.27f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Pasillo principal amplio
        shapeRenderer.color = Color(0.33f, 0.33f, 0.38f, 1f)
        shapeRenderer.rect(
            180f,
            170f,
            worldWidth - 360f,
            worldHeight - 340f
        )

        // Conexión ancha hacia Edificio 2
        shapeRenderer.color = Color(0.35f, 0.35f, 0.40f, 1f)
        shapeRenderer.rect(
            100f,
            worldHeight / 2f - 190f,
            460f,
            380f
        )

        // Línea decorativa central del pasillo
        shapeRenderer.color = Color(0.40f, 0.40f, 0.45f, 1f)
        shapeRenderer.rect(
            worldWidth / 2f - 7f,
            170f,
            14f,
            worldHeight - 340f
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

        // Puerta / unión hacia Edificio 2
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            100f,
            worldHeight / 2f - 130f,
            100f,
            260f
        )

        // Escaleras abajo, al centro
        dibujarEscaleras(
            escaleraX,
            escaleraY,
            escaleraWidth,
            escaleraHeight
        )

        // Flechas
        shapeRenderer.color = Color.YELLOW

        // Regresar a Edificio 2
        dibujarFlechaIzquierda(
            370f,
            worldHeight / 2f,
            45f
        )

        // Subir por escaleras con flecha ABAJO
        dibujarFlechaAbajo(
            escaleraX + escaleraWidth / 2f,
            escaleraY + escaleraHeight + 70f,
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

        shapeRenderer.color = Color(0.24f, 0.24f, 0.29f, 1f)
        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color = Color(0.13f, 0.13f, 0.17f, 1f)

        var stepY = y + 28f

        for (i in 0 until 7) {

            shapeRenderer.rect(
                x + 35f,
                stepY,
                width - 70f,
                18f
            )

            stepY += 28f
        }
    }

    // =========================
    // REVISAR ACCESOS
    // =========================
    private fun revisarAccesos() {

        // Regresar al Edificio 2 planta baja
        if (
            moviendoIzquierda
            &&
            player.x <= salidaEdificio2X + salidaEdificio2Width
            &&
            player.y + player.getHeight() >= salidaEdificio2Y
            &&
            player.y <= salidaEdificio2Y + salidaEdificio2Height
        ) {

            cambiandoPantalla = true

            game.screen = PasilloScreen(
                game,
                800f,
                900f
            )

            dispose()

            return
        }

        // Subir al segundo piso del Edificio de Gobierno
        if (
            moviendoAbajo
            &&
            player.x + player.getWidth() >= escaleraX
            &&
            player.x <= escaleraX + escaleraWidth
            &&
            player.y + player.getHeight() >= escaleraY
            &&
            player.y <= escaleraY + escaleraHeight
        ) {

            cambiandoPantalla = true

            game.screen = EdificioGobiernoSegundoPisoScreen(
                game,
                900f,
                700f
            )

            dispose()

            return
        }
    }

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        procesarInput(delta)

        player.update(delta)

        if (tiempoBloqueoAccesos > 0f) {
            tiempoBloqueoAccesos -= delta
        } else {
            revisarAccesos()
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

        moviendoIzquierda = false
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
    override fun show() {
        game.playBackgroundMusic()
    }

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
