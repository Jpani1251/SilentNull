package com.escom.silentnull

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.escom.silentnull.screens.MenuScreen
import com.escom.silentnull.ui.InventoryManager
import com.escom.silentnull.video.IVideoPlayer

class SilentNullGame(
    val videoPlayer: IVideoPlayer? = null
) : Game() {

    // SpriteBatch:
    // sirve para dibujar TODO el juego
    lateinit var batch: SpriteBatch

    // INVENTARIO GLOBAL
    lateinit var inventoryManager: InventoryManager

    // =========================
    // INICIO DEL JUEGO
    // =========================
    override fun create() {

        // Creamos batch
        batch = SpriteBatch()

        // Inicializamos Inventario
        inventoryManager = InventoryManager()

        // Primera pantalla del juego
        setScreen(MenuScreen(this))
    }

    // =========================
    // CERRAR JUEGO
    // =========================
    override fun dispose() {

        // Liberamos memoria
        batch.dispose()
        inventoryManager.dispose()

        super.dispose()
    }
}
