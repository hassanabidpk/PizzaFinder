package dev.hassanabid.pizzafinder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.hassanabid.pizzafinder.data.PizzaFinderRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val pizzaFinderRepository: PizzaFinderRepository
) : ViewModelProvider.NewInstanceFactory() {


    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(PizzaViewModel::class.java) ->
                    PizzaViewModel(pizzaFinderRepository)
                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}