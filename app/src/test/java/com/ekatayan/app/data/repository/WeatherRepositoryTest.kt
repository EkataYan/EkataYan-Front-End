package com.ekatayan.app.data.repository

import com.ekatayan.app.data.model.WeatherType
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherRepositoryTest {
    @Test
    fun `night icon takes precedence over condition`() {
        assertEquals(WeatherType.NIGHT, weatherTypeFor("Clear", "01n"))
        assertEquals(WeatherType.NIGHT, weatherTypeFor("Thunderstorm", "11N"))
    }

    @Test
    fun `known OpenWeather conditions map to matching types`() {
        assertEquals(WeatherType.CLEAR, weatherTypeFor("Clear sky", "01d"))
        assertEquals(WeatherType.CLOUDY, weatherTypeFor("scattered clouds", "03d"))
        assertEquals(WeatherType.RAIN, weatherTypeFor("Rain", "10d"))
        assertEquals(WeatherType.DRIZZLE, weatherTypeFor("light drizzle", "09d"))
        assertEquals(WeatherType.THUNDERSTORM, weatherTypeFor("Thunderstorm", "11d"))
        assertEquals(WeatherType.MIST, weatherTypeFor("Mist", "50d"))
        assertEquals(WeatherType.MIST, weatherTypeFor("Fog", null))
        assertEquals(WeatherType.MIST, weatherTypeFor("Haze", "50d"))
    }

    @Test
    fun `missing or unknown conditions use default`() {
        assertEquals(WeatherType.DEFAULT, weatherTypeFor(null, null))
        assertEquals(WeatherType.DEFAULT, weatherTypeFor("  ", "bad-code"))
        assertEquals(WeatherType.DEFAULT, weatherTypeFor("Volcanic ash", "50d"))
    }
}
