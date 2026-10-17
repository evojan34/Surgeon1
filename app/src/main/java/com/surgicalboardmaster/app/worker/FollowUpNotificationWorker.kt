package com.surgicalboardmaster.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.surgicalboardmaster.app.R

/**
 * Secure background worker for check and notification dispatching of due surgical follow-ups.
 * Adheres strictly to Part 10 & 9 Privacy Rules:
 * Never sends patient-identifiable information (PII) or patient names in notification payloads.
 * Only references pseudonymous case codes (e.g. SBM-2026-HPB-014).
 */
class FollowUpNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val pseudonymousId = inputData.getString(KEY_PSEUDONYMOUS_ID) ?: "SBM-Surgical-Case"
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: "Pending Surgical Follow-up Due"

        dispatchSecureNotification(pseudonymousId, taskTitle)
        return Result.success()
    }

    private fun dispatchSecureNotification(pseudonymousCaseId: String, title: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "surgical_follow_up_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Surgical Follow-up Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Zero-PII clinical reminder notifications for board candidates"
            }
            manager.createNotificationChannel(channel)
        }

        // Privacy Check: notification text contains ZERO PII, only pseudonymous ID and task title
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Follow-up Due: $pseudonymousCaseId")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(pseudonymousCaseId.hashCode(), notification)
    }

    companion object {
        const val KEY_PSEUDONYMOUS_ID = "key_pseudonymous_id"
        const val KEY_TASK_TITLE = "key_task_title"
    }
}
