package com.escom.silentnull

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.escom.silentnull.screens.MenuScreen

class SilentNullGame : Game() {

    // SpriteBatch:
    // sirve para dibujar TODO el juego
    lateinit var batch: SpriteBatch

    // =========================
    // INICIO DEL JUEGO
    // =========================
    override fun create() {

        // Creamos batch
        batch = SpriteBatch()

        // Primera pantalla del juego
        setScreen(MenuScreen(this))
    }

    // =========================
    // CERRAR JUEGO
    // =========================
    override fun dispose() {

        // Liberamos memoria
        batch.dispose()

        super.dispose()
    }
}
