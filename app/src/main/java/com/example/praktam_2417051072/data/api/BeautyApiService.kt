package com.example.praktam_2417051072.data.api

import com.example.praktam_2417051072.data.model.BeautyItem
import retrofit2.http.GET

interface BeautyApiService {
    // Menggunakan path lengkap ke Gist Anda agar pasti ketemu
    @GET("SetiaDivaErlandi/7c92bf1d6ca7744290b33ffddc176144/raw/BeautyData.json")
    suspend fun getBeautyItems(): List<BeautyItem>
}
