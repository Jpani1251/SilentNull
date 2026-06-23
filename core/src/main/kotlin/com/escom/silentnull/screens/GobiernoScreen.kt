package com.escom.silentnull.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.escom.silentnull.SilentNullGame
import com.escom.silentnull.entities.Enemy
import com.escom.silentnull.entities.Player
import com.escom.silentnull.physics.CollisionBox
import com.escom.silentnull.ui.GameButton

class GobiernoScreen(
    private val game: SilentNullGame
) : Screen {

    // =========================
    // MUNDO INTERIOR
    // =========================
    private val worldWidth = 3000f
    private val worldHeight = 1600f

    // =========================
    // CÁMARAS
    // =========================
    private val camera = OrthographicCamera()

    private val hudCamera = OrthographicCamera()
    private val hudViewport = ScreenViewport(hudCamera)

    private val touchPosition = Vector3()

    // =========================
    // DIBUJO Y TEXTO
    // =========================
    private val shapeRenderer = ShapeRenderer()
    private val font = BitmapFont()
    private val combatFont = BitmapFont()

    // =========================
    // JUGADOR
    // =========================
    private val player = Player()

    private val maxPlayerHealth = 100
    private var playerHealth = maxPlayerHealth

    private val playerDamage = 25
    private val playerAttackRange = 170f
    private val playerAttackCooldown = 0.45f
    private var playerAttackTimer = 0f

    private var playerInvulnerabilityTimer = 0f

    // =========================
    // ENEMIGO DE LA BIBLIOTECA
    // =========================
    private val enemy = Enemy(
        startX = 1800f,
        startY = 810f,
        width = 95f,
        height = 110f,
        speed = 145f,
        detectionRange = 520f,
        attackRange = 105f,
        maxHealth = 100
    )

    private val enemyMinX = 1420f
    private val enemyMaxX = 2200f
    private val enemyMinY = 650f
    private val enemyMaxY = 960f

    private val enemyDamage = 10
    private val enemyAttackRange = 110f
    private val enemyAttackCooldown = 0.85f
    private var enemyAttackTimer = 0f

    private var attackRequested = false

    private var combatMessage =
        "Derrota al enemigo para entrar a la biblioteca"

    private var combatMessageTimer = 4f

    // =========================
    // ZONAS DE CAMBIO
    // =========================
    private val salidaGobierno = CollisionBox(
        0f,
        worldHeight * 0.38f,
        260f,
        460f
    )

    private val entradaBiblioteca = CollisionBox(
        1650f,
        960f,
        360f,
        130f
    )

    private val entradaZonaComun = CollisionBox(
        850f,
        190f,
        820f,
        330f
    )

    private val entradaAuditorio = CollisionBox(
        worldWidth - 280f,
        650f,
        280f,
        360f
    )

    private var movingLeft = false
    private var movingRight = false
    private var movingUp = false
    private var movingDown = false
    private var changingScreen = false

    // =========================
    // BOTONES
    // =========================
    private val buttonSize = 150f
    private val attackButtonSize = 180f

    private lateinit var btnLeft: GameButton
    private lateinit var btnRight: GameButton
    private lateinit var btnUp: GameButton
    private lateinit var btnDown: GameButton

    private val attackButtonArea = Rectangle()

    // =========================
    // INIT
    // =========================
    init {
        font.data.setScale(2.4f)
        combatFont.data.setScale(1.55f)

        btnLeft = GameButton(
            "btn_izq.png",
            0f,
            0f,
            buttonSize,
            buttonSize
        )

        btnRight = GameButton(
            "btn_der.png",
            0f,
            0f,
            buttonSize,
            buttonSize
        )

        btnUp = GameButton(
            "btn_arriba.png",
            0f,
            0f,
            buttonSize,
            buttonSize
        )

        btnDown = GameButton(
            "btn_abajo.png",
            0f,
            0f,
            buttonSize,
            buttonSize
        )

        player.setPosition(
            worldWidth * 0.18f,
            worldHeight * 0.45f
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

        if (changingScreen) {
            return
        }

        ScreenUtils.clear(
            0.04f,
            0.04f,
            0.06f,
            1f
        )

        drawGovernmentBuilding()
        drawEnemy()

        game.batch.projectionMatrix = camera.combined
        game.batch.begin()

        font.draw(
            game.batch,
            "Edificio de Gobierno - Planta baja",
            310f,
            worldHeight - 180f
        )

        font.draw(
            game.batch,
            "Salida",
            70f,
            worldHeight * 0.38f + 520f
        )

        font.draw(
            game.batch,
            "Recepcion",
            430f,
            900f
        )

        font.draw(
            game.batch,
            "Biblioteca",
            1550f,
            1290f
        )

        font.draw(
            game.batch,
            "Zona comun",
            1030f,
            430f
        )

        font.draw(
            game.batch,
            "Auditorio",
            worldWidth - 650f,
            910f
        )

        if (!enemy.isAlive) {
            font.draw(
                game.batch,
                "Entrada desbloqueada",
                1550f,
                935f
            )
        }

        player.render(game.batch)

        game.batch.end()

        // =========================
        // HUD
        // =========================
        hudViewport.apply()

        drawCombatHud()

        game.batch.projectionMatrix = hudCamera.combined
        game.batch.begin()

        btnLeft.render(game.batch)
        btnRight.render(game.batch)
        btnUp.render(game.batch)
        btnDown.render(game.batch)

        combatFont.draw(
            game.batch,
            "VIDA: $playerHealth / $maxPlayerHealth",
            38f,
            hudViewport.worldHeight - 28f
        )

        combatFont.draw(
            game.batch,
            "ATACAR",
            attackButtonArea.x + 24f,
            attackButtonArea.y +
                attackButtonArea.height / 2f + 12f
        )

        if (combatMessageTimer > 0f) {
            combatFont.draw(
                game.batch,
                combatMessage,
                hudViewport.worldWidth / 2f - 310f,
                hudViewport.worldHeight - 35f
            )
        }

        game.batch.end()
    }

    // =========================
    // DISEÑO DEL EDIFICIO
    // =========================
    private fun drawGovernmentBuilding() {
        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        // Piso general
        shapeRenderer.color =
            Color(0.22f, 0.22f, 0.25f, 1f)

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            worldHeight
        )

        // Paredes exteriores
        shapeRenderer.color =
            Color(0.10f, 0.10f, 0.13f, 1f)

        shapeRenderer.rect(
            0f,
            worldHeight - 130f,
            worldWidth,
            130f
        )

        shapeRenderer.rect(
            0f,
            0f,
            worldWidth,
            130f
        )

        shapeRenderer.rect(
            0f,
            0f,
            130f,
            worldHeight
        )

        shapeRenderer.rect(
            worldWidth - 130f,
            0f,
            130f,
            worldHeight
        )

        // Pasillo central
        shapeRenderer.color =
            Color(0.30f, 0.30f, 0.34f, 1f)

        shapeRenderer.rect(
            250f,
            620f,
            2550f,
            360f
        )

        // Recepción
        shapeRenderer.color =
            Color(0.26f, 0.32f, 0.40f, 1f)

        shapeRenderer.rect(
            330f,
            690f,
            420f,
            230f
        )

        // Mostrador
        shapeRenderer.color =
            Color(0.15f, 0.18f, 0.22f, 1f)

        shapeRenderer.rect(
            390f,
            745f,
            300f,
            70f
        )

        // Biblioteca
        shapeRenderer.color =
            Color(0.16f, 0.24f, 0.33f, 1f)

        shapeRenderer.rect(
            900f,
            1040f,
            1500f,
            360f
        )

        // Estantes
        shapeRenderer.color =
            Color(0.09f, 0.12f, 0.17f, 1f)

        shapeRenderer.rect(
            980f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            1120f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            1260f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            1400f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            1540f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            1680f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            1820f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            1960f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            2100f,
            1110f,
            80f,
            230f
        )

        shapeRenderer.rect(
            2240f,
            1110f,
            80f,
            230f
        )

        // Zona común
        shapeRenderer.color =
            Color(0.28f, 0.25f, 0.22f, 1f)

        shapeRenderer.rect(
            850f,
            190f,
            820f,
            330f
        )

        drawTableWithChairs(
            970f,
            300f
        )

        drawTableWithChairs(
            1210f,
            300f
        )

        drawTableWithChairs(
            1450f,
            300f
        )

        // Auditorio
        shapeRenderer.color =
            Color(0.25f, 0.18f, 0.22f, 1f)

        shapeRenderer.rect(
            worldWidth - 530f,
            640f,
            360f,
            350f
        )

        // Puertas
        shapeRenderer.color =
            Color(0.55f, 0.38f, 0.20f, 1f)

        shapeRenderer.rect(
            1760f,
            990f,
            180f,
            50f
        )

        shapeRenderer.rect(
            1150f,
            570f,
            160f,
            50f
        )

        shapeRenderer.rect(
            worldWidth - 300f,
            780f,
            80f,
            120f
        )

        shapeRenderer.end()

        // Flechas
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color = Color.YELLOW

        drawLeftArrow(
            260f,
            salidaGobierno.y +
                salidaGobierno.height / 2f,
            45f
        )

        drawUpArrow(
            entradaBiblioteca.x +
                entradaBiblioteca.width / 2f,
            entradaBiblioteca.y - 65f,
            40f
        )

        drawDownArrow(
            1230f,
            690f,
            40f
        )

        drawRightArrow(
            entradaAuditorio.x - 100f,
            entradaAuditorio.y +
                entradaAuditorio.height / 2f,
            45f
        )

        shapeRenderer.end()
    }

    // =========================
    // DIBUJAR ENEMIGO
    // =========================
    private fun drawEnemy() {
        shapeRenderer.projectionMatrix = camera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        enemy.render(shapeRenderer)

        shapeRenderer.end()
    }

    // =========================
    // HUD DE COMBATE
    // =========================
    private fun drawCombatHud() {
        shapeRenderer.projectionMatrix = hudCamera.combined

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        val healthBarX = 35f
        val healthBarY =
            hudViewport.worldHeight - 82f

        val healthBarWidth = 360f
        val healthBarHeight = 30f

        val healthPercentage =
            playerHealth.toFloat() /
                maxPlayerHealth.toFloat()

        shapeRenderer.color =
            Color(0.08f, 0.08f, 0.08f, 0.95f)

        shapeRenderer.rect(
            healthBarX,
            healthBarY,
            healthBarWidth,
            healthBarHeight
        )

        shapeRenderer.color =
            Color(0.10f, 0.72f, 0.20f, 1f)

        shapeRenderer.rect(
            healthBarX + 3f,
            healthBarY + 3f,
            (healthBarWidth - 6f) *
                healthPercentage,
            healthBarHeight - 6f
        )

        shapeRenderer.color =
            if (playerAttackTimer <= 0f) {
                Color(
                    0.72f,
                    0.10f,
                    0.12f,
                    0.95f
                )
            } else {
                Color(
                    0.30f,
                    0.30f,
                    0.32f,
                    0.95f
                )
            }

        shapeRenderer.rect(
            attackButtonArea.x,
            attackButtonArea.y,
            attackButtonArea.width,
            attackButtonArea.height
        )

        shapeRenderer.color =
            Color(0.95f, 0.95f, 0.95f, 1f)

        shapeRenderer.rect(
            attackButtonArea.x + 34f,
            attackButtonArea.y + 28f,
            18f,
            attackButtonArea.height - 56f
        )

        shapeRenderer.rect(
            attackButtonArea.x + 18f,
            attackButtonArea.y + 54f,
            50f,
            16f
        )

        shapeRenderer.end()
    }

    // =========================
    // MESA
    // =========================
    private fun drawTableWithChairs(
        x: Float,
        y: Float
    ) {
        shapeRenderer.color =
            Color(0.45f, 0.32f, 0.20f, 1f)

        shapeRenderer.rect(
            x,
            y,
            120f,
            80f
        )

        shapeRenderer.color =
            Color(0.12f, 0.13f, 0.16f, 1f)

        shapeRenderer.rect(
            x + 30f,
            y + 95f,
            60f,
            50f
        )

        shapeRenderer.rect(
            x + 30f,
            y - 65f,
            60f,
            50f
        )
    }

    // =========================
    // FLECHAS
    // =========================
    private fun drawUpArrow(
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

    private fun drawDownArrow(
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

    private fun drawRightArrow(
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

    private fun drawLeftArrow(
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

    // =========================
    // UPDATE
    // =========================
    private fun update(delta: Float) {
        updateTimers(delta)

        player.guardarPosicionAnterior()

        processInput(delta)

        player.update(delta)

        enemy.update(
            delta = delta,
            playerX = player.x,
            playerY = player.y,
            playerWidth = player.getWidth(),
            playerHeight = player.getHeight(),
            minX = enemyMinX,
            maxX = enemyMaxX,
            minY = enemyMinY,
            maxY = enemyMaxY
        )

        processCombat()

        if (changingScreen) {
            return
        }

        checkZoneChanges()

        if (changingScreen) {
            return
        }

        player.limitarPantalla(
            worldWidth,
            worldHeight
        )

        updateCamera()
    }

    // =========================
    // TEMPORIZADORES
    // =========================
    private fun updateTimers(delta: Float) {
        playerAttackTimer =
            (playerAttackTimer - delta)
                .coerceAtLeast(0f)

        enemyAttackTimer =
            (enemyAttackTimer - delta)
                .coerceAtLeast(0f)

        playerInvulnerabilityTimer =
            (playerInvulnerabilityTimer - delta)
                .coerceAtLeast(0f)

        combatMessageTimer =
            (combatMessageTimer - delta)
                .coerceAtLeast(0f)
    }

    // =========================
    // INPUT
    // =========================
    private fun processInput(delta: Float) {
        movingLeft =
            Gdx.input.isKeyPressed(
                Input.Keys.LEFT
            ) ||
                Gdx.input.isKeyPressed(
                    Input.Keys.A
                )

        movingRight =
            Gdx.input.isKeyPressed(
                Input.Keys.RIGHT
            ) ||
                Gdx.input.isKeyPressed(
                    Input.Keys.D
                )

        movingUp =
            Gdx.input.isKeyPressed(
                Input.Keys.UP
            ) ||
                Gdx.input.isKeyPressed(
                    Input.Keys.W
                )

        movingDown =
            Gdx.input.isKeyPressed(
                Input.Keys.DOWN
            ) ||
                Gdx.input.isKeyPressed(
                    Input.Keys.S
                )

        attackRequested =
            Gdx.input.isKeyJustPressed(
                Input.Keys.SPACE
            )

        for (pointer in 0 until 5) {
            if (!Gdx.input.isTouched(pointer)) {
                continue
            }

            touchPosition.set(
                Gdx.input.getX(pointer).toFloat(),
                Gdx.input.getY(pointer).toFloat(),
                0f
            )

            hudViewport.unproject(touchPosition)

            val touchX = touchPosition.x
            val touchY = touchPosition.y

            if (
                btnLeft.isTouched(
                    touchX,
                    touchY
                )
            ) {
                movingLeft = true
            }

            if (
                btnRight.isTouched(
                    touchX,
                    touchY
                )
            ) {
                movingRight = true
            }

            if (
                btnUp.isTouched(
                    touchX,
                    touchY
                )
            ) {
                movingUp = true
            }

            if (
                btnDown.isTouched(
                    touchX,
                    touchY
                )
            ) {
                movingDown = true
            }

            if (
                attackButtonArea.contains(
                    touchX,
                    touchY
                )
            ) {
                attackRequested = true
            }
        }

        if (movingLeft) {
            player.moverIzquierda(delta)
        }

        if (movingRight) {
            player.moverDerecha(delta)
        }

        if (movingUp) {
            player.moverArriba(delta)
        }

        if (movingDown) {
            player.moverAbajo(delta)
        }
    }

    // =========================
    // COMBATE
    // =========================
    private fun processCombat() {
        if (
            attackRequested &&
            playerAttackTimer <= 0f
        ) {
            playerAttackTimer =
                playerAttackCooldown

            if (
                enemy.isPlayerInRange(
                    playerX = player.x,
                    playerY = player.y,
                    playerWidth =
                        player.getWidth(),
                    playerHeight =
                        player.getHeight(),
                    range = playerAttackRange
                )
            ) {
                enemy.takeDamage(playerDamage)

                if (enemy.isAlive) {
                    showCombatMessage(
                        "Golpeaste al enemigo: " +
                            "-$playerDamage de vida"
                    )
                } else {
                    showCombatMessage(
                        "Enemigo derrotado. " +
                            "La biblioteca esta abierta",
                        3f
                    )
                }
            } else {
                showCombatMessage(
                    "El enemigo esta fuera de alcance"
                )
            }
        }

        if (
            enemy.isAlive &&
            enemyAttackTimer <= 0f &&
            playerInvulnerabilityTimer <= 0f &&
            enemy.isPlayerInRange(
                playerX = player.x,
                playerY = player.y,
                playerWidth = player.getWidth(),
                playerHeight = player.getHeight(),
                range = enemyAttackRange
            )
        ) {
            playerHealth =
                (playerHealth - enemyDamage)
                    .coerceAtLeast(0)

            enemyAttackTimer =
                enemyAttackCooldown

            playerInvulnerabilityTimer =
                0.35f

            showCombatMessage(
                "El enemigo te golpeo: " +
                    "-$enemyDamage de vida"
            )
        }

        if (playerHealth <= 0) {
            showGameOverScreen()
        }
    }

    // =========================
    // PANTALLA DE MUERTE
    // =========================
    private fun showGameOverScreen() {
        if (changingScreen) {
            return
        }

        changingScreen = true

        game.screen = GameOverScreen(game)

        dispose()
    }

    // =========================
    // MENSAJE DE COMBATE
    // =========================
    private fun showCombatMessage(
        message: String,
        duration: Float = 1.5f
    ) {
        combatMessage = message
        combatMessageTimer = duration
    }

    // =========================
    // CAMBIO DE ZONAS
    // =========================
    private fun checkZoneChanges() {
        if (
            movingLeft &&
            player.collisionBox.overlaps(
                salidaGobierno
            )
        ) {
            changingScreen = true

            game.screen = JuegoScreen(game)

            dispose()

            return
        }

        if (
            movingUp &&
            player.collisionBox.overlaps(
                entradaBiblioteca
            )
        ) {
            if (enemy.isAlive) {
                player.setPosition(
                    player.x,
                    entradaBiblioteca.y -
                        player.getHeight() - 8f
                )

                showCombatMessage(
                    "Primero derrota al enemigo " +
                        "de la biblioteca"
                )

                return
            }

            changingScreen = true

            game.screen = BibliotecaScreen(game)

            dispose()

            return
        }

        if (
            player.collisionBox.overlaps(
                entradaZonaComun
            )
        ) {
            changingScreen = true

            game.screen = ZonaComunScreen(game)

            dispose()

            return
        }

        if (
            movingRight &&
            player.collisionBox.overlaps(
                entradaAuditorio
            )
        ) {
            changingScreen = true

            game.screen = AuditorioScreen(game)

            dispose()

            return
        }
    }

    // =========================
    // CÁMARA
    // =========================
    private fun updateCamera() {
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
            worldWidth - halfViewportWidth

        val minCameraY =
            halfViewportHeight

        val maxCameraY =
            worldHeight - halfViewportHeight

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
    private fun positionButtons() {
        val marginX = 50f
        val marginY = 50f

        btnLeft.x = marginX
        btnLeft.y =
            marginY + buttonSize

        btnRight.x =
            marginX + buttonSize * 2f

        btnRight.y =
            marginY + buttonSize

        btnUp.x =
            marginX + buttonSize

        btnUp.y =
            marginY + buttonSize * 2f

        btnDown.x =
            marginX + buttonSize

        btnDown.y = marginY

        attackButtonArea.set(
            hudViewport.worldWidth -
                attackButtonSize - 65f,
            65f,
            attackButtonSize,
            attackButtonSize
        )
    }

    // =========================
    // MÉTODOS OBLIGATORIOS
    // =========================
    override fun show() {
    }

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

        positionButtons()
        updateCamera()
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
    }

    override fun dispose() {
        shapeRenderer.dispose()
        font.dispose()
        combatFont.dispose()
        player.dispose()

        btnLeft.dispose()
        btnRight.dispose()
        btnUp.dispose()
        btnDown.dispose()
    }
}
