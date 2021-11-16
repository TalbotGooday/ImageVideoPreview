package com.goodayapps.widget.media_viewer.models

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.goodayapps.widget.media_viewer.utils.checkIsVideo

class MediaModel(
    val url: String? = null,
    val uri: Uri? = null,
) : Parcelable {
    val content
        get() = url?.let { Uri.parse(it) } ?: uri

    fun isVideo(context: Context) = checkIsVideo(context, url, uri) == true

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(Uri::class.java.classLoader),
    ) {
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaModel) return false

        if (url != other.url) return false
        if (uri != other.uri) return false

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