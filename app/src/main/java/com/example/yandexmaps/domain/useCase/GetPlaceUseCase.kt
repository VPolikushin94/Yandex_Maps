package com.example.yandexmaps.domain.useCase

import com.example.yandexmaps.domain.model.Place
import com.example.yandexmaps.domain.repository.MapRepository

class GetPlaceUseCase(private val mapRepository: MapRepository) {
    operator fun invoke(): List<Place> {
        return mapRepository.getPlaces()
    }
}