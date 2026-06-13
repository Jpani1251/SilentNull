package com.escom.silentnull.entities

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.escom.silentnull.physics.CollisionBox

class Obstacle(

    val name: String,

    x: Float,
    y: Float,

    width: Float,
    height: Float
) {

    val collisionBox = CollisionBox(
        x,
        y,
        width,
        height
    )

    fun renderDebug(shapeRenderer: ShapeRenderer) {

        shapeRenderer.rect(
            collisionBox.x,
            collisionBox.y,
            collisionBox.width,
            collisionBox.height
        )
    }
}
