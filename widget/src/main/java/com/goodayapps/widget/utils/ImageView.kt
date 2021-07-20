package com.goodayapps.widget.utils

import android.content.Context
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import coil.fetch.VideoFrameFileFetcher
import coil.fetch.VideoFrameUriFetcher
import coil.util.DebugLogger

fun initMedia(context: Context) {
    Coil.setImageLoader(
        ImageLoader.Builder(context)
            .crossfade(false)
            .logger(DebugLogger())
            .componentRegistry {
                add(GifDecoder())
                add(SvgDecoder(context))
                add(VideoFrameFileFetcher(context))
                add(VideoFrameUriFetcher(context))
                add(VideoFrameDecoder(context))
            }
            .build())
}
