package com.example.praktam_2417051072.model

import androidx.annotation.DrawableRes

data class BeautyItem(
    val nama: String,
    val kategori: String,
    val harga: Int,
    @DrawableRes val imageRes: Int
)