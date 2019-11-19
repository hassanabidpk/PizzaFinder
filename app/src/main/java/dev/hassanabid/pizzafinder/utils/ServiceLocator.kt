package dev.hassanabid.pizzafinder.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import dev.hassanabid.android.architecture.data.DefaultPizzaFinderRepository
import dev.hassanabid.android.architecture.data.PizzaFinderRepository

object ServiceLocator {

    private val lock = Any()
    @Volatile
    var pizzaFinderRepository: PizzaFinderRepository? = null
        @VisibleForTesting set

    fun providePizzaFinderRepository(context: Context): PizzaFinderRepository {
        synchronized(this) {
            return pizzaFinderRepository ?: pizzaFinderRepository ?: createPizzaFinderRepository(context)
        }
    }

    private fun createPizzaFinderRepository(context: Context): PizzaFinderRepository {
        return DefaultPizzaFinderRepository()
    }
}