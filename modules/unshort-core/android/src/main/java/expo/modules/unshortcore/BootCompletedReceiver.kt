package expo.modules.unshortcore

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class BootCompletedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

    val prefs = BlockerPreferences(context)
    if (!prefs.blockerEnabled) return
    if (!Settings.canDrawOverlays(context)) return
    if (!AccessibilityUtils.isAccessibilityServiceEnabled(context)) return

    BlockerForegroundService.start(context)
  }
}
