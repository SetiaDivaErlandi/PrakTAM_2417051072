package com.example.praktam_2417051072.data.model

import com.google.gson.annotations.SerializedName

data class BeautyItem(
    @SerializedName("nama")
    val nama: String,
    @SerializedName("kategori")
    val kategori: String,
    @SerializedName("harga")
    val harga: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("deskripsi")
    val deskripsi: String
)