package com.example.bayan.data.repository

import com.example.bayan.data.local.TafsirStudyNoteDao
import com.example.bayan.data.local.TafsirStudyNoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing a clean abstraction for local on-device Tafsir study note operations.
 */
class TafsirStudyNoteRepository(
    private val tafsirStudyNoteDao: TafsirStudyNoteDao
) {
    val allTafsirNotes: Flow<List<TafsirStudyNoteEntity>> = tafsirStudyNoteDao.getAllTafsirNotes()

    fun getTafsirNoteById(id: Long): Flow<TafsirStudyNoteEntity?> =
        tafsirStudyNoteDao.getTafsirNoteById(id)

    fun getNotesBySurah(surahNumber: Int): Flow<List<TafsirStudyNoteEntity>> =
        tafsirStudyNoteDao.getNotesBySurah(surahNumber)

    fun getNotesForAyah(surahNumber: Int, ayah: Int): Flow<List<TafsirStudyNoteEntity>> =
        tafsirStudyNoteDao.getNotesForAyah(surahNumber, ayah)

    fun getFavoriteNotes(): Flow<List<TafsirStudyNoteEntity>> =
        tafsirStudyNoteDao.getFavoriteNotes()

    fun searchTafsirNotes(query: String): Flow<List<TafsirStudyNoteEntity>> =
        tafsirStudyNoteDao.searchTafsirNotes(query)

    suspend fun insertTafsirNote(note: TafsirStudyNoteEntity): Long =
        tafsirStudyNoteDao.insertTafsirNote(note)

    suspend fun updateTafsirNote(note: TafsirStudyNoteEntity) =
        tafsirStudyNoteDao.updateTafsirNote(note)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) =
        tafsirStudyNoteDao.setFavorite(id, isFavorite)

    suspend fun deleteTafsirNoteById(id: Long) =
        tafsirStudyNoteDao.deleteTafsirNoteById(id)

    suspend fun deleteTafsirNote(note: TafsirStudyNoteEntity) =
        tafsirStudyNoteDao.deleteTafsirNote(note)
}
