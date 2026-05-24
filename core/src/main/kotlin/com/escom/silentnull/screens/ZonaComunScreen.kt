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

class ZonaComunScreen(
    private val game: SilentNullGame
) : Screen {

    // =========================
    // MUNDO ZONA COMÚN
    // =========================
    private val worldWidth = 2200f
    private val worldHeight = 1300f

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
    // SALIDA
    // =========================
    private val salidaZonaComun = CollisionBox(
        900f,
        worldHeight - 230f,
        400f,
        230f
    )

    private var moviendoArriba = false
    private var cambiandoPantalla = false
    private var recursosLiberados = false

    // =========================
    // BOTONES
    // =========================
    private val tamañoBoton = 150f

    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton

    // =========================
    // INIT
    // =========================
    init {

        font.data.setScale(2.5f)

        btnIzq = GameButton(
            "btn_izq.png",
            0f,
            0f,
            tamañoBoton,
            tamañoBoton
        )

        btnDer = GameButton(
            "btn_der.png",
            0f,
            0f,
            tamañoBoton,
            tamañoBoton
        )

        btnArriba = GameButton(
            "btn_arriba.png",
            0f,
            0f,
            tamañoBoton,
            tamañoBoton
        )

        btnAbajo = GameButton(
            "btn_abajo.png",
            0f,
            0f,
            tamañoBoton,
            tamañoBoton
        )

        // Aparece entrando desde arriba hacia la zona común.
        player.setPosition(
            worldWidth * 0.48f,
            worldHeight - 360f
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

        dibujarZonaComun()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Zona comun",
            120f,
            worldHeight - 120f
        )

        font.draw(
            game.batch,
            "Sube para regresar al Edificio de Gobierno",
            650f,
            worldHeight - 150f
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
    // DIBUJAR ZONA COMÚN
    // =========================
    private fun dibujarZonaComun() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Piso
        shapeRenderer.color = Color(0.24f, 0.23f, 0.22f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Paredes
        shapeRenderer.color = Color(0.09f, 0.09f, 0.11f, 1f)

        shapeRenderer.rect(
            0f,
            worldHeight - 120f,
            worldWidth,
            120f
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            120f
        )

        shapeRenderer.rect(
            0f,
            0f,
            120f,
            worldHeight
        )

        shapeRenderer.rect(
            worldWidth - 120f,
            0f,
            120f,
            worldHeight
        )

        // Alfombra / área central
        shapeRenderer.color = Color(0.30f, 0.28f, 0.25f, 1f)
        shapeRenderer.rect(
            260f,
            220f,
            worldWidth - 520f,
            worldHeight - 520f
        )

        // Tres filas de mesas, dos sillas por mesa.
        dibujarMesaConDosSillas(520f, 820f)
        dibujarMesaConDosSillas(920f, 820f)
        dibujarMesaConDosSillas(1320f, 820f)

        dibujarMesaConDosSillas(520f, 570f)
        dibujarMesaConDosSillas(920f, 570f)
        dibujarMesaConDosSillas(1320f, 570f)

        dibujarMesaConDosSillas(520f, 320f)
        dibujarMesaConDosSillas(920f, 320f)
        dibujarMesaConDosSillas(1320f, 320f)

        // Flecha para salir hacia arriba
        shapeRenderer.color = Color.YELLOW

        dibujarFlechaArriba(
            salidaZonaComun.x + salidaZonaComun.width / 2f,
            salidaZonaComun.y - 120f,
            80f
        )

        shapeRenderer.end()
    }

    // =========================
    // MESA CON 2 SILLAS
    // =========================
    private fun dibujarMesaConDosSillas(
        x: Float,
        y: Float
    ) {

        // Mesa
        shapeRenderer.color = Color(0.46f, 0.32f, 0.18f, 1f)
        shapeRenderer.rect(
            x,
            y,
            160f,
            90f
        )

        // Silla izquierda
        shapeRenderer.color = Color(0.12f, 0.13f, 0.16f, 1f)
        shapeRenderer.rect(
            x - 80f,
            y + 20f,
            60f,
            55f
        )

        // Silla derecha
        shapeRenderer.rect(
            x + 180f,
            y + 20f,
            60f,
            55f
        )
    }

    // =========================
    // FLECHA ARRIBA
    // =========================
    private fun dibujarFlechaArriba(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        shapeRenderer.triangle(
            centerX,
            centerY + size,
            centerX - size,
            centerY - size,
            centerX + size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX - 25f,
            centerY - size - 120f,
            50f,
            120f
        )
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

        val salioDeZonaComun = revisarSalida()

        if (salioDeZonaComun) {
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
    // SALIR
    // =========================
    private fun revisarSalida(): Boolean {

        if (
            moviendoArriba
            &&
            player.collisionBox.overlaps(salidaZonaComun)
        ) {

            cambiandoPantalla = true

            game.screen = GobiernoScreen(game)

            return true
        }

        return false
    }

    // =========================
    // ACTUALIZAR CÁMARA
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
        btnIzq.y = margenY + tamañoBoton

        btnDer.x = margenX + tamañoBoton * 2f
        btnDer.y = margenY + tamañoBoton

        btnArriba.x = margenX + tamañoBoton
        btnArriba.y = margenY + tamañoBoton * 2f

        btnAbajo.x = margenX + tamañoBoton
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

    // =========================
    // DISPOSE
    // =========================
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
