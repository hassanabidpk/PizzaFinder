package dev.hassanabid.pizzafinder.data

import dev.hassanabid.pizzafinder.service.PizzaFinderApi
import dev.hassanabid.pizzafinder.service.PizzaFinderResponse
import dev.hassanabid.pizzafinder.service.RetrofitClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.GET

class DefaultPizzaFinderRepository (
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): PizzaFinderRepository {

    var client: PizzaFinderApi = RetrofitClient.webservice

    @GET
    override suspend fun getPizzaPlaces(
        format: String,
        location: String,
        rtype: String
    ): List<PizzaFinderResponse> {
        return withContext(ioDispatcher) {
            return@withContext client.getPizzaPlaces(format, location, rtype)
        }
    }
}