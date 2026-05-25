package com.escom.silentnull.physics

import com.badlogic.gdx.math.Rectangle

class CollisionBox(

    var x: Float,
    var y: Float,

    var width: Float,
    var height: Float
) {

    fun getRectangle(): Rectangle {

        return Rectangle(
            x,
            y,
            width,
            height
        )
    }

    fun overlaps(other: CollisionBox): Boolean {

        return getRectangle().overlaps(
            other.getRectangle()
        )
    }
}
