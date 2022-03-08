package com.example.contentresolver

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class MyViewModel : ViewModel() {
    val mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())

    fun retrieveContent(contentResolver: ContentResolver) {
        viewModelScope.launch {
            println("contentresolver: about to query")
            withContext(Dispatchers.IO) {
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
                val whereCondition = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
                val selectionArgs = arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                )
                /************ Crash when adding whereCondition and selectionArgs ***************/

                // 46941
                val uris = mutableListOf<MediaItem>()
                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    Bundle().apply {
                        // Limit & Offset
                        putInt(ContentResolver.QUERY_ARG_LIMIT, 1000)
                        putInt(ContentResolver.QUERY_ARG_OFFSET, 0)
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

                println("contentresolver: result = $uris")
                mediaItems.value = uris
            }
        }
    }

    fun download(context: Context, uri: Uri) {
        println("contentresolver: download")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(context.getExternalFilesDir(null), "picture.jpg")
                val fos = FileOutputStream(file)
                println("contentresolver: download: ${file.absolutePath}")
                context.contentResolver.openInputStream(uri)?.use {
                    println("contentresolver: copyTo")
                    it.copyTo(fos)
                }
            }
        }
    }

    fun getTotalCount(contentResolver: ContentResolver) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val imageCountProjection = arrayOf(
                    "count(${MediaStore.Images.ImageColumns._ID})"
                )

                contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageCountProjection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    cursor.moveToFirst()
                    val existingImageCount = cursor.getInt(0)
                    println("contentresolver: count = $existingImageCount")
                }
            }
        }
    }
}