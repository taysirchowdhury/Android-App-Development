
package com.example.cse489assignment2

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow the image to draw behind system bars.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        // Allow drawing into safe areas around display cutouts.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attributes = window.attributes

            attributes.layoutInDisplayCutoutMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams
                        .LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams
                        .LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }

            window.attributes = attributes
        }

        setContentView(R.layout.activity_main)

        val content = findViewById<View>(R.id.main_content)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val navHostView = findViewById<View>(R.id.nav_host_fragment)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView =
            findViewById<NavigationView>(R.id.navigation_view)

        // Existing Fragment and navigation drawer setup.
        val host = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = host.navController

        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.broadcastFragment,
                R.id.imageFragment,
                R.id.videoFragment,
                R.id.audioFragment
            ),
            drawer
        )

        NavigationUI.setupWithNavController(
            toolbar,
            navController,
            appBarConfig
        )

        NavigationUI.setupWithNavController(
            navigationView,
            navController
        )

        val toolbarBaseHeight =
            resources.getDimensionPixelSize(R.dimen.toolbar_height)

        val surface = ContextCompat.getColor(this, R.color.surface)
        val imageSurface =
            ContextCompat.getColor(this, R.color.image_surface)
        val videoSurface =
            ContextCompat.getColor(this, R.color.video_surface)
        val audioSurface =
            ContextCompat.getColor(this, R.color.audio_background)

        // Dark transparent overlay for readable toolbar icons.
        val darkScrim = Color.argb(110, 0, 0, 0)

        var insetTop = 0
        var insetBottom = 0
        var insetLeft = 0
        var insetRight = 0

        var currentScreen =
            navController.currentDestination?.id
                ?: R.id.broadcastFragment

        fun updateLayout() {
            val isImage = currentScreen == R.id.imageFragment

            val background = when (currentScreen) {
                R.id.imageFragment -> imageSurface
                R.id.videoFragment -> videoSurface
                R.id.audioFragment -> audioSurface
                else -> surface
            }

            // Keep toolbar controls below the camera/status icons.
            val toolbarHeight = toolbarBaseHeight + insetTop

            if (toolbar.layoutParams.height != toolbarHeight) {
                toolbar.layoutParams =
                    toolbar.layoutParams.apply {
                        height = toolbarHeight
                    }
            }

            toolbar.setPadding(
                insetLeft,
                insetTop,
                insetRight,
                0
            )

            if (isImage) {
                // Image fills the entire screen, including behind
                // the transparent system bars and toolbar.
                navHostView.setPadding(0, 0, 0, 0)

                toolbar.setBackgroundColor(darkScrim)
            } else {
                // Other screens stay below the toolbar and
                // outside system navigation / cutout areas.
                navHostView.setPadding(
                    insetLeft,
                    toolbarHeight,
                    insetRight,
                    insetBottom
                )

                toolbar.setBackgroundColor(surface)
            }

            content.setBackgroundColor(background)
            navHostView.setBackgroundColor(background)
            drawer.setBackgroundColor(background)

            val darkBackground =
                currentScreen == R.id.imageFragment ||
                        currentScreen == R.id.videoFragment ||
                        currentScreen == R.id.audioFragment

            val titleColor = if (isImage) {
                Color.WHITE
            } else {
                ContextCompat.getColor(this, R.color.ink)
            }

            toolbar.setTitleTextColor(titleColor)
            toolbar.navigationIcon?.mutate()?.setTint(titleColor)

            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply {
                isAppearanceLightStatusBars = !isImage
                isAppearanceLightNavigationBars = !darkBackground
            }
        }

        // Read system bar dimensions whenever orientation changes.
        ViewCompat.setOnApplyWindowInsetsListener(content) { _, insets ->
            val safe = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )

            insetTop = safe.top
            insetBottom = safe.bottom
            insetLeft = safe.left
            insetRight = safe.right

            updateLayout()
            insets
        }

        navController.addOnDestinationChangedListener {
                _, destination, _ ->
            currentScreen = destination.id
            updateLayout()
        }

        ViewCompat.requestApplyInsets(content)
    }
}
