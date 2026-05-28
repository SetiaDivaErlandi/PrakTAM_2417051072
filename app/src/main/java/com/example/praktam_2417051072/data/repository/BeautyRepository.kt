package com.example.praktam_2417051072.data.repository

import com.example.praktam_2417051072.data.api.RetrofitClient
import com.example.praktam_2417051072.data.model.BeautyItem

class BeautyRepository {
    suspend fun getBeautyItems(): List<BeautyItem> {
        // Mengambil data dari internet (Retrofit)
        return RetrofitClient.instance.getBeautyItems()
    }
}
