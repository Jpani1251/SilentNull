package com.escom.silentnull.physics

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

class GridManager(
    val screenName: String,
    val worldWidth: Float,
    val worldHeight: Float,
    val cellSize: Float = 60f
) {
    private val cols = (worldWidth / cellSize).toInt()
    private val rows = (worldHeight / cellSize).toInt()

    // Matriz de colisiones: false = libre, true = pared
    private val matrix = Array(cols) { BooleanArray(rows) { false } }

    init {
        loadMatrix()
    }

    fun toggleCell(x: Float, y: Float, isWall: Boolean) {
        val col = (x / cellSize).toInt()
        val row = (y / cellSize).toInt()

        if (col in 0 until cols && row in 0 until rows) {
            matrix[col][row] = isWall
        }
    }

    fun checkCollision(x: Float, y: Float, width: Float, height: Float): Boolean {
        val startCol = (x / cellSize).toInt()
        val endCol = ((x + width) / cellSize).toInt()
        val startRow = (y / cellSize).toInt()
        val endRow = ((y + height) / cellSize).toInt()

        for (c in startCol..endCol) {
            for (r in startRow..endRow) {
                if (c in 0 until cols && r in 0 until rows) {
                    if (matrix[c][r]) return true
                } else if (c < 0 || c >= cols || r < 0 || r >= rows) {
                    return true // Límites del mundo
                }
            }
        }
        return false
    }

    fun saveMatrix() {
        val prefs = Gdx.app.getPreferences("SilentNull_Collisions_$screenName")
        val sb = StringBuilder()
        for (i in 0 until cols) {
            for (j in 0 until rows) {
                if (matrix[i][j]) sb.append("$i,$j|")
            }
        }
        prefs.putString("data", sb.toString())
        prefs.flush()
    }

    private fun loadMatrix() {
        val prefs = Gdx.app.getPreferences("SilentNull_Collisions_$screenName")
        val data = prefs.getString("data", "")
        if (data.isNotEmpty()) {
            val pairs = data.split("|")
            for (pair in pairs) {
                if (pair.isEmpty()) continue
                val coords = pair.split(",")
                if (coords.size == 2) {
                    val c = coords[0].toInt()
                    val r = coords[1].toInt()
                    if (c in 0 until cols && r in 0 until rows) {
                        matrix[c][r] = true
                    }
                }
            }
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.3f)

        // Dibujar líneas de la rejilla
        for (i in 0..cols) {
            shapeRenderer.line(i * cellSize, 0f, i * cellSize, worldHeight)
        }
        for (j in 0..rows) {
            shapeRenderer.line(0f, j * cellSize, worldWidth, j * cellSize)
        }
        shapeRenderer.end()

        // Dibujar celdas marcadas como pared
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(1f, 0f, 0f, 0.5f)
        for (i in 0 until cols) {
            for (j in 0 until rows) {
                if (matrix[i][j]) {
                    shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize)
                }
            }
        }
        shapeRenderer.end()
    }

    fun printMatrix() {
        println("--- MATRIZ DE COLISIONES ---")
        for (j in rows - 1 downTo 0) {
            val rowStr = StringBuilder()
            for (i in 0 until cols) {
                rowStr.append(if (matrix[i][j]) "1" else "0")
            }
            println(rowStr.toString())
        }
    }
}
