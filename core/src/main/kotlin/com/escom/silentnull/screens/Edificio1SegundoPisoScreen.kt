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
import com.escom.silentnull.ui.DebugManager
import com.escom.silentnull.ui.GameButton
import com.escom.silentnull.ui.TransitionManager
import kotlin.math.abs

class Edificio1SegundoPisoScreen(
    private val game: SilentNullGame,
    private val spawnX: Float? = null,
    private val spawnY: Float? = null
) : Screen {

    // =========================
    // MUNDO
    // =========================
    private val worldWidth = 2200f
    private val worldHeight = 4300f

    private val wallSize = 120f

    // =========================
    // PASILLO Y SALONES
    // =========================
    private val corridorX = 420f
    private val corridorWidth = 850f

    private val classroomWidth = 620f
    private val classroomHeight = 260f
    private val classroomGap = 115f

    private val classroomX =
        worldWidth - 180f - classroomWidth

    private val classroomStartY = 420f

    // Escaleras para bajar a planta baja.
    private val stairDownY =
        classroomStartY +
            3f * (classroomHeight + classroomGap)

    // Escaleras para subir al tercer piso.
    private val stairUpY =
        classroomStartY +
            4f * (classroomHeight + classroomGap)

    private val salon1109Y =
        classroomStartY +
            5f * (classroomHeight + classroomGap)

    private val salon1110Y =
        classroomStartY +
            6f * (classroomHeight + classroomGap)

    private val salon1111Y =
        classroomStartY +
            7f * (classroomHeight + classroomGap)

    private val bathroomY =
        classroomStartY +
            8f * (classroomHeight + classroomGap)

    private val bathroomHeight = 260f

    // =========================
    // CÁMARAS
    // =========================
    private val camera = OrthographicCamera()

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    // =========================
    // RECURSOS
    // =========================
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()
    private val player = Player()
    private val debugManager = DebugManager("Edificio1_2P", worldWidth, worldHeight)
    private val transitionManager = TransitionManager()

    // =========================
    // CONEXIÓN CON GOBIERNO P2
    // =========================
    private val salidaGobiernoSegundoPiso = CollisionBox(
        corridorX - 220f,
        classroomStartY +
            classroomHeight / 2f -
            120f,
        360f,
        240f
    )

    // =========================
    // CONEXIÓN CON EDIFICIO CENTRAL P2
    // UBICADA A LA ALTURA DE LOS BAÑOS
    // =========================
    private val salidaEdificioCentralSegundoPiso = CollisionBox(
        corridorX - 220f,
        bathroomY +
            bathroomHeight / 2f -
            120f,
        360f,
        240f
    )

    // =========================
    // ESCALERAS
    // =========================
    private val entradaEscalerasBajar = CollisionBox(
        classroomX - 220f,
        stairDownY + 20f,
        260f,
        classroomHeight - 40f
    )

    private val entradaEscalerasSubir = CollisionBox(
        classroomX - 220f,
        stairUpY + 20f,
        260f,
        classroomHeight - 40f
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
        classroomX +
            classroomWidth / 2f +
            20f,
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

    private enum class TipoAccesoDerecho {
        SALON,
        ESCALERAS_BAJAR,
        ESCALERAS_SUBIR
    }

    private data class AccesoDerechoDetectado(
        val tipo: TipoAccesoDerecho,
        val caja: CollisionBox,
        val salon: SalonAccess? = null
    )

    private val entradasSalones =
        crearEntradasSalones()

    private val obstaculos =
        crearObstaculos()

    // =========================
    // ESTADOS
    // =========================
    private var moviendoAbajo = false
    private var moviendoIzquierda = false
    private var moviendoDerecha = false

    private var cambiandoPantalla = false
    private var recursosLiberados = false

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
    // INICIALIZACIÓN
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

        ScreenUtils.clear(
            0.04f,
            0.04f,
            0.05f,
            1f
        )

        dibujarEdificio1SegundoPiso()

        game.batch.projectionMatrix =
            camera.combined

        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio 1 - Segundo piso",
            160f,
            worldHeight - 160f
        )

        font.draw(
            game.batch,
            "Gobierno 2P",
            wallSize + 35f,
            classroomStartY +
                classroomHeight / 2f +
                30f
        )

        // Nueva etiqueta de la conexión central.
        font.draw(
            game.batch,
            "Edificio Central P2",
            wallSize + 20f,
            bathroomY +
                bathroomHeight / 2f +
                30f
        )

        for (salon in entradasSalones) {

            font.draw(
                game.batch,
                salon.nombre,
                classroomX + 190f,
                salon.y +
                    classroomHeight / 2f +
                    25f
            )
        }

        font.draw(
            game.batch,
            "Escaleras a PB",
            classroomX + 110f,
            stairDownY +
                classroomHeight / 2f +
                25f
        )

        font.draw(
            game.batch,
            "Escaleras a 3P",
            classroomX + 105f,
            stairUpY +
                classroomHeight / 2f +
                25f
        )

        font.draw(
            game.batch,
            "Bano H",
            classroomX + 70f,
            bathroomY +
                bathroomHeight / 2f +
                20f
        )

        font.draw(
            game.batch,
            "Bano M",
            classroomX + 390f,
            bathroomY +
                bathroomHeight / 2f +
                20f
        )

        player.render(game.batch)

        game.batch.end()

        // =========================
        // DEBUG TOOLS
        // =========================
        debugManager.render(game.batch, camera, hudCamera, player)

        // =========================
        // HUD
        // =========================
        hudViewport.apply()

        game.batch.projectionMatrix =
            hudCamera.combined

        game.batch.begin()

        btnIzq.render(game.batch)
        btnDer.render(game.batch)
        btnArriba.render(game.batch)
        btnAbajo.render(game.batch)

        game.batch.end()

        // INVENTARIO
        game.inventoryManager.render(game.batch, hudViewport)

        // =========================
        // TRANSICIÓN
        // =========================
        transitionManager.render(game.batch, hudViewport)
    }

    // =========================
    // DIBUJAR SEGUNDO PISO
    // =========================
    private fun dibujarEdificio1SegundoPiso() {

        shapeRenderer.projectionMatrix =
            camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        // Piso general.
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

        // Pasillo principal.
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

        // Pasillo hacia Gobierno.
        shapeRenderer.color = Color(
            0.34f,
            0.34f,
            0.39f,
            1f
        )

        shapeRenderer.rect(
            wallSize,
            classroomStartY + 45f,
            corridorX - wallSize,
            classroomHeight - 90f
        )

        // Nuevo pasillo hacia Edificio Central.
        shapeRenderer.color = Color(
            0.34f,
            0.34f,
            0.39f,
            1f
        )

        shapeRenderer.rect(
            wallSize,
            bathroomY + 45f,
            corridorX - wallSize,
            bathroomHeight - 90f
        )

        // Línea central del pasillo.
        shapeRenderer.color = Color(
            0.38f,
            0.38f,
            0.42f,
            1f
        )

        shapeRenderer.rect(
            corridorX +
                corridorWidth / 2f -
                8f,
            wallSize,
            16f,
            worldHeight - wallSize * 2f
        )

        // Paredes exteriores.
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

        // Salones.
        for (salon in entradasSalones) {

            dibujarSalonDerecho(
                classroomX,
                salon.y,
                classroomWidth,
                classroomHeight
            )
        }

        // Escaleras para bajar.
        dibujarEscalerasDerechas(
            classroomX,
            stairDownY,
            classroomWidth,
            classroomHeight
        )

        // Escaleras para subir.
        dibujarEscalerasDerechas(
            classroomX,
            stairUpY,
            classroomWidth,
            classroomHeight
        )

        // Baños.
        dibujarBanosDerechos(
            classroomX,
            bathroomY,
            classroomWidth,
            bathroomHeight
        )

        // =========================
        // PUERTAS
        // =========================
        shapeRenderer.color = Color(
            0.55f,
            0.38f,
            0.20f,
            1f
        )

        // Puerta hacia Gobierno.
        shapeRenderer.rect(
            corridorX - 60f,
            classroomStartY +
                classroomHeight / 2f -
                60f,
            80f,
            120f
        )

        // Nueva puerta hacia Edificio Central.
        shapeRenderer.rect(
            corridorX - 60f,
            bathroomY +
                bathroomHeight / 2f -
                60f,
            80f,
            120f
        )

        // =========================
        // FLECHAS
        // =========================
        shapeRenderer.color = Color.YELLOW

        // Gobierno.
        dibujarFlechaIzquierda(
            corridorX + 80f,
            classroomStartY +
                classroomHeight / 2f,
            45f
        )

        // Nueva conexión hacia Edificio Central.
        dibujarFlechaIzquierda(
            corridorX + 80f,
            bathroomY +
                bathroomHeight / 2f,
            45f
        )

        // Escaleras para bajar.
        dibujarFlechaDerecha(
            corridorX +
                corridorWidth -
                70f,
            stairDownY +
                classroomHeight / 2f,
            45f
        )

        // Escaleras para subir.
        dibujarFlechaDerecha(
            corridorX +
                corridorWidth -
                70f,
            stairUpY +
                classroomHeight / 2f,
            45f
        )

        // Salones.
        for (salon in entradasSalones) {

            dibujarFlechaDerecha(
                corridorX +
                    corridorWidth -
                    70f,
                salon.entrada.y +
                    salon.entrada.height / 2f,
                35f
            )
        }

        // Baños.
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

    // =========================
    // DIBUJAR SALÓN
    // =========================
    private fun dibujarSalonDerecho(
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
            x,
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
            x - 45f,
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
            x + 120f,
            y + height - 90f,
            width - 190f,
            50f
        )
    }

    // =========================
    // DIBUJAR ESCALERAS
    // =========================
    private fun dibujarEscalerasDerechas(
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
            x,
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
            x - 55f,
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

    // =========================
    // DIBUJAR BAÑOS
    // =========================
    private fun dibujarBanosDerechos(
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {

        val halfWidth =
            width / 2f

        // Baño de hombres.
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

        // Baño de mujeres.
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

        // División central.
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
            x,
            y,
            25f,
            height
        )

        shapeRenderer.rect(
            x + width - 25f,
            y,
            25f,
            height
        )

        // Puertas superiores.
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

    // =========================
    // CREAR ENTRADAS DE SALONES
    // =========================
    private fun crearEntradasSalones():
        MutableList<SalonAccess> {

        val lista =
            mutableListOf<SalonAccess>()

        fun agregarSalon(
            nombre: String,
            y: Float
        ) {

            lista.add(
                SalonAccess(
                    nombre,
                    y,
                    CollisionBox(
                        classroomX - 220f,
                        y +
                            classroomHeight / 2f -
                            85f,
                        260f,
                        170f
                    ),
                    corridorX +
                        corridorWidth / 2f,
                    y +
                        classroomHeight / 2f
                )
            )
        }

        agregarSalon(
            "Salon 1106",
            classroomStartY
        )

        agregarSalon(
            "Salon 1107",
            classroomStartY +
                classroomHeight +
                classroomGap
        )

        agregarSalon(
            "Salon 1108",
            classroomStartY +
                2f *
                (classroomHeight + classroomGap)
        )

        agregarSalon(
            "Salon 1109",
            salon1109Y
        )

        agregarSalon(
            "Salon 1110",
            salon1110Y
        )

        agregarSalon(
            "Salon 1111",
            salon1111Y
        )

        return lista
    }

    // =========================
    // CREAR OBSTÁCULOS
    // =========================
    private fun crearObstaculos():
        MutableList<CollisionBox> {

        val lista =
            mutableListOf<CollisionBox>()

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
                stairDownY,
                classroomWidth,
                classroomHeight
            )
        )

        lista.add(
            CollisionBox(
                classroomX,
                stairUpY,
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

    // =========================
    // DETECCIÓN DE ACCESOS
    // =========================
    private fun centroYJugador(): Float {

        return player.collisionBox.y +
            player.collisionBox.height / 2f
    }

    private fun distanciaVertical(
        acceso: CollisionBox
    ): Float {

        val centroAccesoY =
            acceso.y +
                acceso.height / 2f

        return abs(
            centroYJugador() -
                centroAccesoY
        )
    }

    private fun tocaAccesoDerecho(
        acceso: CollisionBox
    ): Boolean {

        val margenX = 260f
        val margenY = 110f

        val jugadorIzquierda =
            player.collisionBox.x

        val jugadorDerecha =
            player.collisionBox.x +
                player.collisionBox.width

        val jugadorCentroY =
            centroYJugador()

        val tocaHorizontalmente =
            jugadorDerecha >=
                acceso.x -
                margenX &&
                jugadorIzquierda <=
                acceso.x +
                acceso.width +
                margenX

        val tocaVerticalmente =
            jugadorCentroY >=
                acceso.y -
                margenY &&
                jugadorCentroY <=
                acceso.y +
                acceso.height +
                margenY

        return tocaHorizontalmente &&
            tocaVerticalmente
    }

    private fun obtenerAccesoDerecho():
        AccesoDerechoDetectado? {

        val candidatos =
            mutableListOf<AccesoDerechoDetectado>()

        for (salon in entradasSalones) {

            if (
                tocaAccesoDerecho(
                    salon.entrada
                )
            ) {

                candidatos.add(
                    AccesoDerechoDetectado(
                        TipoAccesoDerecho.SALON,
                        salon.entrada,
                        salon
                    )
                )
            }
        }

        if (
            tocaAccesoDerecho(
                entradaEscalerasBajar
            )
        ) {

            candidatos.add(
                AccesoDerechoDetectado(
                    TipoAccesoDerecho.ESCALERAS_BAJAR,
                    entradaEscalerasBajar
                )
            )
        }

        if (
            tocaAccesoDerecho(
                entradaEscalerasSubir
            )
        ) {

            candidatos.add(
                AccesoDerechoDetectado(
                    TipoAccesoDerecho.ESCALERAS_SUBIR,
                    entradaEscalerasSubir
                )
            )
        }

        return candidatos.minByOrNull {
            distanciaVertical(it.caja)
        }
    }

    // =========================
    // REVISAR ACCESOS
    // =========================
    private fun revisarAccesos(): Boolean {

        // Reset estado botón abrir
        transitionManager.setShowButton(false)

        if (cambiandoPantalla) return false

        // =========================
        // EDIFICIO CENTRAL P2
        // Se revisa antes que Gobierno.
        // =========================
        if (
            moviendoIzquierda &&
            player.collisionBox.overlaps(
                salidaEdificioCentralSegundoPiso
            )
        ) {

            cambiandoPantalla = true

            /*
             * Entra por el lado derecho del Edificio Central.
             * La posición evita un regreso inmediato.
             */
            game.screen =
                EdificioCentralSegundoPisoScreen(
                    game,
                    1680f,
                    750f
                )

            return true
        }

        // =========================
        // GOBIERNO P2
        // =========================
        if (
            moviendoIzquierda &&
            player.collisionBox.overlaps(
                salidaGobiernoSegundoPiso
            )
        ) {

            cambiandoPantalla = true

            game.screen =
                EdificioGobiernoSegundoPisoScreen(
                    game,
                    1770f,
                    750f
                )

            return true
        }

        // =========================
        // SALONES Y ESCALERAS
        // =========================
        if (moviendoDerecha) {

            val accesoDerecho =
                obtenerAccesoDerecho()

            if (accesoDerecho != null) {

                when (accesoDerecho.tipo) {

                    TipoAccesoDerecho.SALON -> {

                        val salon =
                            accesoDerecho.salon
                                ?: return false

                        // Mostrar botón abrir
                        transitionManager.setShowButton(true)

                        return false
                    }

                    TipoAccesoDerecho.ESCALERAS_BAJAR -> {

                        cambiandoPantalla = true

                        game.screen = Edificio1Screen(
                            game,
                            corridorX +
                                corridorWidth / 2f,
                            stairDownY +
                                classroomHeight / 2f
                        )

                        return true
                    }

                    TipoAccesoDerecho.ESCALERAS_SUBIR -> {

                        cambiandoPantalla = true

                        game.screen =
                            Edificio1PisoSuperiorScreen(
                                game,
                                3,
                                corridorX +
                                    corridorWidth / 2f,
                                stairUpY +
                                    classroomHeight / 2f
                            )

                        return true
                    }
                }
            }
        }

        // =========================
        // BAÑO HOMBRES
        // =========================
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
                corridorX +
                    corridorWidth / 2f,
                bathroomY +
                    bathroomHeight / 2f,
                2,
                1
            )

            return true
        }

        // =========================
        // BAÑO MUJERES
        // =========================
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
                corridorX +
                    corridorWidth / 2f,
                bathroomY +
                    bathroomHeight / 2f,
                2,
                1
            )

            return true
        }

        return false
    }

    // =========================
    // COLISIONES
    // =========================
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

    // =========================
    // FLECHAS
    // =========================
    private fun dibujarFlechaIzquierda(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth =
            size * 0.45f

        val bodyLength =
            size * 1.4f

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

        val bodyWidth =
            size * 0.45f

        val bodyLength =
            size * 1.4f

        shapeRenderer.triangle(
            centerX + size,
            centerY,
            centerX - size,
            centerY + size,
            centerX - size,
            centerY - size
        )

        shapeRenderer.rect(
            centerX -
                size -
                bodyLength,
            centerY -
                bodyWidth / 2f,
            bodyLength,
            bodyWidth
        )
    }

    private fun dibujarFlechaAbajo(
        centerX: Float,
        centerY: Float,
        size: Float
    ) {

        val bodyWidth =
            size * 0.45f

        val bodyLength =
            size * 1.4f

        shapeRenderer.triangle(
            centerX,
            centerY - size,
            centerX - size,
            centerY + size,
            centerX + size,
            centerY + size
        )

        shapeRenderer.rect(
            centerX -
                bodyWidth / 2f,
            centerY + size,
            bodyWidth,
            bodyLength
        )
    }

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {

        game.inventoryManager.update(delta)

        if (game.inventoryManager.isVisible()) {
            return
        }

        player.guardarPosicionAnterior()

        if (!cambiandoPantalla) {
            procesarInput(delta)
        }

        player.update(delta)

        transitionManager.update(delta)

        if (cambiandoPantalla) {
            actualizarCamara()
            return
        }

        if (
            tiempoBloqueoAccesos > 0f
        ) {

            tiempoBloqueoAccesos -= delta

        } else {

            val cambioPantalla =
                revisarAccesos()

            if (cambioPantalla) {
                return
            }
        }

        if (
            tiempoBloqueoColisiones > 0f
        ) {

            tiempoBloqueoColisiones -= delta

        } else {

            revisarColisiones()
        }

        // Colisión con la rejilla (Global)
        if (debugManager.checkCollision(player)) {
            player.revertirMovimiento()
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

        moviendoAbajo = false
        moviendoIzquierda = false
        moviendoDerecha = false

        if (!Gdx.input.isTouched) {
            debugManager.procesarInput(0f, 0f, camera)
            return
        }

        touchPosition.set(
            Gdx.input.x.toFloat(),
            Gdx.input.y.toFloat(),
            0f
        )

        hudViewport.unproject(
            touchPosition
        )

        val touchX =
            touchPosition.x

        val touchY =
            touchPosition.y

        // Manejar Inventario
        if (game.inventoryManager.handleInput(touchX, touchY)) {
            return
        }

        // Delegar al DebugManager
        if (debugManager.procesarInput(touchX, touchY, camera)) {
            return
        }

        // Manejar botón ABRIR
        if (transitionManager.handleInput(touchX, touchY)) {
            val acceso = obtenerAccesoDerecho()
            if (acceso != null && acceso.tipo == TipoAccesoDerecho.SALON) {
                val salon = acceso.salon!!
                cambiandoPantalla = true
                transitionManager.startFade {
                    game.videoPlayer?.playVideo("Video_anim_entrance.mp4") {
                        game.screen = SalonScreen(
                            game,
                            salon.nombre,
                            salon.regresoX,
                            salon.regresoY,
                            2,
                            1
                        )
                    } ?: run {
                        game.screen = SalonScreen(
                            game,
                            salon.nombre,
                            salon.regresoX,
                            salon.regresoY,
                            2,
                            1
                        )
                    }
                }
            }
            return
        }

        if (
            btnIzq.isTouched(
                touchX,
                touchY
            )
        ) {

            moviendoIzquierda = true
            player.moverIzquierda(delta)
        }

        if (
            btnDer.isTouched(
                touchX,
                touchY
            )
        ) {

            moviendoDerecha = true
            player.moverDerecha(delta)
        }

        if (
            btnArriba.isTouched(
                touchX,
                touchY
            )
        ) {

            player.moverArriba(delta)
        }

        if (
            btnAbajo.isTouched(
                touchX,
                touchY
            )
        ) {

            moviendoAbajo = true
            player.moverAbajo(delta)
        }
    }

    // =========================
    // CÁMARA
    // =========================
    private fun actualizarCamara() {

        val playerCenterX =
            player.x +
                player.getWidth() / 2f

        val playerCenterY =
            player.y +
                player.getHeight() / 2f

        val halfViewportWidth =
            camera.viewportWidth / 2f

        val halfViewportHeight =
            camera.viewportHeight / 2f

        val minCameraX =
            halfViewportWidth

        val maxCameraX =
            worldWidth -
                halfViewportWidth

        val minCameraY =
            halfViewportHeight

        val maxCameraY =
            worldHeight -
                halfViewportHeight

        val cameraX =
            if (
                minCameraX > maxCameraX
            ) {

                worldWidth / 2f

            } else {

                MathUtils.clamp(
                    playerCenterX,
                    minCameraX,
                    maxCameraX
                )
            }

        val cameraY =
            if (
                minCameraY > maxCameraY
            ) {

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

        btnIzq.x =
            margenX

        btnIzq.y =
            margenY + tamanoBoton

        btnDer.x =
            margenX + tamanoBoton * 2f

        btnDer.y =
            margenY + tamanoBoton

        btnArriba.x =
            margenX + tamanoBoton

        btnArriba.y =
            margenY + tamanoBoton * 2f

        btnAbajo.x =
            margenX + tamanoBoton

        btnAbajo.y =
            margenY
    }

    // =========================
    // MÉTODOS DE SCREEN
    // =========================
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

        debugManager.dispose()
        transitionManager.dispose()

        recursosLiberados = true
    }
}
