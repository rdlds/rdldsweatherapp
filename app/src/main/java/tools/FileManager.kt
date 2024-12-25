package tools

import android.content.Context
import android.util.Log
import org.json.JSONObject

// метод для работы с файлами json (получаем контекст чтобы
class FileManager(private val context: Context)
{
    // путь к файлу
    private var api_source = "api.json"

    // метод получения значения по ключу
    // в нашем случае по ключу api
    fun GetApi(): String
    {
        // получаем наш json
        val jsonString = loadJSONFromAssets(api_source)
        // обрабатываем данные и пытаемся вернуть значение по ключу
        jsonString?.let {
            try {
                val jsonObject = JSONObject(it)
                return jsonObject.optString("api")
            } catch (e: Exception)
            {
                Log.e("123123123123", "ошибка чтения json", e)
            }
        }
        Log.e("123123123123", "ошибка загружки json")
        return ""
    }

    // метод получения json файла
    private fun loadJSONFromAssets(fileName: String): String
    {
        // читаем файл и возвращаем его
        return try
        {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        }
        catch (ex: Exception)
        {
            Log.e("123123123123", "ошибка чтения json: $fileName", ex)
            return "";
        }
    }
}