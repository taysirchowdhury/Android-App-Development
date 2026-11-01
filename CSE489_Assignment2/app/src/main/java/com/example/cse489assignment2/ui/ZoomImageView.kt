
package com.example.cse489assignment2.ui

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val transform = Matrix()
    private val values = FloatArray(9)

    private var zoom = 1f
    private var lastX = 0f
    private var lastY = 0f

    private val detector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(
                detector: ScaleGestureDetector
            ): Boolean {

                // Allow zoom between 1x and 5x.
                val next = (zoom * detector.scaleFactor)
                    .coerceIn(1f, 5f)

                val factor = next / zoom
                zoom = next

                transform.postScale(
                    factor,
                    factor,
                    detector.focusX,
                    detector.focusY
                )

                clampTranslation()
                imageMatrix = transform

                return true
            }
        }
    )

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)

        post {
            resetZoom()
        }
    }

    fun resetZoom() {

        val image = drawable ?: return

        val imageWidth = image.intrinsicWidth.toFloat()
        val imageHeight = image.intrinsicHeight.toFloat()

        if (
            imageWidth <= 0f ||
            imageHeight <= 0f ||
            width <= 0 ||
            height <= 0
        ) return

        // FIT_CENTER:
        // Display the whole image without cropping or stretching.
        val fitScale = min(
            width / imageWidth,
            height / imageHeight
        )

        zoom = 1f

        transform.reset()

        transform.postScale(
            fitScale,
            fitScale
        )

        // Center the image in the available screen.
        transform.postTranslate(
            (width - imageWidth * fitScale) / 2f,
            (height - imageHeight * fitScale) / 2f
        )

        imageMatrix = transform
    }

    private fun clampTranslation() {

        val image = drawable ?: return

        transform.getValues(values)

        val contentWidth =
            image.intrinsicWidth * values[Matrix.MSCALE_X]

        val contentHeight =
            image.intrinsicHeight * values[Matrix.MSCALE_Y]

        val x = values[Matrix.MTRANS_X]
        val y = values[Matrix.MTRANS_Y]

        val targetX = if (contentWidth <= width) {
            (width - contentWidth) / 2f
        } else {
            x.coerceIn(width - contentWidth, 0f)
        }

        val targetY = if (contentHeight <= height) {
            (height - contentHeight) / 2f
        } else {
            y.coerceIn(height - contentHeight, 0f)
        }

        transform.postTranslate(
            targetX - x,
            targetY - y
        )
    }

    override fun onTouchEvent(
        event: MotionEvent
    ): Boolean {

        parent?.requestDisallowInterceptTouchEvent(true)

        detector.onTouchEvent(event)

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                return true
            }

            MotionEvent.ACTION_MOVE -> {

                if (
                    event.pointerCount == 1 &&
                    !detector.isInProgress &&
                    drawable != null
                ) {
                    transform.postTranslate(
                        event.x - lastX,
                        event.y - lastY
                    )

                    clampTranslation()
                    imageMatrix = transform
                }

                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val remainingPointer =
                    if (event.actionIndex == 0) 1 else 0

                lastX = event.getX(remainingPointer)
                lastY = event.getY(remainingPointer)
            }

            MotionEvent.ACTION_UP -> {
                performClick()
            }
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
