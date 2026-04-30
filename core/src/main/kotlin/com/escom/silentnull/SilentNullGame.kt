package com.escom.silentnull

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils

// 1. EL ADMINISTRADOR (Controla qué pantalla estamos viendo)
class SilentNullGame : Game() {
    lateinit var batch: SpriteBatch

    override fun create() {
        batch = SpriteBatch()
        // Cuando la app abre, le decimos que muestre la pantalla del Menú
        this.setScreen(MenuScreen(this))
    }

    override fun dispose() {
        batch.dispose()
        super.dispose()
    }
}
// 2. LA PANTALLA DEL MENÚ DE INICIO
class MenuScreen(val game: SilentNullGame) : Screen {
    private val fondo = Texture("fondo_escom.png")
    private val btnJugar = Texture("btn_jugar.png")
    private val logoTexture = Texture("logo.png")

    // Variables del botón
    private val anchoBtn = 300f
    private val altoBtn = 100f
    private var btnX = 0f
    private var btnY = 0f

    // Variables del logo
    private val anchoLogo = 700f
    private val altoLogo = 350f
    private var logoX = 0f
    private var logoY = 0f

    init {
        val anchoPantalla = Gdx.graphics.width.toFloat()
        val altoPantalla = Gdx.graphics.height.toFloat()

        // Centramos el logo horizontalmente y lo ponemos en el tercio SUPERIOR de la pantalla
        logoX = (anchoPantalla / 2) - (anchoLogo / 2)
        logoY = (altoPantalla * 0.65f) - (altoLogo / 2)

        // Centramos el botón horizontalmente y lo bajamos al tercio INFERIOR de la pantalla
        btnX = (anchoPantalla / 2) - (anchoBtn / 2)
        btnY = (altoPantalla * 0.25f) - (altoBtn / 2)
    }

    override fun render(delta: Float) {
        ScreenUtils.clear(0f, 0f, 0f, 1f)

        game.batch.begin()

        // 1. Dibujamos el fondo oscuro
        game.batch.color = com.badlogic.gdx.graphics.Color(0.4f, 0.4f, 0.4f, 1f)
        game.batch.draw(fondo, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())

        // 2. Restauramos el color a blanco para que el logo y el botón no se oscurezcan
        game.batch.color = com.badlogic.gdx.graphics.Color.WHITE

        // 3. Dibujamos tu Logo de Silent Null
        game.batch.draw(logoTexture, logoX, logoY, anchoLogo, altoLogo)

        // 4. Dibujamos el botón de Jugar
        game.batch.draw(btnJugar, btnX, btnY, anchoBtn, altoBtn)

        game.batch.end()

        // Lógica: Tocar el botón de Jugar
        if (Gdx.input.justTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = (Gdx.graphics.height - Gdx.input.y).toFloat()

            if (touchX in btnX..(btnX + anchoBtn) && touchY in btnY..(btnY + altoBtn)) {
                game.screen = JuegoScreen(game)
                dispose()
            }
        }
    }

    override fun show() {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        fondo.dispose()
        btnJugar.dispose()
        logoTexture.dispose() // No olvides liberar la memoria del logo
    }
}

// 3. LA PANTALLA DEL JUEGO (Tu código de movimiento está aquí adentro ahora)
class JuegoScreen(val game: SilentNullGame) : Screen {
    private val fondoEscom = Texture("fondo_escom.png")
    private val alexSprite = Texture("alex_protagonista.png")
    private val texturaBtnIzq = Texture("btn_izq.png")
    private val texturaBtnDer = Texture("btn_der.png")
    private val texturaBtnArriba = Texture("btn_arriba.png")
    private val texturaBtnAbajo = Texture("btn_abajo.png")

    private var alexX = 0f
    private var alexY = 0f
    private val velocidad = 450f

    private val tamañoBoton = 150f
    private var btnIzqX = 0f; private var btnIzqY = 0f
    private var btnDerX = 0f; private var btnDerY = 0f
    private var btnArribaX = 0f; private var btnArribaY = 0f
    private var btnAbajoX = 0f; private var btnAbajoY = 0f

    init {
        alexX = (Gdx.graphics.width / 2 - alexSprite.width / 2).toFloat()
        alexY = (Gdx.graphics.height / 2 - alexSprite.height / 2).toFloat()

        val margenX = 50f; val margenY = 50f
        btnAbajoX = margenX + tamañoBoton; btnAbajoY = margenY
        btnArribaX = margenX + tamañoBoton; btnArribaY = margenY + (tamañoBoton * 2)
        btnIzqX = margenX; btnIzqY = margenY + tamañoBoton
        btnDerX = margenX + (tamañoBoton * 2); btnDerY = margenY + tamañoBoton
    }

    override fun render(delta: Float) {
        procesarMovimiento(delta)

        ScreenUtils.clear(0f, 0f, 0f, 1f)
        game.batch.begin()

        game.batch.draw(fondoEscom, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        game.batch.draw(alexSprite, alexX, alexY)

        game.batch.setColor(1f, 1f, 1f, 1f)
        game.batch.draw(texturaBtnIzq, btnIzqX, btnIzqY, tamañoBoton, tamañoBoton)
        game.batch.draw(texturaBtnDer, btnDerX, btnDerY, tamañoBoton, tamañoBoton)
        game.batch.draw(texturaBtnArriba, btnArribaX, btnArribaY, tamañoBoton, tamañoBoton)
        game.batch.draw(texturaBtnAbajo, btnAbajoX, btnAbajoY, tamañoBoton, tamañoBoton)
        game.batch.setColor(1f, 1f, 1f, 1f)

        game.batch.end()
    }

    private fun procesarMovimiento(delta: Float) {
        if (Gdx.input.isTouched) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = (Gdx.graphics.height - Gdx.input.y).toFloat()
            val movimiento = velocidad * delta

            if (touchX in btnIzqX..(btnIzqX+tamañoBoton) && touchY in btnIzqY..(btnIzqY+tamañoBoton)) alexX -= movimiento
            if (touchX in btnDerX..(btnDerX+tamañoBoton) && touchY in btnDerY..(btnDerY+tamañoBoton)) alexX += movimiento
            if (touchX in btnArribaX..(btnArribaX+tamañoBoton) && touchY in btnArribaY..(btnArribaY+tamañoBoton)) alexY += movimiento
            if (touchX in btnAbajoX..(btnAbajoX+tamañoBoton) && touchY in btnAbajoY..(btnAbajoY+tamañoBoton)) alexY -= movimiento
        }

        if (alexX < 0f) alexX = 0f
        if (alexY < 0f) alexY = 0f
        if (alexX > Gdx.graphics.width - alexSprite.width) alexX = (Gdx.graphics.width - alexSprite.width).toFloat()
        if (alexY > Gdx.graphics.height - alexSprite.height) alexY = (Gdx.graphics.height - alexSprite.height).toFloat()
    }

    override fun show() {}
    override fun resize(width: Int, height: Int) {}
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        fondoEscom.dispose()
        alexSprite.dispose()
        texturaBtnIzq.dispose(); texturaBtnDer.dispose()
        texturaBtnArriba.dispose(); texturaBtnAbajo.dispose()
    }
}
