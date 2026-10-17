package com.surgicalboardmaster.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.ui.graphics.vector.ImageVector

enum class SbmDestination(
    val route: String,
    val titleEn: String,
    val titleAr: String,
    val icon: ImageVector,
    val showInBottomBar: Boolean
) {
    DASHBOARD("dashboard", "Dashboard", "لوحة التحكم", Icons.Default.Dashboard, true),
    SURGICAL_LIBRARY("library", "Library", "المكتبة الجراحية", Icons.Default.MenuBook, true),
    AI_TUTOR("ai_tutor", "AI Tutor", "المدرّس الجراحي", Icons.Default.AutoAwesome, true),
    EXAM_CENTER("exam_center", "Exams", "مركز الامتحانات", Icons.Default.Quiz, true),
    MY_CLINICAL_CASES("clinical_cases", "Cases", "حالاتي السريرية", Icons.Default.FolderSpecial, true),

    // Drawer / Secondary Destinations
    CLINICAL_SIMULATOR("simulator", "Clinical Simulator", "المحاكي السريري", Icons.Default.MedicalServices, false),
    FOLLOW_UP_CALENDAR("follow_up_calendar", "Follow-up Calendar", "جدول المتابعة", Icons.Default.CalendarMonth, false),
    FLASHCARDS("flashcards", "Flashcards", "البطاقات الذكية", Icons.Default.Style, false),
    STUDY_ANALYTICS("analytics", "Study Analytics", "تحليلات الأداء", Icons.Default.Analytics, false),
    NOTES_BOOKMARKS("notes_bookmarks", "Notes & Bookmarks", "الملاحظات والعلامات", Icons.Default.Bookmark, false),
    GLOBAL_SEARCH("global_search", "Global Search", "البحث الشامل", Icons.Default.Search, false),
    SETTINGS_PRIVACY("settings_privacy", "Settings & Privacy", "الإعدادات والخصوصية", Icons.Default.Settings, false)
}

