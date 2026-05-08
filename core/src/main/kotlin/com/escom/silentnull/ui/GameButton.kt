package com.escom.silentnull.ui

import com.badlogic.gdx.Gdx
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

        batch.draw(texture, x, y, width, height)
    }

    // =========================
    // DETECTAR TOQUE
    // =========================
    fun isTouched(): Boolean {

        if (!Gdx.input.isTouched) {
            return false
        }

        val touchX = Gdx.input.x.toFloat()

        val touchY =
            (Gdx.graphics.height - Gdx.input.y).toFloat()

        return (
            touchX in x..(x + width)
                &&
                touchY in y..(y + height)
            )
    }

    // =========================
    // DISPOSE
    // =========================
    fun dispose() {

        texture.dispose()
    }
}

