package com.goodayapps.widget.media_viewer.utils.cache

import android.content.Context
import com.goodayapps.widget.media_viewer.R
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File

class CacheDataSourceFactory(
    private val context: Context,
    private val maxCacheSize: Long,
    private val maxFileSize: Long,
) : DataSource.Factory {
    companion object {
        fun default(context: Context) = CacheDataSourceFactory(
            context,
            100 * 1024 * 1024L,
            5 * 1024 * 1024L
        )
    }

    private var _simpleCache: SimpleCache? = createCache(context, maxCacheSize)
    private val simpleCache: SimpleCache
        get() {
            if (_simpleCache == null) {
                _simpleCache = createCache(context, maxCacheSize)
            }

            return _simpleCache!!
        }

    private var _defaultDatasourceFactory: DefaultDataSourceFactory? = null

    private val defaultDatasourceFactory: DefaultDataSourceFactory
        get() {
            if (_defaultDatasourceFactory == null) {
                _defaultDatasourceFactory = createDatasourseFactory(context)
            }

            return _defaultDatasourceFactory!!
        }

    private fun createCache(context: Context, maxCacheSize: Long): SimpleCache? {

        val evictor = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        val databaseProvider: DatabaseProvider = ExoDatabaseProvider(context)
        return try {
            SimpleCache(File(context.cacheDir, "media"), evictor, databaseProvider)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createDatasourseFactory(context: Context): DefaultDataSourceFactory {
        val userAgent: String = Util.getUserAgent(context, context.getString(R.string.lib_name))
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        return DefaultDataSourceFactory(
            context,
            bandwidthMeter,
            DefaultHttpDataSource.Factory().setUserAgent(userAgent)
        )
    }

    override fun createDataSource(): DataSource {
        return CacheDataSource(
            simpleCache,
            defaultDatasourceFactory.createDataSource(),
            FileDataSource(),
            CacheDataSink(simpleCache, maxFileSize),
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
            null
        )
    }

    fun release() {
        _simpleCache?.release()
    }
}