package dev.hassanabid.android.architecture.service

import retrofit2.http.GET
import retrofit2.http.Query

interface PizzaFinderApi {

    @GET("api/v1/pizzalist/")
    suspend fun getPizzaPlaces(@Query("format") format: String,
                               @Query("latitude") latitude: String,
                               @Query("longitude") longitude: String,
                               @Query("rtype") rtype: String): List<PizzaFinderResponse>


}