package com.example.mediaviewer
object AssetVideoData {
    private const val ASSET_PATH = "file:///android_asset"

    val waves =  "$ASSET_PATH/waves.mp4"
    val christmas = "$ASSET_PATH/christmas.mp4"
    val yellow = "$ASSET_PATH/yellow.mp4"

    val all = listOf(waves, christmas, yellow)

    val webData = listOf(
        "https://firebasestorage.googleapis.com/v0/b/comunitee-v2.appspot.com/o/posts%2Fvideos%2FwxvfL0dKdQp425nYC77i.mp4?alt=media&token=fd0f43c8-c577-46ea-98df-e2b1e50dbd5c",
        "https://www.nawpic.com/media/2020/wallpaper-for-phone-nawpic-10.jpg",
        "https://cs14.pikabu.ru/video/2021/11/14/163691994627424218_960x540.webm",
        "https://cs14.pikabu.ru/video/2021/11/12/1636691841257635472_480x852.webm",
        "https://cs12.pikabu.ru/post_img/2021/11/12/8/1636724990163465710.webp",
        "https://cs14.pikabu.ru/video/2021/11/11/1636602121294435041_576x840.webm",
        "https://cs14.pikabu.ru/video/2021/11/11/163666271029317378_480x368.webm"
    )
}
