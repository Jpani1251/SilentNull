package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.ScreenUtils
import com.escom.silentnull.SilentNullGame

class MenuScreen(val game: SilentNullGame) : Screen {

    // =========================
    // TEXTURAS
    // =========================
    private val fondo = Texture("fondo_escom.png")

    private val btnJugar = Texture("btn_jugar.png")

    private val logoTexture = Texture("logo.png")

    // =========================
    // BOTÓN JUGAR
    // =========================
    private val anchoBtn = 300f
    private val altoBtn = 100f

    private var btnX = 0f
    private var btnY = 0f

    // =========================
    // LOGO
    // =========================
    private val anchoLogo = 700f
    private val altoLogo = 350f

    private var logoX = 0f
    private var logoY = 0f

    // =========================
    // INIT
    // =========================
    init {

        val anchoPantalla =
            Gdx.graphics.width.toFloat()

        val altoPantalla =
            Gdx.graphics.height.toFloat()

        // =========================
        // POSICIÓN LOGO
        // =========================

        logoX =
            (anchoPantalla / 2) - (anchoLogo / 2)

        logoY =
            (altoPantalla * 0.65f) - (altoLogo / 2)

        // =========================
        // POSICIÓN BOTÓN
        // =========================

        btnX =
            (anchoPantalla / 2) - (anchoBtn / 2)

        btnY =
            (altoPantalla * 0.25f) - (altoBtn / 2)
    }

    // =========================
    // RENDER
    // =========================
    override fun render(delta: Float) {

        // Actualizamos lógica
        update()

        // Limpiamos pantalla
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        // Iniciamos dibujo
        game.batch.begin()

        // Oscurecemos fondo
        game.batch.color =
            Color(0.4f, 0.4f, 0.4f, 1f)

        // Dibujamos fondo
        game.batch.draw(
            fondo,
            0f,
            0f,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        )

        // Restauramos color
        game.batch.color = Color.WHITE

        // Dibujamos logo
        game.batch.draw(
            logoTexture,
            logoX,
            logoY,
            anchoLogo,
            altoLogo
        )

        // Dibujamos botón
        game.batch.draw(
            btnJugar,
            btnX,
            btnY,
            anchoBtn,
            altoBtn
        )

        // Terminamos dibujo
        game.batch.end()
    }

    // =========================
    // UPDATE
    // =========================
    private fun update() {

        detectarToqueBoton()
    }

    // =========================
    // INPUT BOTÓN
    // =========================
    private fun detectarToqueBoton() {

        if (Gdx.input.justTouched()) {

            val touchX =
                Gdx.input.x.toFloat()

            val touchY =
                (Gdx.graphics.height - Gdx.input.y).toFloat()

            // Verificamos si tocó el botón
            if (
                touchX in btnX..(btnX + anchoBtn)
                &&
                touchY in btnY..(btnY + altoBtn)
            ) {

                // Cambiamos a pantalla de juego
                game.screen = JuegoScreen(game)

                // Liberamos memoria del menú
                dispose()
            }
        }
    }

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

        fondo.dispose()

        btnJugar.dispose()

        logoTexture.dispose()
    }
}
