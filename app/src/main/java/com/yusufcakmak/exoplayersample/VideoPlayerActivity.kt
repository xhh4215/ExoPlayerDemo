package com.yusufcakmak.exoplayersample

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.example.ijkplayerdemo.messagetype.ExoPlayerEventMessage
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioListener
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.text.Cue
import com.google.android.exoplayer2.text.TextOutput
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.video.VideoListener
import com.yusufcakmak.utils.UDPClient
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*

class VideoPlayerActivity : Activity(),Player.EventListener{

    companion object {
        const val STREAM_URL = "rtmp://172.168.2.5/vod/mp4:sample1_150kbps.f4v"
    }
    //创建一个SimpleExoPlayer实例 用于视频和用户输入
    private lateinit var player: SimpleExoPlayer
    private var showDanmaku = true
    // medirSource 媒体数据源工厂创建方法
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    // 播放的目标视图
    private lateinit var playerView:PlayerView

    private lateinit var danmakuView: DanmakuView

    private lateinit var danmakuContext: DanmakuContext

    private val udpclient = UDPClient()

    private var subtitle: String = "弹幕信息"

    private lateinit var displayer: DisplayMetrics
    private val paeser: BaseDanmakuParser = object : BaseDanmakuParser() {
        override fun parse(): IDanmakus {
            return Danmakus()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        Init()

    }
    private fun Init(){
        danmakuView = findViewById(R.id.danmaku_view)
        danmakuView.enableDanmakuDrawingCache(true)
        displayer = this.resources.displayMetrics
        danmakuView.setCallback(object : DrawHandler.Callback {
            override fun drawingFinished() {
            }

            override fun danmakuShown(danmaku: BaseDanmaku?) {
            }

            override fun prepared() {
                showDanmaku = true
                danmakuView.start()
                addDanmaku(subtitle, false)
            }

            override fun updateTimer(timer: DanmakuTimer?) {
            }

        })
        danmakuContext = DanmakuContext.create()
        danmakuView.prepare(paeser, danmakuContext)
        player = ExoPlayerFactory.newSimpleInstance(this)
        playerView = findViewById(R.id.playerView)
        mediaDataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"))
        // 创建一个常规媒体文件的播放源
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        // 创建一个剪辑视频源
        val clippingSource = ClippingMediaSource(mediaSource,5000000)
        val textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, null, Format.NO_VALUE, Format.NO_VALUE, Locale.getDefault().language, null, Format.OFFSET_SAMPLE_RELATIVE)
        val textMediaSource: MediaSource = SingleSampleMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse("ssssssssssssssssssssssss"), textFormat, C.TIME_UNSET)
        val mergingSource = MergingMediaSource(mediaSource,textMediaSource)
        // 创建一个循环播放的视频源
        val LoopingSource = LoopingMediaSource(mediaSource,100)
        // 创建一个组合数据源
        val fristSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        val secondSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(STREAM_URL))
        val looping = LoopingMediaSource(fristSource,5)
        val concatenateSource = ConcatenatingMediaSource(fristSource,looping)
        with(player) {
            prepare(LoopingSource, false, false)
            playWhenReady = true
        }
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)



    }
    /**
     * 向弹幕View中添加一条弹幕
     * @param content
     *          弹幕的具体内容
     * @param  withBorder
     *          弹幕是否有边框
     */
    private fun addDanmaku(content: String, withBorder: Boolean) {
        val danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content
        danmaku.padding = 5
        danmaku.textSize = sp2px(30).toFloat()
        danmaku.textColor = Color.WHITE
        danmaku.setTime(danmakuView.getCurrentTime());
        if (withBorder) {
            danmaku.borderColor = Color.GREEN
        }
        danmakuView.addDanmaku(danmaku)
    }

    /**
     * sp转px的方法。
     */
    fun sp2px(spValue: Int): Int {
        val fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (spValue * fontScale + 0.5f).toInt()
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        Thread(object : Runnable {
            override fun run() {
                udpclient.receiveUDPData(8800)
            }
        })
    }

    public override fun onResume() {
        super.onResume()
        playerView.player = player
        playerView.requestFocus()
        if (danmakuView != null && danmakuView.isPrepared && danmakuView.isPaused) {
            danmakuView.resume()
        }
    }

    public override fun onPause() {
        super.onPause()

        player.release()
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        player.release()
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(messageEvent: ExoPlayerEventMessage) {
        when {
            messageEvent.code == 1 -> {
                subtitle = messageEvent.message
            }
            messageEvent.code == 2 -> {

            }
        }

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

    override fun onDestroy() {
        super.onDestroy()
        showDanmaku = false
        if (danmakuView != null) {
            danmakuView.release()
        }
    }
}