package expo.modules.unshortcore

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class BoundsCalculatorTest {
  @Test
  fun computeHeuristicBounds_placesShortsOnSecondTab() {
    val context = RuntimeEnvironment.getApplication()
    val metrics = context.resources.displayMetrics
    metrics.widthPixels = 1080
    metrics.heightPixels = 2400
    metrics.density = 3f

    val config = OverlayConfig(tabCount = 5, shortsTabIndex = 1, barHeightDp = 56f)
    val rect = BoundsCalculator.computeHeuristicBounds(context, config, navInsetPx = 0)

    assertEquals(216, rect.left)
    assertEquals(432, rect.right)
    assertEquals(2400 - 168, rect.top)
    assertEquals(2400, rect.bottom)
  }

  @Test
  fun computeCalibrationBounds_usesNormalizedPercentages() {
    val context = RuntimeEnvironment.getApplication()
    val metrics = context.resources.displayMetrics
    metrics.widthPixels = 1000
    metrics.heightPixels = 2000

    val config = OverlayConfig(
      calibrationLeftPct = 0.2f,
      calibrationTopPct = 0.9f,
      calibrationWidthPct = 0.2f,
      calibrationHeightPct = 0.08f,
    )

    val rect = BoundsCalculator.computeBounds(context, config)
    assertEquals(200, rect.left)
    assertEquals(400, rect.right)
    assertEquals(1800, rect.top)
    assertEquals(1960, rect.bottom)
  }
}
