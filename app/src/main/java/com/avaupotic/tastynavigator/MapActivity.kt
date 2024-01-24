package com.avaupotic.tastynavigator

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.avaupotic.tastynavigator.databinding.ActivityMapBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.avaupotic.tastynavigator.location.LocationProviderChangedReceiver
import com.avaupotic.tastynavigator.location.MyEventLocationSettingsChange
import com.avaupotic.tastynavigator.location.MyMapEventsReceiver
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import timber.log.Timber
import java.util.Random
@RequiresApi(Build.VERSION_CODES.O)
class MapActivity : AppCompatActivity() {
    // suffix: A4
    private lateinit var binding: ActivityMapBinding
    private lateinit var app: MyApplication
    val rnd = Random()
    lateinit var map: MapView
    var startPoint: GeoPoint = GeoPoint(46.55951, 15.63970);
    lateinit var mapController: IMapController
    var marker: Marker? = null

    // LOCATION
    private var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient //https://developer.android.com/training/location/retrieve-current
    private var lastLoction: Location? = null
    private var locationCallback: LocationCallback
    private var locationRequest: LocationRequest
    private var requestingLocationUpdates = false

    companion object {
        val REQUEST_CHECK_SETTINGS = 20202
    }

    init {
        locationRequest = LocationRequest.create()
            .apply { //https://stackoverflow.com/questions/66489605/is-constructor-locationrequest-deprecated-in-google-maps-v2
                interval = 1000 //can be much higher
                fastestInterval = 500
                smallestDisplacement = 10f //10m
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                maxWaitTime = 1000
            }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Update UI with location data
                    updateLocation(location) //MY function
                }
            }
        }

        this.activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allAreGranted = true
            for (b in result.values) {
                allAreGranted = allAreGranted && b
            }

            Timber.d("Permissions granted $allAreGranted")
            if (allAreGranted) {
                initCheckLocationSettings()
                initMap() // if settings are ok
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MyApplication

        val br: BroadcastReceiver = LocationProviderChangedReceiver()
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(br, filter)

        //LocalBroadcastManager.getInstance(this).registerReceiver(locationProviderChange)
        Configuration.getInstance()
            .load(applicationContext, this.getPreferences(Context.MODE_PRIVATE))

        map = binding.mvMapA4
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        mapController = map.controller

        val mapEventsReceiver = MyMapEventsReceiver()
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        map.overlays.add(mapEventsOverlay)

        val appPerms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
        activityResultLauncher.launch(appPerms)
    }

    override fun onResume() {
        super.onResume()
        initMap()
        binding.mvMapA4.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (requestingLocationUpdates) {
            requestingLocationUpdates = false
            stopLocationUpdates()
        }
        binding.mvMapA4.onPause()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    fun initMap() {
        initLoaction()
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }

        val iterator = app.vendors.iterator()
        while (iterator.hasNext()) {
            val vendor = iterator.next()
            app.markers.add(vendor.getUUID())

            // CREATE MARKER FOR VENDOR
            val newMarker = Marker(map)
            val geoPoint = GeoPoint(vendor.latitude.toDouble(), vendor.longitude.toDouble())
            newMarker.position = geoPoint
            newMarker.title = vendor.name
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            newMarker.icon = ContextCompat.getDrawable(this,R.drawable.map_vendor)

            // ADD ONCLICK TO MARKER
            newMarker.setOnMarkerClickListener { _, _ ->
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val dialogView = inflater.inflate(R.layout.custom_marker_dialog, null)
                val titleTextView: TextView = dialogView.findViewById(R.id.titleTextView)
                val addressTextView: TextView = dialogView.findViewById(R.id.addressTextView)
                val phoneNumberTextView: TextView = dialogView.findViewById(R.id.phoneNumberTextView)
                titleTextView.text = vendor.name
                addressTextView.text = vendor.location
                phoneNumberTextView.text = vendor.phoneNumber


                android.app.AlertDialog.Builder(this)
                    .setTitle("VENDOR INFO")
                    .setView(dialogView)
                    .setPositiveButton("MORE") { _: DialogInterface, _: Int ->
                        Timber.d("Open Vendor with ID: ${vendor.getUUID()}")
                        val intent = Intent(this@MapActivity, VendorActivity::class.java)
                        intent.putExtra("IDvendor", vendor.getUUID().toString())
                        startActivity(intent)
                    }
                    .show()

                true
            }
            map.overlays.add(newMarker)
            map.invalidate()
        }

        addCompass(map)

        mapController.setZoom(14.0)
        mapController.setCenter(startPoint);
        map.invalidate()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMsg(status: MyEventLocationSettingsChange) {
        if (status.on) {
            initMap()
        } else {
            Timber.i("Stop something")
        }
    }

    fun initLoaction() { //call in create
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        readLastKnownLocation()
    }

    private fun stopLocationUpdates() { //onPause
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() { //onResume
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    //https://developer.android.com/training/location/retrieve-current
    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(it) }
            }
    }

    fun initCheckLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            Timber.d("Settings Location IS OK")
            MyEventLocationSettingsChange.globalState = true //default
            initMap()
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                Timber.d("Settings Location addOnFailureListener call settings")
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        this@MapActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    Timber.d("Settings Location sendEx??")
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("Settings onActivityResult for $requestCode result $resultCode")
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                initMap()
            }
        }
    }

    fun updateLocation(newLocation: Location) {
        lastLoction = newLocation
        // TODO -> UNCOMMENT OUT OF DEBUG
        //startPoint.longitude = newLocation.longitude
        //startPoint.latitude = newLocation.latitude
        mapController.setCenter(startPoint)
        getPositionMarker().position = startPoint
        map.invalidate()

    }

    private fun getPositionMarker(): Marker { //Singelton
        if (marker == null) {
            marker = Marker(map)
            marker!!.title = "I am here"
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker!!.icon = ContextCompat.getDrawable(this, com.avaupotic.tastynavigator.R.drawable.my_location);
            map.overlays.add(marker)
        }
        return marker!!
    }


    fun iAmHere(view: View?) {
        startPoint.latitude = startPoint.latitude + (rnd.nextDouble() - 0.5) * 0.001
        mapController.setCenter(startPoint)
        getPositionMarker().position = startPoint
        map.invalidate()
    }

    fun addCompass(view: View?) {
        val mCompassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), map)
        mCompassOverlay.enableCompass()
        map.overlays.add(mCompassOverlay)
        map.invalidate()
    }
}