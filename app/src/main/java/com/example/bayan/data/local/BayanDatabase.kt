package com.example.bayan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [NoteEntity::class, TafsirStudyNoteEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BayanDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun tafsirStudyNoteDao(): TafsirStudyNoteDao

    companion object {
        @Volatile
        private var INSTANCE: BayanDatabase? = null

        fun getDatabase(context: Context): BayanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BayanDatabase::class.java,
                    "bayan_study.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
