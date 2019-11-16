package dev.hassanabid.pizzafinder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dev.hassanabid.pizzafinder.data.PizzaFinderRepository
import kotlinx.coroutines.Dispatchers

class PizzaViewModel(
    private val pizzaFinderRepository: PizzaFinderRepository
): ViewModel() {

    fun pizzaPlacesList(location: String) = liveData (Dispatchers.IO) {
        val fetchedList = pizzaFinderRepository.getPizzaPlaces("json",location, "pizza")

        try {
            emit(Result.success(fetchedList))
        } catch (ioException: Exception) {
            emit(Result.failure(ioException))
        }
    }

}