package com.example.contentresolver

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.contentresolver.ui.theme.ContentResolverTheme
import kotlinx.coroutines.launch

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
                val mediaItems by viewModel.mediaItems.collectAsState()

                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Column() {
                        Button(onClick = {
                            viewModel.getTotalCount(contentResolver)
                        }) {
                            Text("Total count")
                        }

                        Button(onClick = {
                            activityLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }) {
                            Text("Query")
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(mediaItems) { mediaItem ->
                                Row(
                                    modifier = Modifier.clickable(
                                        onClick = {
                                            viewModel.download(this@MainActivity, mediaItem.uri)
                                        },
                                    )
                                ) {
                                    Text(text = mediaItem.uri.toString(), modifier = Modifier.weight(0.5f))
                                    Text(text = mediaItem.size.toString(), modifier = Modifier.weight(0.5f))
                                }
                            }
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