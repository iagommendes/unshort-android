package expo.modules.unshortcore

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

object OverlayController {
  private const val YOUTUBE_PACKAGE = "com.google.android.youtube"

  private var appContext: Context? = null
  private var windowManager: WindowManager? = null
  private var overlayView: FrameLayout? = null
  private var layoutParams: WindowManager.LayoutParams? = null
  private var currentBounds: Rect? = null
  private var youtubeVisible = false
  private var overlayAttached = false
  private var config: OverlayConfig = OverlayConfig()
  private val mainHandler = Handler(Looper.getMainLooper())

  fun initialize(context: Context) {
    if (appContext != null) return
    appContext = context.applicationContext
    windowManager = appContext?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
  }

  fun updateConfig(newConfig: OverlayConfig) {
    config = newConfig
    if (youtubeVisible) {
      refreshBounds(useAccessibility = false)
      updateOverlayLayout()
    }
  }

  fun setYouTubeVisible(visible: Boolean) {
    youtubeVisible = visible
    if (!visible) {
      hideOverlay()
      return
    }
    if (!canDrawOverlays()) return
    refreshBounds(useAccessibility = false)
    showOverlay()
  }

  fun updateBoundsFromAccessibility(bounds: Rect?) {
    if (!youtubeVisible) return
    if (bounds != null && !bounds.isEmpty) {
      currentBounds = bounds
      updateOverlayLayout()
      return
    }
    refreshBounds(useAccessibility = false)
  }

  fun refreshBounds(useAccessibility: Boolean) {
    val context = appContext ?: return
    val calculated = BoundsCalculator.computeBounds(context, config)
    currentBounds = calculated
    if (useAccessibility) {
      // Bounds may be updated asynchronously by accessibility service.
    }
    updateOverlayLayout()
  }

  fun hideOverlay() {
    mainHandler.post {
      val wm = windowManager ?: return@post
      val view = overlayView ?: return@post
      if (!overlayAttached) return@post
      try {
        wm.removeView(view)
      } catch (_: Exception) {
      } finally {
        overlayAttached = false
      }
    }
  }

  fun showOverlay() {
    mainHandler.post {
      val context = appContext ?: return@post
      if (!canDrawOverlays()) return@post

      val wm = windowManager ?: return@post
      val bounds = currentBounds ?: BoundsCalculator.computeBounds(context, config)
      currentBounds = bounds

      if (overlayView == null) {
        overlayView = createOverlayView(context)
        layoutParams = createLayoutParams(bounds)
        try {
          wm.addView(overlayView, layoutParams)
          overlayAttached = true
        } catch (_: Exception) {
          overlayAttached = false
        }
      } else if (overlayAttached) {
        updateOverlayLayout()
      } else {
        layoutParams = createLayoutParams(bounds)
        try {
          wm.addView(overlayView, layoutParams)
          overlayAttached = true
        } catch (_: Exception) {
          overlayAttached = false
        }
      }
    }
  }

  fun isOverlayAttached(): Boolean = overlayAttached

  fun canDrawOverlays(): Boolean {
    val context = appContext ?: return false
    return Settings.canDrawOverlays(context)
  }

  fun getCurrentBounds(): Rect? = currentBounds

  private fun updateOverlayLayout() {
    val view = overlayView ?: return
    val wm = windowManager ?: return
    val bounds = currentBounds ?: return
    val params = layoutParams ?: return

    params.x = bounds.left
    params.y = bounds.top
    params.width = bounds.width()
    params.height = bounds.height()

    if (overlayAttached) {
      try {
        wm.updateViewLayout(view, params)
      } catch (_: Exception) {
        overlayAttached = false
      }
    }

    val backgroundColor = if (config.debugVisible) {
      Color.argb(120, 255, 0, 0)
    } else {
      Color.TRANSPARENT
    }
    view.setBackgroundColor(backgroundColor)
  }

  private fun createOverlayView(context: Context): FrameLayout {
    return FrameLayout(context).apply {
      importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
      setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
          true
        } else {
          false
        }
      }
    }
  }

  private fun createLayoutParams(bounds: Rect): WindowManager.LayoutParams {
    val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
      @Suppress("DEPRECATION")
      WindowManager.LayoutParams.TYPE_PHONE
    }

    return WindowManager.LayoutParams(
      bounds.width(),
      bounds.height(),
      type,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
      PixelFormat.TRANSLUCENT,
    ).apply {
      gravity = Gravity.TOP or Gravity.START
      x = bounds.left
      y = bounds.top
    }
  }
}
