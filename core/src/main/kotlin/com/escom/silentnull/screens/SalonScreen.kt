package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
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
    // SISTEMA DE DIALOGOS
    // =========================
    private lateinit var dialogueBox: DialogueBox

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

    // =========================
    // BOTONES Y TEXTURAS
    // =========================
    private val tamanoBoton = 150f
    // Usamos el archivo que confirmamos que existe en assets
    private val backgroundTexture = Texture("salon_escom.png")

    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton

    // =========================
    // INIT
    // =========================
    init {

        font.data.setScale(2.4f)
        // Color crema cálido para el texto base
        font.color = Color(1f, 0.95f, 0.8f, 1f)

        dialogueBox = DialogueBox(
            font,
            width = 1400f,
            height = 300f,
            screenWidth = worldWidth
        )

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

        // Diálogo de bienvenida al entrar al salón
        dialogueBox.show(listOf(
            "Has entrado a $nombreSalon.",
            "Parece que las clases han terminado por hoy.",
            "Debo darme prisa."
        ))

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

        // Color de fondo cálido para los bordes de la pantalla (marrón oscuro)
        ScreenUtils.clear(0.12f, 0.08f, 0.05f, 1f)

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        // 1. Dibujamos la imagen de fondo primero para que sea la base
        game.batch.draw(backgroundTexture, 0f, 0f, worldWidth, worldHeight)

        // 2. Dibujamos el nombre del salón en la parte superior
        font.draw(
            game.batch,
            nombreSalon,
            120f,
            worldHeight - 120f
        )

        // 3. Dibujamos el texto de salida con un color dorado cálido
        font.color = Color.GOLD
        font.draw(
            game.batch,
            "Salida",
            worldWidth - 260f,
            worldHeight * 0.38f + 470f
        )
        // Restauramos el color crema para otros elementos
        font.color = Color(1f, 0.95f, 0.8f, 1f)

        // 4. Dibujamos al jugador encima del fondo
        player.render(game.batch)

        // 5. Dibujamos el cuadro de diálogo (siempre al final para estar encima)
        dialogueBox.render(game.batch)

        game.batch.end()

        // =========================
        // HUD (Botones de control)
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
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        procesarInput(delta)

        // Solo actualizamos el juego si no hay diálogos activos
        if (!dialogueBox.isVisible()) {
            player.update(delta)
            revisarSalida()
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

        moviendoDerecha = false

        if (!Gdx.input.justTouched() && !Gdx.input.isTouched) {
            return
        }

        // Si hay diálogo, cualquier toque avanza el diálogo
        if (dialogueBox.isVisible()) {
            if (Gdx.input.justTouched()) {
                dialogueBox.advance()
            }
            return // Bloqueamos el movimiento mientras hay diálogo
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
    private fun revisarSalida() {

        if (
            moviendoDerecha
            &&
            player.collisionBox.overlaps(salidaSalon)
        ) {

            cambiandoPantalla = true

            game.screen =
                when (pisoRegreso) {
                    1, 3 -> PasilloScreen(game, regresoX, regresoY)
                    2 -> Edificio2SegundoPisoScreen(game, regresoX, regresoY)
                    else -> Edificio2PisoSuperiorScreen(game, pisoRegreso, regresoX, regresoY)
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
        backgroundTexture.dispose()
        dialogueBox.dispose()
    }
}
