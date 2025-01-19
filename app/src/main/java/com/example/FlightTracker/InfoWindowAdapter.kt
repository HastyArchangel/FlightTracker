//package com.example.FlightTracker
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.widget.TextView
//import com.example.FlightTracker.R // Replace with your package name
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.model.Marker
//
//class CustomInfoWindowAdapter(context: Context) : GoogleMap.InfoWindowAdapter {
//
//    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
//    private val context: Context
//
//    init {
//        this.context = context
//    }
//
//    @SuppressLint("InflateParams")
//    override fun getInfoContents(marker: Marker): View? {
//        // Inflate the custom layout
//        val view: View = layoutInflater.inflate(R.layout.custom_info_window, null, false)
//
//        // Get references to the TextViews in the layout
//        val titleTextView = view.findViewById<TextView>(R.id.title)
//        val snippetTextView = view.findViewById<TextView>(R.id.snippet)
//
//        // Set the marker's title and snippet to the TextViews
//        titleTextView.text = marker.title
//        snippetTextView.text = marker.snippet
//
//        return view
//    }
//
//    override fun getInfoWindow(marker: Marker): View? {
//        return null // Use getInfoContents to customize the content
//    }
//}