package com.huehome.core.data.local

import androidx.room.TypeConverter
import com.huehome.core.domain.model.ColorInfo
import com.huehome.core.domain.model.LabColor
import org.json.JSONObject

/**
 * Room type converters for complex types
 */
class Converters {
    
    @TypeConverter
    fun fromFloatArray(value: FloatArray?): String? {
        return value?.joinToString(",")
    }
    
    @TypeConverter
    fun toFloatArray(value: String?): FloatArray? {
        return value?.split(",")?.map { it.toFloat() }?.toFloatArray()
    }
    
    @TypeConverter
    fun fromColorInfo(value: ColorInfo?): String? {
        if (value == null) return null
        
        val json = JSONObject()
        json.put("rgb", value.rgb)
        json.put("l", value.lab.l)
        json.put("a", value.lab.a)
        json.put("b", value.lab.b)
        json.put("confidence", value.confidence)
        return json.toString()
    }
    
    @TypeConverter
    fun toColorInfo(value: String?): ColorInfo? {
        if (value == null) return null
        
        return try {
            val json = JSONObject(value)
            val rgbValue = json.getInt("rgb")
            val lab = LabColor(
                l = json.getDouble("l").toFloat(),
                a = json.getDouble("a").toFloat(),
                b = json.getDouble("b").toFloat(),
                rgb = rgbValue
            )
            ColorInfo(
                rgb = rgbValue,
                lab = lab,
                confidence = json.getDouble("confidence").toFloat()
            )
        } catch (e: Exception) {
            null
        }
    }
}
