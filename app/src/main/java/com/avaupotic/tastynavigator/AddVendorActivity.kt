package com.avaupotic.tastynavigator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.avaupotic.tastynavigator.databinding.ActivityAddVendorBinding
import com.avaupotic.tastynavigator.databinding.ActivityMainBinding
import org.json.JSONObject
import java.util.UUID

class AddVendorActivity : AppCompatActivity() {
    // suffix: A4
    private lateinit var binding: ActivityAddVendorBinding
    private lateinit var app: MyApplication
    private lateinit var id: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVendorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MyApplication

        val uuidString = intent.getStringExtra("ID")

        if (uuidString != null) {
            id = UUID.fromString(uuidString)
            val vendor = app.vendors.find { it.getUUID() == id }
            val pos = app.vendors.indexOfFirst { it.getUUID() == id }
            if (vendor != null) {
                binding.etNameA4.setText(vendor.name)
                binding.etLocationA4.setText(vendor.location)
                binding.etPhoneNumA4.setText(vendor.phoneNumber)
                binding.etLatitudeA4.setText(vendor.latitude)
                binding.etLongitudeA4.setText(vendor.longitude)
                binding.etImgLinkA4.setText(vendor.imgLink)

                binding.btnAddVendorA4.text = "EDIT"

                binding.btnAddVendorA4.setOnClickListener {
                    vendor.name = binding.etNameA4.text.toString()
                    vendor.location = binding.etLocationA4.text.toString()
                    vendor.phoneNumber = binding.etPhoneNumA4.text.toString()
                    vendor.latitude = binding.etLatitudeA4.text.toString()
                    vendor.longitude = binding.etLongitudeA4.text.toString()
                    if(binding.etImgLinkA4.text.isNotEmpty())
                        vendor.imgLink = binding.etImgLinkA4.text.toString()

                    app.vendors[pos] = vendor

                    binding.etNameA4.text.clear()
                    binding.etLocationA4.text.clear()
                    binding.etPhoneNumA4.text.clear()
                    binding.etLatitudeA4.text.clear()
                    binding.etLongitudeA4.text.clear()
                    binding.etImgLinkA4.text.clear()

                    binding.btnAddVendorA4.text = "ADD"

                    finish()
                }
            }
        } else {
            binding.btnAddVendorA4.setOnClickListener {
                if (binding.etNameA4.text.isNotEmpty() && binding.etLocationA4.text.isNotEmpty() &&
                    binding.etPhoneNumA4.text.isNotEmpty() && binding.etLatitudeA4.text.isNotEmpty() && binding.etLongitudeA4.text.isNotEmpty()
                ) {
                    if (binding.etImgLinkA4.text.isNotEmpty()) {
                        addVendor(
                            binding.etNameA4.text.toString(),
                            binding.etLocationA4.text.toString(),
                            binding.etPhoneNumA4.text.toString(),
                            binding.etLatitudeA4.text.toString(),
                            binding.etLongitudeA4.text.toString(),
                            binding.etImgLinkA4.text.toString()
                        )
                    } else {
                        addVendor(
                            binding.etNameA4.text.toString(),
                            binding.etLocationA4.text.toString(),
                            binding.etPhoneNumA4.text.toString(),
                            binding.etLatitudeA4.text.toString(),
                            binding.etLongitudeA4.text.toString(),
                        )
                    }

                    binding.etNameA4.text.clear()
                    binding.etLocationA4.text.clear()
                    binding.etPhoneNumA4.text.clear()
                    binding.etLatitudeA4.text.clear()
                    binding.etLongitudeA4.text.clear()
                    binding.etImgLinkA4.text.clear()
                } else {
                    app.showToast("One or more fields are empty!")
                }
            }
        }
    }

    private fun addVendor(name: String, location: String, phoneNumber: String, latitude: String, longitude: String, imgLink: String = "IMG") {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("location", location)
            jsonObject.put("phoneNumber", phoneNumber)
            jsonObject.put("latitude", latitude)
            jsonObject.put("longitude", longitude)
            jsonObject.put("imgLink", imgLink)

            val data = Intent()
            data.putExtra("vendorData", jsonObject.toString())
            setResult(RESULT_OK, data)
            finish()
        }  catch (e: Exception) {
            app.showToast("Error parsing JSON object: ${e.message}")
        }

    }

    override fun onPause() {
        app.saveToFile()
        super.onPause()
    }
}