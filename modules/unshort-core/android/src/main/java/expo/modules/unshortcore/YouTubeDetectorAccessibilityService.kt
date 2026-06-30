package expo.modules.unshortcore

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class YouTubeDetectorAccessibilityService : AccessibilityService() {
  private val mainHandler = Handler(Looper.getMainLooper())
  private val shortsFinder by lazy { ShortsNodeFinder(this) }
  private var retryRunnable: Runnable? = null

  override fun onServiceConnected() {
    super.onServiceConnected()
    OverlayController.initialize(applicationContext)
    BlockerPreferences(applicationContext).let { prefs ->
      if (prefs.blockerEnabled) {
        OverlayController.updateConfig(prefs.toOverlayConfig())
      }
    }
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event == null) return
    if (!BlockerPreferences(this).blockerEnabled) return

    when (event.eventType) {
      AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
      AccessibilityEvent.TYPE_WINDOWS_CHANGED -> handleWindowChange(event.packageName?.toString())
      AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
        if (event.packageName?.toString() == YOUTUBE_PACKAGE) {
          scheduleBoundsLookup()
        }
      }
    }
  }

  override fun onInterrupt() {
    cancelBoundsLookup()
    OverlayController.setYouTubeVisible(false)
  }

  override fun onDestroy() {
    cancelBoundsLookup()
    OverlayController.setYouTubeVisible(false)
    super.onDestroy()
  }

  private fun handleWindowChange(packageName: String?) {
    val isYouTube = packageName == YOUTUBE_PACKAGE
    OverlayController.setYouTubeVisible(isYouTube)
    if (isYouTube) {
      scheduleBoundsLookup()
    } else {
      cancelBoundsLookup()
    }
  }

  private fun scheduleBoundsLookup() {
    cancelBoundsLookup()
    var attempts = 0
    val runnable = object : Runnable {
      override fun run() {
        val root = rootInActiveWindow
        val bounds = shortsFinder.findShortsTabBounds(root)
        if (bounds != null) {
          OverlayController.updateBoundsFromAccessibility(bounds)
        } else if (attempts < MAX_RETRIES) {
          attempts += 1
          mainHandler.postDelayed(this, RETRY_DELAY_MS)
        } else {
          OverlayController.refreshBounds(useAccessibility = false)
        }
      }
    }
    retryRunnable = runnable
    mainHandler.post(runnable)
  }

  private fun cancelBoundsLookup() {
    retryRunnable?.let { mainHandler.removeCallbacks(it) }
    retryRunnable = null
  }

  companion object {
    private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 300L
  }
}
