package dev.hassanabid.pizzafinder.service

import retrofit2.http.GET
import retrofit2.http.Query

interface PizzaFinderApi {

    @GET("api/v1/")
    suspend fun getPizzaPlaces(@Query("format") format: String,
                                   @Query("location") location: String,
                                   @Query("rtype") rtype: String): List<PizzaFinderResponse>


}