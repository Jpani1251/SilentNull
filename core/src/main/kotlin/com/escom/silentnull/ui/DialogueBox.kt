package com.escom.silentnull.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class DialogueBox(
    private val font: BitmapFont,
    private val width: Float,
    private val height: Float,
    private val screenWidth: Float
) {
    private var messages = listOf<String>()
    private var currentMessageIndex = 0
    private var isVisible = false

    // Textura de fondo (un cuadro semi-transparente)
    private val backgroundTexture: Texture by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(0f, 0f, 0f, 0.7f) // Negro con 70% opacidad
        pixmap.fill()
        val tex = Texture(pixmap)
        pixmap.dispose()
        tex
    }

    private val layout = GlyphLayout()
    private val padding = 40f

    fun show(newMessages: List<String>) {
        messages = newMessages
        currentMessageIndex = 0
        isVisible = true
    }

    fun advance(): Boolean {
        if (!isVisible) return false
        currentMessageIndex++
        if (currentMessageIndex >= messages.size) {
            isVisible = false
            return false // Terminó el diálogo
        }
        return true // Hay más mensajes
    }

    fun render(batch: SpriteBatch) {
        if (!isVisible || messages.isEmpty()) return

        val boxX = (screenWidth - width) / 2f
        val boxY = 50f // Distancia desde el fondo

        // Dibujar fondo
        batch.draw(backgroundTexture, boxX, boxY, width, height)

        // Dibujar texto
        val text = messages[currentMessageIndex]
        layout.setText(font, text, Color.WHITE, width - (padding * 2), 1, true)

        font.draw(
            batch,
            layout,
            boxX + padding,
            boxY + height - padding
        )

        // Indicador de "Click para continuar"
        if (currentMessageIndex < messages.size) {
            font.data.setScale(1.5f)
            font.draw(batch, "v", boxX + width - 40f, boxY + 40f)
            font.data.setScale(2.4f) // Volver al tamaño original (ajustar según tu juego)
        }
    }

    fun isVisible(): Boolean = isVisible

    fun dispose() {
        backgroundTexture.dispose()
    }
}
