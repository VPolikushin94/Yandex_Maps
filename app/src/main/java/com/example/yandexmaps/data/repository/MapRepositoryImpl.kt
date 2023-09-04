package com.example.yandexmaps.data.repository

import com.example.yandexmaps.domain.model.Place
import com.example.yandexmaps.domain.repository.MapRepository
import com.yandex.mapkit.geometry.Point

class MapRepositoryImpl : MapRepository {

    private val mockPlaceList = listOf(
        Place(
            0,
            "0",
            20,
            Point(55.080650, 38.801548)
        ),
        Place(
            1,
            "1",
            40,
            Point(55.080848, 38.804435)
        ),
        Place(
            2,
            "2",
            60,
            Point(55.079087, 38.805096)
        ),
        Place(
            3,
            "3",
            90,
            Point(55.078629, 38.801563)
        )
    )

    override fun getPlaces(): List<Place> {
        return mockPlaceList
    }
}