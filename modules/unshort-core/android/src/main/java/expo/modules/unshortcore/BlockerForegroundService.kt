package expo.modules.unshortcore

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BlockerForegroundService : Service() {
  override fun onCreate() {
    super.onCreate()
    OverlayController.initialize(applicationContext)
    createNotificationChannel()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val prefs = BlockerPreferences(this)
    OverlayController.updateConfig(prefs.toOverlayConfig())

    val notification = buildNotification()
    startForeground(NOTIFICATION_ID, notification)

    if (!prefs.blockerEnabled) {
      stopSelf()
      return START_NOT_STICKY
    }

    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onDestroy() {
    OverlayController.hideOverlay()
    super.onDestroy()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
      CHANNEL_ID,
      getString(R.string.blocker_notification_channel),
      NotificationManager.IMPORTANCE_LOW,
    )
    manager.createNotificationChannel(channel)
  }

  private fun buildNotification(): Notification {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
    val pendingIntent = PendingIntent.getActivity(
      this,
      0,
      launchIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle(getString(R.string.blocker_notification_title))
      .setContentText(getString(R.string.blocker_notification_text))
      .setSmallIcon(android.R.drawable.ic_lock_lock)
      .setContentIntent(pendingIntent)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  companion object {
    const val NOTIFICATION_ID = 1001
    private const val CHANNEL_ID = "unshort_blocker_channel"

    fun start(context: Context) {
      val intent = Intent(context, BlockerForegroundService::class.java)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stop(context: Context) {
      context.stopService(Intent(context, BlockerForegroundService::class.java))
    }
  }
}
