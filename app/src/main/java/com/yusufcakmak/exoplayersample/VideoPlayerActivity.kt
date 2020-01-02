package com.yusufcakmak.exoplayersample

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioListener
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.text.TextOutput
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener

class VideoPlayerActivity : Activity(),Player.EventListener{
    companion object {
        const val STREAM_URL = "rtmp://172.168.2.5/vod/mp4:sample1_150kbps.f4v"
    }
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
        //监听播放状态
        player.addListener(this)
        // 收到字幕或者是字幕提示更改的监听
        player.addTextOutput{
            it.add(Cue("这是字幕数据"))
        }
        player.addVideoListener(object :VideoListener{
            //内部包含一定的默认的实现的方法
        })
        playerView = findViewById(R.id.playerView)
        mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"))
       // 创建一个常规媒体文件的播放源
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        // 创建一个剪辑视频源
        val clippingSource = ClippingMediaSource(mediaSource,5000000)
        // 创建一个循环播放的视频源
        val LoopingSource = LoopingMediaSource(mediaSource,3)
        // 创建一个组合数据源
        val fristSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        val secondSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        val looping = LoopingMediaSource(fristSource,5)
        val concatenateSource = ConcatenatingMediaSource(fristSource,looping)
        with(player) {
            prepare(mediaSource, false, false)
            playWhenReady
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
    /**
     * @description  播放出错的时候的回调
     * @param  播放出错的异常数据
     * @return  null
     * @author 18734
     * @time 2020/1/2 14:03
     */
    override fun onPlayerError(error: ExoPlaybackException?) {

    }
    /**
     * @description 播放的状态发生变化的时候的回调
     * @param 1
     * @param 2 当前的播放的状态
     * @return  null
     * @author 18734
     * @time 2020/1/2 14:04
     */
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
    }

}