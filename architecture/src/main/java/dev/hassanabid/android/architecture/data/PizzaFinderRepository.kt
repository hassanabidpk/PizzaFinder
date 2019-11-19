package dev.hassanabid.android.architecture.data

import dev.hassanabid.android.architecture.service.PizzaFinderResponse

interface PizzaFinderRepository {

    suspend fun getPizzaPlaces(format: String, latitude: String, longitude: String, rtype: String): List<PizzaFinderResponse>

}