package com.escom.silentnull.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.Viewport

class InventoryManager {
    private val textureInventory = Texture("inventory.png")
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont().apply { data.setScale(2f) }

    // Botón para abrir inventario (en el HUD)
    private val btnAbrir = GameButton("btn_der.png", 0f, 0f, 150f, 150f)

    // Botón para cerrar inventario (dentro del inventario)
    private val btnCerrar = GameButton("btn_izq.png", 0f, 0f, 120f, 120f)

    private var fadeAlpha = 0f
    private var isFadingIn = false
    private var isFadingOut = false
    private var inventoryVisible = false
    private var closingFlag = false

    private val fadeSpeed = 2f

    fun update(delta: Float) {
        if (isFadingIn) {
            fadeAlpha += delta * fadeSpeed
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f
                isFadingIn = false

                if (closingFlag) {
                    inventoryVisible = false
                    closingFlag = false
                } else {
                    inventoryVisible = true
                }

                isFadingOut = true
            }
        } else if (isFadingOut) {
            fadeAlpha -= delta * fadeSpeed
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f
                isFadingOut = false
                // Reset de seguridad al terminar todo el ciclo
                isFadingIn = false
                closingFlag = false
            }
        }
    }

    fun render(batch: SpriteBatch, viewport: Viewport) {
        val width = viewport.worldWidth
        val height = viewport.worldHeight

        // 1. Dibujar Texturas
        batch.projectionMatrix = viewport.camera.combined
        batch.begin()

        // Botón INV en HUD
        if (!inventoryVisible && !isFadingIn && !isFadingOut) {
            btnAbrir.x = width - 200f
            btnAbrir.y = height - 200f
            btnAbrir.render(batch)
            font.draw(batch, "INV", btnAbrir.x + 45f, btnAbrir.y + 90f)
        }

        // Fondo del inventario
        if (inventoryVisible) {
            batch.draw(textureInventory, 0f, 0f, width, height)

            btnCerrar.x = 50f
            btnCerrar.y = height - 170f
            btnCerrar.render(batch)
            font.draw(batch, "VOLVER", btnCerrar.x + 130f, btnCerrar.y + 75f)

            font.draw(batch, "INVENTARIO", width / 2f - 100f, height - 100f)
        }

        batch.end()

        // 2. Capa de desvanecimiento (Negro) con ShapeRenderer
        if (fadeAlpha > 0f || isFadingIn || isFadingOut) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            shapeRenderer.projectionMatrix = viewport.camera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0f, 0f, fadeAlpha)
            shapeRenderer.rect(0f, 0f, width, height)
            shapeRenderer.end()
        }
    }

    fun handleInput(touchX: Float, touchY: Float): Boolean {
        if (isFadingIn || isFadingOut) return true

        if (!inventoryVisible) {
            if (btnAbrir.isTouched(touchX, touchY)) {
                openInventory()
                return true
            }
        } else {
            if (btnCerrar.isTouched(touchX, touchY)) {
                closeInventory()
                return true
            }
            return true // Bloquear input
        }
        return false
    }

    private fun openInventory() {
        isFadingIn = true
        closingFlag = false
        fadeAlpha = 0f
    }

    private fun closeInventory() {
        isFadingIn = true
        closingFlag = true
        fadeAlpha = 0f
    }

    fun isVisible(): Boolean = inventoryVisible || isFadingIn || isFadingOut

    fun dispose() {
        textureInventory.dispose()
        shapeRenderer.dispose()
        font.dispose()
        btnAbrir.dispose()
        btnCerrar.dispose()
    }
}
