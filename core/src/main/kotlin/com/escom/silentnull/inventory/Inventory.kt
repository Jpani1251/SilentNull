package com.escom.silentnull.inventory

import com.badlogic.gdx.graphics.Texture

data class Item(
    val id: String,
    val name: String,
    val description: String,
    val texturePath: String
)

class Inventory(val capacity: Int = 20) {
    private val items = mutableListOf<Item>()

    fun addItem(item: Item): Boolean {
        if (items.size < capacity) {
            items.add(item)
            return true
        }
        return false
    }

    fun removeItem(item: Item): Boolean {
        return items.remove(item)
    }

    fun getItems(): List<Item> = items.toList()

    fun isFull(): Boolean = items.size >= capacity

    fun getItemCount(): Int = items.size
}
