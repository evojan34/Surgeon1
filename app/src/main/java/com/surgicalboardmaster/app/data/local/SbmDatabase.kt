package com.surgicalboardmaster.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.surgicalboardmaster.app.data.local.dao.SurgicalLibraryDao
import com.surgicalboardmaster.app.data.local.entity.BookEntity
import com.surgicalboardmaster.app.data.local.entity.ChapterEntity
import com.surgicalboardmaster.app.data.local.entity.TopicEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        TopicEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SbmDatabase : RoomDatabase() {
    abstract fun surgicalLibraryDao(): SurgicalLibraryDao
}
