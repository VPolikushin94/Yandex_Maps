package com.example.yandexmaps.domain.model

import com.yandex.mapkit.geometry.Point

data class Place(
    val id: Int,
    val name: String,
    val charge: Int,
    val address: Point
)
