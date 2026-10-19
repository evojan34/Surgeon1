package com.surgicalboardmaster.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.surgicalboardmaster.app.data.local.dao.EducationalLessonDao
import com.surgicalboardmaster.app.data.local.dao.FlashcardDao
import com.surgicalboardmaster.app.data.local.entity.LessonEntity
import com.surgicalboardmaster.app.data.local.entity.FlashcardEntity

@Database(
    entities = [
        LessonEntity::class,
        FlashcardEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class SbmDatabase : RoomDatabase() {
    abstract fun educationalLessonDao(): EducationalLessonDao
    abstract fun flashcardDao(): FlashcardDao
}
