package com.avaupotic.tastynavigator

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.avaupotic.lib.Vendor
import com.avaupotic.tastynavigator.databinding.ActivityMainBinding
import com.avaupotic.tastynavigator.notification.MyNotificationReceiver
import com.avaupotic.tastynavigator.notification.NotificationUtil
import org.json.JSONObject
import org.osmdroid.library.BuildConfig
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.avaupotic.tastynavigator.recyclerview.MyIRecyclerView
import com.avaupotic.tastynavigator.recyclerview.MyRecyclerViewAdapterVendor

class MainActivity : AppCompatActivity() {
    // suffix: A1
    private lateinit var binding: ActivityMainBinding
    private lateinit var app: MyApplication
    private lateinit var notificationUtil: NotificationUtil
    private val br: BroadcastReceiver = MyNotificationReceiver()
    companion object {
        const val CHANNEL_ID = "com.avaupotic.tastynavigator" //my channel id
        const val TIME_ID = "TIME_ID"
        const val MY_ACTION_FILTER = "com.avaupotic.tastynavigator.open"
        const val VOTING_KEY = "com.avaupotic.tastynavigator.vote"
        const val VOTING_ANSW_OPEN = "OPEN"
        private var notificationId = 0
        fun getNotificationUniqueID(): Int {
            return notificationId++
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val addVendorActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val vendorData = data.getStringExtra("vendorData")
                if (vendorData != null) {
                    try {
                        val jsonObject = JSONObject(vendorData)
                        val name = jsonObject.getString("name")
                        val location = jsonObject.getString("location")
                        val phoneNumber = jsonObject.getString("phoneNumber")
                        val latitude = jsonObject.getString("latitude")
                        val longitude = jsonObject.getString("longitude")
                        val imgLink = jsonObject.getString("imgLink")

                        val newVendor = Vendor(name,location, phoneNumber, latitude,longitude, imgLink)
                        app.vendors.add(newVendor)
                        app.showToast("Successfully added VENDOR")

                        app.saveToFile()

                        val currentDateTime = LocalDateTime.now()
                        val time = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

                        notificationUtil.createNotifyWithIntent(
                            "VENDOR ALERT",
                            "Check out this new Food Vendor: $name!",
                            time.toString(),
                            R.drawable.burger,
                            VendorActivity::class.java,
                            "",
                            newVendor.getUUID().toString()
                        )
                    } catch (e: Exception) {
                        app.showToast("Error parsing JSON object: ${e.message}")
                    }
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        createNotificationChannel()

        app = application as MyApplication

        notificationUtil = NotificationUtil(this)

        binding.rvDataA1.layoutManager = LinearLayoutManager(this)
        val adapter = MyRecyclerViewAdapterVendor(app.vendors, object:MyIRecyclerView{
            override fun onClick(p0: View?, position: Int) {
                val intent = Intent(this@MainActivity, VendorActivity::class.java)
                intent.putExtra("IDvendor", app.vendors[position].getUUID().toString())
                startActivity(intent)
            }
            override fun onLongClick(p0: View?, position: Int) {
                val item = app.vendors[position]
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Delete")
                builder.setMessage("Delete ${item.name}\n(ID: ${item.getUUID()})?")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("Yes") { _, _ ->
                    Toast.makeText(applicationContext, "Deleted Vendor", Toast.LENGTH_LONG).show()
                    app.vendors.removeAt(position)
                    binding.rvDataA1.adapter?.notifyItemChanged(position)
                }
                builder.setNeutralButton("Cancel") { _ , _ -> }
                builder.setNegativeButton("No") { _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        })
        binding.rvDataA1.adapter = adapter

        binding.btnMapA1.setOnClickListener {
            openMapActivity()
        }
        binding.btnAddVendorA1.setOnClickListener {
            openAddVendor()
        }
    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "TastyNavigatorChannel"
            val descriptionText = "Tasty Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.rvDataA1.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(MY_ACTION_FILTER)
        registerReceiver(br, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(br)
    }

    override fun onPause() {
        app.saveToFile()
        super.onPause()
    }


    private fun openMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openAddVendor() {
        val intent = Intent(this, AddVendorActivity::class.java)
        addVendorActivity.launch(intent)
    }
}