package com.example.marsphotos.data

import com.google.gson.annotations.SerializedName

class ShopSearchResponse : ArrayList<ShopSearchItem>()

data class ShopSearchItem(
    @SerializedName("display_name")
    val displayName: String,
    val lat: String,
    val lon: String,
    // 👇 お店の詳細情報（電話番号など）を受け取るための拡張エリア
    val extratags: ExtraTags?
)

data class ExtraTags(
    // 📞 データベースに登録されている電話番号
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("contact:phone")
    val contactPhone: String?
)