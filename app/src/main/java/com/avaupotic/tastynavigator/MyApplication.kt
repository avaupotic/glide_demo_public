package com.avaupotic.tastynavigator

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.TextView
import org.apache.commons.io.FileUtils
import android.widget.Toast
import com.avaupotic.lib.Dish
import com.avaupotic.lib.Vendor
import io.github.serpro69.kfaker.Faker
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.Random
import java.util.UUID
import kotlin.math.pow
import kotlin.math.round

class MyApplication : Application() {
    // LIST OF VENDORS
    var vendors = mutableListOf<Vendor>()
    var markers = mutableListOf<UUID>()

    private lateinit var id: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = getSharedPreferences("shared_prefs", Context.MODE_PRIVATE)
        id = (getUUID() ?: genAndSaveUUID()).toString()

        cleanTestData()
        loadFromFile()
    }

    fun getID() : String {
        return this.id
    }
    private fun getUUID(): String? {
        return sharedPreferences.getString("ID", null)
    }
    private fun genAndSaveUUID() {
        val editor = sharedPreferences.edit()
        val uuid = UUID.randomUUID().toString()

        editor.putString("ID", uuid)
        editor.apply()
    }
    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun genRandomDouble(downLimit : Double, upLimit : Double, decimals : Int) : Double {
        return round((Random().nextDouble()*upLimit + downLimit) * 10.0.pow(decimals)) /10.0.pow(decimals)
    }

    fun saveToFile() {
        try {
            val jsonArray = JSONArray()
            for (vendor in vendors) {
                val vendorJson = JSONObject()
                vendorJson.put("id", vendor.getUUID().toString())
                vendorJson.put("name", vendor.name)
                vendorJson.put("location", vendor.location)
                vendorJson.put("phoneNumber", vendor.phoneNumber)
                vendorJson.put("latitude", vendor.latitude)
                vendorJson.put("longitude", vendor.longitude)
                vendorJson.put("imgLink", vendor.imgLink)

                val itemsArray = JSONArray()
                for (item in vendor.items) {
                    val itemJson = JSONObject()
                    itemJson.put("id", item.getUUID().toString())
                    itemJson.put("dishName", item.name)
                    itemJson.put("price", item.price)
                    itemJson.put("description", item.description)
                    itemJson.put("imgLink", item.imgLink)
                    itemJson.put("lactose", item.lactose)
                    itemJson.put("nuts", item.nuts)
                    itemJson.put("gluten", item.gluten)

                    itemsArray.put(itemJson)
                }

                vendorJson.put("items", itemsArray)
                jsonArray.put(vendorJson)
            }
            val fileName = "vendors_data.json"
            val file = File(filesDir, fileName)
            FileUtils.writeStringToFile(file, jsonArray.toString(), "UTF-8", false)
            Timber.d("Vendors data saved to $fileName")
        } catch (e: IOException) {
            Timber.d("Error saving vendors data: ${e.message}")
        }
    }

    fun loadFromFile() {
        try {
            // Load JSON array from file using FileUtils
            val fileName = "vendors_data.json"
            val file = File(filesDir, fileName)

            if (file.exists()) {
                val jsonString = FileUtils.readFileToString(file, "UTF-8")
                val jsonArray = JSONArray(jsonString)

                // Clear existing vendors list
                vendors.clear()

                // Iterate through JSON array and populate vendors list
                for (i in 0 until jsonArray.length()) {
                    val vendorJson = jsonArray.getJSONObject(i)
                    val vendor = Vendor(
                        vendorJson.getString("name"),
                        vendorJson.getString("location"),
                        vendorJson.getString("phoneNumber"),
                        vendorJson.getString("latitude"),
                        vendorJson.getString("longitude"),
                        vendorJson.getString("imgLink")
                    )
                    vendor.setUUID(UUID.fromString(vendorJson.getString("id")))

                    val itemsArray = vendorJson.getJSONArray("items")
                    for (j in 0 until itemsArray.length()) {
                        val itemJson = itemsArray.getJSONObject(j)
                        val dish = Dish(
                            itemJson.getString("dishName"),
                            itemJson.getDouble("price"),
                            itemJson.getString("description"),
                            itemJson.getString("imgLink"),
                            itemJson.getBoolean("lactose"),
                            itemJson.getBoolean("nuts"),
                            itemJson.getBoolean("gluten")
                        )
                        dish.setUUID(UUID.fromString(itemJson.getString("id")))
                        vendor.items.add(dish)
                    }

                    vendors.add(vendor)
                }

                Timber.d("Vendors data loaded from $fileName")
            } else {
                Timber.d("File $fileName does not exist")
            }
        } catch (e: IOException) {
            Timber.d("Error loading vendors data: ${e.message}")
        }
    }

    fun addVendorMarker(vendor: Vendor, map: MapView) {
        var newMarker = Marker(map)
        var geoPoint = GeoPoint(vendor.latitude.toDouble(), vendor.longitude.toDouble())
        newMarker.position = geoPoint
        newMarker.title = vendor.name
        //newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        newMarker.icon = getDrawable(R.drawable.vendor_placeholder)

        newMarker.setOnMarkerClickListener { marker, mapView ->
            // Customizing the dialog
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val dialogView = inflater.inflate(R.layout.custom_marker_dialog, null)
            val titleTextView: TextView = dialogView.findViewById(R.id.titleTextView)
            titleTextView.text = marker.title ?: "No Title"

            AlertDialog.Builder(this)
                .setTitle("Custom Marker Dialog")
                .setView(dialogView)
                .setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                    Timber.d("PRESSED OK")
                }
                .show()

            // Return true to consume the event
            true
        }

        map.overlays.add(newMarker)

        map.invalidate()
    }


    fun cleanTestData() {
        val faker = Faker()

        vendors.clear()

        vendors.add(Vendor("Food Ena", "Prva Ulica 1", "123 456 789", "46.591111", "15.644444", "https://shorturl.at/nqH67"))
        vendors[0].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.ZIJWO8AaQL8JYtPQOnD3DAHaIf%26pid%3DApi&f=1&ipt=eeebc9964127f6fb4474d6b9ededf858a581c39d70cb61946338bd70561d4125&ipo=images", true, true, true))

        vendors.add(Vendor("Food Dva", "Druga Ulica 2", "987 654 321", "46.582222", "15.623444", "https://shorturl.at/vAJ05"))
        vendors[1].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.VrLrVpKHxjSk7bNvyZzbnwHaE-%26pid%3DApi&f=1&ipt=eacf0f39a6ddfbe22083ab64dd798463c517d371d40a62c8b3b37ef847e0894d&ipo=images", true, true, true))

        vendors.add(Vendor("Food Tri", "Tretja Ulica 3", "543 210 987", "46.574444", "15.645544", "https://shorturl.at/bopUY"))
        vendors[2].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.VrLrVpKHxjSk7bNvyZzbnwHaE-%26pid%3DApi&f=1&ipt=eacf0f39a6ddfbe22083ab64dd798463c517d371d40a62c8b3b37ef847e0894d&ipo=images", true, true, true))

        vendors.add(Vendor("Food Štiri", "Četrta Ulica 4", "876 543 210", "46.565555", "15.643344", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse3.mm.bing.net%2Fth%3Fid%3DOIP.n6ZEyiPCInahEw1Pm8ZjogHaHa%26pid%3DApi&f=1&ipt=f1e7333a0b5d5460c7c26b73b1ef46b0f4a4cef3696d2543b8a7ccaf86683a6b&ipo=images"))
        vendors[3].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.VrLrVpKHxjSk7bNvyZzbnwHaE-%26pid%3DApi&f=1&ipt=eacf0f39a6ddfbe22083ab64dd798463c517d371d40a62c8b3b37ef847e0894d&ipo=images", true, true, true))

        vendors.add(Vendor("Food Pet", "Peta Ulica 5", "012 345 678", "46.556666", "15.566444", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%3Fid%3DOIP.QB3Jh5zKuCyGvZRgoCO3mQHaFj%26pid%3DApi&f=1&ipt=6e6b368e9e2ef92e27a15ef85c0506c86b365256790c628aeffcd2f30c27d194&ipo=images"))
        vendors[4].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.VrLrVpKHxjSk7bNvyZzbnwHaE-%26pid%3DApi&f=1&ipt=eacf0f39a6ddfbe22083ab64dd798463c517d371d40a62c8b3b37ef847e0894d&ipo=images", true, true, true))

        vendors.add(Vendor("Food Šest", "Šesta Ulica 6", "456 789 012", "46.567777", "15.577444", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.Qzp2nn-kzlJJjtiTz6NAbAHaG3%26pid%3DApi&f=1&ipt=c9fabd745b48b6c7e715a9a84ab40392627c31416bb649f2285bb656a83f5da8&ipo=images"))
        vendors[5].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.VrLrVpKHxjSk7bNvyZzbnwHaE-%26pid%3DApi&f=1&ipt=eacf0f39a6ddfbe22083ab64dd798463c517d371d40a62c8b3b37ef847e0894d&ipo=images", true, true, true))

        vendors.add(Vendor("Food Sedem", "Sedma Ulica 7", "789 012 345", "46.578888", "15.663444", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%3Fid%3DOIP.CuRU8NUQEK_YjxlP-b8acwHaKK%26pid%3DApi&f=1&ipt=90745413d677178a89e631b261b7b76c80057804a6cd1a08edbe7775a7e08f17&ipo=images"))
        vendors[6].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.VrLrVpKHxjSk7bNvyZzbnwHaE-%26pid%3DApi&f=1&ipt=eacf0f39a6ddfbe22083ab64dd798463c517d371d40a62c8b3b37ef847e0894d&ipo=images", true, true, true))

        vendors.add(Vendor("Food Osem", "Osma Ulica 8", "321 654 987", "46.589999", "15.599999", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse1.mm.bing.net%2Fth%3Fid%3DOIP.kTBDkc96hkkqaawnDOcMuwHaEK%26pid%3DApi&f=1&ipt=8d1d455491e235ee7f105bf82f8433488fae88a5de5e31ac253d918bb2243c4f&ipo=images"))
        vendors[7].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.VrLrVpKHxjSk7bNvyZzbnwHaE-%26pid%3DApi&f=1&ipt=eacf0f39a6ddfbe22083ab64dd798463c517d371d40a62c8b3b37ef847e0894d&ipo=images", true, true, true))

        vendors.add(Vendor("Food Devet", "Deveta Ulica 9", "234 567 890", "46.594321", "15.588889", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse2.mm.bing.net%2Fth%3Fid%3DOIP.CmrgoSbcaO742k4lN46_NAHaF0%26pid%3DApi&f=1&ipt=950c89d45b7ecefe9f0f663564918cb2cefe55f72054623a68b1ecf08d7ea558&ipo=images"))
        vendors[8].items.add(Dish(faker.food.dish(), genRandomDouble(1.5, 10.50, 2), "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse4.mm.bing.net%2Fth%3Fid%3DOIP.VrLrVpKHxjSk7bNvyZzbnwHaE-%26pid%3DApi&f=1&ipt=eacf0f39a6ddfbe22083ab64dd798463c517d371d40a62c8b3b37ef847e0894d&ipo=images", true, true, true))


        saveToFile()
    }
}