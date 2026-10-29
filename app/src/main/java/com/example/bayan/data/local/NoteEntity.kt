package com.example.bayan.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    // Quranic study specific metadata fields
    @ColumnInfo(name = "surah_number")
    val surahNumber: Int = 0,

    @ColumnInfo(name = "ayah_start")
    val ayahStart: Int = 0,

    @ColumnInfo(name = "ayah_end")
    val ayahEnd: Int = 0,

    @ColumnInfo(name = "category")
    val category: String = "tafsir", // tafsir, tadabbur, linguistics, theology, asbab_nuzul, general

    @ColumnInfo(name = "arabic_snippet")
    val arabicSnippet: String = "",

    @ColumnInfo(name = "translation_snippet")
    val translationSnippet: String = ""
)
