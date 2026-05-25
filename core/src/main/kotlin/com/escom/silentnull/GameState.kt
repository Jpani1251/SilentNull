package com.escom.silentnull

import com.escom.silentnull.inventory.Inventory

/**
 * Singleton para mantener el estado global del juego
 * que debe persistir entre cambios de pantalla.
 */
object GameState {
    // Inventario global único para que no se pierda al cambiar de Screen
    val inventory = Inventory(20)

    // Conjunto de IDs de eventos que ya han sucedido
    val eventosCompletados = mutableSetOf<String>()

    fun esEventoCompletado(id: String): Boolean = eventosCompletados.contains(id)

    fun completarEvento(id: String) {
        eventosCompletados.add(id)
    }
}
