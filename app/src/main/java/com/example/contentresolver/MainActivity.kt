package com.example.contentresolver

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.pm.PackageManager
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.lifecycleScope
import com.example.contentresolver.ui.theme.ContentResolverTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            lifecycleScope.launch {
                retrieveContent()
            }
        } else {
            println("Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()

            ContentResolverTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Column() {
                        Button(onClick = {
                            activityLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }) {
                            Text("Query")
                        }
                    }
                }
            }
        }
    }

    private suspend fun retrieveContent() {
        println("contentresolver: about to query")
        withContext(Dispatchers.IO) {
//            data or OWNER_PACKAGE_NAME
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.OWNER_PACKAGE_NAME, // todo check sdk >= 29 then use this else use data
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATE_ADDED,
//                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Video.Media.DURATION
            )

            val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
            val selectionArgs = arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            )

            val uris = mutableListOf<MediaItem>()
//        todo this is for after Q
            // todo offset should be remembered
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                Bundle().apply {
                    // Limit & Offset
                    putInt(ContentResolver.QUERY_ARG_LIMIT, 5)
                    putInt(ContentResolver.QUERY_ARG_OFFSET, 20000)
                    // Sort function
                    putStringArray(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(MediaStore.Files.FileColumns.DATE_MODIFIED)
                    )
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
                    )
                    /*putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        selectionArgs
                    )*/
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
            uris.forEach {
                println("contentresolver: ${it.data}")
                println("contentresolver: ${it.uri.authority}")
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ContentResolverTheme {
        Greeting("Android")
    }
}