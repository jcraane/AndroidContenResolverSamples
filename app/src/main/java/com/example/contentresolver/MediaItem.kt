package com.example.contentresolver

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val packageName: String?,
    val mime: String?,
    val size: Long
)
