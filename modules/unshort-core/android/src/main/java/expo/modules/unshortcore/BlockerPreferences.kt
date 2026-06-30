package expo.modules.unshortcore

import android.content.Context
import android.content.SharedPreferences

class BlockerPreferences(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  var blockerEnabled: Boolean
    get() = prefs.getBoolean(KEY_BLOCKER_ENABLED, false)
    set(value) = prefs.edit().putBoolean(KEY_BLOCKER_ENABLED, value).apply()

  var tabCount: Int
    get() = prefs.getInt(KEY_TAB_COUNT, 5)
    set(value) = prefs.edit().putInt(KEY_TAB_COUNT, value.coerceIn(2, 6)).apply()

  var shortsTabIndex: Int
    get() = prefs.getInt(KEY_SHORTS_TAB_INDEX, 1)
    set(value) = prefs.edit().putInt(KEY_SHORTS_TAB_INDEX, value.coerceAtLeast(0)).apply()

  var calibrationLeftPct: Float?
    get() = prefs.getFloatOrNull(KEY_CALIB_LEFT)
    set(value) = prefs.putFloatOrNull(KEY_CALIB_LEFT, value)

  var calibrationTopPct: Float?
    get() = prefs.getFloatOrNull(KEY_CALIB_TOP)
    set(value) = prefs.putFloatOrNull(KEY_CALIB_TOP, value)

  var calibrationWidthPct: Float?
    get() = prefs.getFloatOrNull(KEY_CALIB_WIDTH)
    set(value) = prefs.putFloatOrNull(KEY_CALIB_WIDTH, value)

  var calibrationHeightPct: Float?
    get() = prefs.getFloatOrNull(KEY_CALIB_HEIGHT)
    set(value) = prefs.putFloatOrNull(KEY_CALIB_HEIGHT, value)

  var debugOverlayVisible: Boolean
    get() = prefs.getBoolean(KEY_DEBUG_OVERLAY, false)
    set(value) = prefs.edit().putBoolean(KEY_DEBUG_OVERLAY, value).apply()

  fun toOverlayConfig(): OverlayConfig = OverlayConfig(
    tabCount = tabCount,
    shortsTabIndex = shortsTabIndex.coerceAtMost(tabCount - 1),
    calibrationLeftPct = calibrationLeftPct,
    calibrationTopPct = calibrationTopPct,
    calibrationWidthPct = calibrationWidthPct,
    calibrationHeightPct = calibrationHeightPct,
    debugVisible = debugOverlayVisible,
  )

  fun applyCalibrationProfile(profile: CalibrationProfile) {
    tabCount = profile.tabCount
    shortsTabIndex = profile.shortsTabIndex
    calibrationLeftPct = profile.leftPct
    calibrationTopPct = profile.topPct
    calibrationWidthPct = profile.widthPct
    calibrationHeightPct = profile.heightPct
  }

  fun clearCalibration() {
    calibrationLeftPct = null
    calibrationTopPct = null
    calibrationWidthPct = null
    calibrationHeightPct = null
  }

  private fun SharedPreferences.getFloatOrNull(key: String): Float? {
    if (!contains(key)) return null
    return getFloat(key, 0f)
  }

  private fun SharedPreferences.putFloatOrNull(key: String, value: Float?) {
    val editor = edit()
    if (value == null) {
      editor.remove(key)
    } else {
      editor.putFloat(key, value)
    }
    editor.apply()
  }

  companion object {
    private const val PREFS_NAME = "unshort_blocker_prefs"
    private const val KEY_BLOCKER_ENABLED = "blocker_enabled"
    private const val KEY_TAB_COUNT = "tab_count"
    private const val KEY_SHORTS_TAB_INDEX = "shorts_tab_index"
    private const val KEY_CALIB_LEFT = "calibration_left_pct"
    private const val KEY_CALIB_TOP = "calibration_top_pct"
    private const val KEY_CALIB_WIDTH = "calibration_width_pct"
    private const val KEY_CALIB_HEIGHT = "calibration_height_pct"
    private const val KEY_DEBUG_OVERLAY = "debug_overlay_visible"
  }
}
