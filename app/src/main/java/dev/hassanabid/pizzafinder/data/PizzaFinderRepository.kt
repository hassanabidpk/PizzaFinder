package dev.hassanabid.pizzafinder.data

import dev.hassanabid.pizzafinder.service.PizzaFinderResponse

interface PizzaFinderRepository {

    suspend fun getPizzaPlaces(format: String, location: String, rtype: String): List<PizzaFinderResponse>

}