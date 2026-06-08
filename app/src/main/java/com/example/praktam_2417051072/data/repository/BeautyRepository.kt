package com.example.praktam_2417051072.data.repository

import com.example.praktam_2417051072.data.api.RetrofitClient
import com.example.praktam_2417051072.data.model.BeautyItem

class BeautyRepository {
    suspend fun getBeautyItems(): List<BeautyItem> {
        // Langsung panggil tanpa try-catch di sini agar UI bisa menangkap pesan error teknisnya
        return RetrofitClient.instance.getBeautyItems()
    }
}
