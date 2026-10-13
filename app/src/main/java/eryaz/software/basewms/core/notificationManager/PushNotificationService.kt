package eryaz.software.basewms.core.notificationManager

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import eryaz.software.basewms.R
import eryaz.software.basewms.data.persistence.SessionManager
import eryaz.software.basewms.ui.MainActivity
import eryaz.software.basewms.data.repositories.UserRepo
import eryaz.software.zeus.zeusInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PushNotificationService : FirebaseMessagingService() {

    private val userRepo: UserRepo by zeusInject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = SessionManager.userId
        if (userId != 0) {
            serviceScope.launch {
                userRepo.updateUserNotificationToken(userId, token)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notification = message.notification
        showNotification(notification?.title, notification?.body)
    }

    private fun showNotification(title: String?, body: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("notification_title", title)
        intent.putExtra("notification_body", body)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_zeus_logo)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.zeus_logo
                    )
                )
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(getColor(R.color.ic_launcher_background))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}
