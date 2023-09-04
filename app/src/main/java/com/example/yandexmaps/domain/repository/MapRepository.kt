package com.example.yandexmaps.domain.repository

import com.example.yandexmaps.domain.model.Place

interface MapRepository {
    fun getPlaces(): List<Place>
}