package com.goodayapps.widget.media_viewer.core

import android.content.Context
import android.view.LayoutInflater
import com.goodayapps.widget.media_viewer.core.players.ExoAppPlayer
import com.goodayapps.widget.media_viewer.core.players.RecyclerViewPreviewerModelUpdater
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.utils.cache.CacheDataSourceFactory
import com.goodayapps.widget.media_viewer.utils.initMediaView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.util.EventLogger

object MediaViewer {
    private var _appPlayerView: AppPlayerView? = null
    private var _player: ExoAppPlayer? = null

    var imageUriResolver: UriResolver = UriResolverImpl()
    var mediaUriResolver: UriResolver = UriResolverImpl()

    val appPlayerView
        get() = _appPlayerView!!

    val player
        get() = _player!!

    private var cacheDataSourceFactory: CacheDataSourceFactory? = null

    class UriResolverImpl : UriResolver()

    fun init(context: Context) {
        initMediaView(context)
        cacheDataSourceFactory = CacheDataSourceFactory.default(context)
        _player = ExoAppPlayer(
            exoPlayer = createPlayer(
                context = context,
                startAutoPlay = false,
                cacheDataSourceFactory = cacheDataSourceFactory!!
            ),
            updater = RecyclerViewPreviewerModelUpdater()
        )
    }

    fun release() {
        _player?.exoPlayer?.release()
        _player = null
        cacheDataSourceFactory?.release()
        cacheDataSourceFactory = null
    }

    fun appPlayerView(layoutInflater: LayoutInflater): AppPlayerView {
        this._appPlayerView = ExoAppPlayerView(layoutInflater)
        return appPlayerView
    }

    private fun createPlayer(
        context: Context,
        startAutoPlay: Boolean,
        cacheDataSourceFactory: CacheDataSourceFactory
    ): ExoPlayer {
        return SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(cacheDataSourceFactory)
            ).build().also { player ->
                player.addAnalyticsListener(EventLogger(null))
                player.playWhenReady = startAutoPlay
                player.repeatMode = Player.REPEAT_MODE_ONE
            }
    }

    abstract class UriResolver {
        open suspend fun resolve(originalData: MediaModel): MediaModel? {
            return originalData.apply {
                this.resolvedUri = this.content
            }
        }

        open suspend fun resolve(originalData: List<MediaModel>): List<MediaModel> {
            return originalData.onEach {
                it.resolvedUri = it.content
            }
        }
    }
}