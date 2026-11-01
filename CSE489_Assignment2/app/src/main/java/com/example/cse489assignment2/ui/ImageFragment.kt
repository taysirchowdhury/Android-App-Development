
package com.example.cse489assignment2.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cse489assignment2.R
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ImageFragment : Fragment() {

    companion object {
        // Fixed image ID: the same photograph is downloaded each time.
        private const val IMAGE_ENDPOINT =
            "https://picsum.photos/id/1015/1200/900"
    }

    private val executor = Executors.newSingleThreadExecutor()

    private var requestId = 0

    private lateinit var image: ZoomImageView
    private lateinit var loading: ProgressBar
    private lateinit var error: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_image,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        state: Bundle?
    ) {
        super.onViewCreated(view, state)

        image = view.findViewById(R.id.zoom_image)

        loading = view.findViewById(
            R.id.image_loading_indicator
        )

        error = view.findViewById(R.id.image_error)

        // Retry only if a download fails.
        error.setOnClickListener {
            downloadImage()
        }

        // Download once when this screen is opened.
        downloadImage()
    }

    private fun downloadImage() {

        val currentId = ++requestId

        image.setImageDrawable(null)

        loading.visibility = View.VISIBLE
        error.visibility = View.GONE

        executor.execute {

            var bitmap: android.graphics.Bitmap? = null
            var connection: HttpURLConnection? = null

            try {
                // Fixed URL: no random image or changing timestamp.
                val url = URL(IMAGE_ENDPOINT)

                connection =
                    url.openConnection() as HttpURLConnection

                connection.connectTimeout = 12000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true

                connection.connect()

                if (
                    connection.responseCode in 200..299 &&
                    connection.contentType.orEmpty()
                        .startsWith("image/")
                ) {
                    connection.inputStream.use {
                        bitmap = BitmapFactory.decodeStream(it)
                    }
                }

            } catch (_: Exception) {
                // Show the existing error view if downloading fails.
            } finally {
                connection?.disconnect()
            }

            val downloaded = bitmap

            activity?.runOnUiThread {

                if (
                    !isAdded ||
                    view == null ||
                    currentId != requestId
                ) {
                    downloaded?.recycle()
                    return@runOnUiThread
                }

                loading.visibility = View.GONE

                if (downloaded == null) {
                    error.visibility = View.VISIBLE
                } else {
                    image.setImageBitmap(downloaded)

                    // Show the complete photograph at initial scale.
                    image.resetZoom()
                }
            }
        }
    }

    override fun onDestroyView() {
        requestId++
        super.onDestroyView()
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
