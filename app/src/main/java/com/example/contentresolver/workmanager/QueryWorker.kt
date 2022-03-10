package com.example.contentresolver.workmanager

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.database.getStringOrNull
import androidx.work.*
import com.example.contentresolver.Constants
import com.example.contentresolver.MediaItem
import com.example.contentresolver.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

class QueryWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        startForegroundService()
//        todo download and upload in same worker or different ones?
        delay(2000)
        return queryContentResolver()
    }

    // one work with query and upload functon. launch  worker if on unmetered connection. Check in worker itself if more works is to be done
    // and schedule again. Store status in preferences.
    private suspend fun queryContentResolver(): Result {
        println("contentresolver: about to query from offset = $offset")
        val uris = withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.OWNER_PACKAGE_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Video.Media.DURATION
            )

            /************ Crash when adding whereCondition and selectionArgs ***************/
            /************ Crash when adding whereCondition and selectionArgs ***************/
            val whereCondition = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
            )
            /************ Crash when adding whereCondition and selectionArgs ***************/
            /************ Crash when adding whereCondition and selectionArgs ***************/

            val uris = mutableListOf<MediaItem>()
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                Bundle().apply {
                    // Limit & Offset
                    putInt(ContentResolver.QUERY_ARG_LIMIT, LIMIT)
                    putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                    // Sort function
                    putStringArray(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED)
                    )
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
//                        putString(ContentResolver.QUERY_ARG_SQL_SELECTION, whereCondition)
//                        putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                },
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    uris.add(
                        MediaItem(
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)),
                            ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                            ),
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.OWNER_PACKAGE_NAME)),
                            cursor.getStringOrNull(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
                        )
                    )
                }
            }

            uris
        }

        if (uris.size >= LIMIT) {
            println("SIZE: ${uris.size}")
//schedule a new worker inside this worker.
            offset += LIMIT
            val queryRequest = OneTimeWorkRequestBuilder<QueryWorker>()
                .addTag("query")
                .build()
            val workManager = WorkManager.getInstance(applicationContext)
            workManager.beginUniqueWork("query_$offset", ExistingWorkPolicy.APPEND, queryRequest)
                .enqueue()

            // we could return request id here for the new requets but is that reuired? We could also store status in persistent storage.
        }
//        todo enqueu this task again if there are more urls
        println("return result $offset")
        return Result.success(
            workDataOf(
                KEY_MEDIA_ITEMS to offset
            )
        )
    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentText("Querying...")
                    .setContentTitle("Query in progress")
                    .build()
            )
        )
    }

    companion object {
        const val KEY_MEDIA_ITEMS = "mediaItems"
        private const val LIMIT = 2
        // in a production app, the offset should be stored in persistent storage
        var offset = 0
    }
}