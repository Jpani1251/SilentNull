package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.ScreenUtils
import com.escom.silentnull.SilentNullGame
import com.escom.silentnull.entities.Player
import com.escom.silentnull.ui.GameButton
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.escom.silentnull.physics.CollisionBox

class JuegoScreen(val game: SilentNullGame) : Screen {

    // =========================
    // TEXTURAS
    // =========================
    private val fondoEscom = Texture("fondo_escom.png")


    private val texturaBtnIzq = Texture("btn_izq.png")
    private val texturaBtnDer = Texture("btn_der.png")
    private val texturaBtnArriba = Texture("btn_arriba.png")
    private val texturaBtnAbajo = Texture("btn_abajo.png")

    // =========================
    // POSICIÓN DEL PERSONAJE
    // =========================
    private val player = Player()
    private lateinit var camera: OrthographicCamera
    private val worldWidth = 3000f
    private val worldHeight = 3000f

    private val wall = CollisionBox(
        1000f,
        1000f,
        300f,
        300f
    )
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

        // Posición inicial del personaje

        // Posiciones botones
        val margenX = 50f
        val margenY = 50f

        btnIzq = GameButton(
            "btn_izq.png",
            margenX,
            margenY + tamañoBoton,
            tamañoBoton,
            tamañoBoton
        )

        btnDer = GameButton(
            "btn_der.png",
            margenX + (tamañoBoton * 2),
            margenY + tamañoBoton,
            tamañoBoton,
            tamañoBoton
        )

        btnArriba = GameButton(
            "btn_arriba.png",
            margenX + tamañoBoton,
            margenY + (tamañoBoton * 2),
            tamañoBoton,
            tamañoBoton
        )

        btnAbajo = GameButton(
            "btn_abajo.png",
            margenX + tamañoBoton,
            margenY,
            tamañoBoton,
            tamañoBoton
        )
        camera = OrthographicCamera()

        camera.setToOrtho(
            false,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        )
    }

    // =========================
    // RENDER PRINCIPAL
    // =========================
    override fun render(delta: Float) {

        // Actualizamos lógica
        update(delta)

        // Limpiamos pantalla
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        game.batch.projectionMatrix = camera.combined
        // Iniciamos dibujo
        game.batch.begin()

        // Dibujamos fondo
        game.batch.draw(
            fondoEscom,
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Dibujamos personaje
        player.render(game.batch)

        // Dibujamos botones
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

        // =========================
        // INPUT BOTONES
        // =========================

        if (btnIzq.isTouched()) {

            player.moverIzquierda(delta)
        }

        if (btnDer.isTouched()) {

            player.moverDerecha(delta)
        }

        if (btnArriba.isTouched()) {

            player.moverArriba(delta)
        }

        if (btnAbajo.isTouched()) {

            player.moverAbajo(delta)
        }

        // =========================
        // UPDATE PLAYER
        // =========================

        player.update(delta)
        if (player.collisionBox.overlaps(wall)) {

            player.revertirMovimiento()
            println("Funciona!")
        }

        // =========================
        // LIMITES PANTALLA
        // =========================

        player.limitarPantalla(
            worldWidth,
            worldHeight
        )

        camera.position.set(

            player.x + player.getWidth() / 2f,

            player.y + player.getHeight() / 2f,

            0f
        )

        camera.update()
    }

    // =========================
    // MOVIMIENTO
    // =========================

    // =========================
    // MÉTODOS OBLIGATORIOS
    // =========================
    override fun show() {}

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    // =========================
    // LIBERAR MEMORIA
    // =========================
    override fun dispose() {

        fondoEscom.dispose()

        player.dispose()

        texturaBtnIzq.dispose()
        texturaBtnDer.dispose()

        texturaBtnArriba.dispose()
        texturaBtnAbajo.dispose()
    }
}
