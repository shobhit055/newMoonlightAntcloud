package com.antcloud.app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.antcloud.app.activity.NavActivity
import com.antcloud.app.activity.SplashActivity

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL


class FirebaseMessageReceiver : FirebaseMessagingService() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        //Log.d("TAG", "From: ${remoteMessage.from}")
        /*if (remoteMessage.data.isNotEmpty()) {
            Log.d("TAG", "Message data payload: ${remoteMessage.data}")
        }*/
        remoteMessage.notification?.let {
            it.body?.let { sendNotification(remoteMessage) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(messageBody: RemoteMessage) {
        lateinit var intent : Intent
        var channelId = ""

        if(messageBody.notification?.channelId == "1") {
            channelId = "route"
            intent = Intent(this, NavActivity::class.java)
            if(messageBody.data.containsKey("route")){
                intent.putExtra("route", messageBody.data["route"].toString())
            }
        }
        /*else if(messageBody.notification?.channelId == "2") {
            channelId = "game"
            intent = Intent(this, GameDetailsActivity::class.java)
            if(messageBody.data.containsKey("gameId")){
                intent.putExtra("gameId", messageBody.data["gameId"].toString())
            }
        } */
        else {
            channelId = getString(R.string.default_notification_channel_id)
            intent = Intent(this, SplashActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(messageBody.notification!!.title)
            .setContentText(messageBody.notification!!.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        if(messageBody.notification!!.imageUrl.toString().isNotEmpty()) {
            applyImage(notificationBuilder, messageBody.notification!!.imageUrl.toString())
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        val channel = NotificationChannel(channelId, getString(R.string.default_notification_channel_name), NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
        notificationManager.notify((0..100).random() /* ID of notification */, notificationBuilder.build())
    }

    private fun applyImage(builder: NotificationCompat.Builder, imageUrl: String) = runBlocking {
        val url = URL(imageUrl)
        withContext(Dispatchers.IO) {
            try {
                val input = url.openStream()
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                null
            }
        }?.let {
            bitmap ->
            builder.setLargeIcon(bitmap)
        }
    }
}