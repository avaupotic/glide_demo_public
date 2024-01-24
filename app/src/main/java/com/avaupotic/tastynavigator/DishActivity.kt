package com.avaupotic.tastynavigator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.avaupotic.lib.Dish
import com.avaupotic.tastynavigator.databinding.ActivityDishBinding
import com.bumptech.glide.Glide
import timber.log.Timber
import java.util.UUID

class DishActivity : AppCompatActivity() {
    // suffix: A3
    private lateinit var binding: ActivityDishBinding
    private lateinit var app: MyApplication
    private lateinit var idDish: UUID
    private lateinit var idVendor: UUID
    private lateinit var data: Dish

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MyApplication

        val uuidStringDish = intent.getStringExtra("IDdish")
        val uuidStringVendor = intent.getStringExtra("IDvendor")


        if(uuidStringDish != null && uuidStringVendor != null) {
            idDish = UUID.fromString(uuidStringDish)
            idVendor = UUID.fromString(uuidStringVendor)

            updateUI()

            binding.btnEditDishA3.setOnClickListener {
                openEditDish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        val pos = app.vendors.indexOfFirst { it.getUUID() == idVendor }
        data = app.vendors[pos].items.first { it.getUUID() == idDish }
        //data = app.vendors[0].items[0]

        binding.tvNameA3.text = data.name
        binding.tvPriceA3.text = data.price.toString() + "€"
        binding.tvDescA3.text = data.description

        Glide.with(binding.ivPhotoA3)
            .load(data.imgLink)
            .placeholder(R.drawable.dish_placeholder)
            .error(R.drawable.dish_placeholder)
            .into(binding.ivPhotoA3)

        val allergensArray = mutableListOf<String>()

        if (data.lactose) allergensArray.add("Lactose")
        if (data.gluten) allergensArray.add("Gluten")
        if (data.nuts) allergensArray.add("Nuts")

        if (allergensArray.isEmpty()) {
            allergensArray.add("No allergens")
        }

        val adapter = ArrayAdapter(this, R.layout.lv_item, R.id.tvListItem, allergensArray)
        binding.lvAllergensA3.adapter = adapter
    }
    private fun openEditDish() {
        val intent = Intent(this, AddDishActivity::class.java)
        intent.putExtra("IDdish", idDish.toString())
        intent.putExtra("IDvendor", idVendor.toString())
        startActivity(intent)
    }
}