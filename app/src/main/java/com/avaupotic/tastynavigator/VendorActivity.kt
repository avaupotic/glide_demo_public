package com.avaupotic.tastynavigator

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.avaupotic.lib.Dish
import com.avaupotic.tastynavigator.databinding.ActivityVendorBinding
import com.avaupotic.tastynavigator.notification.NotificationUtil
import com.avaupotic.tastynavigator.recyclerview.MyRecyclerViewAdapterDish
import com.avaupotic.tastynavigator.recyclerview.MyIRecyclerView
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
class VendorActivity : AppCompatActivity() {
    // suffix: A2
    private lateinit var binding: ActivityVendorBinding
    private lateinit var app: MyApplication
    private lateinit var id: UUID
    private lateinit var notificationUtil: NotificationUtil

    private val addDishActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val dishData = data.getStringExtra("dishData")
                if (dishData != null) {
                    try {
                        val jsonObject = JSONObject(dishData)
                        val name = jsonObject.getString("name")
                        val price = jsonObject.getDouble("price")
                        val description = jsonObject.getString("description")
                        val imgLink = jsonObject.getString("imgLink")
                        val lactose = jsonObject.getBoolean("lactose")
                        val nuts = jsonObject.getBoolean("nuts")
                        val gluten = jsonObject.getBoolean("gluten")

                        val pos = app.vendors.indexOfFirst { it.getUUID() == id }
                        val idDish = app.vendors[pos].addItem(Dish(name,price, description,imgLink, lactose, nuts, gluten))

                        app.saveToFile()

                        app.showToast("Successfully added DISH")
                        notificationUtil.createNotifyWithIntent(
                            "DISH ALERT",
                            "Check out this new Dish:$name by ${app.vendors[pos].name}" ,
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                            R.drawable.burger,
                            DishActivity::class.java,
                            idDish.toString(),
                            id.toString()
                        )

                    } catch (e: Exception) {
                        app.showToast("Error parsing JSON object: ${e.message}")
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MyApplication

        notificationUtil = NotificationUtil(this)

        val uuidString = intent.getStringExtra("IDvendor")

        if(uuidString != null) {
            id = UUID.fromString(uuidString)

            updateUI()

            binding.btnAddDishA2.setOnClickListener {
                openAddDish()
            }

            binding.btnEditVendorA2.setOnClickListener {
                openEditVendor()
            }
        }
        val pos = app.vendors.indexOfFirst { it.getUUID() == id }

        binding.rvMenuA2.layoutManager = LinearLayoutManager(this)
        val adapter = MyRecyclerViewAdapterDish(app.vendors[pos].items, object:MyIRecyclerView{
            override fun onClick(p0: View?, position: Int) {
                val intent = Intent(this@VendorActivity, DishActivity::class.java)
                intent.putExtra("IDdish", app.vendors[pos].items[position].getUUID().toString())
                intent.putExtra("IDvendor", uuidString)
                startActivity(intent)
            }
            override fun onLongClick(p0: View?, position: Int) {
                val item = app.vendors[pos].items[position]
                val builder = AlertDialog.Builder(this@VendorActivity)
                builder.setTitle("Delete")
                builder.setMessage("Delete ${item.name}\n(ID: ${item.getUUID()})?")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("Yes") { dialogInterface, which ->
                    Toast.makeText(applicationContext, "Deleted Dish", Toast.LENGTH_LONG).show()
                    app.vendors[pos].items.removeAt(position)
                    binding.rvMenuA2.adapter?.notifyItemChanged(position)
                }
                builder.setNeutralButton("Cancel"){dialogInterface , which ->
                    Toast.makeText(applicationContext,"Clicked Cancel", Toast.LENGTH_LONG).show()
                }
                builder.setNegativeButton("No"){dialogInterface, which ->
                    Toast.makeText(applicationContext,"Clicked No", Toast.LENGTH_LONG).show()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        })
        binding.rvMenuA2.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        binding.rvMenuA2.adapter?.notifyDataSetChanged()
    }

    private fun openAddDish() {
        val intent = Intent(this, AddDishActivity::class.java)
        addDishActivity.launch(intent)
    }

    private fun openEditVendor() {
        val intent = Intent(this, AddVendorActivity::class.java)
        intent.putExtra("ID", id.toString())
        startActivity(intent)
    }

    private fun updateUI() {
        val data = app.vendors.find { it.getUUID() == id }
        if (data != null) {
            binding.tvNameA2.text = data.name
            binding.tvLocationA2.text = data.location
            binding.tvPhoneNumA2.text = data.phoneNumber
            Glide.with(binding.ivPhotoA2)
                .load(data.imgLink)
                .placeholder(R.drawable.vendor_placeholder)
                .error(R.drawable.vendor_placeholder)
                .into(binding.ivPhotoA2)
        }
    }
}