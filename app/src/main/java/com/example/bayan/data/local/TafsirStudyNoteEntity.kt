package com.example.bayan.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Room Database entity representing a structured classical or contemporary
 * Tafsir & Quranic study note persisted locally on-device.
 */
@Entity(tableName = "tafsir_study_notes")
@TypeConverters(Converters::class)
data class TafsirStudyNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "surah_number")
    val surahNumber: Int,

    @ColumnInfo(name = "surah_name")
    val surahName: String = "",

    @ColumnInfo(name = "ayah_start")
    val ayahStart: Int,

    @ColumnInfo(name = "ayah_end")
    val ayahEnd: Int = ayahStart,

    @ColumnInfo(name = "tafsir_source")
    val tafsirSource: String = "Tafsir Ibn Kathir",

    @ColumnInfo(name = "arabic_text")
    val arabicText: String = "",

    @ColumnInfo(name = "translation_text")
    val translationText: String = "",

    @ColumnInfo(name = "classical_exegesis")
    val classicalExegesis: String = "",

    @ColumnInfo(name = "linguistic_breakdown")
    val linguisticBreakdown: String = "",

    @ColumnInfo(name = "personal_reflection")
    val personalReflection: String = "",

    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
