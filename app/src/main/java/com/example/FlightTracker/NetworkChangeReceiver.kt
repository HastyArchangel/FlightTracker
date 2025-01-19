package com.example.FlightTracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            if (isConnected) {
                Log.d("NetworkChangeReceiver", "Connected to the internet")
                Toast.makeText(context, "Connected to the internet", Toast.LENGTH_SHORT).show()
                // Aici poți apela funcția de refresh a datelor
                // Exemplu: refreshData()
            } else {
                Log.d("NetworkChangeReceiver", "Connection to the internet lost")
                Toast.makeText(context, "Connection to the internet lost", Toast.LENGTH_SHORT).show()
            }
        }
    }
}