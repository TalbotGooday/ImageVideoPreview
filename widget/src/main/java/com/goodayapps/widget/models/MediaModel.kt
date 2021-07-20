package com.goodayapps.widget.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.goodayapps.widget.utils.WEB_VIDEO_PATTERN

class MediaModel(
    val url: String? = null,
    val uri: Uri? = null,
    val previewUrl: String? = null,
    val previewUri: Uri? = null,
) : Parcelable {
    val isVideo: Boolean
        get() = (url != null && WEB_VIDEO_PATTERN.toRegex().matches(url)) ||
            (uri?.toString() != null && WEB_VIDEO_PATTERN.toRegex().matches(uri.toString()))

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader)
    ) {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaModel) return false

        if (url != other.url) return false
        if (uri != other.uri) return false

        if (previewUrl != other.previewUrl) return false
        if (previewUri != other.previewUri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url?.hashCode() ?: 0
        result = 31 * result + (uri?.hashCode() ?: 0)
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeParcelable(uri, flags)
        parcel.writeString(previewUrl)
        parcel.writeParcelable(previewUri, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<MediaModel> {
        override fun createFromParcel(parcel: Parcel): MediaModel {
            return MediaModel(parcel)
        }

        override fun newArray(size: Int): Array<MediaModel?> {
            return arrayOfNulls(size)
        }
    }
}