package com.example.praktam_2417051072.data.api

import com.example.praktam_2417051072.data.model.BeautyItem
import retrofit2.http.GET

interface BeautyApiService {
    // Menghapus hash revisi (9a7004...) agar selalu mengambil data TERBARU.
    // Titik (.) di akhir 'json.' tetap saya sertakan karena nama file di Gist kamu memang ada titiknya.
    @GET("https://gist.githubusercontent.com/SetiaDivaErlandi/7c92bf1d6ca7744290b33ffddc176144/raw/BeautyData.json.")
    suspend fun getBeautyItems(): List<BeautyItem>
}