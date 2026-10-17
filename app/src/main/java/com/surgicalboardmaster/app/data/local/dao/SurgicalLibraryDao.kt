package com.surgicalboardmaster.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.surgicalboardmaster.app.data.local.entity.BookEntity
import com.surgicalboardmaster.app.data.local.entity.ChapterEntity
import com.surgicalboardmaster.app.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurgicalLibraryDao {

    @Query("SELECT * FROM books WHERE userId = :userId ORDER BY createdAtEpochMillis ASC")
    fun observeBooksForUser(userId: String): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM books WHERE userId = :userId")
    suspend fun countBooksForUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooks(books: List<BookEntity>)

    @Query("DELETE FROM books WHERE id = :bookId AND userId = :userId")
    suspend fun deleteBook(bookId: String, userId: String)

    @Query("SELECT * FROM chapters WHERE userId = :userId ORDER BY sortOrder ASC, chapterNumber ASC")
    fun observeAllChaptersForUser(userId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE userId = :userId AND bookId = :bookId ORDER BY sortOrder ASC, chapterNumber ASC")
    suspend fun getChaptersForBook(userId: String, bookId: String): List<ChapterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChapters(chapters: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE id = :chapterId AND userId = :userId")
    suspend fun deleteChapter(chapterId: String, userId: String)

    @Query("DELETE FROM chapters WHERE bookId = :bookId AND userId = :userId")
    suspend fun deleteChaptersByBook(bookId: String, userId: String)

    @Query("SELECT * FROM topics WHERE userId = :userId ORDER BY sortOrder ASC")
    fun observeAllTopicsForUser(userId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE userId = :userId AND chapterId = :chapterId ORDER BY sortOrder ASC")
    suspend fun getTopicsForChapter(userId: String, chapterId: String): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :topicId AND userId = :userId LIMIT 1")
    suspend fun getTopicById(topicId: String, userId: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTopic(topic: TopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTopics(topics: List<TopicEntity>)

    @Query("DELETE FROM topics WHERE id = :topicId AND userId = :userId")
    suspend fun deleteTopic(topicId: String, userId: String)

    @Query("DELETE FROM topics WHERE chapterId = :chapterId AND userId = :userId")
    suspend fun deleteTopicsByChapter(chapterId: String, userId: String)

    @Query("DELETE FROM topics WHERE bookId = :bookId AND userId = :userId")
    suspend fun deleteTopicsByBook(bookId: String, userId: String)
}
