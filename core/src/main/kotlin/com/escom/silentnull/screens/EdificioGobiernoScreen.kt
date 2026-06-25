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
    // DEBUG TOOLS
    // =========================
    private val debugManager = DebugManager("EdificioGobierno", worldWidth, worldHeight)

    // =========================
    // ZONAS IMPORTANTES
    // =========================

    // Salida hacia Edificio 2, lado izquierdo.
    private val salidaEdificio2 = CollisionBox(
        0f,
        worldHeight / 2f - 230f,
        420f,
        460f
    )

    // Salida hacia Edificio 1, lado derecho.
    private val salidaEdificio1 = CollisionBox(
        worldWidth - 420f,
        worldHeight / 2f - 230f,
        420f,
        460f
    )

    // Escaleras hacia segundo piso, parte inferior.
    private val entradaEscalerasSubir = CollisionBox(
        worldWidth / 2f - 170f,
        210f,
        340f,
        240f
    )

    // =========================
    // ESTADOS
    // =========================
    private var moviendoIzquierda = false
    private var moviendoDerecha = false
    private var moviendoAbajo = false
    private var cambiandoPantalla = false
    private var recursosLiberados = false

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

        // Al venir desde Edificio 2 aparece del lado izquierdo.
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

        if (cambiandoPantalla) {
            return
        }

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
            "Pasillo hacia Edificio 1",
            worldWidth - 650f,
            worldHeight / 2f + 295f
        )

        font.draw(
            game.batch,
            "Escaleras a segundo piso",
            entradaEscalerasSubir.x - 40f,
            entradaEscalerasSubir.y + entradaEscalerasSubir.height + 90f
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

        game.batch.projectionMatrix = hudCamera.combined

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

        // Conexión hacia Edificio 2
        shapeRenderer.color = Color(0.35f, 0.35f, 0.40f, 1f)
        shapeRenderer.rect(
            100f,
            worldHeight / 2f - 190f,
            460f,
            380f
        )

        // Conexión hacia Edificio 1
        shapeRenderer.color = Color(0.35f, 0.35f, 0.40f, 1f)
        shapeRenderer.rect(
            worldWidth - 560f,
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

        // Puerta hacia Edificio 2
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            100f,
            worldHeight / 2f - 130f,
            100f,
            260f
        )

        // Puerta hacia Edificio 1
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            worldWidth - 200f,
            worldHeight / 2f - 130f,
            100f,
            260f
        )

        // Escaleras en la parte inferior
        dibujarEscaleras(
            entradaEscalerasSubir.x,
            entradaEscalerasSubir.y,
            entradaEscalerasSubir.width,
            entradaEscalerasSubir.height
        )

        // Flechas
        shapeRenderer.color = Color.YELLOW

        // Regresar a Edificio 2
        dibujarFlechaIzquierda(
            370f,
            worldHeight / 2f,
            45f
        )

        // Ir a Edificio 1
        dibujarFlechaDerecha(
            worldWidth - 370f,
            worldHeight / 2f,
            45f
        )

        // Subir por escaleras
        dibujarFlechaAbajo(
            entradaEscalerasSubir.x + entradaEscalerasSubir.width / 2f,
            entradaEscalerasSubir.y + entradaEscalerasSubir.height + 80f,
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
    private fun revisarAccesos(): Boolean {

        // Regresar al Edificio 2 planta baja.
        if (
            moviendoIzquierda
            &&
            player.collisionBox.overlaps(salidaEdificio2)
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2Screen(
                game,
                930f + 850f / 2f,
                420f + 260f / 2f
            )

            return true
        }

        // Ir al Edificio 1 planta baja.
        // Aquí sí debe pasar después de haber caminado dentro de Gobierno.
        if (
            moviendoDerecha
            &&
            player.collisionBox.overlaps(salidaEdificio1)
        ) {

            cambiandoPantalla = true

            game.screen = Edificio1Screen(
                game,
                420f + 850f / 2f,
                420f + 260f / 2f
            )

            return true
        }

        // Subir al segundo piso del Edificio de Gobierno.
        if (
            moviendoAbajo
            &&
            player.collisionBox.overlaps(entradaEscalerasSubir)
        ) {

            cambiandoPantalla = true

            game.screen = EdificioGobiernoSegundoPisoScreen(
                game,
                900f,
                700f
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
        } else {

            val cambioPantalla =
                revisarAccesos()

            if (cambioPantalla) {
                return
            }
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

        // Manejar Inventario
        if (game.inventoryManager.handleInput(touchX, touchY)) {
            return
        }

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
