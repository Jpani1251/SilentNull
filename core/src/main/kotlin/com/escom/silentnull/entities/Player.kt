package com.escom.silentnull.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.escom.silentnull.physics.CollisionBox

class Player {

    // =========================
    // TEXTURA
    // =========================
    private val textura = Texture("alex_protagonista.png")

    // =========================
    // POSICIÓN
    // =========================
    var x = 0f
    var y = 0f

    private var previousX = 0f
    private var previousY = 0f

    val collisionBox = CollisionBox(
        x,
        y,
        textura.width.toFloat(),
        textura.height.toFloat()
    )
    // =========================
    // VELOCIDAD
    // =========================
    private val velocidad = 450f

    // =========================
    // INIT
    // =========================
    init {

        // Posición inicial del jugador
        x = (Gdx.graphics.width / 2 - textura.width / 2).toFloat()

        y = (Gdx.graphics.height / 2 - textura.height / 2).toFloat()
    }

    // =========================
    // UPDATE
    // =========================
    fun update(delta: Float) {

        // Aquí irá lógica futura:
        // animaciones
        // físicas
        // colisiones
        // estados
        previousX = x
        previousY = y
        collisionBox.x = x
        collisionBox.y = y
    }

    // =========================
    // RENDER
    // =========================
    fun render(batch: SpriteBatch) {

        batch.draw(textura, x, y)
    }

    // =========================
    // MOVIMIENTO
    // =========================
    fun moverIzquierda(delta: Float) {

        x -= velocidad * delta
    }

    fun moverDerecha(delta: Float) {

        x += velocidad * delta
    }

    fun moverArriba(delta: Float) {

        y += velocidad * delta
    }

    fun moverAbajo(delta: Float) {

        y -= velocidad * delta
    }
    fun revertirMovimiento() {

        x = previousX
        y = previousY

        collisionBox.x = x
        collisionBox.y = y
    }

    // =========================
    // LIMITES PANTALLA
    // =========================
    fun limitarPantalla(
        worldWidth: Float,
        worldHeight: Float
    ) {

        // Límite izquierdo
        if (x < 0f) {
            x = 0f
        }

        // Límite abajo
        if (y < 0f) {
            y = 0f
        }

        // Límite derecho
        if (x > worldWidth - textura.width) {

            x =
                worldWidth - textura.width
        }

        // Límite arriba
        if (y > worldHeight - textura.height) {

            y =
                worldHeight - textura.height
        }
    }

    // =========================
    // GETTERS
    // =========================
    fun getWidth(): Int {

        return textura.width
    }

    fun getHeight(): Int {

        return textura.height
    }

    // =========================
    // DISPOSE
    // =========================
    fun dispose() {

        textura.dispose()
    }
}
