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

class GobiernoScreen(
    private val game: SilentNullGame
) : Screen {

    // =========================
    // MUNDO INTERIOR
    // =========================
    private val worldWidth = 3000f
    private val worldHeight = 1600f

    // =========================
    // CÁMARA DEL MUNDO
    // =========================
    private val camera = OrthographicCamera()

    // =========================
    // CÁMARA HUD / BOTONES
    // =========================
    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    // =========================
    // DIBUJO DEL EDIFICIO
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
    // ZONAS DE CAMBIO
    // =========================

    // Salida al pasillo: lado izquierdo
    private val salidaGobierno = CollisionBox(
        0f,
        worldHeight * 0.38f,
        260f,
        460f
    )

    // Entrada a Biblioteca: arriba, más a la derecha
    private val entradaBiblioteca = CollisionBox(
        1650f,
        960f,
        360f,
        130f
    )

    // Entrada a Zona Común:
    // Ahora cubre toda el área café delimitada.
    private val entradaZonaComun = CollisionBox(
        850f,
        190f,
        820f,
        330f
    )

    // Entrada al Auditorio: lado derecho
    private val entradaAuditorio = CollisionBox(
        worldWidth - 280f,
        650f,
        280f,
        360f
    )

    private var moviendoIzquierda = false
    private var moviendoDerecha = false
    private var moviendoArriba = false
    private var moviendoAbajo = false
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

        font.data.setScale(2.4f)

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

        // Al entrar desde el pasillo, aparece cerca de la salida izquierda.
        player.setPosition(
            worldWidth * 0.18f,
            worldHeight * 0.45f
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

        ScreenUtils.clear(0.04f, 0.04f, 0.06f, 1f)

        dibujarEdificioGobierno()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio de Gobierno - Planta baja",
            310f,
            worldHeight - 180f
        )

        font.draw(
            game.batch,
            "Salida",
            70f,
            worldHeight * 0.38f + 520f
        )

        font.draw(
            game.batch,
            "Recepcion",
            430f,
            900f
        )

        font.draw(
            game.batch,
            "Biblioteca",
            1550f,
            1290f
        )

        font.draw(
            game.batch,
            "Zona comun",
            1030f,
            430f
        )

        font.draw(
            game.batch,
            "Auditorio",
            worldWidth - 650f,
            910f
        )

        player.render(game.batch)

        game.batch.end()

        // =========================
        // DIBUJAR HUD / BOTONES
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
    // DISEÑO VISUAL DEL EDIFICIO
    // =========================
    private fun dibujarEdificioGobierno() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // =========================
        // PISO GENERAL
        // =========================
        shapeRenderer.color = Color(0.22f, 0.22f, 0.25f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // =========================
        // PAREDES EXTERIORES
        // =========================
        shapeRenderer.color = Color(0.10f, 0.10f, 0.13f, 1f)

        // Pared superior
        shapeRenderer.rect(
            0f,
            worldHeight - 130f,
            worldWidth,
            130f
        )

        // Pared inferior
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            130f
        )

        // Pared izquierda
        shapeRenderer.rect(
            0f,
            0f,
            130f,
            worldHeight
        )

        // Pared derecha
        shapeRenderer.rect(
            worldWidth - 130f,
            0f,
            130f,
            worldHeight
        )

        // =========================
        // PASILLO CENTRAL
        // =========================
        shapeRenderer.color = Color(0.30f, 0.30f, 0.34f, 1f)
        shapeRenderer.rect(
            250f,
            620f,
            2550f,
            360f
        )

        // =========================
        // RECEPCIÓN
        // =========================
        shapeRenderer.color = Color(0.26f, 0.32f, 0.40f, 1f)
        shapeRenderer.rect(
            330f,
            690f,
            420f,
            230f
        )

        // Mostrador
        shapeRenderer.color = Color(0.15f, 0.18f, 0.22f, 1f)
        shapeRenderer.rect(
            390f,
            745f,
            300f,
            70f
        )

        // =========================
        // BIBLIOTECA EXTENDIDA
        // =========================
        shapeRenderer.color = Color(0.16f, 0.24f, 0.33f, 1f)
        shapeRenderer.rect(
            900f,
            1040f,
            1500f,
            360f
        )

        // Estantes dentro de Biblioteca
        shapeRenderer.color = Color(0.09f, 0.12f, 0.17f, 1f)

        shapeRenderer.rect(980f, 1110f, 80f, 230f)
        shapeRenderer.rect(1120f, 1110f, 80f, 230f)
        shapeRenderer.rect(1260f, 1110f, 80f, 230f)
        shapeRenderer.rect(1400f, 1110f, 80f, 230f)
        shapeRenderer.rect(1540f, 1110f, 80f, 230f)
        shapeRenderer.rect(1680f, 1110f, 80f, 230f)
        shapeRenderer.rect(1820f, 1110f, 80f, 230f)
        shapeRenderer.rect(1960f, 1110f, 80f, 230f)
        shapeRenderer.rect(2100f, 1110f, 80f, 230f)
        shapeRenderer.rect(2240f, 1110f, 80f, 230f)

        // =========================
        // ZONA COMÚN
        // =========================
        shapeRenderer.color = Color(0.28f, 0.25f, 0.22f, 1f)
        shapeRenderer.rect(
            850f,
            190f,
            820f,
            330f
        )

        // Vista previa de mesas
        dibujarMesaConSillas(970f, 300f)
        dibujarMesaConSillas(1210f, 300f)
        dibujarMesaConSillas(1450f, 300f)

        // =========================
        // ENTRADA VISUAL A AUDITORIO
        // =========================
        shapeRenderer.color = Color(0.25f, 0.18f, 0.22f, 1f)
        shapeRenderer.rect(
            worldWidth - 530f,
            640f,
            360f,
            350f
        )

        // =========================
        // PUERTAS / ACCESOS VISUALES
        // =========================
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)

        // Puerta Biblioteca
        shapeRenderer.rect(
            1760f,
            990f,
            180f,
            50f
        )

        // Entrada visual de Zona Común
        shapeRenderer.rect(
            1150f,
            570f,
            160f,
            50f
        )

        // Puerta Auditorio
        shapeRenderer.rect(
            worldWidth - 300f,
            780f,
            80f,
            120f
        )

        shapeRenderer.end()

        // =========================
        // FLECHAS DE GUÍA
        // =========================
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color.YELLOW

        // Flecha salida izquierda
        dibujarFlechaIzquierda(
            300f,
            salidaGobierno.y + salidaGobierno.height / 2f,
            80f
        )

        // Flecha Biblioteca
        dibujarFlechaArriba(
            entradaBiblioteca.x + entradaBiblioteca.width / 2f,
            entradaBiblioteca.y - 80f,
            60f
        )

        // Flecha Zona Común:
        // Ahora está ubicada justo en la entrada café dibujada.
        dibujarFlechaAbajo(
            1230f,
            720f,
            60f
        )

        // Flecha Auditorio
        dibujarFlechaDerecha(
            entradaAuditorio.x - 140f,
            entradaAuditorio.y + entradaAuditorio.height / 2f,
            80f
        )

        shapeRenderer.end()
    }

    // =========================
    // MESA CON 2 SILLAS
    // =========================
    private fun dibujarMesaConSillas(
        x: Float,
        y: Float
    ) {

        // Mesa
        shapeRenderer.color = Color(0.45f, 0.32f, 0.20f, 1f)
        shapeRenderer.rect(
            x,
            y,
            120f,
            80f
        )

        // Silla superior
        shapeRenderer.color = Color(0.12f, 0.13f, 0.16f, 1f)
        shapeRenderer.rect(
            x + 30f,
            y + 95f,
            60f,
            50f
        )

        // Silla inferior
        shapeRenderer.rect(
            x + 30f,
            y - 65f,
            60f,
            50f
        )
    }

    // =========================
    // FLECHAS
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
            centerX - 20f,
            centerY - size - 100f,
            40f,
            100f
        )
    }

    private fun dibujarFlechaAbajo(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        shapeRenderer.triangle(
            centerX,
            centerY - size,
            centerX - size,
            centerY + size,
            centerX + size,
            centerY + size
        )

        shapeRenderer.rect(
            centerX - 20f,
            centerY + size,
            40f,
            100f
        )
    }

    private fun dibujarFlechaDerecha(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        shapeRenderer.triangle(
            centerX + size,
            centerY,
            centerX - size,
            centerY + size,
            centerX - size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX - size - 120f,
            centerY - 25f,
            120f,
            50f
        )
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

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        player.guardarPosicionAnterior()

        procesarInput(delta)

        player.update(delta)

        revisarCambiosDeZona()

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
        moviendoDerecha = false
        moviendoArriba = false
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

            moviendoArriba = true
            player.moverArriba(delta)
        }

        if (btnAbajo.isTouched(touchX, touchY)) {

            moviendoAbajo = true
            player.moverAbajo(delta)
        }
    }

    // =========================
    // CAMBIOS DE ZONA
    // =========================
    private fun revisarCambiosDeZona() {

        // Salir al pasillo
        if (
            moviendoIzquierda
            &&
            player.collisionBox.overlaps(salidaGobierno)
        ) {

            cambiandoPantalla = true

            game.screen = JuegoScreen(game)

            dispose()

            return
        }

        // Entrar a Biblioteca
        if (
            moviendoArriba
            &&
            player.collisionBox.overlaps(entradaBiblioteca)
        ) {

            cambiandoPantalla = true

            game.screen = BibliotecaScreen(game)

            dispose()

            return
        }

        // Entrar a Zona Común.
        // Aquí ya no importa si va hacia abajo o no:
        // si toca cualquier parte del área café, entra.
        if (player.collisionBox.overlaps(entradaZonaComun)) {

            cambiandoPantalla = true

            game.screen = ZonaComunScreen(game)

            dispose()

            return
        }

        // Entrar al Auditorio
        if (
            moviendoDerecha
            &&
            player.collisionBox.overlaps(entradaAuditorio)
        ) {

            cambiandoPantalla = true

            game.screen = AuditorioScreen(game)

            dispose()

            return
        }
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

    override fun hide() {}

    // =========================
    // DISPOSE
    // =========================
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
