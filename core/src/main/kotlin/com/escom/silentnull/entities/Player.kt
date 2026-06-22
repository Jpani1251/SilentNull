package com.escom.silentnull.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.escom.silentnull.physics.CollisionBox

class Player {

    // =========================
    // ENUM DIRECCIÓN
    // =========================
    enum class Direction {
        NORTH, SOUTH, EAST, WEST
    }

    // =========================
    // ANIMACIONES
    // =========================
    private val textureNorth = Texture("north_anim_alex.png")
    private val textureSouth = Texture("south_anim_alex.png")
    private val textureEast = Texture("east_anim_alex.png")
    private val textureWest = Texture("west_anim_alex.png")

    private val animNorth: Animation<TextureRegion>
    private val animSouth: Animation<TextureRegion>
    private val animEast: Animation<TextureRegion>
    private val animWest: Animation<TextureRegion>

    private var stateTime = 0f
    private var currentDirection = Direction.SOUTH
    private var isMoving = false

    private val frameWidth: Int
    private val frameHeight: Int

    // =========================
    // ESCALA
    // =========================
    private val escala = 2f

    // =========================
    // POSICIÓN
    // =========================
    var x = 0f
    var y = 0f

    private var previousX = 0f
    private var previousY = 0f

    // =========================
    // COLLISION BOX
    // =========================
    val collisionBox: CollisionBox

    // =========================
    // VELOCIDAD
    // =========================
    private val velocidad = 450f

    // =========================
    // INIT
    // =========================
    init {

        // Cada imagen tiene 6 frames en horizontal
        val framesCount = 6

        frameWidth = textureSouth.width / framesCount
        frameHeight = textureSouth.height

        animNorth = crearAnimacion(textureNorth, framesCount)
        animSouth = crearAnimacion(textureSouth, framesCount)
        animEast = crearAnimacion(textureEast, framesCount)
        animWest = crearAnimacion(textureWest, framesCount)

        x = (Gdx.graphics.width / 2f) - (frameWidth * escala / 2f)
        y = (Gdx.graphics.height / 2f) - (frameHeight * escala / 2f)

        collisionBox = CollisionBox(
            x + (frameWidth * escala) / 2f - 30f,
            y + 60f, // Subimos considerablemente para alinear con los pies
            60f,
            20f  // Aún más delgado para una base precisa
        )

        actualizarCollisionBox()
    }

    private fun crearAnimacion(texture: Texture, frames: Int): Animation<TextureRegion> {
        val temp = TextureRegion.split(texture, texture.width / frames, texture.height)
        val framesArray = com.badlogic.gdx.utils.Array<TextureRegion>(frames)
        for (i in 0 until frames) {
            framesArray.add(temp[0][i])
        }
        return Animation(0.1f, framesArray, Animation.PlayMode.LOOP)
    }

    // =========================
    // UPDATE
    // =========================
    fun update(delta: Float) {

        if (isMoving) {
            stateTime += delta
        } else {
            stateTime = 0f
        }

        actualizarCollisionBox()

        // Reset isMoving para el siguiente frame, procesarInput lo activará si hay presión
        isMoving = false
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

        val currentAnim = when (currentDirection) {
            Direction.NORTH -> animNorth
            Direction.SOUTH -> animSouth
            Direction.EAST -> animEast
            Direction.WEST -> animWest
        }

        val currentFrame = currentAnim.getKeyFrame(stateTime, true)

        batch.draw(
            currentFrame,
            x,
            y,
            frameWidth * escala,
            frameHeight * escala
        )
    }

    // =========================
    // MOVIMIENTO
    // =========================
    fun moverIzquierda(delta: Float) {

        currentDirection = Direction.WEST
        isMoving = true

        x -= velocidad * delta
    }

    fun moverDerecha(delta: Float) {

        currentDirection = Direction.EAST
        isMoving = true

        x += velocidad * delta
    }

    fun moverArriba(delta: Float) {

        currentDirection = Direction.NORTH
        isMoving = true

        y += velocidad * delta
    }

    fun moverAbajo(delta: Float) {

        currentDirection = Direction.SOUTH
        isMoving = true

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

        if (x > worldWidth - frameWidth * escala) {
            x = worldWidth - frameWidth * escala
        }

        if (y > worldHeight - frameHeight * escala) {
            y = worldHeight - frameHeight * escala
        }

        actualizarCollisionBox()
    }

    // =========================
    // ACTUALIZAR COLLISION BOX
    // =========================
    private fun actualizarCollisionBox() {

        val visualWidth = frameWidth * escala

        collisionBox.x = x + (visualWidth / 2f) - 30f
        collisionBox.y = y + 60f // Ajuste final para que esté en los pies (elevado 60px)
        collisionBox.width = 60f
        collisionBox.height = 20f
    }

    // =========================
    // GETTERS
    // =========================
    fun getWidth(): Float {

        return frameWidth * escala
    }

    fun getHeight(): Float {

        return frameHeight * escala
    }

    // =========================
    // DISPOSE
    // =========================
    fun dispose() {

        textureNorth.dispose()
        textureSouth.dispose()
        textureEast.dispose()
        textureWest.dispose()
    }
}
