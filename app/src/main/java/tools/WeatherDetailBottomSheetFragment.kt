package tools

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.weatherapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// класс для всплывающего окошка снизу вверх
class WeatherDetailBottomSheetFragment : BottomSheetDialogFragment()
{
    // создаем переменные для хранения данных
    private var feelsLike: Double = 0.0
    private var humidity: Int = 0
    private var windSpeed: Double = 0.0
    private var weatherCondition: String = ""
    private var maxTemperature: Double = 0.0
    private var minTemperature: Double = 0.0

    // хз что это, взял с инета
    companion object
    {
        // создаем переменные где хранить будем все наши данные
        private const val feels_like_key = "feels_like_key"
        private const val humidity_key = "humidity_key"
        private const val wind_speed_key = "wind_speed_key"
        private const val weather_condition_key = "weather_condition_key"
        private const val max_temperature_key = "max_temperature_key"
        private const val min_temperature_key = "min_temperature_key"

        // при создании запрашиваем данные и обрабатываем
        fun newInstance(feelsLike: Double, humidity: Int, windSpeed: Double, weatherCondition: String, maxTemperature: Double, minTemperature: Double): WeatherDetailBottomSheetFragment
        {
            val fragment = WeatherDetailBottomSheetFragment()
            val args = Bundle().apply {
                putDouble(feels_like_key, feelsLike)
                putInt(humidity_key, humidity)
                putDouble(wind_speed_key, windSpeed)
                putString(weather_condition_key, weatherCondition)
                putDouble(max_temperature_key, maxTemperature)
                putDouble(min_temperature_key, minTemperature)
            }
            fragment.arguments = args
            return fragment
        }
    }

    // метод где мы пишем что именно создаем(какую активность тип)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_weather_detail_bottom_sheet, container, false)
    }

    // при создании заполняем данные
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        // извлекаем данные из аргументов если они есть
        arguments?.let {
            feelsLike = it.getDouble(feels_like_key)
            humidity = it.getInt(humidity_key)
            windSpeed = it.getDouble(wind_speed_key)
            weatherCondition = it.getString(weather_condition_key, "")
            maxTemperature = it.getDouble(max_temperature_key)
            minTemperature = it.getDouble(min_temperature_key)
        }

        // устанавливаем данные в текстовые поля
        view.findViewById<TextView>(R.id.feelsLike).text = "${feelsLike}°C"
        view.findViewById<TextView>(R.id.humidity).text = "${humidity}%"
        view.findViewById<TextView>(R.id.windSpeed).text = "${windSpeed} м/с"
        view.findViewById<TextView>(R.id.description).text = weatherCondition
        view.findViewById<TextView>(R.id.maxTemp).text = "${maxTemperature}°C"
        view.findViewById<TextView>(R.id.minTemp).text = "${minTemperature}°C"
    }
}