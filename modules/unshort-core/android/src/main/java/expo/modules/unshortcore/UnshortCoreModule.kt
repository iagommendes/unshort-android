package expo.modules.unshortcore

import android.content.Context
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.Promise
import org.json.JSONArray
import org.json.JSONObject

class UnshortCoreModule : Module() {
  private val context: Context
    get() = requireNotNull(appContext.reactContext ?: appContext.activityProvider?.currentActivity)

  override fun definition() = ModuleDefinition {
    Name("UnshortCore")

    Events("blockerStatusChanged")

    AsyncFunction("canDrawOverlays") {
      AccessibilityUtils.canDrawOverlays(context)
    }

    Function("openOverlaySettings") {
      AccessibilityUtils.openOverlaySettings(context)
    }

    AsyncFunction("isAccessibilityEnabled") {
      AccessibilityUtils.isAccessibilityServiceEnabled(context)
    }

    Function("openAccessibilitySettings") {
      AccessibilityUtils.openAccessibilitySettings(context)
    }

    AsyncFunction("startBlocker") { promise: Promise ->
      try {
        val prefs = BlockerPreferences(context)
        prefs.blockerEnabled = true
        OverlayController.initialize(context.applicationContext)
        OverlayController.updateConfig(prefs.toOverlayConfig())
        BlockerForegroundService.start(context.applicationContext)
        emitStatusChanged(prefs)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("START_FAILED", e.message, e)
      }
    }

    AsyncFunction("stopBlocker") { promise: Promise ->
      try {
        val prefs = BlockerPreferences(context)
        prefs.blockerEnabled = false
        OverlayController.hideOverlay()
        BlockerForegroundService.stop(context.applicationContext)
        emitStatusChanged(prefs)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("STOP_FAILED", e.message, e)
      }
    }

    AsyncFunction("getBlockerStatus") {
      buildStatusMap(BlockerPreferences(context))
    }

    AsyncFunction("setOverlayConfig") { config: Map<String, Any?> ->
      val prefs = BlockerPreferences(context)
      (config["tabCount"] as? Number)?.toInt()?.let { prefs.tabCount = it }
      (config["shortsTabIndex"] as? Number)?.toInt()?.let { prefs.shortsTabIndex = it }
      (config["debugOverlayVisible"] as? Boolean)?.let { prefs.debugOverlayVisible = it }
      (config["calibrationLeftPct"] as? Number)?.toFloat()?.let { prefs.calibrationLeftPct = it }
      (config["calibrationTopPct"] as? Number)?.toFloat()?.let { prefs.calibrationTopPct = it }
      (config["calibrationWidthPct"] as? Number)?.toFloat()?.let { prefs.calibrationWidthPct = it }
      (config["calibrationHeightPct"] as? Number)?.toFloat()?.let { prefs.calibrationHeightPct = it }

      val overlayConfig = prefs.toOverlayConfig()
      OverlayController.updateConfig(overlayConfig)
      buildStatusMap(prefs)
    }

    AsyncFunction("clearCalibration") {
      val prefs = BlockerPreferences(context)
      prefs.clearCalibration()
      OverlayController.updateConfig(prefs.toOverlayConfig())
      buildStatusMap(prefs)
    }

    AsyncFunction("captureHeuristicCalibration") {
      val prefs = BlockerPreferences(context)
      val profile = BoundsCalculator.heuristicToCalibrationPercentages(
        context,
        prefs.toOverlayConfig(),
      )
      prefs.applyCalibrationProfile(profile)
      OverlayController.updateConfig(prefs.toOverlayConfig())
      profileToMap(profile)
    }

    AsyncFunction("applyCalibrationProfile") { profile: Map<String, Any?> ->
      val prefs = BlockerPreferences(context)
      val calibration = CalibrationProfile(
        name = profile["name"] as? String ?: "custom",
        deviceModel = profile["deviceModel"] as? String ?: android.os.Build.MODEL,
        tabCount = (profile["tabCount"] as? Number)?.toInt() ?: prefs.tabCount,
        shortsTabIndex = (profile["shortsTabIndex"] as? Number)?.toInt() ?: prefs.shortsTabIndex,
        leftPct = (profile["leftPct"] as? Number)?.toFloat() ?: 0f,
        topPct = (profile["topPct"] as? Number)?.toFloat() ?: 0f,
        widthPct = (profile["widthPct"] as? Number)?.toFloat() ?: 0f,
        heightPct = (profile["heightPct"] as? Number)?.toFloat() ?: 0f,
      )
      prefs.applyCalibrationProfile(calibration)
      OverlayController.updateConfig(prefs.toOverlayConfig())
      profileToMap(calibration)
    }

    AsyncFunction("exportCalibrationProfile") {
      val prefs = BlockerPreferences(context)
      val config = prefs.toOverlayConfig()
      val profile = if (
        config.calibrationLeftPct != null &&
        config.calibrationTopPct != null &&
        config.calibrationWidthPct != null &&
        config.calibrationHeightPct != null
      ) {
        CalibrationProfile(
          name = "exported",
          deviceModel = android.os.Build.MODEL,
          tabCount = config.tabCount,
          shortsTabIndex = config.shortsTabIndex,
          leftPct = config.calibrationLeftPct,
          topPct = config.calibrationTopPct,
          widthPct = config.calibrationWidthPct,
          heightPct = config.calibrationHeightPct,
        )
      } else {
        BoundsCalculator.heuristicToCalibrationPercentages(context, config)
      }
      profileToMap(profile)
    }

    AsyncFunction("getOverlayBounds") {
      OverlayController.getCurrentBounds()?.toMap()
    }
  }

  private fun emitStatusChanged(prefs: BlockerPreferences) {
    sendEvent("blockerStatusChanged", buildStatusMap(prefs))
  }

  private fun buildStatusMap(prefs: BlockerPreferences): Map<String, Any?> {
    val ctx = context.applicationContext
    return mapOf(
      "enabled" to prefs.blockerEnabled,
      "overlayGranted" to AccessibilityUtils.canDrawOverlays(ctx),
      "accessibilityEnabled" to AccessibilityUtils.isAccessibilityServiceEnabled(ctx),
      "serviceRunning" to prefs.blockerEnabled,
      "tabCount" to prefs.tabCount,
      "shortsTabIndex" to prefs.shortsTabIndex,
      "debugOverlayVisible" to prefs.debugOverlayVisible,
      "calibrationLeftPct" to prefs.calibrationLeftPct,
      "calibrationTopPct" to prefs.calibrationTopPct,
      "calibrationWidthPct" to prefs.calibrationWidthPct,
      "calibrationHeightPct" to prefs.calibrationHeightPct,
    )
  }

  private fun profileToMap(profile: CalibrationProfile): Map<String, Any> = mapOf(
    "name" to profile.name,
    "deviceModel" to profile.deviceModel,
    "tabCount" to profile.tabCount,
    "shortsTabIndex" to profile.shortsTabIndex,
    "leftPct" to profile.leftPct,
    "topPct" to profile.topPct,
    "widthPct" to profile.widthPct,
    "heightPct" to profile.heightPct,
  )
}
