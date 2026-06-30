package expo.modules.unshortcore

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils

object AccessibilityUtils {
  private const val SERVICE_CLASS =
    "expo.modules.unshortcore.YouTubeDetectorAccessibilityService"

  fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
      context.contentResolver,
      Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)
    val expectedComponent = ComponentName(context.packageName, SERVICE_CLASS).flattenToString()

    while (colonSplitter.hasNext()) {
      val component = colonSplitter.next()
      if (component.equals(expectedComponent, ignoreCase = true)) {
        return true
      }
    }
    return false
  }

  fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  }

  fun openOverlaySettings(context: Context) {
    val intent = Intent(
      Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
      Uri.parse("package:${context.packageName}"),
    ).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  }

  fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)
}
