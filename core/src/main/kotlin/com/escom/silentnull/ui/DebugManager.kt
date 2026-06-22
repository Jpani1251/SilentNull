package com.escom.silentnull.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import com.escom.silentnull.entities.Player
import com.escom.silentnull.physics.GridManager

class DebugManager(
    val screenName: String,
    val worldWidth: Float,
    val worldHeight: Float
) {
    private val gridManager = GridManager(screenName, worldWidth, worldHeight, 60f)
    private val font = BitmapFont().apply { data.setScale(2f) }
    private val shapeRenderer = ShapeRenderer()

    private var debugMode = false
    private var modoPared = true // true = Pared, false = Borrar

    private val worldTouchPosition = Vector3()

    // Configuración del Menú
    private var menuX = 100f
    private var menuY = 100f
    private val menuWidth = 650f
    private val menuHeight = 650f
    private var arrastrandoMenu = false
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    // Botones (usando texturas de botones existentes)
    private val btnDebug = GameButton("btn_der.png", 0f, 0f, 100f, 100f)
    private val btnModoPared = GameButton("btn_der.png", 0f, 0f, 200f, 80f)
    private val btnModoBorrar = GameButton("btn_izq.png", 0f, 0f, 200f, 80f)
    private val btnGuardar = GameButton("btn_abajo.png", 0f, 0f, 200f, 80f)
    private val btnPrint = GameButton("btn_arriba.png", 0f, 0f, 200f, 80f)

    init {
        actualizarPosicionesBotones()
    }

    private fun actualizarPosicionesBotones() {
        btnDebug.x = Gdx.graphics.width - 150f
        btnDebug.y = Gdx.graphics.height - 150f

        btnModoPared.x = menuX + 50f
        btnModoPared.y = menuY + 250f

        btnModoBorrar.x = menuX + 350f
        btnModoBorrar.y = menuY + 250f

        btnGuardar.x = menuX + 50f
        btnGuardar.y = menuY + 100f

        btnPrint.x = menuX + 350f
        btnPrint.y = menuY + 100f
    }

    fun render(batch: SpriteBatch, camera: OrthographicCamera, hudCamera: OrthographicCamera, player: Player) {
        // 1. Coordenadas en HUD
        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.draw(batch, "X: ${player.x.toInt()}  Y: ${player.y.toInt()}", 50f, Gdx.graphics.height - 50f)
        btnDebug.render(batch)
        batch.end()

        // 2. Grid en el mundo
        if (debugMode) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            shapeRenderer.projectionMatrix = camera.combined
            gridManager.render(shapeRenderer)

            // Hitbox de Alex
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.GREEN
            shapeRenderer.rect(player.collisionBox.x, player.collisionBox.y, player.collisionBox.width, player.collisionBox.height)
            shapeRenderer.end()

            // 3. Menú de diseño en HUD
            batch.projectionMatrix = hudCamera.combined
            batch.begin()
            batch.end()

            shapeRenderer.projectionMatrix = hudCamera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0.1f, 0.1f, 0.15f, 0.85f)
            shapeRenderer.rect(menuX, menuY, menuWidth, menuHeight)

            shapeRenderer.color = Color.YELLOW
            shapeRenderer.rect(menuX, menuY + menuHeight - 60f, menuWidth, 60f)
            shapeRenderer.end()

            batch.begin()
            btnModoPared.render(batch)
            btnModoBorrar.render(batch)
            btnGuardar.render(batch)
            btnPrint.render(batch)

            font.draw(batch, "DISEÑADOR GLOBAL", menuX + 50f, menuY + menuHeight - 50f)
            font.draw(batch, if (modoPared) "MODO: PARED" else "MODO: BORRAR", menuX + 50f, menuY + menuHeight - 120f)
            font.draw(batch, "PARED", btnModoPared.x + 40f, btnModoPared.y + 55f)
            font.draw(batch, "BORRAR", btnModoBorrar.x + 30f, btnModoBorrar.y + 55f)
            font.draw(batch, "GUARDAR", btnGuardar.x + 30f, btnGuardar.y + 55f)
            font.draw(batch, "MATRIX", btnPrint.x + 45f, btnPrint.y + 55f)
            font.draw(batch, "(Arrastra desde barra amarilla)", menuX + 50f, menuY + 30f)
            batch.end()
        }
    }

    fun procesarInput(touchX: Float, touchY: Float, camera: OrthographicCamera): Boolean {
        if (!Gdx.input.isTouched) {
            arrastrandoMenu = false
            return false
        }

        if (Gdx.input.justTouched()) {
            if (btnDebug.isTouched(touchX, touchY)) {
                debugMode = !debugMode
                return true
            }

            if (debugMode) {
                // Arrastre
                if (touchX >= menuX && touchX <= menuX + menuWidth &&
                    touchY >= menuY + menuHeight - 80f && touchY <= menuY + menuHeight + 20f) {
                    arrastrandoMenu = true
                    touchOffsetX = touchX - menuX
                    touchOffsetY = touchY - menuY
                    return true
                }

                if (btnModoPared.isTouched(touchX, touchY)) { modoPared = true; return true }
                if (btnModoBorrar.isTouched(touchX, touchY)) { modoPared = false; return true }
                if (btnGuardar.isTouched(touchX, touchY)) { gridManager.saveMatrix(); return true }
                if (btnPrint.isTouched(touchX, touchY)) { gridManager.printMatrix(); return true }
            }
        }

        if (arrastrandoMenu) {
            menuX = touchX - touchOffsetX
            menuY = touchY - touchOffsetY
            actualizarPosicionesBotones()
            return true
        }

        if (debugMode) {
            val enMenu = touchX >= menuX && touchX <= menuX + menuWidth && touchY >= menuY && touchY <= menuY + menuHeight
            if (!enMenu) {
                worldTouchPosition.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
                camera.unproject(worldTouchPosition)
                gridManager.toggleCell(worldTouchPosition.x, worldTouchPosition.y, modoPared)
            }
            return true
        }

        return false
    }

    fun checkCollision(player: Player): Boolean {
        return gridManager.checkCollision(player.collisionBox.x, player.collisionBox.y, player.collisionBox.width, player.collisionBox.height)
    }

    fun dispose() {
        font.dispose()
        shapeRenderer.dispose()
        btnDebug.dispose()
        btnModoPared.dispose()
        btnModoBorrar.dispose()
        btnGuardar.dispose()
        btnPrint.dispose()
    }
}
