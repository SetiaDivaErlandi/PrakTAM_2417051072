package com.example.praktam_2417051072.network

import com.example.praktam_2417051072.model.BeautyItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface BeautyApiService {
    @GET("BeautyData.json") // Sesuai instruksi, sesuaikan dengan path Gist Anda
    suspend fun getBeautyItems(): List<BeautyItem>
}

object RetrofitClient {
    private const val BASE_URL = "https://gist.githubusercontent.com/username/" // Ganti dengan URL Gist Anda

    val instance: BeautyApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(BeautyApiService::class.java)
    }
}
