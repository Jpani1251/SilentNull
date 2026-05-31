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
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.escom.silentnull.GameState
import com.escom.silentnull.SilentNullGame
import com.escom.silentnull.entities.Player
import com.escom.silentnull.inventory.InventoryUI
import com.escom.silentnull.inventory.Item
import com.escom.silentnull.physics.CollisionBox
import com.escom.silentnull.ui.DialogueBox
import com.escom.silentnull.ui.GameButton

class LaboratorioScreen(
    private val game: SilentNullGame,
    private val nombreLab: String,
    private val regresoX: Float,
    private val regresoY: Float,
    private val pisoRegreso: Int = 2
) : Screen {

    private enum class EventState { IDLE, DIALOGO1, FOTO, DIALOGO2, FINISHED }
    private var currentState = EventState.IDLE
    private val credencialFoto = Texture("credencial_evento.png")

    private val worldWidth = 1800f
    private val worldHeight = 1300f

    private val camera = OrthographicCamera()
    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)
    private val touchPosition = Vector3()

    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()
    private val player = Player()
    private lateinit var dialogueBox: DialogueBox

    private var moviendoIzquierda = false
    private var moviendoDerecha = false
    private var moviendoAbajo = false
    private var cambiandoPantalla = false

    private val tamanoBoton = 150f
    private val backgroundTexture = Texture("Labs200.png")

    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton
    private lateinit var btnInventario: GameButton
    private lateinit var btnInspeccionar: GameButton

    private lateinit var inventoryUI: InventoryUI
    private var cercaDelObjetivo = false

    init {
        font.data.setScale(2.4f)
        font.color = Color(1f, 0.95f, 0.8f, 1f)

        // Inicializamos con un screenWidth temporal, se ajustará en resize
        dialogueBox = DialogueBox(font, width = 1400f, height = 300f, screenWidth = worldWidth)

        btnIzq = GameButton("btn_izq.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnDer = GameButton("btn_der.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnArriba = GameButton("btn_arriba.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnAbajo = GameButton("btn_abajo.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnInventario = GameButton("logo.png", 0f, 0f, tamanoBoton, tamanoBoton)
        btnInspeccionar = GameButton("btn_jugar.png", 0f, 0f, tamanoBoton, tamanoBoton)

        inventoryUI = InventoryUI(player, font)

        player.setPosition(670f, 37f)
        player.escala = 4.0f

        dialogueBox.show(listOf(
            "Has entrado al $nombreLab.",
            "Este laboratorio parece estar muy bien equipado.",
            "Debo seguir buscando."
        ))

        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun render(delta: Float) {
        update(delta)
        if (cambiandoPantalla) return

        ScreenUtils.clear(0.05f, 0.05f, 0.1f, 1f)

        // --- DIBUJO DEL MUNDO ---
        game.batch.projectionMatrix = camera.combined
        game.batch.begin()
        game.batch.draw(backgroundTexture, 0f, 0f, worldWidth, worldHeight)
        font.draw(game.batch, nombreLab, 120f, worldHeight - 120f)
        font.color = Color.GOLD
        font.draw(game.batch, "Salida", 20f, 60f)
        font.color = Color(1f, 0.95f, 0.8f, 1f)

        // Solo dibujamos al player si no estamos en el modo foto
        if (currentState != EventState.FOTO) {
            player.render(game.batch)
        }
        game.batch.end()

        // --- DIBUJO DEL HUD / INTERFAZ ---
        hudViewport.apply()
        game.batch.projectionMatrix = hudCamera.combined
        game.batch.begin()

        // Si estamos mostrando la foto, la dibujamos en TODA la pantalla usando el HUD
        if (currentState == EventState.FOTO) {
            game.batch.draw(credencialFoto, 0f, 0f, hudViewport.worldWidth, hudViewport.worldHeight)
        }

        // El cuadro de diálogo ahora se dibuja en el HUD para estar siempre al frente
        dialogueBox.render(game.batch)

        // Botones y coordenadas
        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)
        btnInventario.render(game.batch)

        font.draw(game.batch, "X: ${player.x.toInt()}  Y: ${player.y.toInt()}", 0f, hudViewport.worldHeight - 50f, hudViewport.worldWidth, Align.center, false)

        inventoryUI.render(game.batch)
        game.batch.end()
    }

    private fun update(delta: Float) {
        procesarInput(delta)

        if (currentState == EventState.IDLE) {
            revisarEvento()
        } else if (currentState == EventState.DIALOGO1 && !dialogueBox.isVisible()) {
            currentState = EventState.FOTO
        } else if (currentState == EventState.DIALOGO2 && !dialogueBox.isVisible()) {
            currentState = EventState.FINISHED
            // Marcamos el evento como completado globalmente
            GameState.completarEvento("evento_credencial_lab206")

            player.inventory.addItem(Item(
                "credencial",
                "Credencial",
                "Mi credencial de ESCOM.",
                "item_credencial.jpg"
            ))
        }

        if (!dialogueBox.isVisible() && !inventoryUI.isOpen() && currentState != EventState.FOTO) {
            player.update(delta)
            revisarSalida()
        }
        player.limitarPantalla(worldWidth, worldHeight)
        actualizarCamara()
    }

    private fun revisarEvento() {
        if (nombreLab != "Laboratorio 206") return

        // Verificamos si el evento ya se completó en GameState
        if (GameState.esEventoCompletado("evento_credencial_lab206")) return

        if (Math.abs(player.x - 1257f) < 80f && Math.abs(player.y - 505f) < 80f) {
            currentState = EventState.DIALOGO1
            dialogueBox.show(listOf("Esto me podría ser útil"))
        }
    }

    private fun procesarInput(delta: Float) {
        moviendoIzquierda = false
        moviendoDerecha = false
        moviendoAbajo = false

        if (Gdx.input.justTouched()) {
            touchPosition.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            hudViewport.unproject(touchPosition)

            if (btnInventario.isTouched(touchPosition.x, touchPosition.y)) {
                inventoryUI.toggle()
            }

            if (currentState == EventState.FOTO) {
                currentState = EventState.DIALOGO2
                dialogueBox.show(listOf("Y pensar que te había perdido, tal vez aún me seas de utilidad."))
                return
            }
        }

        if (!Gdx.input.isTouched) return

        if (dialogueBox.isVisible() || inventoryUI.isOpen() || currentState == EventState.FOTO) {
            if (Gdx.input.justTouched() && dialogueBox.isVisible()) dialogueBox.advance()
            return
        }

        touchPosition.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        hudViewport.unproject(touchPosition)
        val touchX = touchPosition.x
        val touchY = touchPosition.y

        if (btnIzq.isTouched(touchX, touchY)) { moviendoIzquierda = true; player.moverIzquierda(delta) }
        if (btnDer.isTouched(touchX, touchY)) { moviendoDerecha = true; player.moverDerecha(delta) }
        if (btnArriba.isTouched(touchX, touchY)) player.moverArriba(delta)
        if (btnAbajo.isTouched(touchX, touchY)) { moviendoAbajo = true; player.moverAbajo(delta) }
    }

    private fun revisarSalida() {
        val areaSalida = CollisionBox(0f, 0f, 150f, 150f)
        if ((moviendoIzquierda || moviendoAbajo) && player.collisionBox.overlaps(areaSalida)) {
            cambiandoPantalla = true
            game.screen = Edificio2SegundoPisoScreen(game, regresoX, regresoY)
            dispose()
        }
    }

    private fun actualizarCamara() {
        val playerCenterX = player.x + player.getWidth() / 2f
        val playerCenterY = player.y + player.getHeight() / 2f
        val cameraX = MathUtils.clamp(playerCenterX, camera.viewportWidth / 2f, worldWidth - camera.viewportWidth / 2f)
        val cameraY = MathUtils.clamp(playerCenterY, camera.viewportHeight / 2f, worldHeight - camera.viewportHeight / 2f)
        camera.position.set(cameraX, cameraY, 0f)
        camera.update()
    }

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
        btnInventario.x = Gdx.graphics.width - tamanoBoton - 50f
        btnInventario.y = Gdx.graphics.height - tamanoBoton - 50f
    }

    override fun show() {
        game.stopBackgroundMusic()
    }
    override fun resize(width: Int, height: Int) {
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
        camera.update()
        hudViewport.update(width, height, true)

        // Ajustamos el screenWidth de la caja de diálogos para que use el HUD actual
        if (::dialogueBox.isInitialized) {
            // Re-creamos o actualizamos si la clase lo permite (en este caso es privada la propiedad, así que la re-inicializamos)
            dialogueBox = DialogueBox(font, width = 1400f, height = 300f, screenWidth = hudViewport.worldWidth)
        }

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
        btnInventario.dispose()
        inventoryUI.dispose()
        backgroundTexture.dispose()
        credencialFoto.dispose()
        dialogueBox.dispose()
    }
}
