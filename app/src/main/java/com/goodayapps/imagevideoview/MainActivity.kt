package com.goodayapps.imagevideoview

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.goodayapps.widget.media_viewer.screens.main.MediaPreviewerActivity
import com.goodayapps.widget.media_viewer.models.MediaModel
import com.goodayapps.widget.media_viewer.models.PreviewerModel
import com.goodayapps.widget.media_viewer.utils.initMediaView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        initMediaView(this)

        findViewById<View>(R.id.aaaaa).setOnClickListener {
            openPreview()
        }
        openPreview()
    }

    private fun openPreview() {
        MediaPreviewerActivity.open(this, PreviewerModel.build {
            withAuthor(author)
            withAuthorVisibility(false)
            withSupposedItems(
                listOf(
                    MediaModel(
                        url = "https://smapp-staging.s3.ap-east-1.amazonaws.com/upload/4/post/mZDcBKCo_video.mp4",
                        thumbnailUrl = "https://smapp-staging.s3.ap-east-1.amazonaws.com/upload/4/post/yeBZQFKD_image.jpeg"
                    ),
                    MediaModel(
                        url = "https://cs14.pikabu.ru/video/2021/07/20/16267540982279202_576x1024.webm",
                    ),
                    MediaModel(
                        url = "https://i.pinimg.com/originals/39/97/4a/39974af37c9c5d99e01e6263fa589f37.jpg",
                    )
                )
            )
        })
    }
}