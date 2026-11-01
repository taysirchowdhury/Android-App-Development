package com.example.cse489assignment2.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cse489assignment2.BatteryActivity
import com.example.cse489assignment2.CustomInputActivity
import com.example.cse489assignment2.R
import com.google.android.material.button.MaterialButton

/** First Activity / Broadcast Receiver drawer destination; matches the PDF's minimal selection screen. */
class BroadcastFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        inflater.inflate(R.layout.fragment_broadcast, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val spinner = view.findViewById<Spinner>(R.id.broadcast_spinner)
        // Expanded options use the PDF's exact wording; selected labels are short like the screenshot.
        val options = listOf(getString(R.string.custom_type), getString(R.string.battery_type))
        val shortOptions = listOf(getString(R.string.custom_short), getString(R.string.battery_short))
        val adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, options) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val result = super.getView(position, convertView, parent)
                (result as TextView).apply {
                    text = shortOptions[position]
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    textSize = 16f
                }
                return result
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        view.findViewById<MaterialButton>(R.id.proceed_button).setOnClickListener {
            val destination = when (spinner.selectedItemPosition) {
                0 -> CustomInputActivity::class.java
                else -> BatteryActivity::class.java
            }
            startActivity(Intent(requireContext(), destination))
        }
    }
}
