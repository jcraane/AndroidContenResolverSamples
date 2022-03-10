package com.example.contentresolver

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    val id: Long,
    val uri: Uri,
    val packageName: String?,
    val mime: String?,
    val size: Long
) : Parcelable
