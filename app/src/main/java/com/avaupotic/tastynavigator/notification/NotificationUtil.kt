package com.avaupotic.tastynavigator.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.avaupotic.tastynavigator.MainActivity
import com.avaupotic.tastynavigator.R
import timber.log.Timber

class NotificationUtil(private val context: Context) {
    private val CHANNEL_ID = "com.avaupotic.tastynavigator"

    fun createNotify(title: String, content: String, imageId: Int) {
        val myTimeIntent = Intent(context, MyNotificationReceiver::class.java).apply {
            action = MainActivity.MY_ACTION_FILTER
            putExtra(MainActivity.TIME_ID, "TODO SOME VAR")
        }
        Timber.d("createNotifyWithIntent")
        val myPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                context,
                MainActivity.getNotificationUniqueID(),
                myTimeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )


        var builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(imageId)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(myPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MainActivity.getNotificationUniqueID(), builder.build())
    }

    fun createNotifyWithIntent(title: String, content: String, time: String, imageId: Int, targetAcitivity: Class<*>, dishId: String, vendorId: String) {
        val myTimeIntent = Intent(context, MyNotificationReceiver::class.java).apply {
            action = MainActivity.MY_ACTION_FILTER
            putExtra(MainActivity.TIME_ID, time)
        }

        Timber.d("createNotifyWithIntent")
        val myPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(
                context,
                MainActivity.getNotificationUniqueID(),
                myTimeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        val inYes = Intent(context, targetAcitivity)
        inYes.putExtra(MainActivity.VOTING_KEY, MainActivity.VOTING_ANSW_OPEN)

        if(dishId != "") { inYes.putExtra("IDdish", dishId) }
        inYes.putExtra("IDvendor", vendorId)
        //inYes.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        inYes.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntentYes =
            PendingIntent.getActivity(
                context,
                MainActivity.getNotificationUniqueID(),
                inYes,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )


        var builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
            .setSmallIcon(imageId)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            //.setContentIntent(myPendingIntent)
            .addAction(
                R.drawable.ic_action_yes, "Open",
                pendingIntentYes
            )

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MainActivity.getNotificationUniqueID(), builder.build())
        Timber.d("Exec new Notification")
    }
}