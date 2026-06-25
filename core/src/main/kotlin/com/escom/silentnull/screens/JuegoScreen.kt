package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.escom.silentnull.SilentNullGame
import com.escom.silentnull.entities.Player
import com.escom.silentnull.physics.CollisionBox
import com.escom.silentnull.ui.DebugManager
import com.escom.silentnull.ui.DialogueManager
import com.escom.silentnull.ui.GameButton
import com.escom.silentnull.ui.InventoryManager

class JuegoScreen(
    val game: SilentNullGame,

    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    // =========================
    // TEXTURAS
    // =========================
    private val fondoEscom = Texture("fondo_escom.png")

    // =========================
    // MUNDO
    // =========================
    private val worldWidth = 3000f
    private val worldHeight = 3000f

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

    private val mostrarFlechas = true

    // =========================
    // JUGADOR
    // =========================
    private val player = Player()

    // =========================
    // DEBUG TOOLS
    // =========================
    private val debugManager = DebugManager("JuegoScreen", worldWidth, worldHeight)

    // =========================
    // DIÁLOGOS
    // =========================
    private val dialogueManager = DialogueManager()

    // =========================
    // ENTRADAS
    // =========================

    // Gobierno: hacia la derecha
    private val entradaGobierno = CollisionBox(
        worldWidth * 0.62f,
        worldHeight * 0.40f,
        worldWidth * 0.08f,
        worldHeight * 0.22f
    )

    // Edificio 2: hacia enfrente / arriba
    private val entradaEdificio2 = CollisionBox(
        worldWidth * 0.40f,
        worldHeight * 0.62f,
        worldWidth * 0.20f,
        worldHeight * 0.08f
    )

    private var moviendoDerecha = false
    private var moviendoArriba = false
    private var cambiandoPantalla = false

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

        player.setPosition(
            spawnX ?: worldWidth * 0.45f,
            spawnY ?: worldHeight * 0.45f
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

        ScreenUtils.clear(0f, 0f, 0f, 1f)

        // =========================
        // DIBUJAR MUNDO
        // =========================
        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        game.batch.draw(
            fondoEscom,
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        player.render(game.batch)

        game.batch.end()

        // =========================
        // DEBUG TOOLS
        // =========================
        debugManager.render(game.batch, camera, hudCamera, player)

        // =========================
        // DIBUJAR FLECHAS
        // =========================
        if (mostrarFlechas) {

            shapeRenderer.projectionMatrix = camera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

            shapeRenderer.color = Color.YELLOW

            // Flecha hacia Gobierno
            dibujarFlechaDerecha(
                entradaGobierno.x - 100f,
                entradaGobierno.y + entradaGobierno.height / 2f,
                45f
            )

            // Flecha hacia Edificio 2
            dibujarFlechaArriba(
                entradaEdificio2.x + entradaEdificio2.width / 2f,
                entradaEdificio2.y - 90f,
                45f
            )

            shapeRenderer.end()
        }

        // =========================
        // DIBUJAR HUD
        // =========================
        hudViewport.apply()

        game.batch.projectionMatrix = hudCamera.combined

        game.batch.begin()

        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)

        game.batch.end()

        // =========================
        // DIÁLOGOS (Encima de todo)
        // DIÁLOGOS (Encima de todo)
        dialogueManager.render(game.batch, hudCamera)

        // INVENTARIO
        game.inventoryManager.render(game.batch, hudViewport)
    }

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        game.inventoryManager.update(delta)

        if (game.inventoryManager.isVisible()) {
            return
        }

        player.guardarPosicionAnterior()

        val prevX = player.x
        val prevY = player.y

        procesarInput(delta)

        player.update(delta)

        // Colisión con la rejilla (Global)
        if (debugManager.checkCollision(player)) {
            player.x = prevX
            player.y = prevY
        }

        revisarEntradas()

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

        moviendoDerecha = false
        moviendoArriba = false

        if (!Gdx.input.isTouched) {
            debugManager.procesarInput(0f, 0f, camera) // Reset dragging
            return
        }

        // Si hay un diálogo, el primer toque lo cierra y bloquea el resto del input
        if (dialogueManager.handleInput()) {
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

            moviendoDerecha = true
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
    // ENTRADAS
    // =========================
    private fun revisarEntradas() {

        if (
            moviendoDerecha
            &&
            player.collisionBox.overlaps(entradaGobierno)
        ) {

            cambiandoPantalla = true

            game.screen = GobiernoScreen(game)

            dispose()

            return
        }

        if (
            moviendoArriba
            &&
            player.collisionBox.overlaps(entradaEdificio2)
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2Screen(game)

            dispose()

            return
        }
    }

    // =========================
    // FLECHAS
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
    override fun show() {
        // Al entrar por primera vez (o volver), si es el inicio, mostrar diálogo
        dialogueManager.show("¿En dónde carajos estoy?")
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

        fondoEscom.dispose()

        player.dispose()

        btnIzq.dispose()
        btnDer.dispose()
        btnArriba.dispose()
        btnAbajo.dispose()

        debugManager.dispose()
        dialogueManager.dispose()

        shapeRenderer.dispose()
    }
}
