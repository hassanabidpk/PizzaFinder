package dev.hassanabid.android.architecture.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dev.hassanabid.android.architecture.data.PizzaFinderRepository
import kotlinx.coroutines.Dispatchers

class PizzaViewModel(
    private val pizzaFinderRepository: PizzaFinderRepository
): ViewModel() {

    fun pizzaPlacesList(lat: String, lng: String) = liveData (Dispatchers.IO) {
        val fetchedList = pizzaFinderRepository.getPizzaPlaces("json",lat, lng, "pizza")

        try {
            emit(Result.success(fetchedList))
        } catch (ioException: Exception) {
            emit(Result.failure(ioException))
        }
    }

}