package com.example.ksharsutra

import com.example.ksharsutra.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object SendinblueHelper {
    private const val API_KEY = BuildConfig.API_KEY
    private const val SEND_EMAIL_URL = "https://api.sendinblue.com/v3/smtp/email"

    private val client = OkHttpClient()

    fun sendEmail(to: String, subject: String, content: String, callback: (Boolean, String?) -> Unit) {
        val json = JSONObject().apply {
            put("sender", JSONObject().put("name", "Dr. Kothia's Clinic").put("email", "drkothiasclinic@gmail.com"))
            put("to", JSONArray().put(JSONObject().put("email", to)))
            put("subject", subject)
            put("htmlContent", content)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(SEND_EMAIL_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("api-key", API_KEY)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(response.isSuccessful, response.message)
            }
        })
    }
}