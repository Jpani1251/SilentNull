package com.escom.silentnull.ui

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class GameButton(

    texturePath: String,

    var x: Float,
    var y: Float,

    var width: Float,
    var height: Float
) {

    // =========================
    // TEXTURA
    // =========================
    private val texture = Texture(texturePath)

    // =========================
    // RENDER
    // =========================
    fun render(batch: SpriteBatch) {

        batch.draw(
            texture,
            x,
            y,
            width,
            height
        )
    }

    // =========================
    // DETECTAR TOQUE
    // =========================
    fun isTouched(
        touchX: Float,
        touchY: Float
    ): Boolean {

        return (
            touchX >= x
                &&
                touchX <= x + width
                &&
                touchY >= y
                &&
                touchY <= y + height
            )
    }

    // =========================
    // DISPOSE
    // =========================
    fun dispose() {

        texture.dispose()
    }
}
