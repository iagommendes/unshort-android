package expo.modules.unshortcore

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue

object BoundsCalculator {
  fun computeBounds(context: Context, config: OverlayConfig, navInsetPx: Int = 0): Rect {
    val calibration = computeCalibrationBounds(context, config)
    if (calibration != null) {
      return calibration
    }
    return computeHeuristicBounds(context, config, navInsetPx)
  }

  fun computeHeuristicBounds(
    context: Context,
    config: OverlayConfig,
    navInsetPx: Int = 0,
  ): Rect {
    val metrics = context.resources.displayMetrics
    val tabCount = config.tabCount.coerceAtLeast(2)
    val tabIndex = config.shortsTabIndex.coerceIn(0, tabCount - 1)
    val barHeightPx = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      config.barHeightDp,
      metrics,
    ).toInt()

    val tabWidth = metrics.widthPixels / tabCount
    val left = tabWidth * tabIndex
    val top = metrics.heightPixels - barHeightPx - navInsetPx
    val right = left + tabWidth
    val bottom = metrics.heightPixels - navInsetPx

    return Rect(left, top, right, bottom)
  }

  fun computeCalibrationBounds(context: Context, config: OverlayConfig): Rect? {
    val leftPct = config.calibrationLeftPct ?: return null
    val topPct = config.calibrationTopPct ?: return null
    val widthPct = config.calibrationWidthPct ?: return null
    val heightPct = config.calibrationHeightPct ?: return null

    val metrics = context.resources.displayMetrics
    val left = (metrics.widthPixels * leftPct).toInt()
    val top = (metrics.heightPixels * topPct).toInt()
    val width = (metrics.widthPixels * widthPct).toInt()
    val height = (metrics.heightPixels * heightPct).toInt()

    return Rect(left, top, left + width, top + height)
  }

  fun heuristicToCalibrationPercentages(context: Context, config: OverlayConfig): CalibrationProfile {
    val rect = computeHeuristicBounds(context, config)
    val metrics = context.resources.displayMetrics
    return CalibrationProfile(
      name = "custom",
      deviceModel = android.os.Build.MODEL,
      tabCount = config.tabCount,
      shortsTabIndex = config.shortsTabIndex,
      leftPct = rect.left.toFloat() / metrics.widthPixels,
      topPct = rect.top.toFloat() / metrics.heightPixels,
      widthPct = rect.width().toFloat() / metrics.widthPixels,
      heightPct = rect.height().toFloat() / metrics.heightPixels,
    )
  }

  fun getNavigationBarInset(context: Context): Int {
    // Best-effort inset when overlay is attached; may be 0 without a window.
    return 0
  }
}
