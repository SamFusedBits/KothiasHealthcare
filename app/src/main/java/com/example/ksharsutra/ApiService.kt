package com.example.ksharsutra

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/submit/")
    fun submitQuestionnaire(@Body questionnaire: Questionnaire): Call<ResponseData>

    @GET("api/predict/")
    fun getPrediction(): Call<PredictionResponse>
}