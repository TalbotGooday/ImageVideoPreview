package com.goodayapps.widget.media_viewer.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class PreviewerModel private constructor(
    val author: String?,
    val time: Long,
    val showAuthor: Boolean,
    val showNumeration: Boolean,
    val extension: String?,
    val data: List<MediaModel>?,
    var current: MediaModel? = null,
) : Parcelable {
    val previewData
        get() = data ?: current?.let { listOf(it) }

    private constructor(builder: Builder) : this(
        author = builder.author,
        time = builder.time,
        showAuthor = builder.showAuthor,
        showNumeration = builder.showNumeration,
        extension = builder.extension,
        data = builder.data,
        current = builder.start
    )

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.createTypedArrayList(MediaModel),
        parcel.readParcelable(MediaModel::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(author)
        parcel.writeLong(time)
        parcel.writeByte(if (showAuthor) 1 else 0)
        parcel.writeByte(if (showNumeration) 1 else 0)
        parcel.writeString(extension)
        parcel.writeTypedList(data)
        parcel.writeParcelable(current, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    class Builder {
        var author: String? = ""
            private set
        var extension: String? = null
            private set
        var time: Long = 0
            private set
        var showAuthor: Boolean = false
            private set
        var showNumeration: Boolean = false
            private set
        var data: MutableList<MediaModel>? = null
            private set
        var start: MediaModel? = null
            private set

        fun withNumeration(showNumeration: Boolean) = apply { this.showNumeration = showNumeration }
        fun withAuthor(author: String?) = apply { this.author = author }
        fun withExtension(extension: String?) = apply { this.extension = extension }
        fun withTime(time: Long) = apply { this.time = time }
        fun withAuthorVisibility(showAuthor: Boolean) = apply { this.showAuthor = showAuthor }

        fun startWithUrl(url: String?) = apply {
            this.start = MediaModel(url = url)
        }

        fun startWithUri(uri: Uri?) = apply {
            this.start = MediaModel(uri = uri)
        }

        fun startWithItem(item: MediaModel?) = apply {
            this.start = item
        }

        fun withSupposedUrls(urls: List<String>?) = apply {
            val newData = urls?.map { MediaModel(url = it) } ?: return@apply

            if (this.data == null) {
                this.data = mutableListOf()
            }

            this.data?.addAll(newData)
        }

        fun withSupposedUrls(vararg urls: String) = apply {
            withSupposedUrls(urls.toList())
        }

        fun withSupposedUris(uris: List<Uri>?) = apply {
            val newData = uris?.map { MediaModel(uri = it) } ?: return@apply

            if (this.data == null) {
                this.data = mutableListOf()
            }

            this.data?.addAll(newData)
        }

        fun withSupposedUris(vararg uris: Uri) = apply {
            withSupposedUris(uris.toList())
        }

        fun withSupposedItems(media: List<MediaModel>?) = apply {
            val newData = media ?: return@apply

            if (this.data == null) {
                this.data = mutableListOf()
            }

            this.data?.addAll(newData)
        }

        fun withSupposedItems(vararg media: MediaModel) = apply {
            withSupposedItems(media.toList())
        }

        fun build() = PreviewerModel(this)
    }

    companion object CREATOR : Parcelable.Creator<PreviewerModel> {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
        override fun createFromParcel(parcel: Parcel): PreviewerModel {
            return PreviewerModel(parcel)
        }

        override fun newArray(size: Int): Array<PreviewerModel?> {
            return arrayOfNulls(size)
        }
    }
}