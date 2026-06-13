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

class EdificioGobiernoSegundoPisoScreen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    // =========================
    // MUNDO
    // =========================
    private val worldWidth = 2200f
    private val worldHeight = 1500f

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
    // CONEXIÓN DE REGRESO AL EDIFICIO 2
    // =========================
    private val salidaEdificio2 = CollisionBox(
        0f,
        worldHeight / 2f - 190f,
        280f,
        380f
    )

    // =========================
    // ESCALERAS A PRIMER PISO
    // =========================
    private val escalerasX = worldWidth / 2f - 190f
    private val escalerasY = worldHeight - 470f
    private val escalerasWidth = 380f
    private val escalerasHeight = 250f

    private val entradaEscalerasBajar = CollisionBox(
        escalerasX,
        escalerasY - 120f,
        escalerasWidth,
        escalerasHeight + 120f
    )

    // =========================
    // CONEXIÓN A EDIFICIO 1 SEGUNDO PISO
    // =========================
    private val conexionEdificio1SegundoPiso = CollisionBox(
        worldWidth - 310f,
        worldHeight / 2f - 160f,
        260f,
        320f
    )

    // =========================
    // SALONES INFERIORES
    // =========================
    private val salonWidth = 520f
    private val salonHeight = 280f

    // Bajamos los salones para que no invadan la conexión hacia Edificio 1.
    private val salonY = 120f

    private val salonIzquierdoX = 360f
    private val salonDerechoX = worldWidth - salonWidth - 360f

    private data class SalonGobiernoAccess(
        val nombre: String,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val entrada: CollisionBox,
        val regresoX: Float,
        val regresoY: Float
    )

    private val salonesGobierno = crearSalonesGobierno()

    // =========================
    // ESTADOS
    // =========================
    private var moviendoIzquierda = false
    private var moviendoDerecha = false
    private var moviendoArriba = false
    private var moviendoAbajo = false
    private var cambiandoPantalla = false
    private var recursosLiberados = false

    private var tiempoBloqueoAccesos = 0.35f

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
            spawnX ?: 320f,
            spawnY ?: worldHeight / 2f
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

        if (cambiandoPantalla) {
            return
        }

        update(delta)

        if (cambiandoPantalla) {
            return
        }

        ScreenUtils.clear(0.04f, 0.04f, 0.05f, 1f)

        dibujarEdificioGobiernoSegundoPiso()

        game.batch.projectionMatrix = camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio de Gobierno - Segundo piso",
            120f,
            worldHeight - 120f
        )

        font.draw(
            game.batch,
            "Regresar a Edificio 2",
            140f,
            worldHeight / 2f + 250f
        )

        font.draw(
            game.batch,
            "Escaleras a primer piso",
            escalerasX - 20f,
            escalerasY + escalerasHeight + 80f
        )

        font.draw(
            game.batch,
            "Conexion a Edificio 1",
            worldWidth - 560f,
            worldHeight / 2f + 220f
        )

        for (salon in salonesGobierno) {
            font.draw(
                game.batch,
                salon.nombre,
                salon.x + 145f,
                salon.y + salon.height / 2f + 25f
            )
        }

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
    // DIBUJAR ESCENARIO
    // =========================
    private fun dibujarEdificioGobiernoSegundoPiso() {

        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Piso general
        shapeRenderer.color = Color(0.23f, 0.23f, 0.27f, 1f)
        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Área principal
        shapeRenderer.color = Color(0.33f, 0.33f, 0.38f, 1f)
        shapeRenderer.rect(
            180f,
            160f,
            worldWidth - 360f,
            worldHeight - 320f
        )

        // Conexión hacia Edificio 2
        shapeRenderer.color = Color(0.35f, 0.35f, 0.40f, 1f)
        shapeRenderer.rect(
            100f,
            worldHeight / 2f - 210f,
            480f,
            420f
        )

        // Área central superior
        shapeRenderer.color = Color(0.36f, 0.36f, 0.41f, 1f)
        shapeRenderer.rect(
            650f,
            820f,
            900f,
            300f
        )

        // Conexión hacia Edificio 1
        shapeRenderer.color = Color(0.35f, 0.35f, 0.40f, 1f)
        shapeRenderer.rect(
            worldWidth - 520f,
            worldHeight / 2f - 180f,
            360f,
            360f
        )

        // Línea decorativa central
        shapeRenderer.color = Color(0.40f, 0.40f, 0.45f, 1f)
        shapeRenderer.rect(
            worldWidth / 2f - 7f,
            160f,
            14f,
            worldHeight - 320f
        )

        // Salones inferiores
        for (salon in salonesGobierno) {
            dibujarSalonGobierno(
                salon
            )
        }

        // Escaleras superiores
        dibujarEscaleras(
            escalerasX,
            escalerasY,
            escalerasWidth,
            escalerasHeight
        )

        // Paredes exteriores
        shapeRenderer.color = Color(0.08f, 0.08f, 0.11f, 1f)

        shapeRenderer.rect(
            0f,
            worldHeight - 100f,
            worldWidth,
            100f
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            100f
        )

        shapeRenderer.rect(
            0f,
            0f,
            100f,
            worldHeight
        )

        shapeRenderer.rect(
            worldWidth - 100f,
            0f,
            100f,
            worldHeight
        )

        // Entrada desde Edificio 2
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            100f,
            worldHeight / 2f - 100f,
            100f,
            200f
        )

        // Entrada hacia Edificio 1
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            worldWidth - 190f,
            worldHeight / 2f - 100f,
            90f,
            200f
        )

        // Flechas
        shapeRenderer.color = Color.YELLOW

        // Regresar a Edificio 2
        dibujarFlechaIzquierda(
            320f,
            worldHeight / 2f,
            45f
        )

        // Entrar a salones inferiores
        for (salon in salonesGobierno) {
            dibujarFlechaAbajo(
                salon.entrada.x + salon.entrada.width / 2f,
                salon.entrada.y + salon.entrada.height + 55f,
                35f
            )
        }

        // Bajar a primer piso por escaleras superiores
        dibujarFlechaArriba(
            escalerasX + escalerasWidth / 2f,
            escalerasY - 70f,
            45f
        )

        // Flecha derecha para conexión con Edificio 1
        dibujarFlechaDerecha(
            worldWidth - 430f,
            worldHeight / 2f,
            45f
        )

        shapeRenderer.end()
    }

    // =========================
    // CREAR SALONES
    // =========================
    private fun crearSalonesGobierno(): MutableList<SalonGobiernoAccess> {

        val lista = mutableListOf<SalonGobiernoAccess>()

        fun agregarSalon(
            nombre: String,
            x: Float,
            y: Float
        ) {

            val entradaWidth = 220f
            val entradaHeight = 150f

            val entrada = CollisionBox(
                x + salonWidth / 2f - entradaWidth / 2f,
                y + salonHeight - 30f,
                entradaWidth,
                entradaHeight
            )

            lista.add(
                SalonGobiernoAccess(
                    nombre,
                    x,
                    y,
                    salonWidth,
                    salonHeight,
                    entrada,
                    entrada.x + entrada.width / 2f,
                    y + salonHeight + 60f
                )
            )
        }

        agregarSalon(
            "Salon G201",
            salonIzquierdoX,
            salonY
        )

        agregarSalon(
            "Salon G202",
            salonDerechoX,
            salonY
        )

        return lista
    }

    // =========================
    // DIBUJAR SALÓN
    // =========================
    private fun dibujarSalonGobierno(
        salon: SalonGobiernoAccess
    ) {

        // Piso del salón
        shapeRenderer.color = Color(0.17f, 0.23f, 0.30f, 1f)
        shapeRenderer.rect(
            salon.x,
            salon.y,
            salon.width,
            salon.height
        )

        // Pared superior
        shapeRenderer.color = Color(0.09f, 0.09f, 0.12f, 1f)
        shapeRenderer.rect(
            salon.x,
            salon.y + salon.height - 25f,
            salon.width,
            25f
        )

        // Puerta superior
        shapeRenderer.color = Color(0.55f, 0.38f, 0.20f, 1f)
        shapeRenderer.rect(
            salon.x + salon.width / 2f - 60f,
            salon.y + salon.height - 20f,
            120f,
            60f
        )

        // Pizarrón / detalle
        shapeRenderer.color = Color(0.07f, 0.10f, 0.14f, 1f)
        shapeRenderer.rect(
            salon.x + 80f,
            salon.y + 70f,
            salon.width - 160f,
            45f
        )

        // Mesa
        shapeRenderer.color = Color(0.42f, 0.30f, 0.18f, 1f)
        shapeRenderer.rect(
            salon.x + 155f,
            salon.y + 145f,
            210f,
            75f
        )

        // Sillas
        shapeRenderer.color = Color(0.12f, 0.13f, 0.16f, 1f)

        shapeRenderer.rect(
            salon.x + 175f,
            salon.y + 105f,
            55f,
            35f
        )

        shapeRenderer.rect(
            salon.x + 285f,
            salon.y + 105f,
            55f,
            35f
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

        shapeRenderer.color = Color(0.24f, 0.24f, 0.29f, 1f)
        shapeRenderer.rect(
            x,
            y,
            width,
            height
        )

        shapeRenderer.color = Color(0.13f, 0.13f, 0.17f, 1f)

        var stepY = y + 30f

        for (i in 0 until 7) {

            shapeRenderer.rect(
                x + 40f,
                stepY,
                width - 80f,
                18f
            )

            stepY += 30f
        }
    }

    // =========================
    // REVISAR ACCESOS
    // =========================
    private fun revisarAccesos(): Boolean {

        // Regresar al Edificio 2 segundo piso
        if (
            moviendoIzquierda
            &&
            player.collisionBox.overlaps(salidaEdificio2)
        ) {

            cambiandoPantalla = true

            game.screen = Edificio2SegundoPisoScreen(
                game,
                930f + 850f / 2f,
                420f + 260f / 2f
            )

            return true
        }

        // Entrar a salones inferiores
        if (moviendoAbajo) {

            for (salon in salonesGobierno) {

                if (tocaAccesoAbajo(salon.entrada)) {

                    cambiandoPantalla = true

                    game.screen = SalonGobiernoScreen(
                        game,
                        salon.nombre,
                        salon.regresoX,
                        salon.regresoY
                    )

                    return true
                }
            }
        }

        // Bajar al primer piso de Gobierno desde escaleras superiores
        if (
            moviendoArriba
            &&
            player.collisionBox.overlaps(entradaEscalerasBajar)
        ) {

            cambiandoPantalla = true

            game.screen = EdificioGobiernoScreen(
                game,
                900f,
                500f
            )

            return true
        }

        // Conexión a Edificio 1 segundo piso
        if (
            moviendoDerecha
            &&
            player.collisionBox.overlaps(conexionEdificio1SegundoPiso)
        ) {

            cambiandoPantalla = true

            game.screen = Edificio1SegundoPisoScreen(
                game,
                540f,
                550f
            )

            return true
        }

        return false
    }

    // =========================
    // DETECCIÓN DE ENTRADA INFERIOR
    // =========================
    private fun tocaAccesoAbajo(
        acceso: CollisionBox
    ): Boolean {

        val margenX = 140f
        val margenY = 190f

        val jugadorCentroX =
            player.collisionBox.x + player.collisionBox.width / 2f

        val jugadorAbajo =
            player.collisionBox.y

        val jugadorArriba =
            player.collisionBox.y + player.collisionBox.height

        val tocaHorizontalmente =
            jugadorCentroX >= acceso.x - margenX &&
                jugadorCentroX <= acceso.x + acceso.width + margenX

        val tocaVerticalmente =
            jugadorAbajo <= acceso.y + acceso.height + margenY &&
                jugadorArriba >= acceso.y - margenY

        return tocaHorizontalmente && tocaVerticalmente
    }

    // =========================
    // FLECHAS
    // =========================
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

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        if (cambiandoPantalla) {
            return
        }

        procesarInput(delta)

        player.update(delta)

        if (tiempoBloqueoAccesos > 0f) {
            tiempoBloqueoAccesos -= delta
        } else {

            val cambioPantalla =
                revisarAccesos()

            if (cambioPantalla) {
                return
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

        if (cambiandoPantalla) {
            return
        }

        moviendoIzquierda = false
        moviendoDerecha = false
        moviendoArriba = false
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

            moviendoArriba = true
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
