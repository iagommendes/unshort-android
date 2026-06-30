package expo.modules.unshortcore

import android.content.Context
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class ShortsNodeFinder(private val context: Context) {
  private val catalog: List<ShortsIdEntry> by lazy { loadCatalog() }

  fun findShortsTabBounds(root: AccessibilityNodeInfo?): Rect? {
    if (root == null) return null

    for (entry in catalog) {
      for (description in entry.contentDescriptions) {
        val nodes = root.findAccessibilityNodeInfosByText(description)
        val bounds = pickBottomBarNode(nodes)
        if (bounds != null) return bounds
      }

      for (viewId in entry.viewIds) {
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        val bounds = pickBottomBarNode(nodes)
        if (bounds != null) return bounds
      }
    }

    return findShortsByTraversal(root)
  }

  private fun pickBottomBarNode(nodes: List<AccessibilityNodeInfo>?): Rect? {
    if (nodes.isNullOrEmpty()) return null

    val metrics = context.resources.displayMetrics
    val bottomThreshold = metrics.heightPixels * 0.75f

    for (node in nodes) {
      val rect = Rect()
      node.getBoundsInScreen(rect)
      if (rect.isEmpty) continue
      if (rect.top >= bottomThreshold && node.isClickable) {
        return rect
      }
    }

    for (node in nodes) {
      val rect = Rect()
      node.getBoundsInScreen(rect)
      if (!rect.isEmpty && rect.top >= bottomThreshold) {
        return rect
      }
    }

    return null
  }

  private fun findShortsByTraversal(root: AccessibilityNodeInfo): Rect? {
    val metrics = context.resources.displayMetrics
    val bottomThreshold = metrics.heightPixels * 0.75f
    val queue = ArrayDeque<AccessibilityNodeInfo>()
    queue.add(root)

    while (queue.isNotEmpty()) {
      val node = queue.removeFirst()
      val text = node.text?.toString()?.trim().orEmpty()
      val desc = node.contentDescription?.toString()?.trim().orEmpty()
      val matchesShorts = text.equals("Shorts", ignoreCase = true) ||
        desc.equals("Shorts", ignoreCase = true) ||
        desc.contains("Shorts", ignoreCase = true)

      if (matchesShorts) {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        if (!rect.isEmpty && rect.top >= bottomThreshold) {
          return rect
        }
      }

      for (i in 0 until node.childCount) {
        node.getChild(i)?.let { queue.add(it) }
      }
    }

    return null
  }

  private fun loadCatalog(): List<ShortsIdEntry> {
    return try {
      val input = context.assets.open("shorts-view-ids.json")
      val json = BufferedReader(InputStreamReader(input)).use { it.readText() }
      val array = JSONArray(json)
      buildList {
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          add(
            ShortsIdEntry(
              youtubeVersion = obj.optString("youtubeVersion"),
              contentDescriptions = obj.getStringArray("contentDescriptions"),
              viewIds = obj.getStringArray("viewIds"),
            ),
          )
        }
      }
    } catch (_: Exception) {
      DEFAULT_CATALOG
    }
  }

  private fun JSONObject.getStringArray(key: String): List<String> {
    val array = optJSONArray(key) ?: return emptyList()
    return buildList {
      for (i in 0 until array.length()) {
        add(array.getString(i))
      }
    }
  }

  data class ShortsIdEntry(
    val youtubeVersion: String,
    val contentDescriptions: List<String>,
    val viewIds: List<String>,
  )

  companion object {
    private val DEFAULT_CATALOG = listOf(
      ShortsIdEntry(
        youtubeVersion = "default",
        contentDescriptions = listOf("Shorts", "Shorts tab"),
        viewIds = listOf(
          "com.google.android.youtube:id/pivot_bar_shorts",
          "com.google.android.youtube:id/shorts_tab",
        ),
      ),
    )
  }
}
