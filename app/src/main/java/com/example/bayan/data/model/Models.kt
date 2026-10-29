package com.example.bayan.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Note(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "tafsir", // tafsir, tadabbur, linguistics, theology, asbab_nuzul, general
    val surahNumber: Long = 0L,
    val ayahStart: Long = 0L,
    val ayahEnd: Long = 0L,
    val tags: List<String> = emptyList(),
    val arabicSnippet: String = "",
    val translationSnippet: String = "",
    val mindMapId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "title" to title,
            "content" to content,
            "category" to category,
            "surahNumber" to surahNumber,
            "ayahStart" to ayahStart,
            "ayahEnd" to ayahEnd,
            "tags" to tags,
            "arabicSnippet" to arabicSnippet,
            "translationSnippet" to translationSnippet,
            "mindMapId" to mindMapId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }

    fun toUpdateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "title" to title,
            "content" to content,
            "category" to category,
            "surahNumber" to surahNumber,
            "ayahStart" to ayahStart,
            "ayahEnd" to ayahEnd,
            "tags" to tags,
            "arabicSnippet" to arabicSnippet,
            "translationSnippet" to translationSnippet,
            "mindMapId" to mindMapId,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class MindMap(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val surahNumber: Long = 0L,
    val nodesJson: String = "[]",
    val edgesJson: String = "[]",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "title" to title,
            "description" to description,
            "surahNumber" to surahNumber,
            "nodesJson" to nodesJson,
            "edgesJson" to edgesJson,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }

    fun toUpdateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "title" to title,
            "description" to description,
            "surahNumber" to surahNumber,
            "nodesJson" to nodesJson,
            "edgesJson" to edgesJson,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }
}

@IgnoreExtraProperties
data class Bookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val surahNumber: Long = 1L,
    val ayahNumber: Long = 1L,
    val surahName: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "surahNumber" to surahNumber,
            "ayahNumber" to ayahNumber,
            "surahName" to surahName,
            "note" to note,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return map.filterValues { it != null }
    }

    fun toUpdateMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "surahNumber" to surahNumber,
            "ayahNumber" to ayahNumber,
            "surahName" to surahName,
            "note" to note
        )
        return map.filterValues { it != null }
    }
}

// Data models for interactive Mind Map nodes and edges
data class MapNode(
    val id: String,
    val label: String,
    val arabicText: String = "",
    val category: String = "Core Theme", // Core Theme, Prophetic Story, Legal Ruling, Theological Gem, Linguistic Insight
    val colorHex: String = "#0D5C3A", // Rich Quranic emerald green default
    val posX: Float = 0f,
    val posY: Float = 0f,
    val notes: String = ""
)

data class MapEdge(
    val id: String,
    val fromNodeId: String,
    val toNodeId: String,
    val label: String = "Connects to"
)

enum class SurahOrderMode {
    MUSHAF,
    REVELATION
}

// Quran Data Models
data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameTransliterated: String,
    val nameEnglish: String,
    val versesCount: Int,
    val revelationType: String, // Meccan or Medinan
    val revelationOrder: Int = 1
)

data class Ayah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val translation: String,
    val transliteration: String = "",
    val tafsirExcerpt: String = ""
)
