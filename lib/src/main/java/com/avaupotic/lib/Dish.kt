package com.avaupotic.lib

import java.util.UUID

data class Dish(
    var name: String,
    var price: Double,
    var description: String,
    var imgLink: String,
    var lactose: Boolean,
    var nuts: Boolean,
    var gluten: Boolean
) {
    private var id: UUID = UUID.randomUUID()

    override fun toString(): String {
        return "Dish(name='$name', price='$price')"
    }

    fun getUUID(): UUID{
        return this.id
    }

    fun setUUID(id: UUID) {
        this.id = id
    }

}