package com.example.marsphotos.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ShopApiService {
    @Headers("User-Agent: LunchLogApp/1.0")
    @GET("search")
    suspend fun searchShop(
        @Query("q") keyword: String,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1,
        @Query("extratags") extraTags: Int = 1,
        @Query("limit") limit: Int = 30 // 👈 1 から 5 に増やして、最大5件の候補を取得！
    ): ShopSearchResponse
}

object ShopApi {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val retrofitService: ShopApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ShopApiService::class.java)
    }
}