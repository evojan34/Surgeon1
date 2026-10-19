package com.surgicalboardmaster.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.surgicalboardmaster.app.data.local.dao.ClinicalCasesDao
import com.surgicalboardmaster.app.data.local.dao.EducationalLessonDao
import com.surgicalboardmaster.app.data.local.dao.ExamDao
import com.surgicalboardmaster.app.data.local.dao.FlashcardDao
import com.surgicalboardmaster.app.data.local.dao.SurgicalLibraryDao
import com.surgicalboardmaster.app.data.local.entity.BookEntity
import com.surgicalboardmaster.app.data.local.entity.CaseTimelineEventEntity
import com.surgicalboardmaster.app.data.local.entity.ChapterEntity
import com.surgicalboardmaster.app.data.local.entity.ClinicalCaseEntity
import com.surgicalboardmaster.app.data.local.entity.ExamAttemptEntity
import com.surgicalboardmaster.app.data.local.entity.ExamQuestionEntity
import com.surgicalboardmaster.app.data.local.entity.FlashcardEntity
import com.surgicalboardmaster.app.data.local.entity.FollowUpTaskEntity
import com.surgicalboardmaster.app.data.local.entity.LessonEntity
import com.surgicalboardmaster.app.data.local.entity.TopicEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        TopicEntity::class,
        LessonEntity::class,
        ExamQuestionEntity::class,
        ExamAttemptEntity::class,
        FlashcardEntity::class,
        ClinicalCaseEntity::class,
        CaseTimelineEventEntity::class,
        FollowUpTaskEntity::class
    ],
    version = 5, // تم رفع الإصدار لإجبار إعادة البناء
    exportSchema = false
)
abstract class SbmDatabase : RoomDatabase() {
    abstract fun surgicalLibraryDao(): SurgicalLibraryDao
    abstract fun educationalLessonDao(): EducationalLessonDao
    abstract fun examDao(): ExamDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun clinicalCasesDao(): ClinicalCasesDao
}
