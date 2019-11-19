package dev.hassanabid.android.architecture.data

import dev.hassanabid.android.architecture.service.PizzaFinderApi
import dev.hassanabid.android.architecture.service.PizzaFinderResponse
import dev.hassanabid.android.architecture.service.RetrofitClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultPizzaFinderRepository (
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): PizzaFinderRepository {

    var client: PizzaFinderApi = RetrofitClient.webservice

    override suspend fun getPizzaPlaces(
        format: String,
        latitude: String,
        longitude: String,
        rtype: String
    ): List<PizzaFinderResponse> {
        return withContext(ioDispatcher) {
            return@withContext client.getPizzaPlaces(format, latitude, longitude, rtype)
        }
    }
}