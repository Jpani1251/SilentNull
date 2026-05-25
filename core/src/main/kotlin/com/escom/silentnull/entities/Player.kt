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

    // =========================
    // DIRECCIÓN DEL PERSONAJE
    // =========================
    private var mirandoDerecha = true

    // =========================
    // COLLISION BOX
    // =========================
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

        x = (Gdx.graphics.width / 2f) - (textura.width / 2f)
        y = (Gdx.graphics.height / 2f) - (textura.height / 2f)

        actualizarCollisionBox()
    }

    // =========================
    // UPDATE
    // =========================
    fun update(delta: Float) {

        actualizarCollisionBox()
    }

    // =========================
    // GUARDAR POSICIÓN ANTERIOR
    // =========================
    fun guardarPosicionAnterior() {

        previousX = x
        previousY = y
    }

    // =========================
    // RENDER
    // =========================
    fun render(batch: SpriteBatch) {

        val width = textura.width.toFloat()
        val height = textura.height.toFloat()

        /*
            Si tu imagen original mira hacia la derecha,
            esta configuración está bien.

            Si al probar queda al revés, cambia:
            val flipX = !mirandoDerecha
            por:
            val flipX = mirandoDerecha
        */
        val flipX = mirandoDerecha

        batch.draw(
            textura,
            x,
            y,
            0f,
            0f,
            width,
            height,
            1f,
            1f,
            0f,
            0,
            0,
            textura.width,
            textura.height,
            flipX,
            false
        )
    }

    // =========================
    // MOVIMIENTO
    // =========================
    fun moverIzquierda(delta: Float) {

        mirandoDerecha = false

        x -= velocidad * delta
    }

    fun moverDerecha(delta: Float) {

        mirandoDerecha = true

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

        actualizarCollisionBox()
    }

    // =========================
    // POSICIONAR JUGADOR
    // =========================
    fun setPosition(
        newX: Float,
        newY: Float
    ) {

        x = newX
        y = newY

        actualizarCollisionBox()
    }

    // =========================
    // LÍMITES DEL MAPA
    // =========================
    fun limitarPantalla(
        worldWidth: Float,
        worldHeight: Float
    ) {

        if (x < 0f) {
            x = 0f
        }

        if (y < 0f) {
            y = 0f
        }

        if (x > worldWidth - textura.width.toFloat()) {
            x = worldWidth - textura.width.toFloat()
        }

        if (y > worldHeight - textura.height.toFloat()) {
            y = worldHeight - textura.height.toFloat()
        }

        actualizarCollisionBox()
    }

    // =========================
    // ACTUALIZAR COLLISION BOX
    // =========================
    private fun actualizarCollisionBox() {

        collisionBox.x = x
        collisionBox.y = y
        collisionBox.width = textura.width.toFloat()
        collisionBox.height = textura.height.toFloat()
    }

    // =========================
    // GETTERS
    // =========================
    fun getWidth(): Float {

        return textura.width.toFloat()
    }

    fun getHeight(): Float {

        return textura.height.toFloat()
    }

    // =========================
    // DISPOSE
    // =========================
    fun dispose() {

        textura.dispose()
    }
}
