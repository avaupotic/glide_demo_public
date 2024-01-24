package com.avaupotic.tastynavigator.location

import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import timber.log.Timber

class MyMapEventsReceiver : MapEventsReceiver {

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        Timber.d("singleTapConfirmedHelper", "${p?.latitude} - ${p?.longitude}")
        return true
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        Timber.d("longPressHelper", "${p?.latitude} - ${p?.longitude}")
        return false
    }

}

