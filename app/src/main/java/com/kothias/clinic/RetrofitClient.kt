package com.kothias.clinic

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Base URL for the API endpoint
    private const val BASE_URL = BuildConfig.BASE_URL

    // Singleton instance of Retrofit
    private var retrofit: Retrofit? = null

    // Get the Retrofit instance
    fun getInstance(): Retrofit {
        // If the Retrofit instance is null, create a new instance
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        // Return the Retrofit instance
        return retrofit!!
    }
}