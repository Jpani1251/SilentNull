package com.escom.silentnull.inventory

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import com.escom.silentnull.entities.Player

class InventoryUI(private val player: Player, private val font: BitmapFont) {
    private val shapeRenderer = ShapeRenderer()
    private var isVisible = false

    // Configuración visual
    private val padding = 50f
    private val slotSize = 100f
    private val slotSpacing = 20f
    private val columns = 5
    private val rows = 4 // 5x4 = 20 slots

    private val uiWidth = (columns * slotSize) + ((columns + 1) * slotSpacing)
    private val uiHeight = (rows * slotSize) + ((rows + 1) * slotSpacing) + 100f // Espacio extra para título

    fun toggle() {
        isVisible = !isVisible
    }

    fun isOpen(): Boolean = isVisible

    fun render(batch: SpriteBatch) {
        if (!isVisible) return

        batch.end() // Pausamos el batch para usar el ShapeRenderer

        // Dibujar fondo del inventario
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        val startX = (Gdx.graphics.width - uiWidth) / 2f
        val startY = (Gdx.graphics.height - uiHeight) / 2f

        // Fondo semi-transparente oscuro
        shapeRenderer.color = Color(0f, 0f, 0f, 0.8f)
        shapeRenderer.rect(startX, startY, uiWidth, uiHeight)

        // Dibujar los slots
        shapeRenderer.color = Color.DARK_GRAY
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val x = startX + slotSpacing + (col * (slotSize + slotSpacing))
                val y = startY + slotSpacing + (row * (slotSize + slotSpacing))
                shapeRenderer.rect(x, y, slotSize, slotSize)
            }
        }

        shapeRenderer.end()

        batch.begin() // Reanudamos el batch

        // Dibujar título
        font.draw(
            batch,
            "INVENTARIO (20 slots)",
            startX,
            startY + uiHeight - 30f,
            uiWidth,
            Align.center,
            false
        )

        // Dibujar items en los slots
        val items = player.inventory.getItems()
        for (i in items.indices) {
            val col = i % columns
            val row = i / columns
            val x = startX + slotSpacing + (col * (slotSize + slotSpacing))
            val y = startY + slotSpacing + (row * (slotSize + slotSpacing))

            // Aquí cargaríamos la textura del item. Por ahora solo el nombre
            font.data.setScale(1f)
            font.draw(batch, items[i].name, x, y + slotSize / 2f, slotSize, Align.center, true)
            font.data.setScale(2.4f) // Restauramos escala
        }
    }

    fun dispose() {
        shapeRenderer.dispose()
    }
}
