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

class Edificio2SegundoPisoScreen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    // =========================
    // MUNDO
    // =========================
    private val worldWidth = 2200f
    private val worldHeight = 3900f

    // =========================
    // MEDIDAS
    // =========================
    private val wallSize = 120f

    private val corridorX = 930f
    private val corridorWidth = 850f

    private val classroomX = 180f
    private val classroomStartY = 420f
    private val classroomWidth = 620f
    private val classroomHeight = 260f
    private val classroomGap = 115f

    // Donde estaría el salón 204.
    // Aquí van las escaleras para bajar a planta baja.
    private val stairDownY =
        classroomStartY + 3f * (classroomHeight + classroomGap)

    // Donde estaría el salón 205.
    // Aquí van las escaleras para subir al tercer piso.
    private val stairUpY =
        classroomStartY + 4f * (classroomHeight + classroomGap)

    // Después de las escaleras queda el salón 206.
    private val salon6Y =
        classroomStartY + 5f * (classroomHeight + classroomGap)

    private val bathroomY =
        salon6Y + classroomHeight + classroomGap

    private val bathroomHeight = 260f

    // =========================
    // CÁMARAS
    // =========================
    private val camera = OrthographicCamera()

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    // =========================
    // DIBUJO
    // =========================
    private val shapeRenderer = ShapeRenderer()

    // =========================
    // TEXTO
    // =========================
    private val font = BitmapFont()

    // =========================
    // JUGADOR
    // =========================
    private val player = Player()

    // =========================
    // ESCALERAS
    // =========================
    private val entradaEscalerasBajar = CollisionBox(
        corridorX - 120f,
        stairDownY + 20f,
        260f,
        classroomHeight - 40f
    )

    private val entradaEscalerasSubir = CollisionBox(
        corridorX - 120f,
        stairUpY + 20f,
        260f,
        classroomHeight - 40f
    )

    // =========================
    // CONEXIÓN A EDIFICIO DE GOBIERNO
    // =========================
    private val salidaGobiernoSegundoPiso = CollisionBox(
        corridorX + corridorWidth - 140f,
        classroomStartY + classroomHeight / 2f - 100f,
        320f,
        200f
    )

    // =========================
    // BAÑOS
    // =========================
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

    // =========================
    // SALONES
    // =========================
    private data class SalonAccess(
        val nombre: String,
        val y: Float,
        val entrada: CollisionBox,
        val regresoX: Float,
        val regresoY: Float
    )

    private val entradasSalones = crearEntradasSalones()

    // =========================
    // COLISIONES
    // =========================
    private val obstaculos = crearObstaculos()

    // =========================
    // ESTADOS
    // =========================
    private var moviendoIzquierda = false
    private var moviendoDerecha = false
    private var moviendoAbajo = false
    private var cambiandoPantalla = false

    private var tiempoBloqueoAccesos = 0.35f
    private var tiempoBloqueoColisiones = 0.45f

    // =========================
    // BOTONES
    // =========================
    private val tamanoBoton = 150f

    private lateinit var btnIzq: GameButton
    private lateinit var btnDer: GameButton
    private lateinit var btnArriba: GameButton
    private lateinit var btnAbajo: GameButton

    // =========================
    // INIT
    // =========================
    init {

        font.data.setScale(2.2f)

        btnIzq = GameButton(
            "btn_izq.png",
            0f,
            0f,
            tamanoBoton,
            tamanoBoton
        )

        btnDer = GameButton(
            "btn_der.png",
            0f,
            0f,
            tamanoBoton,
            tamanoBoton
        )

        btnArriba = GameButton(
            "btn_arriba.png",
            0f,
            0f,
            tamanoBoton,
            tamanoBoton
        )

        btnAbajo = GameButton(
            "btn_abajo.png",
            0f,
            0f,
            tamanoBoton,
            tamanoBoton
        )

        player.setPosition(
            spawnX ?: corridorX + corridorWidth / 2f,
            spawnY ?: stairDownY + classroomHeight / 2f
        )

        resize(
            Gdx.graphics.width,
            Gdx.graphics.height
        )
    }

    // =========================
    // RENDER
    // =========================
    override fun render(delta: Float) {

        update(delta)

        if (cambiandoPantalla) {
            return
        }

        ScreenUtils.clear(0.04f, 0.04f, 0.05f, 1f)

        dibujarSegundoPiso()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio 2 - Segundo piso",
            160f,
            worldHeight - 160f
        )

        for (salon in entradasSalones) {
            font.draw(
                game.batch,
                salon.nombre,
                classroomX + 170f,
                salon.y + classroomHeight / 2f + 25f
            )
        }

        font.draw(
            game.batch,
            "Escaleras a PB",
            classroomX + 115f,
            stairDownY + classroomHeight / 2f + 25f
        )

        font.draw(
            game.batch,
            "Escaleras a piso 3",
            classroomX + 70f,
            stairUpY + classroomHeight / 2f + 25f
        )

        font.draw(
            game.batch,
            "Gobierno P2",
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

        player.render(game.batch)

        game.batch.end()

        // =========================
        // HUD
        // =========================
        hudViewport.apply()

        game.batch.projectionMatrix = hudCamera.combined

        game.batch.begin()

        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)

        game.batch.end()
    }

    // =========================
    // DIBUJAR SEGUNDO PISO
    // =========================
    private fun dibujarSegundoPiso() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Piso general
        shapeRenderer.color = Color(0.21f, 0.21f, 0.24f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Pasillo
        shapeRenderer.color = Color(0.32f, 0.32f, 0.36f, 1f)
        shapeRenderer.rect(
            corridorX,
            wallSize,
            corridorWidth,
            worldHeight - wallSize * 2f
        )

        // Conexión hacia edificio de gobierno segundo piso
        shapeRenderer.color = Color(0.34f, 0.34f, 0.39f, 1f)
        shapeRenderer.rect(
            corridorX + corridorWidth,
            classroomStartY + 45f,
            worldWidth - (corridorX + corridorWidth) - wallSize,
            classroomHeight - 90f
        )

        // Línea central del pasillo
        shapeRenderer.color = Color(0.38f, 0.38f, 0.42f, 1f)
        shapeRenderer.rect(
            corridorX + corridorWidth / 2f - 8f,
            wallSize,
            16f,
            worldHeight - wallSize * 2f
        )

        // Paredes
        shapeRenderer.color = Color(0.08f, 0.08f, 0.11f, 1f)

        shapeRenderer.rect(
            0f,
            worldHeight - wallSize,
            worldWidth,
            wallSize
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
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

        // Salones
        for (salon in entradasSalones) {
            dibujarSalonIzquierdo(
                classroomX,
                salon.y,
                classroomWidth,
                classroomHeight
            )
        }

        // Escaleras donde estaría el salón 204.
        dibujarEscaleras(
            classroomX,
            stairDownY,
            classroomWidth,
            classroomHeight
        )

        // Escaleras donde estaría el salón 205.
        dibujarEscaleras(
            classroomX,
            stairUpY,
            classroomWidth,
            classroomHeight
        )

        // Baños
        dibujarBanos(
            classroomX,
            bathroomY,
            classroomWidth,
            bathroomHeight
        )

        // Flechas
        shapeRenderer.color = Color.YELLOW

        dibujarFlechaIzquierda(
            corridorX + 70f,
            stairDownY + classroomHeight / 2f,
            45f
        )

        dibujarFlechaIzquierda(
            corridorX + 70f,
            stairUpY + classroomHeight / 2f,
            45f
        )

        dibujarFlechaDerecha(
            corridorX + corridorWidth - 80f,
            classroomStartY + classroomHeight / 2f,
            45f
        )

        for (salon in entradasSalones) {
            dibujarFlechaIzquierda(
                corridorX + 70f,
                salon.entrada.y + salon.entrada.height / 2f,
                35f
            )
        }

        dibujarFlechaAbajo(
            entradaBanoHombres.x + entradaBanoHombres.width / 2f,
            entradaBanoHombres.y + 90f,
            35f
        )

        dibujarFlechaAbajo(
            entradaBanoMujeres.x + entradaBanoMujeres.width / 2f,
            entradaBanoMujeres.y + 90f,
            35f
        )

        shapeRenderer.end()
    }

    // =========================
    // DIBUJAR SALÓN
    // =========================
    private fun dibujarSalonIzquierdo(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        shapeRenderer.color = Color(0.17f, 0.23f, 0.30f, 1f)
        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color = Color(0.09f, 0.09f, 0.12f, 1f)
        shapeRenderer.rect(
            x + width - 25f,
            y,
            25f,
            height
        )

        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            x + width - 25f,
            y + height / 2f - 55f,
            70f,
            110f
        )

        shapeRenderer.color = Color(0.07f, 0.10f, 0.14f, 1f)
        shapeRenderer.rect(
            x + 70f,
            y + height - 90f,
            width - 190f,
            50f
        )
    }

    // =========================
    // DIBUJAR ESCALERAS
    // =========================
    private fun dibujarEscaleras(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        shapeRenderer.color = Color(0.23f, 0.23f, 0.28f, 1f)
        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color = Color(0.09f, 0.09f, 0.12f, 1f)
        shapeRenderer.rect(
            x + width - 25f,
            y,
            25f,
            height
        )

        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            x + width - 25f,
            y + height / 2f - 60f,
            80f,
            120f
        )

        shapeRenderer.color = Color(0.13f, 0.13f, 0.17f, 1f)

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

    // =========================
    // DIBUJAR BAÑOS
    // =========================
    private fun dibujarBanos(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        val halfWidth = width / 2f

        shapeRenderer.color = Color(0.18f, 0.25f, 0.30f, 1f)
        shapeRenderer.rect(
            x,
            y,
            halfWidth - 15f,
            height
        )

        shapeRenderer.color = Color(0.25f, 0.20f, 0.28f, 1f)
        shapeRenderer.rect(
            x + halfWidth + 15f,
            y,
            halfWidth - 15f,
            height
        )

        shapeRenderer.color = Color(0.09f, 0.09f, 0.12f, 1f)

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

        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)

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

    // =========================
    // CREAR ENTRADAS A SALONES
    // =========================
    private fun crearEntradasSalones(): MutableList<SalonAccess> {

        val lista = mutableListOf<SalonAccess>()

        fun agregarSalon(
            nombre: String,
            y: Float
        ) {

            lista.add(
                SalonAccess(
                    nombre,
                    y,
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
            "Salon 201",
            classroomStartY
        )

        agregarSalon(
            "Salon 202",
            classroomStartY + 1f * (classroomHeight + classroomGap)
        )

        agregarSalon(
            "Salon 203",
            classroomStartY + 2f * (classroomHeight + classroomGap)
        )

        // No agregamos Salon 204 porque ahí están las escaleras para bajar.
        // No agregamos Salon 205 porque ahí están las escaleras para subir.

        agregarSalon(
            "Salon 206",
            salon6Y
        )

        return lista
    }

    // =========================
    // CREAR OBSTÁCULOS
    // =========================
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
                worldWidth,
                wallSize
            )
        )

        for (salon in entradasSalones) {
            lista.add(
                CollisionBox(
                    classroomX,
                    salon.y,
                    classroomWidth,
                    classroomHeight
                )
            )
        }

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

    // =========================
    // REVISAR ACCESOS
    // =========================
    private fun revisarAccesos() {

        // Conexión hacia Edificio de Gobierno segundo piso.
        if (
            moviendoDerecha
            &&
            player.collisionBox.overlaps(salidaGobiernoSegundoPiso)
        ) {

            cambiandoPantalla = true

            game.screen = EdificioGobiernoSegundoPisoScreen(
                game
            )

            dispose()

            return
        }

        // Bajar a planta baja desde las escaleras del espacio 204.
        if (
            moviendoIzquierda
            &&
            player.collisionBox.overlaps(entradaEscalerasBajar)
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2Screen(
                game,
                corridorX + corridorWidth / 2f,
                stairDownY + classroomHeight / 2f
            )

            dispose()

            return
        }

        // Subir a tercer piso desde las escaleras del espacio 205.
        if (
            moviendoIzquierda
            &&
            player.collisionBox.overlaps(entradaEscalerasSubir)
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2PisoSuperiorScreen(
                game,
                3,
                corridorX + corridorWidth / 2f,
                stairUpY + classroomHeight / 2f
            )

            dispose()

            return
        }

        // Entrar a salones.
        for (salon in entradasSalones) {

            if (
                moviendoIzquierda
                &&
                player.collisionBox.overlaps(salon.entrada)
            ) {

                cambiandoPantalla = true

                game.screen = SalonScreen(
                    game,
                    salon.nombre,
                    salon.regresoX,
                    salon.regresoY,
                    2
                )

                dispose()

                return
            }
        }

        // Entrar a baño hombres.
        if (
            moviendoAbajo
            &&
            player.collisionBox.overlaps(entradaBanoHombres)
        ) {

            cambiandoPantalla = true

            game.screen = BanoScreen(
                game,
                "Bano de Hombres",
                corridorX + corridorWidth / 2f,
                bathroomY + bathroomHeight / 2f,
                2
            )

            dispose()

            return
        }

        // Entrar a baño mujeres.
        if (
            moviendoAbajo
            &&
            player.collisionBox.overlaps(entradaBanoMujeres)
        ) {

            cambiandoPantalla = true

            game.screen = BanoScreen(
                game,
                "Bano de Mujeres",
                corridorX + corridorWidth / 2f,
                bathroomY + bathroomHeight / 2f,
                2
            )

            dispose()

            return
        }
    }

    // =========================
    // REVISAR COLISIONES
    // =========================
    private fun revisarColisiones() {

        for (obstaculo in obstaculos) {

            if (player.collisionBox.overlaps(obstaculo)) {

                player.revertirMovimiento()

                return
            }
        }
    }

    // =========================
    // FLECHAS
    // =========================
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

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        player.guardarPosicionAnterior()

        procesarInput(delta)

        player.update(delta)

        if (tiempoBloqueoAccesos > 0f) {
            tiempoBloqueoAccesos -= delta
        } else {
            revisarAccesos()
        }

        if (tiempoBloqueoColisiones > 0f) {
            tiempoBloqueoColisiones -= delta
        } else {
            if (!cambiandoPantalla) {
                revisarColisiones()
            }
        }

        player.limitarPantalla(
            worldWidth,
            worldHeight
        )

        actualizarCamara()
    }

    // =========================
    // INPUT
    // =========================
    private fun procesarInput(delta: Float) {

        moviendoIzquierda = false
        moviendoDerecha = false
        moviendoAbajo = false

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

    // =========================
    // CÁMARA
    // =========================
    private fun actualizarCamara() {

        val playerCenterX =
            player.x + player.getWidth() / 2f

        val playerCenterY =
            player.y + player.getHeight() / 2f

        val halfViewportWidth =
            camera.viewportWidth / 2f

        val halfViewportHeight =
            camera.viewportHeight / 2f

        val minCameraX = halfViewportWidth
        val maxCameraX = worldWidth - halfViewportWidth

        val minCameraY = halfViewportHeight
        val maxCameraY = worldHeight - halfViewportHeight

        val cameraX =
            if (minCameraX > maxCameraX) {
                worldWidth / 2f
            } else {
                MathUtils.clamp(
                    playerCenterX,
                    minCameraX,
                    maxCameraX
                )
            }

        val cameraY =
            if (minCameraY > maxCameraY) {
                worldHeight / 2f
            } else {
                MathUtils.clamp(
                    playerCenterY,
                    minCameraY,
                    maxCameraY
                )
            }

        camera.position.set(
            cameraX,
            cameraY,
            0f
        )

        camera.update()
    }

    // =========================
    // BOTONES
    // =========================
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

    // =========================
    // MÉTODOS OBLIGATORIOS
    // =========================
    override fun show() {}

    override fun resize(width: Int, height: Int) {

        camera.setToOrtho(
            false,
            width.toFloat(),
            height.toFloat()
        )

        camera.update()

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

    override fun hide() {}

    override fun dispose() {

        shapeRenderer.dispose()

        font.dispose()

        player.dispose()

        btnIzq.dispose()
        btnDer.dispose()
        btnArriba.dispose()
        btnAbajo.dispose()
    }
}
