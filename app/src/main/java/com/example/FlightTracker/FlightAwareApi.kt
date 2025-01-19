package com.example.FlightTracker
import okhttp3.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.IOException

data class AircraftInformation(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var heading: Int = 0,
    var altitude: Int = 0,
    var ident: String? = null,
    var origin: Airport? = null,
    var destination: Airport? = null,
    var actual_off: String? = null,
    var actual_on: String? = null,
    var aircraft_type: String? = null,
)

data class Airport(
    val code: String?,
    val name: String?,
    val city: String?
)

fun fetchAircraftPositions(query: String, callback: (List<AircraftInformation>, String) -> Unit) {
    val apiKey = BuildConfig.FLIGHTAWARE_API_KEY

    if (apiKey.isNullOrBlank()) {
        callback(emptyList(), "FLIGHTAWARE_API_KEY is not set")
        Log.e("FlightTrackerAPI", "API Key not found in BuildConfig")
        return
    } else {
        Log.d("FlightTrackerAPI", "API KEY: $apiKey")
    }
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://aeroapi.flightaware.com/aeroapi/flights/search?query=$query")
        .addHeader("x-apikey", apiKey) // Correct authentication header
        .addHeader("Content-Type", "application/json")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val responseData = response.body()?.string()
                if (responseData != null) {
                    val positions = parseAircraftPositions(responseData)
                    callback(positions, "")
                }
                else {
                    callback(emptyList(), "Empty response data")
                }
                Log.d("FlightTrackerAPI", "Response: $responseData")
            } else {
                val error = "Error: ${response.code()} - ${response.message()}"
                callback(emptyList(), error)
//                Log.e("FlightTrackerAPI", "Error: ${response.code()} - ${response.message()}")
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            val error = "Request failed: ${e.message}"
            callback(emptyList(), error)
//            Log.e("FlightTrackerAPI", "Request failed: ${e.message}")
        }
    })
}

fun parseAircraftPositions(responseData: String): MutableList<AircraftInformation> {
    val positions = mutableListOf<AircraftInformation>()
    try {
        val jsonObject = JSONObject(responseData)
        val flights = jsonObject.getJSONArray("flights")

        for (i in 0 until flights.length()) {
            val flight = flights.getJSONObject(i)
            val lastPositionJson = flight.optJSONObject("last_position")
            val originJson = flight.optJSONObject("origin")
            val destinationJson = flight.optJSONObject("destination")
            val ident = flight.optString("ident")
            val actualOff = flight.optString("actual_off")
            val actualOn = flight.optString("actual_on")
            val aircraftType = flight.optString("aircraft_type")
            val origin = originJson?.let {
                Airport(
                    code = originJson.optString("code"),
                    name = originJson.optString("name"),
                    city = originJson.optString("city")
                )
            }
            val destination = destinationJson?.let {
                Airport(
                    code = destinationJson.optString("code"),
                    name = destinationJson.optString("name"),
                    city = destinationJson.optString("city")
                )
            }
            if (lastPositionJson != null) {
                val latitude = lastPositionJson.optDouble("latitude", Double.NaN)
                val longitude = lastPositionJson.optDouble("longitude", Double.NaN)
                val heading = lastPositionJson.optInt("heading")
                val altitude = lastPositionJson.optInt("altitude")

                val aircraftInfo = AircraftInformation(
                    latitude = latitude,
                    longitude = longitude,
                    heading = heading,
                    altitude = altitude,
                    ident = ident,
                    origin = origin,
                    destination = destination,
                    actual_off = actualOff,
                    actual_on = actualOn,
                    aircraft_type = aircraftType,
                )
                if (!aircraftInfo.latitude.isNaN() && !aircraftInfo.longitude.isNaN()) {
                    positions.add(aircraftInfo)
                }
                Log.d("FlightTrackerAPI", "$positions")
            }
        }
    } catch (e: Exception) {
        Log.e("FlightTrackerAPI", "Error parsing response: ${e.message}")
    }

    return positions
}