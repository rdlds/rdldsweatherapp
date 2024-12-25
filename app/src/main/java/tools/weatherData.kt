package tools

// класс для хранения данных погоды
// температура,
// ощущается как,
// влажность,
// скорость ветра,
// состояние погоды,
// макс. температура
// мин. температура
// иконка
// дата
data class weatherData
    (
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCondition: String,
    val maxTemperature: Double,
    val minTemperature: Double,
    val icon: String,
    val date: String
)