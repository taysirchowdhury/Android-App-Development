package com.example.vangtichai

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.math.BigInteger

class MainActivity : AppCompatActivity() {

    private lateinit var amountText: TextView
    private lateinit var notesLeft: LinearLayout
    private var notesRight: LinearLayout? = null
    private lateinit var keypad: GridLayout

    // Preserve the exact digits the user typed, including leading zeros.
    private var enteredDigits = ""
    private val denominations = listOf(500, 100, 50, 20, 10, 5, 2, 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The toolbar is provided by our XML, so disable the template ActionBar.
        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.vangti_teal)
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false
        setContentView(R.layout.activity_main)

        // Fraction resources must be resolved numerically before applying
        // guideline percentages. Passing @fraction in XML can produce
        // a string like "22.000004%" and crash ConstraintLayout inflation.
        configureTabletGuidelines()

        val root = findViewById<View>(R.id.rootLayout)
        val originalLeft = root.paddingLeft
        val originalTop = root.paddingTop
        val originalRight = root.paddingRight
        val originalBottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(
                originalLeft + bars.left,
                originalTop + bars.top,
                originalRight + bars.right,
                originalBottom + bars.bottom
            )
            insets
        }

        amountText = findViewById(R.id.amountText)
        notesLeft = findViewById(R.id.notesLeft)
        notesRight = findViewById(R.id.notesRight) // Only landscape XML defines this ID.
        keypad = findViewById(R.id.keypad)

        enteredDigits = savedInstanceState?.getString(KEY_DIGITS).orEmpty()
        buildKeypad()
        refreshDisplay()
    }

    private fun configureTabletGuidelines() {
        // Phone layouts have no tabletWorkspace, so nothing changes for phones.
        val workspace = findViewById<ConstraintLayout>(R.id.tabletWorkspace) ?: return
        val constraints = ConstraintSet()
        constraints.clone(workspace)

        if (findViewById<View>(R.id.tabletPortraitSplit) != null) {
            constraints.setGuidelinePercent(
                R.id.tabletPortraitSplit,
                resources.getFraction(R.fraction.tablet_portrait_notes_fraction, 1, 1)
            )
        } else if (findViewById<View>(R.id.tabletLandscapeFirstSplit) != null) {
            constraints.setGuidelinePercent(
                R.id.tabletLandscapeFirstSplit,
                resources.getFraction(R.fraction.tablet_landscape_first_split, 1, 1)
            )
            constraints.setGuidelinePercent(
                R.id.tabletLandscapeSecondSplit,
                resources.getFraction(R.fraction.tablet_landscape_second_split, 1, 1)
            )
        }
        constraints.applyTo(workspace)
    }

    private fun buildKeypad() {
        keypad.removeAllViews()

        // Landscape files contain notesRight; portrait files do not.
        val landscape = notesRight != null
        keypad.columnCount = if (landscape) 4 else 3
        keypad.rowCount = if (landscape) 3 else 4

        if (landscape) {
            for (digit in 1..8) {
                val position = digit - 1
                addKey(digit.toString(), position / 4, position % 4)
            }
            addKey("9", 2, 0)
            addKey("0", 2, 1)
            addKey(getString(R.string.clear_key), 2, 2, span = 2, clear = true)
        } else {
            for (digit in 1..9) {
                val position = digit - 1
                addKey(digit.toString(), position / 3, position % 3)
            }
            addKey("0", 3, 0)
            addKey(getString(R.string.clear_key), 3, 1, span = 2, clear = true)
        }
    }

    private fun addKey(
        label: String,
        row: Int,
        column: Int,
        span: Int = 1,
        clear: Boolean = false
    ) {
        val button = Button(this).apply {
            text = label
            isAllCaps = false
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            elevation = 0f
            stateListAnimator = null
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.vangti_text))
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.key_text_size)
            )
            background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(this@MainActivity, R.color.vangti_key_gray))
                cornerRadius = resources.getDimension(R.dimen.key_corner_radius)
            }
            setOnClickListener {
                enteredDigits = if (clear) "" else enteredDigits + label
                refreshDisplay()
            }
        }

        val spacing = resources.getDimensionPixelSize(R.dimen.key_gap)
        val params = GridLayout.LayoutParams(
            GridLayout.spec(row),
            GridLayout.spec(column, span, 1f)
        ).apply {
            width = 0
            height = resources.getDimensionPixelSize(R.dimen.key_height)
            setMargins(spacing, spacing, spacing, spacing)
            setGravity(Gravity.FILL_HORIZONTAL)
        }
        keypad.addView(button, params)
    }

    private fun refreshDisplay() {
        amountText.text = if (enteredDigits.isEmpty()) {
            getString(R.string.taka_empty)
        } else {
            getString(R.string.taka_amount, enteredDigits)
        }

        // Integer-only calculation; handles input bigger than a Long.
        var remaining = if (enteredDigits.isEmpty()) {
            BigInteger.ZERO
        } else {
            BigInteger(enteredDigits)
        }

        val rows = denominations.map { noteValue ->
            val denomination = BigInteger.valueOf(noteValue.toLong())
            val (count, rest) = remaining.divideAndRemainder(denomination)
            remaining = rest
            getString(R.string.note_count, noteValue, count.toString())
        }

        notesLeft.removeAllViews()
        notesRight?.removeAllViews()
        if (notesRight == null) {
            rows.forEach { addNoteRow(notesLeft, it) }
        } else {
            rows.take(4).forEach { addNoteRow(notesLeft, it) }
            rows.drop(4).forEach { addNoteRow(notesRight!!, it) }
        }
    }

    private fun addNoteRow(column: LinearLayout, text: String) {
        val row = TextView(this).apply {
            this.text = text
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.vangti_text))
            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.note_text_size)
            )
        }
        column.addView(
            row,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.note_row_height)
            )
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_DIGITS, enteredDigits)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val KEY_DIGITS = "entered_digits"
    }
}
