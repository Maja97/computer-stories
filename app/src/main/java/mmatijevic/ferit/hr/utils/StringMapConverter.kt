package mmatijevic.ferit.hr.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object StringMapConverter {
    @JvmStatic
    @TypeConverter
    fun fromString(value: String?): Map<String, String> {
        val mapType: Type = object : TypeToken<Map<String?, String?>?>() {}.type
        return Gson().fromJson(value, mapType)
    }

    @JvmStatic
    @TypeConverter
    fun fromStringMap(map: Map<String?, String?>?): String {
        val gson = Gson()
        return gson.toJson(map)
    }
}