package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tools.API
import tools.FileManager
import tools.WeatherAdapter
import tools.WeatherDetailBottomSheetFragment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity()
{
    // для работы с апи и определением геолокации
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // ввод названия города
    private lateinit var searchCityEditText: EditText
    // кнопка поиска города
    private lateinit var getLocationButton: Button
    // кнопка геолокации
    private lateinit var locationButton: ImageButton
    // лейаут с погодой
    private lateinit var weatherLayout: LinearLayout
    // текст вью города
    private lateinit var cityTextView: TextView
    // текущий город
    private var currentCity: String = ""
    // текст вью температуры
    private lateinit var degreesTextView: TextView
    // картинка погоды
    private lateinit var weatherImageView: ImageView
    // текст вью даты
    private lateinit var dateTextView: TextView
    // горизонтальный лист карточек
    private lateinit var recyclerView: RecyclerView

    // файл менеджер для получения api
    private var FileManager: FileManager = FileManager(this)
    // апишка
    private lateinit var api: API

    // хз для чего
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        // создаем класс апи куда сразу передаем ключ
        api = API(FileManager.GetApi())

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // находимм ввод текста
        searchCityEditText = findViewById(R.id.searchCity)

        // находим кнопку поиска города
        getLocationButton = findViewById(R.id.getLocation)
        getLocationButton.setOnClickListener()
        {
            updateInformation(searchCityEditText.text.toString())
        }

        // находим кнопку по айди
        locationButton = findViewById(R.id.get_location)
        // добавляем обработчик получения локации
        locationButton.setOnClickListener() {
            // метод проверки есть ли интернет
            if (isInternetAvailable())
            {
                // метод проверки есть ли разрешение на получение геолокации
                checkLocationPermission()
            }
            else
            {
                Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show()
            }
        }
        // находим по айди лейаут погоды
        weatherLayout = findViewById(R.id.showWeather)

        // находим текст по айди
        cityTextView = findViewById(R.id.city)
        // находим по айди текст градусов
        degreesTextView = findViewById(R.id.degrees)
        // находим картинку погоды
        weatherImageView = findViewById(R.id.weather_image)
        // находим текст даты
        dateTextView = findViewById(R.id.dateTextView)
        // находим наш горизонтальный лист
        recyclerView = findViewById(R.id.weatherRecyclerView)

        // тема для локации
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkcheck(name: String)
    {
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show()
    }


    // Метод проверки наличия интернета
    private fun isInternetAvailable(): Boolean
    {
        // штука для проверки состояния сети
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // сохраняем инфу о сети
        val network = connectivityManager.activeNetwork
        // получаем данные по этой сети
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        // если данные есть и есть возможность выхода в сеть через вайфай или мобильный инет
        return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }


    // метод проверки есть ли разрешение на получение геолокации
    private fun checkLocationPermission()
    {
        // проверка есть ли права
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            // если есть
            getLocation()
        }
        else
        {
            // если нет - запрашиваем
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // метод получения данных погоды и обновления UI
    private fun updateInformation(city: String? = null, latitude: Double? = null, longitude: Double? = null, units: String = "metric", lang: String = "ru")
    {
        // Проверка входных данных
        if ((city.isNullOrEmpty() || city == "Город не найден") && (latitude == null || longitude == null)) {
            weatherLayout.visibility = View.INVISIBLE
            recyclerView.visibility = View.INVISIBLE
            return
        }

        // Запуск фонового потока
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Вызов API
                val weatherData = if (city != null) { api.getWeatherData(city, units = units, lang = lang) }
                else { api.getWeatherData(latitude = latitude!!, longitude = longitude!!, units = units, lang = lang) }
                    withContext(Dispatchers.Main)
                    {
                        // выводим данные на главную карточку
                        val currentWeather = weatherData[0]
                        currentCity = city!!
                        cityTextView.text = currentCity
                        degreesTextView.text = currentWeather.temperature.toString()

                        // для норм даты
                        val date = LocalDate.parse(currentWeather.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        dateTextView.text = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))

                        // загружаем картинку нужную погоды
                        val imageUrl = "https://openweathermap.org/img/wn/${currentWeather.icon}@2x.png"
                        // загружаем
                        Glide.with(this@MainActivity).load(imageUrl).into(weatherImageView)

                        // добавляем вывод подробных данных при нажатии
                        weatherLayout.setOnClickListener(){
                            // создаем эту менюшку куда передаем данные
                            val fragment = WeatherDetailBottomSheetFragment.newInstance(currentWeather.feelsLike, currentWeather.humidity, currentWeather.windSpeed, currentWeather.weatherCondition, currentWeather.maxTemperature, currentWeather.minTemperature)
                            // показываем
                            fragment.show(supportFragmentManager, fragment.tag)
                        }

                        // теперь создаем карточки в списке
                        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                        recyclerView.adapter = WeatherAdapter(weatherData.subList(1, weatherData.count() - 1), this@MainActivity)

                        // отображаем все это если не видно
                        if (weatherLayout.visibility == View.INVISIBLE)
                        {
                            weatherLayout.visibility = View.VISIBLE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
            }
            catch (e: Exception)
            {
                withContext(Dispatchers.Main)
                {
                    // напишем что город не найден и скроем все
                    currentCity = "Город не найден"
                    cityTextView.text = currentCity
                    weatherLayout.visibility = View.INVISIBLE
                    recyclerView.visibility = View.INVISIBLE
                    Toast.makeText(this@MainActivity, "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // метод обработки диалоговых окон
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // если запрос - запрос на геолокацию
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            // если ответ положительный
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                getLocation()
            }
            // если отрицательный
            else
            {
                cityTextView.text = "Требуется разрешение на доступ к геолокации"
            }
        }
    }

    // метод получения локации по гпс
    private fun getLocation()
    {
        try {
            // получаем ласт локацию
            // ошибка потому что студия не видит проверки на разрешение
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                // если локация есть
                if (location != null)
                {
                    // штука для преобразования широты и долготы в адрес
                    val geocoder = Geocoder(this, Locale.getDefault())
                    // получаем возможные адреса благодаря штуке, устанавливаем результат 1 чтобы получить ток 1 адрес
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    // если переменная не пустая
                    if (addresses != null && addresses.isNotEmpty())
                    {
                        // получаем название
                        var cityName = addresses[0].locality
                        // проверяем есть ли что-то внутри названия города
                        cityName = cityName ?: "Город не найден"
                        // если город найден/ не найден
                        updateInformation(cityName, location.latitude, location.longitude)
                    }
                }
                // если последней локации нет
                else
                {
                    cityTextView.text = "Не удалось определить местоположение"
                }
            }.addOnFailureListener {
                // если произошла какая-то ошибка во время получения локации
                cityTextView.text = "Ошибка получения местоположения"
            }
        }
        catch (e: Exception)
        {
            // если произошла какая-то ошибка, то показываем всплывающее окно
            Toast.makeText(this, "Ошибка: $e", Toast.LENGTH_SHORT).show();
        }
    }
}