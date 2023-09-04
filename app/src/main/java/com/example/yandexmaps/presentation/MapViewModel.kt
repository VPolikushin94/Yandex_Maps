package com.example.yandexmaps.presentation

import androidx.lifecycle.ViewModel
import com.example.yandexmaps.data.repository.MapRepositoryImpl
import com.example.yandexmaps.domain.useCase.GetPlaceUseCase
import java.lang.RuntimeException

class MapViewModel : ViewModel() {
    private val repository = MapRepositoryImpl()

    private val getPlacesUseCase = GetPlaceUseCase(repository)

    val placeList = getPlacesUseCase()

    fun getChargeLvl(charge: Int): Int {
        return when(charge) {
            in 0..30 -> MainActivity.DISCHARGED
            in 31..75 -> MainActivity.CHARGED_HALF
            in 76..100 -> MainActivity.CHARGED
            else -> throw RuntimeException("Charge level out of bounds")
        }
    }
}