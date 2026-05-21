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
import com.escom.silentnull.ui.DialogueBox
import com.escom.silentnull.ui.GameButton

class AuditorioScreen(
    private val game: SilentNullGame
) : Screen {

    private val worldWidth = 2400f
    private val worldHeight = 1400f

    private val camera = OrthographicCamera()

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()

    private val player = Player()

    // =========================
    // SISTEMA DE DIALOGOS
    // =========================
    private lateinit var dialogueBox: DialogueBox

    private val salidaAuditorio = CollisionBox(
        0f,
        worldHeight * 0.40f,
        260f,
        420f
    )

    private var moviendoIzquierda = false
    private var cambiandoPantalla = false

    private val tamañoBoton = 150f

    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton

    init {

        font.data.setScale(2.6f)

        // Inicializar cuadro de diálogo
        dialogueBox = DialogueBox(
            font,
            width = 1200f,
            height = 250f,
            screenWidth = 1280f // Usaremos un ancho virtual para el HUD
        )

        btnIzq = GameButton("btn_izq.png", 0f, 0f, tamañoBoton, tamañoBoton)
        btnDer = GameButton("btn_der.png", 0f, 0f, tamañoBoton, tamañoBoton)
        btnArriba = GameButton("btn_arriba.png", 0f, 0f, tamañoBoton, tamañoBoton)
        btnAbajo = GameButton("btn_abajo.png", 0f, 0f, tamañoBoton, tamañoBoton)

        player.setPosition(
            300f,
            worldHeight * 0.48f
        )

        // Diálogo inicial opcional
        dialogueBox.show(listOf(
            "El auditorio está en silencio...",
            "Debo buscar si hay algún rastro del profesor."
        ))

        resize(
            Gdx.graphics.width,
            Gdx.graphics.height
        )
    }

    override fun render(delta: Float) {

        update(delta)

        if (cambiandoPantalla) {
            return
        }

        ScreenUtils.clear(0.03f, 0.03f, 0.05f, 1f)

        dibujarAuditorio()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Auditorio",
            120f,
            worldHeight - 130f
        )

        font.draw(
            game.batch,
            "Izquierda para regresar al Edificio de Gobierno",
            330f,
            780f
        )

        player.render(game.batch)

        game.batch.end()

        // =========================
        // HUD Y DIALOGOS
        // =========================
        hudViewport.apply()
        game.batch.projectionMatrix = hudCamera.combined

        game.batch.begin()

        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)

        // Dibujar el diálogo sobre el HUD
        dialogueBox.render(game.batch)

        game.batch.end()
    }

    private fun dibujarAuditorio() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Piso
        shapeRenderer.color = Color(0.15f, 0.14f, 0.18f, 1f)
        shapeRenderer.rect(0f, 0f, worldWidth, worldHeight)

        // Paredes
        shapeRenderer.color = Color(0.07f, 0.07f, 0.10f, 1f)
        shapeRenderer.rect(0f, worldHeight - 120f, worldWidth, 120f)
        shapeRenderer.rect(0f, 0f, worldWidth, 120f)
        shapeRenderer.rect(0f, 0f, 120f, worldHeight)
        shapeRenderer.rect(worldWidth - 120f, 0f, 120f, worldHeight)

        // Escenario
        shapeRenderer.color = Color(0.35f, 0.20f, 0.20f, 1f)
        shapeRenderer.rect(
            worldWidth - 620f,
            360f,
            420f,
            680f
        )

        // Filas de asientos
        shapeRenderer.color = Color(0.10f, 0.12f, 0.18f, 1f)

        for (row in 0 until 5) {
            for (col in 0 until 7) {
                shapeRenderer.rect(
                    420f + col * 150f,
                    300f + row * 160f,
                    90f,
                    70f
                )
            }
        }

        // Flecha salida
        shapeRenderer.color = Color.YELLOW
        dibujarFlechaIzquierda(
            310f,
            salidaAuditorio.y + salidaAuditorio.height / 2f,
            80f
        )

        shapeRenderer.end()
    }

    private fun dibujarFlechaIzquierda(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

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
            centerY - 25f,
            120f,
            50f
        )
    }

    private fun update(delta: Float) {

        procesarInput(delta)

        // Solo actualizamos el juego si no hay diálogos activos
        if (!dialogueBox.isVisible()) {
            player.guardarPosicionAnterior()
            player.update(delta)
            revisarSalida()
        }

        player.limitarPantalla(worldWidth, worldHeight)

        actualizarCamara()
    }

    private fun procesarInput(delta: Float) {

        moviendoIzquierda = false

        if (!Gdx.input.justTouched() && !Gdx.input.isTouched) {
            return
        }

        // Si hay diálogo, cualquier toque avanza el diálogo
        if (dialogueBox.isVisible()) {
            if (Gdx.input.justTouched()) {
                dialogueBox.advance()
            }
            return // Bloqueamos el movimiento
        }

        touchPosition.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)

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

    private fun revisarSalida() {

        if (
            moviendoIzquierda
            &&
            player.collisionBox.overlaps(salidaAuditorio)
        ) {

            cambiandoPantalla = true

            // game.screen = GobiernoScreen(game) // Descomentar cuando esté disponible o usar la navegación correcta

            dispose()
        }
    }

    private fun actualizarCamara() {

        val playerCenterX = player.x + player.getWidth() / 2f
        val playerCenterY = player.y + player.getHeight() / 2f

        val halfViewportWidth = camera.viewportWidth / 2f
        val halfViewportHeight = camera.viewportHeight / 2f

        val cameraX = MathUtils.clamp(
            playerCenterX,
            halfViewportWidth,
            worldWidth - halfViewportWidth
        )

        val cameraY = MathUtils.clamp(
            playerCenterY,
            halfViewportHeight,
            worldHeight - halfViewportHeight
        )

        camera.position.set(cameraX, cameraY, 0f)
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

        camera.setToOrtho(false, width.toFloat(), height.toFloat())
        camera.update()

        hudViewport.update(width, height, true)

        // Actualizar el ancho del diálogo al cambiar el tamaño de ventana
        dialogueBox = DialogueBox(font, 1000f, 200f, width.toFloat())

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
        dialogueBox.dispose()
    }
}
