package com.escom.silentnull.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.escom.silentnull.physics.CollisionBox

class Player {

    enum class Direction { NORTH, SOUTH, EAST, WEST }

    // =========================
    // ANIMACIONES POR DIRECCIÓN
    // =========================
    private val texNorte = Texture("north_anim_alex.png")
    private val texSur = Texture("south_anim_alex.png")
    private val texEste = Texture("east_anim_alex.png")
    private val texOeste = Texture("west_anim_alex.png")

    private lateinit var animNorte: Animation<TextureRegion>
    private lateinit var animSur: Animation<TextureRegion>
    private lateinit var animEste: Animation<TextureRegion>
    private lateinit var animOeste: Animation<TextureRegion>

    private var currentDirection = Direction.SOUTH
    private var stateTime = 0f
    private var isMoving = false

    // Posición y Física
    var x = 0f
    var y = 0f
    private var previousX = 0f
    private var previousY = 0f
    private val escala = 2.0f // Multiplicador de tamaño
    private val velocidad = 450f

    // Usamos el tamaño del primer cuadro para la colisión
    lateinit var currentFrame: TextureRegion
    val collisionBox = CollisionBox(0f, 0f, 64f, 64f) // Valores temporales

    init {
        // Configuramos las animaciones (6 cuadros según lo solicitado)
        animNorte = crearAnimacion(texNorte, 6)
        animSur = crearAnimacion(texSur, 6)
        animEste = crearAnimacion(texEste, 6)
        animOeste = crearAnimacion(texOeste, 6)

        // Inicializar currentFrame para evitar errores antes del primer render
        currentFrame = animSur.getKeyFrame(0f)

        // Centrar jugador tomando en cuenta la escala
        x = (Gdx.graphics.width / 2f) - (getWidth() / 2f)
        y = (Gdx.graphics.height / 2f) - (getHeight() / 2f)

        actualizarCollisionBox()
    }

    private fun crearAnimacion(texture: Texture, frames: Int): Animation<TextureRegion> {
        val temp = TextureRegion.split(texture, texture.width / frames, texture.height)
        return Animation(0.15f, *temp[0])
    }

    fun update(delta: Float) {
        if (isMoving) {
            stateTime += delta
        } else {
            stateTime = 0f // Se queda en el primer cuadro si está quieto
        }

        // Resetear para el siguiente frame
        isMoving = false
        actualizarCollisionBox()
    }

    fun render(batch: SpriteBatch) {
        // Seleccionar animación según dirección
        val animation = when (currentDirection) {
            Direction.NORTH -> animNorte
            Direction.SOUTH -> animSur
            Direction.EAST -> animEste
            Direction.WEST -> animOeste
        }

        currentFrame = animation.getKeyFrame(stateTime, true)

        // Dibujar con el tamaño escalado
        batch.draw(
            currentFrame,
            x, y,
            getWidth(),
            getHeight()
        )
    }

    // =========================
    // MÉTODOS DE MOVIMIENTO
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

    // =========================
    // PERSISTENCIA DE POSICIÓN
    // =========================
    fun guardarPosicionAnterior() {
        previousX = x
        previousY = y
    }

    fun revertirMovimiento() {
        x = previousX
        y = previousY
        actualizarCollisionBox()
    }

    private fun actualizarCollisionBox() {
        collisionBox.x = x
        collisionBox.y = y
        // Ajusta el tamaño de la colisión al frame actual con escala
        if (::currentFrame.isInitialized) {
            collisionBox.width = getWidth()
            collisionBox.height = getHeight()
        }
    }

    // Getters con escala aplicada
    fun getWidth() = if (::currentFrame.isInitialized) currentFrame.regionWidth.toFloat() * escala else 0f
    fun getHeight() = if (::currentFrame.isInitialized) currentFrame.regionHeight.toFloat() * escala else 0f

    fun dispose() {
        texNorte.dispose()
        texSur.dispose()
        texEste.dispose()
        texOeste.dispose()
    }

    fun setPosition(newX: Float, newY: Float) {
        x = newX
        y = newY
        actualizarCollisionBox()
    }

    fun limitarPantalla(worldWidth: Float, worldHeight: Float) {
        x = x.coerceIn(0f, worldWidth - getWidth())
        y = y.coerceIn(0f, worldHeight - getHeight())
        actualizarCollisionBox()
    }
}
