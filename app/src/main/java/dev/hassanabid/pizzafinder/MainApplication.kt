package dev.hassanabid.pizzafinder

import android.app.Application
import dev.hassanabid.android.architecture.data.PizzaFinderRepository
import dev.hassanabid.pizzafinder.utils.ServiceLocator

class MainApplication: Application() {

    val pizzaFinderRepository:
            PizzaFinderRepository
        get() = ServiceLocator.providePizzaFinderRepository(this)
}