package tools

import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import org.json.JSONObject
import kotlin.math.roundToInt

// класс для работы с api ( при создании получаем ключик)
class API(private val apiKey: String)
{
    // метод получения списка данных погоды на 5 дней(включая этот)
    fun getWeatherData(City: String? = null, latitude: Double = 0.0, longitude: Double = 0.0, units: String = "metric", lang: String = "ru"): List<weatherData>
    {
        // переменная для запроса
        var urlString = ""
        // если человек хочет получить погоду по введенному городу
        if (City != null)
        {
            urlString = "https://api.openweathermap.org/data/2.5/forecast?q=$City&appid=$apiKey&units=$units&lang=$lang"
        }
        // если по геолокации своей
        else
        {
            urlString = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=$units&lang=$lang&appid=$apiKey"
        }

        // создаем из этого запрос
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        // пытаемся получиться ответ
        try
        {
            connection.requestMethod = "GET"
            connection.connect()

            // Проверяем код ответа
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Ошибка: ${connection.responseCode}")
            }

            // Читаем ответ
            val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val jsonResponse = JSONObject(response)

            // Распарсим JSON и вернем объект weatherData
            return parseWeatherData(jsonResponse)
        }
        finally
        {
            connection.disconnect()
        }
    }

    // метод обработки запроса
    private fun parseWeatherData(json: JSONObject): List<weatherData>
    {
        // создаем лист куда будем записывать данные дня
        val dailyWeatherDataList = mutableListOf<weatherData>()
        // получаем список дней
        val list = json.getJSONArray("list")
        // текущий день
        var currentDate = list.getJSONObject(0).getString("dt_txt").split(' ')[0]

        // так как данные в запросе по часам а мне нужны данные за день то я не придумал ничего лучше
        // кроме как собирать эти данные за день и после работать с ними
        // к примеру собирать все температуру и потом ее в среднюю перевести
        // находить самую минимальную температуру
        // выбирать состояние погоды из тех что чаще всего была за время
        // переменные для этого
        var temperatureSum = 0.0
        var feelsLikeSum = 0.0
        var humiditySum = 0
        var windSpeedSum = 0.0
        var minTemperature = Double.MAX_VALUE
        var maxTemperature = Double.MIN_VALUE
        var weatherConditions = mutableListOf<String>()
        var icons = mutableListOf<String>()
        // а еще человек может сделать запрос днем а не ночью из-за чего список который придет будет начинаться не
        // 01.01.2025 00:00
        // а 01.01.2025 12:00
        // из-за чего для подсчета средних значений надо ввести счетчики данных чтобы потом делить на них
        var avgTempCount = 0
        var avgFeelsLikeCount = 0
        var avgHumidityCount = 0
        var avgWindSpeedCount = 0

        // обходим список дней
        for (i in 0 until list.length())
        {
            // получаем список по индексу
            val currentJson = list.getJSONObject(i)
            // получаем оттуда основные данные
            val main = currentJson.getJSONObject("main")
            val wind = currentJson.getJSONObject("wind")
            val weather = currentJson.getJSONArray("weather").getJSONObject(0)

            // получаем нужные данные
            val temperature = main.getDouble("temp")
            val feelsLike = main.getDouble("feels_like")
            val minTemp = main.getDouble("temp_min")
            val maxTemp = main.getDouble("temp_max")
            val humidity = main.getInt("humidity")
            val windSpeed = wind.getDouble("speed")
            val weatherCond = weather.getString("description")
            val icon = weather.getString("icon")

            // получаем дату этих данных
            val date = currentJson.getString("dt_txt").split(' ')[0]

            // если текущая дата не равна той что сейчас значит перешли на новый день -> записываем все в данные текущего дня и в список
            if (date != currentDate)
            {
                // группируем и высчитываем нужные данные
                val avgTemperature = if (avgTempCount > 0) temperatureSum / avgTempCount else 0.0
                val avgFeelsLike = if (avgFeelsLikeCount > 0) feelsLikeSum / avgFeelsLikeCount else 0.0
                val avgHumidity = if (avgHumidityCount > 0) humiditySum / avgHumidityCount else 0
                val avgWindSpeed = if (avgWindSpeedCount > 0) windSpeedSum / avgWindSpeedCount else 0.0
                val mostCommonCondition = weatherConditions.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "N/A"
                val mostCommonIcon = icons.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "01d"

                // Создаем объект дату текущего дня
                // дробные числа я округляю до 2 чисел после запятой
                val dailyWeather = weatherData(
                    temperature = (avgTemperature * 100).roundToInt() / 100.0,
                    feelsLike = (avgFeelsLike * 100).roundToInt() / 100.0,
                    humidity = avgHumidity,
                    windSpeed = (avgWindSpeed * 100).roundToInt() / 100.0,
                    weatherCondition = mostCommonCondition,
                    maxTemperature = maxTemperature,
                    minTemperature = minTemperature,
                    icon = mostCommonIcon,
                    date = date
                )

                // добавляем в список
                dailyWeatherDataList.add(dailyWeather)

                // Сбросим данные для следующего дня
                temperatureSum = 0.0
                feelsLikeSum = 0.0
                humiditySum = 0
                windSpeedSum = 0.0
                minTemperature = Double.MAX_VALUE
                maxTemperature = Double.MIN_VALUE
                weatherConditions.clear()
                icons.clear()
                avgTempCount = 0
                avgFeelsLikeCount = 0
                avgHumidityCount = 0
                avgWindSpeedCount = 0

                currentDate = date
            }

            // Обрабатываем данные текущие
            temperatureSum += temperature
            feelsLikeSum += feelsLike
            humiditySum += humidity
            windSpeedSum += windSpeed
            minTemperature = minOf(minTemperature, minTemp)
            maxTemperature = maxOf(maxTemperature, maxTemp)
            weatherConditions.add(weatherCond)
            icons.add(icon)

            // увеличиваем счетчик для высчета среднего значения
            avgTempCount++
            avgFeelsLikeCount++
            avgHumidityCount++
            avgWindSpeedCount++
        }
        // так как последний день в цикле не обрабатывается, то мы обработаем его тут
        val avgTemperature = if (avgTempCount > 0) temperatureSum / avgTempCount else 0.0
        val avgFeelsLike = if (avgFeelsLikeCount > 0) feelsLikeSum / avgFeelsLikeCount else 0.0
        val avgHumidity = if (avgHumidityCount > 0) humiditySum / avgHumidityCount else 0
        val avgWindSpeed = if (avgWindSpeedCount > 0) windSpeedSum / avgWindSpeedCount else 0.0
        val mostCommonCondition = weatherConditions.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "N/A"
        val mostCommonIcon = icons.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "01d"

        // лепим из всего этого данные последнего дня
        val dailyWeather = weatherData(
            temperature = (avgTemperature * 100).roundToInt() / 100.0,
            feelsLike = (avgFeelsLike * 100).roundToInt() / 100.0,
            humidity = avgHumidity,
            windSpeed = (avgWindSpeed * 100).roundToInt() / 100.0,
            weatherCondition = mostCommonCondition,
            maxTemperature = maxTemperature,
            minTemperature = minTemperature,
            icon = mostCommonIcon,
            date = currentDate
        )

        // добавляем в спосик
        dailyWeatherDataList.add(dailyWeather)

        // и возвращаем его
        return dailyWeatherDataList

    }
}