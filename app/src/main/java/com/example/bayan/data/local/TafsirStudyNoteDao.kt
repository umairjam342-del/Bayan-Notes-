package com.example.bayan.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for storing and retrieving Tafsir study notes locally with Room.
 */
@Dao
interface TafsirStudyNoteDao {

    @Query("SELECT * FROM tafsir_study_notes ORDER BY updated_at DESC")
    fun getAllTafsirNotes(): Flow<List<TafsirStudyNoteEntity>>

    @Query("SELECT * FROM tafsir_study_notes WHERE id = :id")
    fun getTafsirNoteById(id: Long): Flow<TafsirStudyNoteEntity?>

    @Query("SELECT * FROM tafsir_study_notes WHERE surah_number = :surahNumber ORDER BY ayah_start ASC")
    fun getNotesBySurah(surahNumber: Int): Flow<List<TafsirStudyNoteEntity>>

    @Query("SELECT * FROM tafsir_study_notes WHERE surah_number = :surahNumber AND :ayah BETWEEN ayah_start AND ayah_end")
    fun getNotesForAyah(surahNumber: Int, ayah: Int): Flow<List<TafsirStudyNoteEntity>>

    @Query("SELECT * FROM tafsir_study_notes WHERE tafsir_source = :source ORDER BY updated_at DESC")
    fun getNotesBySource(source: String): Flow<List<TafsirStudyNoteEntity>>

    @Query("SELECT * FROM tafsir_study_notes WHERE is_favorite = 1 ORDER BY updated_at DESC")
    fun getFavoriteNotes(): Flow<List<TafsirStudyNoteEntity>>

    @Query("""
        SELECT * FROM tafsir_study_notes 
        WHERE title LIKE '%' || :query || '%' 
           OR classical_exegesis LIKE '%' || :query || '%' 
           OR personal_reflection LIKE '%' || :query || '%'
           OR linguistic_breakdown LIKE '%' || :query || '%'
           OR arabic_text LIKE '%' || :query || '%'
           OR translation_text LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
        ORDER BY updated_at DESC
    """)
    fun searchTafsirNotes(query: String): Flow<List<TafsirStudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafsirNote(note: TafsirStudyNoteEntity): Long

    @Update
    suspend fun updateTafsirNote(note: TafsirStudyNoteEntity)

    @Query("UPDATE tafsir_study_notes SET is_favorite = :isFavorite, updated_at = :timestamp WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM tafsir_study_notes WHERE id = :id")
    suspend fun deleteTafsirNoteById(id: Long)

    @Delete
    suspend fun deleteTafsirNote(note: TafsirStudyNoteEntity)
}
