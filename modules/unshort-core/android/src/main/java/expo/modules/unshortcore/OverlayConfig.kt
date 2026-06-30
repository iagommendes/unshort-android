package expo.modules.unshortcore

import android.graphics.Rect

data class OverlayConfig(
  val tabCount: Int = 5,
  val shortsTabIndex: Int = 1,
  val barHeightDp: Float = 56f,
  val calibrationLeftPct: Float? = null,
  val calibrationTopPct: Float? = null,
  val calibrationWidthPct: Float? = null,
  val calibrationHeightPct: Float? = null,
  val debugVisible: Boolean = false,
)

data class BlockerStatus(
  val enabled: Boolean,
  val overlayGranted: Boolean,
  val accessibilityEnabled: Boolean,
  val serviceRunning: Boolean,
)

data class CalibrationProfile(
  val name: String,
  val deviceModel: String,
  val tabCount: Int,
  val shortsTabIndex: Int,
  val leftPct: Float,
  val topPct: Float,
  val widthPct: Float,
  val heightPct: Float,
)

fun Rect.toMap(): Map<String, Int> = mapOf(
  "left" to left,
  "top" to top,
  "right" to right,
  "bottom" to bottom,
  "width" to width(),
  "height" to height(),
)
