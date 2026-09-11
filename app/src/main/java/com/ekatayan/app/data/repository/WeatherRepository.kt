package com.ekatayan.app.data.repository

import com.ekatayan.app.R
import com.ekatayan.app.data.model.WeatherInfo
import com.ekatayan.app.data.model.WeatherType
import com.ekatayan.app.data.remote.api.EkataYanApiService
import java.time.LocalDate
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val api: EkataYanApiService) {
    suspend fun forecast(location: String): WeatherInfo {
        val response = api.weather(location.trim(), LocalDate.now().toString())
        val data = response.data
        if (!response.success || data == null) throw IllegalStateException(response.error?.message ?: "Weather is unavailable.")
        val text = data.condition.lowercase()
        val type = when {
            "thunder" in text || "storm" in text -> WeatherType.STORMY
            "rain" in text || "drizzle" in text || "shower" in text -> WeatherType.RAINY
            "cloud" in text || "overcast" in text || "mist" in text || "fog" in text -> WeatherType.CLOUDY
            else -> WeatherType.SUNNY
        }
        val image = when {
            data.location.contains("kandy", true) -> R.drawable.kandy
            data.location.contains("galle", true) -> R.drawable.galle
            data.location.contains("sigiriya", true) -> R.drawable.sigiriya
            else -> R.drawable.colombo
        }
        return WeatherInfo(data.location, ((data.minCelsius + data.maxCelsius) / 2).toInt(), data.condition,
            data.humidity ?: 0, type, image, data.windKph, data.sunrise, data.minCelsius, data.maxCelsius,
            data.rainChance)
    }
}
