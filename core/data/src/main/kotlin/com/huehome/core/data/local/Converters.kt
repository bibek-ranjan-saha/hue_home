package com.huehome.core.data.local

import androidx.room.TypeConverter

/**
 * Type converters for Room database
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
}
