package com.example.cse489assignment2

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/** Keep views clear of system status/navigation bars on edge-to-edge Android. */
object AppInsets {
    fun apply(activity: Activity, rootId: Int, includeKeyboard: Boolean = false) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        val root = activity.findViewById<View>(rootId)
        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val system = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val keyboardBottom = if (includeKeyboard)
                insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0
            view.setPadding(left + system.left, top + system.top,
                right + system.right, bottom + maxOf(system.bottom, keyboardBottom))
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }
}
