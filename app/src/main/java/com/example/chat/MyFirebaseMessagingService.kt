package com.example.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.chat.NewMessageActivity.Companion.USER_KEY
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val intent = Intent(this, ChatActivity::class.java)

        //intent.putExtra(USER_KEY, User("dWsUBv8Iv4P29CE6RK9szQdfpBs1", "Marco Rossi", "https://firebasestorage.googleapis.com/v0/b/chat-5b8f7.appspot.com/o/images%2Ffc7f01c9-80bf-4735-9819-4d2e7584f382?alt=media&token=7cce0641-4897-4109-a644-b93716f0f9c3"))

        val username = p0.data["title"]

        var user : User? = null

        val ref = FirebaseDatabase.getInstance().getReference("/users/").orderByChild("username").equalTo(username).addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for(child in snapshot.children){
                    var user : User? = child.getValue(User::class.java)
                    if(user != null){
                        intent.putExtra(USER_KEY, User(user.uid, user.username, user.image_url))
                        showNotification(intent, p0)
                    }
                }
            }
            })

    }

    fun showNotification(intent: Intent, p0: RemoteMessage){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        /*
        val largeIcon = BitmapFactory.decodeResource(
            resources,
            R.drawable.ic_delete
        )*/

        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.custom_user_icon)
                //.setLargeIcon(largeIcon)
                .setContentTitle(p0?.data?.get("title"))
                .setContentText(p0?.data?.get("message"))
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent)

        //Set notification color to match app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.color = resources.getColor(R.color.black)
        }
        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.GREEN
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }
}