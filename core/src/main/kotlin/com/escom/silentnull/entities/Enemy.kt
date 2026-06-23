package com.escom.silentnull.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.escom.silentnull.physics.CollisionBox
import kotlin.math.sqrt

class Enemy(
    startX: Float,
    startY: Float,
    private val width: Float = 95f,
    private val height: Float = 110f,
    private val speed: Float = 145f,
    private val detectionRange: Float = 520f,
    private val attackRange: Float = 105f,
    private val maxHealth: Int = 100
) {

    private val spawnX = startX
    private val spawnY = startY

    var x = startX
        private set

    var y = startY
        private set

    var health = maxHealth
        private set

    val isAlive: Boolean
        get() = health > 0

    val collisionBox: CollisionBox
        get() = CollisionBox(x, y, width, height)

    fun update(
        delta: Float,
        playerX: Float,
        playerY: Float,
        playerWidth: Float,
        playerHeight: Float,
        minX: Float,
        maxX: Float,
        minY: Float,
        maxY: Float
    ) {
        if (!isAlive) {
            return
        }

        val enemyCenterX = x + width / 2f
        val enemyCenterY = y + height / 2f
        val playerCenterX = playerX + playerWidth / 2f
        val playerCenterY = playerY + playerHeight / 2f

        val differenceX = playerCenterX - enemyCenterX
        val differenceY = playerCenterY - enemyCenterY
        val distanceSquared = differenceX * differenceX + differenceY * differenceY

        if (distanceSquared > detectionRange * detectionRange) {
            return
        }

        if (distanceSquared <= attackRange * attackRange) {
            return
        }

        val distance = sqrt(distanceSquared)

        if (distance <= 0f) {
            return
        }

        x += differenceX / distance * speed * delta
        y += differenceY / distance * speed * delta

        x = MathUtils.clamp(x, minX, maxX - width)
        y = MathUtils.clamp(y, minY, maxY - height)
    }

    fun isPlayerInRange(
        playerX: Float,
        playerY: Float,
        playerWidth: Float,
        playerHeight: Float,
        range: Float
    ): Boolean {
        if (!isAlive) {
            return false
        }

        val enemyCenterX = x + width / 2f
        val enemyCenterY = y + height / 2f
        val playerCenterX = playerX + playerWidth / 2f
        val playerCenterY = playerY + playerHeight / 2f

        val differenceX = playerCenterX - enemyCenterX
        val differenceY = playerCenterY - enemyCenterY

        return differenceX * differenceX + differenceY * differenceY <= range * range
    }

    fun takeDamage(damage: Int) {
        if (!isAlive || damage <= 0) {
            return
        }

        health = (health - damage).coerceAtLeast(0)
    }

    fun reset() {
        x = spawnX
        y = spawnY
        health = maxHealth
    }

    fun render(shapeRenderer: ShapeRenderer) {
        if (!isAlive) {
            return
        }

        shapeRenderer.color = Color(0.36f, 0.08f, 0.12f, 1f)
        shapeRenderer.rect(x, y, width, height)

        shapeRenderer.color = Color(0.60f, 0.12f, 0.17f, 1f)
        shapeRenderer.rect(x + 12f, y + 16f, width - 24f, height - 32f)

        shapeRenderer.color = Color.WHITE
        shapeRenderer.rect(x + 20f, y + height - 34f, 16f, 16f)
        shapeRenderer.rect(x + width - 36f, y + height - 34f, 16f, 16f)

        shapeRenderer.color = Color.BLACK
        shapeRenderer.rect(x + 25f, y + height - 30f, 7f, 9f)
        shapeRenderer.rect(x + width - 31f, y + height - 30f, 7f, 9f)

        val healthBarWidth = width
        val healthPercentage = health.toFloat() / maxHealth.toFloat()

        shapeRenderer.color = Color(0.08f, 0.08f, 0.08f, 1f)
        shapeRenderer.rect(x, y + height + 15f, healthBarWidth, 14f)

        shapeRenderer.color = Color(0.80f, 0.08f, 0.08f, 1f)
        shapeRenderer.rect(
            x + 2f,
            y + height + 17f,
            (healthBarWidth - 4f) * healthPercentage,
            10f
        )
    }
}
