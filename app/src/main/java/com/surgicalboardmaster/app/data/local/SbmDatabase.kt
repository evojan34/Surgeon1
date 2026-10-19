package com.surgicalboardmaster.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.surgicalboardmaster.app.data.local.dao.ClinicalCasesDao
import com.surgicalboardmaster.app.data.local.dao.EducationalLessonDao
import com.surgicalboardmaster.app.data.local.dao.ExamDao
import com.surgicalboardmaster.app.data.local.dao.FlashcardDao
import com.surgicalboardmaster.app.data.local.dao.SurgicalLibraryDao

// هنا قمنا بتقليل قائمة الكيانات لتشمل فقط الأساسيات لتخطي الخطأ.
// إذا كان هناك كيانات أخرى (مثل الكتب أو الفصول) سنضيفها لاحقاً إذا احتجنا.
import com.surgicalboardmaster.app.data.local.entity.LessonEntity
import com.surgicalboardmaster.app.data.local.entity.FlashcardEntity

@Database(
    entities = [
        LessonEntity::class,
        FlashcardEntity::class
    ],
    version = 7, // رفعنا الإصدار للتأكد من تحديث الكاش
    exportSchema = false
)
abstract class SbmDatabase : RoomDatabase() {
    abstract fun surgicalLibraryDao(): SurgicalLibraryDao
    abstract fun educationalLessonDao(): EducationalLessonDao
    abstract fun examDao(): ExamDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun clinicalCasesDao(): ClinicalCasesDao
}
