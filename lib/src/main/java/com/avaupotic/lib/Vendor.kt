package com.avaupotic.lib

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

data class Vendor(
    var name: String,
    var location: String,
    var phoneNumber: String,
    var latitude: String,
    var longitude: String,
    var imgLink: String = "IMG",
    val items: MutableList<Dish> = mutableListOf()
) {
    private var id: UUID = UUID.randomUUID()
    fun getUUID(): UUID{
        return this.id
    }
    fun setUUID(id: UUID) {
        this.id = id
    }
    fun addItem(item: Dish) : UUID{
        items.add(item)
        return item.getUUID()
    }

    fun displayItems(){
        for(item in items) {
            println(item.toString())
        }
    }

    fun sortItemsById(){
        items.sortBy { it.getUUID() }
    }

    fun addOrUpdateItem(item: Dish) {
        val existingItemIndex = items.indexOfFirst { it.getUUID() == item.getUUID() }
        if (existingItemIndex != -1) {
            items[existingItemIndex] = item
        } else {
            addItem(item)
        }
    }

    fun deleteItem(itemId: UUID?) {
        val iterator: MutableIterator<Dish> = items.iterator()
        while (iterator.hasNext()) {
            val item: Dish = iterator.next()
            if (item.getUUID() == itemId) {
                iterator.remove()
            }
        }
    }

    fun addItemsFromJson(json: String, itemType: Class<out Dish>) {
        val type = TypeToken.getParameterized(MutableList::class.java, itemType).type
        val gson = Gson()
        val loadedItems: List<Dish> = gson.fromJson(json, type)

        items.addAll(loadedItems)
    }

    fun saveItemsToJson(file: File) {
        val gson = Gson()
        val jsonString = gson.toJson(items)

        file.writeText(jsonString)
    }

}