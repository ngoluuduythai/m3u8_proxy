package com.webrtc.localserver

import android.os.Bundle
import android.net.Uri
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.webrtc.localserver.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniffi.M3U8ProxyServer.M3u8ProxyServer
import uniffi.M3U8ProxyServer.ZenDataChannel

class MainActivity : BaseActivity() {
    private val hls1 = "https://video.cdnbye.com/0cf6732evodtransgzp1257070836/e0d4b12e5285890803440736872/v.f100220.m3u8"
    private val hls2 = "https://wowza.peer5.com/live/smil:bbb_abr.smil/chunklist_b591000.m3u8"
    //private val localServer = HttpServer(49809)

    private lateinit var exoBinding: ActivityMainBinding
    private var player: ExoPlayer? = null
    private var currentUrl = hls2
    private var totalHttpDownloaded = 0.0
    private var totalP2pDownloaded = 0.0
    private var totalP2pUploaded = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNeedBackGesture(true)
        //localServer.start()

        exoBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(exoBinding.root)

        exoBinding.hls1.setOnClickListener {
            currentUrl = hls1
            clearData()
            startPlay(currentUrl)
        }

        exoBinding.hls2.setOnClickListener {
            currentUrl = hls2
            clearData()
            startPlay(currentUrl)
        }

        exoBinding.btnPlay.setOnClickListener {
            val playUrl = exoBinding.playUrl.text.toString()
            if (playUrl.isNotEmpty()) {
                currentUrl = playUrl
                clearData()
                startPlay(currentUrl)
            }
        }


        val dataChannel = object : ZenDataChannel{
            override fun getTsSegment(name: String): List<UByte>? {
                println("Rust =============== getTsSegment $name")
                return mutableListOf()
            }

            override fun getMasterPlaylist(): List<UByte>? {
                println("Rust =============== getMasterPlaylist ")
                val m3u8 =
                "#EXTM3U\n"+
                "#EXT-X-TARGETDURATION:10\n"+
                "#EXT-X-VERSION:3\n"+
                "#EXTINF:9.009,\n"+
                "first.ts\n"+
                "#EXTINF:9.009,\n"+
                "second.ts\n"+
                "#EXTINF:3.003,\n"+
                "third.ts\n"+
                "#EXT-X-ENDLIST"

                println("Rust m3u8 dummy $m3u8")

                return m3u8.toByteArray().asUByteArray().asList()
            }

        }
        val m3u8ProxyServer =  M3u8ProxyServer(dataChannel)
        //m3u8ProxyServer.call(datachannel = dataChannel)

        CoroutineScope(Dispatchers.IO).launch {
            m3u8ProxyServer.startServer(49809.toUInt())
            println("Rust =============== $dataChannel")
        }
    }

    @Synchronized
    private fun startPlay(url: String) {
        if (player != null) {
            player?.stop()
        }
        println("startPlay $url")
        val parsedUrl = parseStreamUrl(url)

        // Create LoadControl
        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                VideoPlayerConfig.MIN_BUFFER_DURATION,
                VideoPlayerConfig.MAX_BUFFER_DURATION,
                VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
                VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        // Create a data source factory.
        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
        // Create a HLS media source pointing to a playlist uri.
        val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(parsedUrl)))
        // Create a player instance.
        player = ExoPlayer.Builder(applicationContext)
            .setLoadControl(loadControl)
            .build()
        // Set the media source to be played.
        player?.setMediaSource(hlsMediaSource)
        // Prepare the player.
        player?.prepare()
        // Attach player to the view.
        exoBinding.playerView.player = player

        val s1 = System.currentTimeMillis()

        // Start play when ready
        player?.playWhenReady = true

//        playerView.
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                println("ExoPlaybackException $error")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == 3) {
                    val e1 = System.currentTimeMillis()
                    println("time to start play " + (e1 - s1))
                }
            }
        })
    }

    private fun parseStreamUrl(url: String): String? {
               //https://wowza.peer5.com/live/smil:bbb_abr.smil/chunklist_b591000.m3u8
        return "http://127.0.0.1:49809/live/smil:bbb_abr.smil/chunklist_b591000.m3u8"
    }

    private fun refreshRatio() {
        var ratio = 0.0
        if (totalHttpDownloaded + totalP2pDownloaded != 0.0) {
            ratio = totalP2pDownloaded / (totalHttpDownloaded + totalP2pDownloaded)
        }
    }

    private fun clearData() {
        totalHttpDownloaded = 0.0
        totalP2pDownloaded = 0.0
        totalP2pUploaded = 0.0
        refreshRatio()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player?.release()
            player = null
        }
    }

    object VideoPlayerConfig {
        //Minimum Video you want to buffer while Playing
        const val MIN_BUFFER_DURATION = 7000

        //Max Video you want to buffer during PlayBack
        const val MAX_BUFFER_DURATION = 15000

        //Min Video you want to buffer before start Playing it
        const val MIN_PLAYBACK_START_BUFFER = 7000

        //Min video You want to buffer when user resumes video
        const val MIN_PLAYBACK_RESUME_BUFFER = 7000
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

}