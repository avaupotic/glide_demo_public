package com.avaupotic.tastynavigator

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.avaupotic.lib.Dish
import com.avaupotic.tastynavigator.databinding.ActivityAddDishBinding
import com.avaupotic.tastynavigator.notification.NotificationUtil
import org.json.JSONObject
import java.util.UUID

class AddDishActivity : AppCompatActivity() {
    // suffix: A5
    private lateinit var binding: ActivityAddDishBinding
    private lateinit var app: MyApplication
    private lateinit var idDish: UUID
    private lateinit var idVendor: UUID
    private lateinit var data: Dish
    private lateinit var notificationUtil: NotificationUtil
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MyApplication

        notificationUtil = NotificationUtil(this)

        val uuidStringDish = intent.getStringExtra("IDdish")
        val uuidStringVendor = intent.getStringExtra("IDvendor")

        if(uuidStringDish != null && uuidStringVendor != null) {
            idDish = UUID.fromString(uuidStringDish)
            idVendor = UUID.fromString(uuidStringVendor)

            val pos = app.vendors.indexOfFirst { it.getUUID() == idVendor }
            data = app.vendors[pos].items.first { it.getUUID() == idDish }

            binding.etNameA5.setText(data.name)
            binding.etImgLinkA5.setText(data.imgLink)
            binding.etDescA5.setText(data.description)
            binding.etPriceA5.setText(data.price.toString())
            binding.cbMilkA5.isChecked = data.lactose
            binding.cbGlutenA5.isChecked = data.gluten
            binding.cbNutsA5.isChecked = data.nuts

            binding.btnAddDishA5.text = "EDIT"

            binding.btnAddDishA5.setOnClickListener {
                data.name = binding.etNameA5.text.toString()
                data.imgLink = binding.etImgLinkA5.text.toString()
                data.description = binding.etDescA5.text.toString()
                data.price = binding.etPriceA5.text.toString().toDouble()
                data.gluten = binding.cbGlutenA5.isChecked
                data.lactose = binding.cbMilkA5.isChecked
                data.nuts = binding.cbNutsA5.isChecked

                val dPos = app.vendors[pos].items.indexOfFirst { it.getUUID() == idDish }
                app.vendors[pos].items[dPos] = data

                binding.etNameA5.text.clear()
                binding.etImgLinkA5.text.clear()
                binding.etDescA5.text.clear()
                binding.etPriceA5.text.clear()
                binding.cbMilkA5.isChecked = false
                binding.cbGlutenA5.isChecked = false
                binding.cbNutsA5.isChecked = false

                binding.btnAddDishA5.text = "ADD"

                finish()
            }

        } else {
            binding.btnAddDishA5.setOnClickListener {
                if(binding.etNameA5.text.isNotEmpty() && binding.etPriceA5.text.isNotEmpty() &&
                    binding.etImgLinkA5.text.isNotEmpty() && binding.etDescA5.text.isNotEmpty()) {

                    addDish(binding.etNameA5.text.toString(),
                        binding.etPriceA5.text.toString(),
                        binding.etImgLinkA5.text.toString(),
                        binding.etDescA5.text.toString(),
                        binding.cbMilkA5.isChecked,
                        binding.cbNutsA5.isChecked,
                        binding.cbGlutenA5.isChecked)

                    binding.etNameA5.text.clear()
                    binding.etPriceA5.text.clear()
                    binding.etImgLinkA5.text.clear()
                    binding.etDescA5.text.clear()
                    binding.cbMilkA5.isChecked = false
                    binding.cbNutsA5.isChecked = false
                    binding.cbGlutenA5.isChecked = false

                } else {
                    app.showToast("One or more fields are empty!")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addDish(name: String, price: String, imgLink: String, description: String,
                        lactose: Boolean, nuts: Boolean, gluten: Boolean) {
        try{
            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("price", price)
            jsonObject.put("imgLink", imgLink)
            jsonObject.put("description", description)
            jsonObject.put("lactose", lactose)
            jsonObject.put("nuts", nuts)
            jsonObject.put("gluten", gluten)

            val data = Intent()
            data.putExtra("dishData", jsonObject.toString())
            setResult(RESULT_OK, data)
            finish()
        } catch ( e: Exception) {
            app.showToast("Error parsing JSON object: ${e.message}")
        }
    }

    override fun onPause() {
        app.saveToFile()
        super.onPause()
    }
}