package com.yusufcakmak.exoplayersample

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class VideoPlayerActivity : AppCompatActivity() {
    //创建一个SimpleExoPlayer实例 用于视频和用户输入
    private lateinit var player: SimpleExoPlayer
    // medirSource 媒体数据源工厂创建方法
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    // 播放的目标视图
    private lateinit var playerView:PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
    }

    private fun initializePlayer() {

        player = ExoPlayerFactory.newSimpleInstance(this)
        playerView = findViewById(R.id.playerView)
        mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"))
       // 创建一个常规媒体文件的播放源
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        with(player) {
            prepare(mediaSource, false, false)
            playWhenReady
            repeatMode
        }
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
        playerView.player = player
        playerView.requestFocus()

    }

    private fun releasePlayer() {
        player.release()
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }

    companion object {
        const val STREAM_URL = "rtmp://172.168.2.5/vod/mp4:sample1_150kbps.f4v"
    }
}