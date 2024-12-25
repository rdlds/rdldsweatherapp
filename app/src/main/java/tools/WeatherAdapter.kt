package tools

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// класс для работы с карточками внутри RecycleView
// благодаря нему мы можем заполнять данными карточки
// получаем список данных погоды и контекст для изображения
class WeatherAdapter(private val weatherDataList: List<weatherData>, private val context: Context) : RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>()
{
    // метод где мы пишем что именно создаем(какую активность тип)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.weather_cards, parent, false)
        return WeatherViewHolder(itemView)
    }

    // вот тут заполняем данные
    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int)
    {
        // получаем дату по позиции
        val weatherData = weatherDataList[position]
        holder.degreesTextView.text = weatherData.temperature.toString()

        // для красивой даты
        val date = LocalDate.parse(weatherData.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        holder.dateTextView.text  = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))

        // загружаем картинку нужную погоды
        val imageUrl = "https://openweathermap.org/img/wn/${weatherData.icon}@2x.png"
        Glide.with(context).load(imageUrl).into(holder.weatherImageView)

        // добавляем вывод подробных данных при нажатии
        holder.cardView.setOnClickListener(){
            // создаем эту менюшку куда передаем данные
            val fragment = WeatherDetailBottomSheetFragment.newInstance(weatherData.feelsLike, weatherData.humidity, weatherData.windSpeed, weatherData.weatherCondition, weatherData.maxTemperature, weatherData.minTemperature)
            // показываем
            fragment.show((context as AppCompatActivity).supportFragmentManager, fragment.tag)
        }

    }

    override fun getItemCount(): Int
    {
        return weatherDataList.size
    }

    // инициализируем наши ui элементы в ссылки на них
    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val degreesTextView: TextView = itemView.findViewById(R.id.degrees)
        val weatherImageView: ImageView = itemView.findViewById(R.id.weather_image)
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val cardView: CardView = itemView.findViewById(R.id.card)
    }
}