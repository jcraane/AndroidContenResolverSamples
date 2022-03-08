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
import androidx.activity.viewModels
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
    val viewModel: MyViewModel by viewModels()

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            lifecycleScope.launch {
                viewModel.retrieveContent(contentResolver)
            }
        } else {
            println("Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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