package com.example.bayan.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTagsList(tags: List<String>?): String {
        return tags?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toTagsList(tagsString: String?): List<String> {
        if (tagsString.isNullOrBlank()) return emptyList()
        return tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
