package com.kothias.clinic

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object SendinblueHelper {
    // API key for the Sendinblue API endpoint
    private const val API_KEY = BuildConfig.API_KEY
    private const val SEND_EMAIL_URL = "https://api.sendinblue.com/v3/smtp/email"

    // OkHttpClient instance for making network requests
    private val client = OkHttpClient()

    // Function to send an email using the Sendinblue API
    fun sendEmail(to: String, subject: String, content: String, callback: (Boolean, String?) -> Unit) {
        // Create a JSON object with the email details
        val json = JSONObject().apply {
            put("sender", JSONObject().put("name", "Dr. Kothia's Clinic").put("email", "drkothiasclinic@gmail.com"))
            put("to", JSONArray().put(JSONObject().put("email", to)))
            put("subject", subject)
            put("htmlContent", content)
        }

        // Create a request body with the JSON object
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(SEND_EMAIL_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("api-key", API_KEY)
            .build()

        // Make the network request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            // Handle the response from the API
            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful, response.message)
            }
        })
    }
}