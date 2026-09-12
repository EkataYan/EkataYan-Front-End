package com.ekatayan.app.data.repository

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
        val type = weatherTypeFor(data.condition, data.icon)
        return WeatherInfo(data.location, ((data.minCelsius + data.maxCelsius) / 2).toInt(), data.condition,
            data.humidity ?: 0, type, data.windKph, data.sunrise, data.minCelsius, data.maxCelsius,
            data.rainChance)
    }
}

internal fun weatherTypeFor(condition: String?, iconCode: String?): WeatherType {
    if (iconCode?.trim()?.lowercase()?.endsWith('n') == true) return WeatherType.NIGHT

    val normalizedCondition = condition?.trim()?.lowercase().orEmpty()
    if (normalizedCondition.isEmpty()) return WeatherType.DEFAULT

    return when {
        "thunderstorm" in normalizedCondition || "thunder" in normalizedCondition || "storm" in normalizedCondition ->
            WeatherType.THUNDERSTORM
        "drizzle" in normalizedCondition -> WeatherType.DRIZZLE
        "rain" in normalizedCondition || "shower" in normalizedCondition -> WeatherType.RAIN
        "mist" in normalizedCondition || "fog" in normalizedCondition || "haze" in normalizedCondition ->
            WeatherType.MIST
        "cloud" in normalizedCondition || "overcast" in normalizedCondition -> WeatherType.CLOUDY
        "clear" in normalizedCondition || "sunny" in normalizedCondition -> WeatherType.CLEAR
        else -> WeatherType.DEFAULT
    }
}
