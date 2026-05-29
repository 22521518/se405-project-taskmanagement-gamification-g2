package com.example.se405.android.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.apollographql.apollo.ApolloClient
import com.example.se405.android.R
import com.example.se405.android.graphql.UpdateFcmTokenMutation
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import android.os.Handler
import androidx.lifecycle.Lifecycle
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val apolloClient: ApolloClient by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToBackend(token)
    }

    private fun sendTokenToBackend(token: String) {
        serviceScope.launch {
            try {
                apolloClient.mutation(UpdateFcmTokenMutation(token)).execute()
            } catch (e: Exception) {
                Log.e("FCM", "Failed to update token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        //KIỂM TRA TRẠNG THÁI APP ĐANG CHẠY MỘT CÁCH ĐỒNG BỘ
        var isAppInForeground = false
        val latch = CountDownLatch(1) // Bộ đếm lùi để đợi kết quả từ Main Thread

        Handler(Looper.getMainLooper()).post {
            isAppInForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
                Lifecycle.State.STARTED)
            latch.countDown()
        }

        // Đợi tối đa 200 milliseconds để lấy kết quả
        latch.await(200, TimeUnit.MILLISECONDS)

        if (isAppInForeground) {
            // App đang mở trên màn hình -> Chặn Notification
            Log.d("FCM", "App is in foreground. Skipping notification.")
            return
        }

        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val channelId = "se405_chat_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tin nhắn mới",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo khi có tin nhắn chat mới"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            // Thay R.drawable.ic_launcher_foreground bằng icon logo app của bạn
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(Random.nextInt(), builder.build())
    }
}