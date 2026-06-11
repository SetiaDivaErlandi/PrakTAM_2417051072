package com.example.praktam_2417051072.data.repository

import com.example.praktam_2417051072.data.api.RetrofitClient
import com.example.praktam_2417051072.data.model.BeautyItem

class BeautyRepository {
    // Fungsi ini sekarang murni mengambil data dari Gist JSON melalui Retrofit
    // Kita biarkan Exception dilempar agar bisa dideteksi oleh UI (isError)
    suspend fun getBeautyItems(): List<BeautyItem> {
        return RetrofitClient.instance.getBeautyItems()
    }
}
