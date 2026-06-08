package com.example.praktam_2417051072.data.repository

import com.example.praktam_2417051072.data.api.RetrofitClient
import com.example.praktam_2417051072.data.model.BeautyItem

class BeautyRepository {
    // Fungsi ini sekarang murni mengambil data dari Gist JSON melalui Retrofit
    suspend fun getBeautyItems(): List<BeautyItem> {
        return try {
            RetrofitClient.instance.getBeautyItems()
        } catch (e: Exception) {
            // Mengembalikan list kosong jika terjadi kesalahan koneksi/API
            emptyList()
        }
    }
}
