package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.escom.silentnull.SilentNullGame
import com.escom.silentnull.entities.Player
import com.escom.silentnull.physics.CollisionBox
import com.escom.silentnull.ui.GameButton

class Edificio2Screen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    private val worldWidth = 2200f
    private val worldHeight = 3900f

    private val wallSize = 120f

    private val corridorX = 930f
    private val corridorWidth = 850f

    private val classroomX = 180f
    private val classroomStartY = 420f
    private val classroomWidth = 620f
    private val classroomHeight = 260f
    private val classroomGap = 115f

    private val stairY =
        classroomStartY + 3f * (classroomHeight + classroomGap)

    private val salon5Y =
        classroomStartY + 4f * (classroomHeight + classroomGap)

    private val salon6Y =
        classroomStartY + 5f * (classroomHeight + classroomGap)

    private val bathroomY =
        salon6Y + classroomHeight + classroomGap

    private val bathroomHeight = 260f

    private val camera = OrthographicCamera()

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()

    private val player = Player()

    private val salidaEdificio2 = CollisionBox(
        corridorX + 220f,
        0f,
        420f,
        260f
    )

    private val entradaEscaleras = CollisionBox(
        corridorX - 120f,
        stairY + 20f,
        260f,
        classroomHeight - 40f
    )

    // =========================
    // CONEXION A GOBIERNO
    // =========================
    private val salidaGobiernoPlantaBaja = CollisionBox(
        corridorX + corridorWidth - 140f,
        classroomStartY + classroomHeight / 2f - 120f,
        360f,
        240f
    )

    // =========================
    // CONEXION AL EDIFICIO CENTRAL
    // =========================
    private val salidaEdificioCentral = CollisionBox(
        corridorX + corridorWidth - 140f,
        bathroomY + bathroomHeight / 2f - 120f,
        360f,
        240f
    )

    private val entradaBanoHombres = CollisionBox(
        classroomX + 20f,
        bathroomY + bathroomHeight - 20f,
        classroomWidth / 2f - 40f,
        160f
    )

    private val entradaBanoMujeres = CollisionBox(
        classroomX + classroomWidth / 2f + 20f,
        bathroomY + bathroomHeight - 20f,
        classroomWidth / 2f - 40f,
        160f
    )

    private data class SalonAccess(
        val nombre: String,
        val entrada: CollisionBox,
        val regresoX: Float,
        val regresoY: Float
    )

    private val entradasSalones = crearEntradasSalones()
    private val obstaculos = crearObstaculos()

    private var moviendoAbajo = false
    private var moviendoIzquierda = false
    private var moviendoDerecha = false

    private var cambiandoPantalla = false
    private var recursosLiberados = false

    private var tiempoBloqueoAccesos = 0.35f
    private var tiempoBloqueoColisiones = 0.45f

    private val tamanoBoton = 150f

    private val btnIzq = GameButton(
        "btn_izq.png",
        0f,
        0f,
        tamanoBoton,
        tamanoBoton
    )

    private val btnDer = GameButton(
        "btn_der.png",
        0f,
        0f,
        tamanoBoton,
        tamanoBoton
    )

    private val btnArriba = GameButton(
        "btn_arriba.png",
        0f,
        0f,
        tamanoBoton,
        tamanoBoton
    )

    private val btnAbajo = GameButton(
        "btn_abajo.png",
        0f,
        0f,
        tamanoBoton,
        tamanoBoton
    )

    init {

        font.data.setScale(2.2f)

        player.setPosition(
            spawnX ?: corridorX + corridorWidth / 2f,
            spawnY ?: 300f
        )

        resize(
            Gdx.graphics.width,
            Gdx.graphics.height
        )
    }

    override fun render(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        update(delta)

        if (cambiandoPantalla) {
            return
        }

        ScreenUtils.clear(
            0.04f,
            0.04f,
            0.05f,
            1f
        )

        dibujarEdificio2()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio 2 - Planta baja",
            160f,
            worldHeight - 160f
        )

        font.draw(
            game.batch,
            "Salon 1",
            classroomX + 190f,
            classroomStartY + classroomHeight / 2f + 25f
        )

        font.draw(
            game.batch,
            "Salon 2",
            classroomX + 190f,
            classroomStartY +
                classroomHeight +
                classroomGap +
                classroomHeight / 2f +
                25f
        )

        font.draw(
            game.batch,
            "Salon 3",
            classroomX + 190f,
            classroomStartY +
                2f * (classroomHeight + classroomGap) +
                classroomHeight / 2f +
                25f
        )

        font.draw(
            game.batch,
            "Escaleras",
            classroomX + 160f,
            stairY + classroomHeight / 2f + 25f
        )

        font.draw(
            game.batch,
            "Salon 5",
            classroomX + 190f,
            salon5Y + classroomHeight / 2f + 25f
        )

        font.draw(
            game.batch,
            "Salon 6",
            classroomX + 190f,
            salon6Y + classroomHeight / 2f + 25f
        )

        font.draw(
            game.batch,
            "Gobierno PB",
            corridorX + corridorWidth + 35f,
            classroomStartY + classroomHeight / 2f + 30f
        )

        font.draw(
            game.batch,
            "Bano H",
            classroomX + 70f,
            bathroomY + bathroomHeight / 2f + 20f
        )

        font.draw(
            game.batch,
            "Bano M",
            classroomX + 390f,
            bathroomY + bathroomHeight / 2f + 20f
        )

        font.draw(
            game.batch,
            "Edificio Central",
            corridorX + corridorWidth + 20f,
            bathroomY + bathroomHeight / 2f + 30f
        )

        player.render(game.batch)

        game.batch.end()

        hudViewport.apply()

        game.batch.projectionMatrix = hudCamera.combined

        game.batch.begin()

        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)

        game.batch.end()
    }

    private fun dibujarEdificio2() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        // Fondo
        shapeRenderer.color = Color(
            0.21f,
            0.21f,
            0.24f,
            1f
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Pasillo principal
        shapeRenderer.color = Color(
            0.32f,
            0.32f,
            0.36f,
            1f
        )

        shapeRenderer.rect(
            corridorX,
            wallSize,
            corridorWidth,
            worldHeight - wallSize * 2f
        )

        // Pasillo hacia Gobierno
        shapeRenderer.color = Color(
            0.34f,
            0.34f,
            0.39f,
            1f
        )

        shapeRenderer.rect(
            corridorX + corridorWidth,
            classroomStartY + 45f,
            worldWidth -
                (corridorX + corridorWidth) -
                wallSize,
            classroomHeight - 90f
        )

        // Pasillo hacia Edificio Central
        shapeRenderer.color = Color(
            0.34f,
            0.34f,
            0.39f,
            1f
        )

        shapeRenderer.rect(
            corridorX + corridorWidth,
            bathroomY + 45f,
            worldWidth -
                (corridorX + corridorWidth) -
                wallSize,
            bathroomHeight - 90f
        )

        // Linea central
        shapeRenderer.color = Color(
            0.38f,
            0.38f,
            0.42f,
            1f
        )

        shapeRenderer.rect(
            corridorX + corridorWidth / 2f - 8f,
            wallSize,
            16f,
            worldHeight - wallSize * 2f
        )

        // Paredes
        shapeRenderer.color = Color(
            0.08f,
            0.08f,
            0.11f,
            1f
        )

        shapeRenderer.rect(
            0f,
            worldHeight - wallSize,
            worldWidth,
            wallSize
        )

        shapeRenderer.rect(
            0f,
            0f,
            salidaEdificio2.x,
            wallSize
        )

        shapeRenderer.rect(
            salidaEdificio2.x + salidaEdificio2.width,
            0f,
            worldWidth -
                (salidaEdificio2.x + salidaEdificio2.width),
            wallSize
        )

        shapeRenderer.rect(
            0f,
            0f,
            wallSize,
            worldHeight
        )

        shapeRenderer.rect(
            worldWidth - wallSize,
            0f,
            wallSize,
            worldHeight
        )

        dibujarSalonIzquierdo(
            classroomX,
            classroomStartY,
            classroomWidth,
            classroomHeight
        )

        dibujarSalonIzquierdo(
            classroomX,
            classroomStartY +
                classroomHeight +
                classroomGap,
            classroomWidth,
            classroomHeight
        )

        dibujarSalonIzquierdo(
            classroomX,
            classroomStartY +
                2f * (classroomHeight + classroomGap),
            classroomWidth,
            classroomHeight
        )

        dibujarEscaleras(
            classroomX,
            stairY,
            classroomWidth,
            classroomHeight
        )

        dibujarSalonIzquierdo(
            classroomX,
            salon5Y,
            classroomWidth,
            classroomHeight
        )

        dibujarSalonIzquierdo(
            classroomX,
            salon6Y,
            classroomWidth,
            classroomHeight
        )

        dibujarBanos(
            classroomX,
            bathroomY,
            classroomWidth,
            bathroomHeight
        )

        // Puerta inferior
        shapeRenderer.color = Color(
            0.55f,
            0.38f,
            0.20f,
            1f
        )

        shapeRenderer.rect(
            salidaEdificio2.x,
            wallSize,
            salidaEdificio2.width,
            70f
        )

        // Puerta Gobierno
        shapeRenderer.rect(
            corridorX + corridorWidth - 20f,
            classroomStartY + classroomHeight / 2f - 60f,
            80f,
            120f
        )

        // Puerta Edificio Central
        shapeRenderer.rect(
            corridorX + corridorWidth - 20f,
            bathroomY + bathroomHeight / 2f - 60f,
            80f,
            120f
        )

        shapeRenderer.color = Color.YELLOW

        dibujarFlechaAbajo(
            salidaEdificio2.x + salidaEdificio2.width / 2f,
            salidaEdificio2.y + salidaEdificio2.height + 100f,
            45f
        )

        dibujarFlechaArriba(
            corridorX + corridorWidth / 2f,
            720f,
            45f
        )

        dibujarFlechaIzquierda(
            corridorX + 70f,
            stairY + classroomHeight / 2f,
            45f
        )

        dibujarFlechaDerecha(
            corridorX + corridorWidth - 80f,
            classroomStartY + classroomHeight / 2f,
            45f
        )

        // Flecha hacia Edificio Central
        dibujarFlechaDerecha(
            corridorX + corridorWidth - 80f,
            bathroomY + bathroomHeight / 2f,
            45f
        )

        for (salon in entradasSalones) {

            dibujarFlechaIzquierda(
                corridorX + 70f,
                salon.entrada.y +
                    salon.entrada.height / 2f,
                35f
            )
        }

        dibujarFlechaAbajo(
            entradaBanoHombres.x +
                entradaBanoHombres.width / 2f,
            entradaBanoHombres.y + 90f,
            35f
        )

        dibujarFlechaAbajo(
            entradaBanoMujeres.x +
                entradaBanoMujeres.width / 2f,
            entradaBanoMujeres.y + 90f,
            35f
        )

        shapeRenderer.end()
    }

    private fun dibujarSalonIzquierdo(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        shapeRenderer.color = Color(
            0.17f,
            0.23f,
            0.30f,
            1f
        )

        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color = Color(
            0.09f,
            0.09f,
            0.12f,
            1f
        )

        shapeRenderer.rect(
            x + width - 25f,
            y,
            25f,
            height
        )

        shapeRenderer.color = Color(
            0.55f,
            0.38f,
            0.20f,
            1f
        )

        shapeRenderer.rect(
            x + width - 25f,
            y + height / 2f - 55f,
            70f,
            110f
        )

        shapeRenderer.color = Color(
            0.07f,
            0.10f,
            0.14f,
            1f
        )

        shapeRenderer.rect(
            x + 70f,
            y + height - 90f,
            width - 190f,
            50f
        )
    }

    private fun dibujarEscaleras(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        shapeRenderer.color = Color(
            0.23f,
            0.23f,
            0.28f,
            1f
        )

        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color = Color(
            0.09f,
            0.09f,
            0.12f,
            1f
        )

        shapeRenderer.rect(
            x + width - 25f,
            y,
            25f,
            height
        )

        shapeRenderer.color = Color(
            0.55f,
            0.38f,
            0.20f,
            1f
        )

        shapeRenderer.rect(
            x + width - 25f,
            y + height / 2f - 60f,
            80f,
            120f
        )

        shapeRenderer.color = Color(
            0.13f,
            0.13f,
            0.17f,
            1f
        )

        var stepY = y + 35f

        for (i in 0 until 7) {

            shapeRenderer.rect(
                x + 90f,
                stepY,
                width - 220f,
                20f
            )

            stepY += 32f
        }
    }

    private fun dibujarBanos(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        val halfWidth = width / 2f

        shapeRenderer.color = Color(
            0.18f,
            0.25f,
            0.30f,
            1f
        )

        shapeRenderer.rect(
            x,
            y,
            halfWidth - 15f,
            height
        )

        shapeRenderer.color = Color(
            0.25f,
            0.20f,
            0.28f,
            1f
        )

        shapeRenderer.rect(
            x + halfWidth + 15f,
            y,
            halfWidth - 15f,
            height
        )

        shapeRenderer.color = Color(
            0.09f,
            0.09f,
            0.12f,
            1f
        )

        shapeRenderer.rect(
            x + halfWidth - 15f,
            y,
            30f,
            height
        )

        shapeRenderer.rect(
            x + width - 25f,
            y,
            25f,
            height
        )

        shapeRenderer.color = Color(
            0.55f,
            0.38f,
            0.20f,
            1f
        )

        shapeRenderer.rect(
            x + 95f,
            y + height - 20f,
            120f,
            60f
        )

        shapeRenderer.rect(
            x + halfWidth + 110f,
            y + height - 20f,
            120f,
            60f
        )
    }

    private fun crearEntradasSalones(): MutableList<SalonAccess> {

        val lista = mutableListOf<SalonAccess>()

        fun agregarSalon(
            nombre: String,
            y: Float
        ) {

            lista.add(
                SalonAccess(
                    nombre,
                    CollisionBox(
                        classroomX + classroomWidth - 40f,
                        y + classroomHeight / 2f - 85f,
                        260f,
                        170f
                    ),
                    corridorX + 100f,
                    y + classroomHeight / 2f
                )
            )
        }

        agregarSalon(
            "Salon 1",
            classroomStartY
        )

        agregarSalon(
            "Salon 2",
            classroomStartY +
                classroomHeight +
                classroomGap
        )

        agregarSalon(
            "Salon 3",
            classroomStartY +
                2f * (classroomHeight + classroomGap)
        )

        agregarSalon(
            "Salon 5",
            salon5Y
        )

        agregarSalon(
            "Salon 6",
            salon6Y
        )

        return lista
    }

    private fun crearObstaculos(): MutableList<CollisionBox> {

        val lista = mutableListOf<CollisionBox>()

        lista.add(
            CollisionBox(
                0f,
                worldHeight - wallSize,
                worldWidth,
                wallSize
            )
        )

        lista.add(
            CollisionBox(
                0f,
                0f,
                wallSize,
                worldHeight
            )
        )

        lista.add(
            CollisionBox(
                worldWidth - wallSize,
                0f,
                wallSize,
                worldHeight
            )
        )

        lista.add(
            CollisionBox(
                0f,
                0f,
                salidaEdificio2.x,
                wallSize
            )
        )

        lista.add(
            CollisionBox(
                salidaEdificio2.x + salidaEdificio2.width,
                0f,
                worldWidth -
                    (salidaEdificio2.x + salidaEdificio2.width),
                wallSize
            )
        )

        lista.add(
            CollisionBox(
                classroomX,
                classroomStartY,
                classroomWidth,
                classroomHeight
            )
        )

        lista.add(
            CollisionBox(
                classroomX,
                classroomStartY +
                    classroomHeight +
                    classroomGap,
                classroomWidth,
                classroomHeight
            )
        )

        lista.add(
            CollisionBox(
                classroomX,
                classroomStartY +
                    2f * (classroomHeight + classroomGap),
                classroomWidth,
                classroomHeight
            )
        )

        lista.add(
            CollisionBox(
                classroomX,
                salon5Y,
                classroomWidth,
                classroomHeight
            )
        )

        lista.add(
            CollisionBox(
                classroomX,
                salon6Y,
                classroomWidth,
                classroomHeight
            )
        )

        lista.add(
            CollisionBox(
                classroomX,
                bathroomY,
                classroomWidth,
                bathroomHeight
            )
        )

        return lista
    }

    private fun revisarAccesos(): Boolean {

        // Edificio Central
        if (
            moviendoDerecha &&
            player.collisionBox.overlaps(
                salidaEdificioCentral
            )
        ) {

            cambiandoPantalla = true

            game.screen = EdificioCentralScreen(
                game,
                360f,
                700f
            )

            return true
        }

        // Gobierno
        if (
            moviendoDerecha &&
            player.collisionBox.overlaps(
                salidaGobiernoPlantaBaja
            )
        ) {

            cambiandoPantalla = true

            game.screen = EdificioGobiernoScreen(
                game,
                430f,
                700f
            )

            return true
        }

        // Mapa principal
        if (
            moviendoAbajo &&
            player.collisionBox.overlaps(
                salidaEdificio2
            )
        ) {

            cambiandoPantalla = true

            game.screen = JuegoScreen(
                game,
                3000f * 0.46f,
                3000f * 0.58f
            )

            return true
        }

        // Segundo piso
        if (
            moviendoIzquierda &&
            player.collisionBox.overlaps(
                entradaEscaleras
            )
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2SegundoPisoScreen(
                game,
                corridorX + corridorWidth / 2f,
                stairY + classroomHeight / 2f
            )

            return true
        }

        // Salones
        for (salon in entradasSalones) {

            if (
                moviendoIzquierda &&
                player.collisionBox.overlaps(
                    salon.entrada
                )
            ) {

                cambiandoPantalla = true

                game.screen = SalonScreen(
                    game,
                    salon.nombre,
                    salon.regresoX,
                    salon.regresoY,
                    1,
                    2
                )

                return true
            }
        }

        // Bano hombres
        if (
            moviendoAbajo &&
            player.collisionBox.overlaps(
                entradaBanoHombres
            )
        ) {

            cambiandoPantalla = true

            game.screen = BanoScreen(
                game,
                "Bano de Hombres",
                corridorX + corridorWidth / 2f,
                bathroomY + bathroomHeight / 2f,
                1,
                2
            )

            return true
        }

        // Bano mujeres
        if (
            moviendoAbajo &&
            player.collisionBox.overlaps(
                entradaBanoMujeres
            )
        ) {

            cambiandoPantalla = true

            game.screen = BanoScreen(
                game,
                "Bano de Mujeres",
                corridorX + corridorWidth / 2f,
                bathroomY + bathroomHeight / 2f,
                1,
                2
            )

            return true
        }

        return false
    }

    private fun revisarColisiones() {

        for (obstaculo in obstaculos) {

            if (
                player.collisionBox.overlaps(
                    obstaculo
                )
            ) {

                player.revertirMovimiento()
                return
            }
        }
    }

    private fun dibujarFlechaAbajo(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth = size * 0.45f
        val bodyLength = size * 1.4f

        shapeRenderer.triangle(
            centerX,
            centerY - size,
            centerX - size,
            centerY + size,
            centerX + size,
            centerY + size
        )

        shapeRenderer.rect(
            centerX - bodyWidth / 2f,
            centerY + size,
            bodyWidth,
            bodyLength
        )
    }

    private fun dibujarFlechaArriba(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth = size * 0.45f
        val bodyLength = size * 1.4f

        shapeRenderer.triangle(
            centerX,
            centerY + size,
            centerX - size,
            centerY - size,
            centerX + size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX - bodyWidth / 2f,
            centerY - size - bodyLength,
            bodyWidth,
            bodyLength
        )
    }

    private fun dibujarFlechaIzquierda(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth = size * 0.45f
        val bodyLength = size * 1.4f

        shapeRenderer.triangle(
            centerX - size,
            centerY,
            centerX + size,
            centerY + size,
            centerX + size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX + size,
            centerY - bodyWidth / 2f,
            bodyLength,
            bodyWidth
        )
    }

    private fun dibujarFlechaDerecha(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth = size * 0.45f
        val bodyLength = size * 1.4f

        shapeRenderer.triangle(
            centerX + size,
            centerY,
            centerX - size,
            centerY + size,
            centerX - size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX - size - bodyLength,
            centerY - bodyWidth / 2f,
            bodyLength,
            bodyWidth
        )
    }

    private fun update(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        player.guardarPosicionAnterior()

        procesarInput(delta)

        player.update(delta)

        if (tiempoBloqueoAccesos > 0f) {

            tiempoBloqueoAccesos -= delta

        } else if (revisarAccesos()) {

            return
        }

        if (tiempoBloqueoColisiones > 0f) {

            tiempoBloqueoColisiones -= delta

        } else {

            revisarColisiones()
        }

        player.limitarPantalla(
            worldWidth,
            worldHeight
        )

        actualizarCamara()
    }

    private fun procesarInput(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        moviendoAbajo = false
        moviendoIzquierda = false
        moviendoDerecha = false

        if (!Gdx.input.isTouched) {
            return
        }

        touchPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat(),
            0f
        )

        hudViewport.unproject(touchPosition)

        val touchX = touchPosition.x
        val touchY = touchPosition.y

        if (btnIzq.isTouched(touchX, touchY)) {

            moviendoIzquierda = true
            player.moverIzquierda(delta)
        }

        if (btnDer.isTouched(touchX, touchY)) {

            moviendoDerecha = true
            player.moverDerecha(delta)
        }

        if (btnArriba.isTouched(touchX, touchY)) {
            player.moverArriba(delta)
        }

        if (btnAbajo.isTouched(touchX, touchY)) {

            moviendoAbajo = true
            player.moverAbajo(delta)
        }
    }

    private fun actualizarCamara() {

        val centerX =
            player.x + player.getWidth() / 2f

        val centerY =
            player.y + player.getHeight() / 2f

        val halfWidth =
            camera.viewportWidth / 2f

        val halfHeight =
            camera.viewportHeight / 2f

        val cameraX =
            if (halfWidth > worldWidth - halfWidth) {

                worldWidth / 2f

            } else {

                MathUtils.clamp(
                    centerX,
                    halfWidth,
                    worldWidth - halfWidth
                )
            }

        val cameraY =
            if (halfHeight > worldHeight - halfHeight) {

                worldHeight / 2f

            } else {

                MathUtils.clamp(
                    centerY,
                    halfHeight,
                    worldHeight - halfHeight
                )
            }

        camera.position.set(
            cameraX,
            cameraY,
            0f
        )

        camera.update()
    }

    private fun posicionarBotones() {

        val margenX = 50f
        val margenY = 50f

        btnIzq.x = margenX
        btnIzq.y = margenY + tamanoBoton

        btnDer.x = margenX + tamanoBoton * 2f
        btnDer.y = margenY + tamanoBoton

        btnArriba.x = margenX + tamanoBoton
        btnArriba.y = margenY + tamanoBoton * 2f

        btnAbajo.x = margenX + tamanoBoton
        btnAbajo.y = margenY
    }

    override fun show() {}

    override fun resize(
        width: Int,
        height: Int
    ) {

        camera.setToOrtho(
            false,
            width.toFloat(),
            height.toFloat()
        )

        hudViewport.update(
            width,
            height,
            true
        )

        posicionarBotones()
        actualizarCamara()
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {
        dispose()
    }

    override fun dispose() {

        if (recursosLiberados) {
            return
        }

        shapeRenderer.dispose()
        font.dispose()
        player.dispose()

        btnIzq.dispose()
        btnDer.dispose()
        btnArriba.dispose()
        btnAbajo.dispose()

        recursosLiberados = true
    }
}
