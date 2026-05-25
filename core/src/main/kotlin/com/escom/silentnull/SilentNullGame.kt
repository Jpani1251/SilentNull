package com.escom.silentnull

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.escom.silentnull.screens.MenuScreen

class SilentNullGame : Game() {

    // SpriteBatch:
    // sirve para dibujar TODO el juego
    lateinit var batch: SpriteBatch

    // Gestión de Audio
    private var backgroundMusic: Music? = null

    // =========================
    // INICIO DEL JUEGO
    // =========================
    override fun create() {

        // Creamos batch
        batch = SpriteBatch()

        // Primera pantalla del juego
        setScreen(MenuScreen(this))
    }

    override fun render() {
        super.render()

        // Controlamos que el audio solo llegue a 0.5 segundos y reinicie
        backgroundMusic?.let {
            if (it.isPlaying && it.position >= 0.5f) {
                it.position = 0f
            }
        }
    }

    fun playBackgroundMusic() {
        if (backgroundMusic == null) {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio_fondo.mp3"))
            backgroundMusic?.isLooping = true
            backgroundMusic?.volume = 0.5f
        }

        if (backgroundMusic?.isPlaying == false) {
            backgroundMusic?.play()
        }
    }

    fun stopBackgroundMusic() {
        backgroundMusic?.stop()
    }

    // =========================
    // CERRAR JUEGO
    // =========================
    override fun dispose() {

        // Liberamos memoria
        batch.dispose()
        backgroundMusic?.dispose()

        super.dispose()
    }
}
