package com.escom.silentnull.physics

import com.badlogic.gdx.math.Rectangle

class CollisionBox(

    var x: Float,
    var y: Float,

    var width: Float,
    var height: Float
) {

    private val rectangle = Rectangle(
        x,
        y,
        width,
        height
    )

    fun getRectangle(): Rectangle {

        rectangle.set(
            x,
            y,
            width,
            height
        )

        return rectangle
    }

    fun overlaps(other: CollisionBox): Boolean {

        return x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y
    }
}
