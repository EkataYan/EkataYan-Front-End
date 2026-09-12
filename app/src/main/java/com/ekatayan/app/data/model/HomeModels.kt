package com.ekatayan.app.data.model

import androidx.annotation.DrawableRes

data class User(
    val name: String,
    @param:DrawableRes val profileImageRes: Int?,
)

data class UpcomingTrip(
    val id: Int,
    val destination: String,
    val date: String,
    val duration: String,
    @param:DrawableRes val imageRes: Int,
)

enum class WeatherType {
    CLEAR,
    CLOUDY,
    RAIN,
    DRIZZLE,
    THUNDERSTORM,
    MIST,
    NIGHT,
    DEFAULT,
}

data class WeatherInfo(
    val location: String,
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val weatherType: WeatherType,
    val windKph: Double? = null,
    val sunrise: String? = null,
    val minCelsius: Double? = null,
    val maxCelsius: Double? = null,
    val rainChance: Int? = null,
)
