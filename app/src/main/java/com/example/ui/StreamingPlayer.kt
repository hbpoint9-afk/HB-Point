package com.example.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

enum class PlayerEngineType {
    NATIVE_EXO,
    BUNNY_WEB_EMBED
}

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

/**
 * Utility to extract clean stream / embed URL from raw inputs
 * (handles full <iframe> tags, bunny stream embed links, and direct media urls)
 */
fun cleanStreamUrl(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""

    // If user pasted an iframe code like <iframe src="https://iframe.mediadelivery.net/embed/..." ...>
    if (trimmed.contains("<iframe", ignoreCase = true)) {
        val srcRegex = Regex("""src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val match = srcRegex.find(trimmed)
        if (match != null && match.groupValues.size > 1) {
            return match.groupValues[1].trim()
        }
    }

    return trimmed
}

/**
 * Detects whether the URL is likely a Web Embed / BunnyStream iframe rather than a raw direct video file.
 */
fun isLikelyWebEmbed(url: String): Boolean {
    val lower = url.lowercase()
    if (lower.contains("iframe.mediadelivery.net") ||
        lower.contains("player.mediadelivery.net") ||
        lower.contains("video.bunnycdn.com") ||
        lower.contains("mediadelivery.net") ||
        lower.contains("youtube.com") ||
        lower.contains("youtu.be") ||
        lower.contains("vimeo.com") ||
        lower.contains("streamtape.com") ||
        lower.contains("doodstream.com") ||
        lower.contains("dood.") ||
        lower.contains("drive.google.com") ||
        lower.contains("mega.nz") ||
        lower.contains("/embed/") ||
        lower.contains("/play/") ||
        lower.contains("embed")
    ) {
        return true
    }

    // Direct stream extensions usually run natively on ExoPlayer
    if (lower.endsWith(".m3u8") ||
        lower.endsWith(".mp4") ||
        lower.endsWith(".mkv") ||
        lower.endsWith(".webm") ||
        lower.endsWith(".mpd") ||
        lower.endsWith(".ts")
    ) {
        return false
    }

    return false
}

@Composable
fun UniversalStreamingPlayer(
    rawUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    screenRatioMode: String = "FIT",
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onToggleRatio: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val cleanedUrl = remember(rawUrl) { cleanStreamUrl(rawUrl) }

    // Auto-detect default engine based on URL type
    var currentEngine by remember(cleanedUrl) {
        mutableStateOf(
            if (isLikelyWebEmbed(cleanedUrl)) PlayerEngineType.BUNNY_WEB_EMBED else PlayerEngineType.NATIVE_EXO
        )
    }

    var showGestureIndicator by remember { mutableStateOf("") }
    var playerError by remember { mutableStateOf<String?>(null) }
    var isBuffering by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }
    var htmlCustomView by remember { mutableStateOf<View?>(null) }

    // Fullscreen back press handling
    BackHandler(enabled = isFullscreen || htmlCustomView != null) {
        if (htmlCustomView != null) {
            htmlCustomView = null
        }
        if (isFullscreen) {
            onToggleFullscreen()
        }
    }

    // Manage activity orientation & system bars for Fullscreen mode
    DisposableEffect(isFullscreen) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (isFullscreen) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
            } else {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            if (isFullscreen && activity != null) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                activity.window?.let { w ->
                    val controller = WindowCompat.getInsetsController(w, w.decorView)
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .testTag("universal_player_container"),
        contentAlignment = Alignment.Center
    ) {
        if (cleanedUrl.isBlank()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayDisabled,
                    contentDescription = "No Stream URL",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No stream server link configured for this title.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            key(cleanedUrl, currentEngine, refreshKey) {
                when (currentEngine) {
                    PlayerEngineType.NATIVE_EXO -> {
                        NativeExoPlayerView(
                            url = cleanedUrl,
                            screenRatioMode = screenRatioMode,
                            onBufferingChanged = { isBuffering = it },
                            onError = { err ->
                                playerError = err
                            },
                            onDoubleTapSeek = { indicator ->
                                showGestureIndicator = indicator
                            },
                            onFullscreenClick = onToggleFullscreen
                        )
                    }

                    PlayerEngineType.BUNNY_WEB_EMBED -> {
                        BunnyWebEmbedPlayerView(
                            url = cleanedUrl,
                            onLoading = { isBuffering = it },
                            onError = { err ->
                                playerError = err
                            },
                            onCustomViewChanged = { customView ->
                                htmlCustomView = customView
                                if (customView != null && !isFullscreen) {
                                    onToggleFullscreen()
                                } else if (customView == null && isFullscreen) {
                                    onToggleFullscreen()
                                }
                            }
                        )
                    }
                }
            }

            // If HTML5 video requested full screen custom view, overlay it
            if (htmlCustomView != null) {
                AndroidView(
                    factory = { htmlCustomView!! },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Quick Native Overlay Controls: Fullscreen Button & Aspect Ratio (Top / Bottom)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Top-Left Exit Fullscreen back button when in fullscreen
                if (isFullscreen) {
                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Bottom-Right Controls: Aspect Ratio & Fullscreen Toggle Buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 6.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fullscreen Toggle Action Button
                    IconButton(
                        onClick = onToggleFullscreen,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Buffering Spinner
            if (isBuffering && playerError == null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFEF4444),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Gesture Seek Indicator HUD
            if (showGestureIndicator.isNotEmpty()) {
                LaunchedEffect(showGestureIndicator) {
                    delay(650)
                    showGestureIndicator = ""
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (showGestureIndicator.startsWith("-")) Icons.Default.Replay10 else Icons.Default.Forward10,
                            contentDescription = "Seek HUD",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = showGestureIndicator,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Clean Native Error & Retry Overlay
            if (playerError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Playback Error",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "Playback Error",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = playerError ?: "Unable to stream this title right now.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                playerError = null
                                // Auto fallback engine toggle on retry
                                currentEngine = if (currentEngine == PlayerEngineType.NATIVE_EXO) {
                                    PlayerEngineType.BUNNY_WEB_EMBED
                                } else {
                                    PlayerEngineType.NATIVE_EXO
                                }
                                refreshKey++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Retry Playback",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-performance ExoPlayer implementation for direct HLS (m3u8), MP4, MKV, DASH and TS video streams.
 */
@OptIn(UnstableApi::class)
@Composable
fun NativeExoPlayerView(
    url: String,
    screenRatioMode: String,
    onBufferingChanged: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onDoubleTapSeek: (String) -> Unit,
    onFullscreenClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val exoPlayer = remember(context) {
        val userAgent = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                onBufferingChanged(playbackState == Player.STATE_BUFFERING)
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorMsg = error.message ?: "Failed to stream direct media"
                onError(errorMsg)
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    LaunchedEffect(url) {
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Invalid stream URL")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(exoPlayer) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val halfWidth = size.width / 2
                        val currentPos = exoPlayer.currentPosition
                        if (offset.x < halfWidth) {
                            val newPos = (currentPos - 10000).coerceAtLeast(0)
                            exoPlayer.seekTo(newPos)
                            onDoubleTapSeek("-10s")
                        } else {
                            val duration = exoPlayer.duration
                            val newPos = if (duration > 0) (currentPos + 10000).coerceAtMost(duration) else currentPos + 10000
                            exoPlayer.seekTo(newPos)
                            onDoubleTapSeek("+10s")
                        }
                    }
                )
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    controllerShowTimeoutMs = 3000
                    controllerAutoShow = true
                    setFullscreenButtonClickListener {
                        onFullscreenClick()
                    }
                    resizeMode = when (screenRatioMode) {
                        "FILL" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        "STRETCH" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.setFullscreenButtonClickListener {
                    onFullscreenClick()
                }
                playerView.resizeMode = when (screenRatioMode) {
                    "FILL" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    "STRETCH" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Dedicated Hardware-Accelerated Full-Viewport HTML5 Web/Embed Player for BunnyStream & Web video players.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BunnyWebEmbedPlayerView(
    url: String,
    onLoading: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onCustomViewChanged: (View?) -> Unit = {}
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(0xFF000000.toInt())

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                    allowContentAccess = true
                    setSupportZoom(true)
                    builtInZoomControls = false
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    cacheMode = WebSettings.LOAD_DEFAULT
                    userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onLoading(newProgress < 90)
                    }

                    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                        onCustomViewChanged(view)
                    }

                    override fun onHideCustomView() {
                        onCustomViewChanged(null)
                    }

                    override fun getDefaultVideoPoster(): android.graphics.Bitmap? {
                        return android.graphics.Bitmap.createBitmap(50, 50, android.graphics.Bitmap.Config.ARGB_8888)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoading(false)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        onLoading(false)
                        if (errorCode != ERROR_TIMEOUT) {
                            onError("Web engine error: $description")
                        }
                    }
                }

                loadTargetUrl(this, url)
            }
        },
        update = { webView ->
            if (webView.tag != url) {
                webView.tag = url
                loadTargetUrl(webView, url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Loads BunnyStream iframe or web video URL with a responsive full-viewport HTML wrapper if needed.
 */
private fun loadTargetUrl(webView: WebView, rawUrl: String) {
    val cleanUrl = cleanStreamUrl(rawUrl)
    if (cleanUrl.isBlank()) return

    // If it's a BunnyStream embed (iframe.mediadelivery.net / player.mediadelivery.net / video.bunnycdn.com), or if user provided an embed URL
    if (cleanUrl.contains("iframe.mediadelivery.net") ||
        cleanUrl.contains("player.mediadelivery.net") ||
        cleanUrl.contains("video.bunnycdn.com") ||
        cleanUrl.contains("mediadelivery.net") ||
        cleanUrl.contains("/embed/") ||
        cleanUrl.contains("/play/")
    ) {
        val htmlContent = """
            <!DOCTYPE html>
            <html style="width:100%; height:100%; margin:0; padding:0; background:#000;">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; background-color: #000; overflow: hidden; }
                    html, body { width: 100%; height: 100%; width: 100vw; height: 100vh; background: #000; }
                    iframe {
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100% !important;
                        height: 100% !important;
                        border: 0;
                        outline: none;
                        display: block;
                    }
                </style>
            </head>
            <body>
                <iframe 
                    src="$cleanUrl" 
                    loading="lazy" 
                    allow="accelerometer; gyroscope; autoplay; encrypted-media; picture-in-picture; fullscreen; *" 
                    allowfullscreen="true"
                    webkitallowfullscreen="true"
                    mozallowfullscreen="true">
                </iframe>
            </body>
            </html>
        """.trimIndent()
        val baseUrl = if (cleanUrl.contains("player.mediadelivery.net")) "https://player.mediadelivery.net" else "https://iframe.mediadelivery.net"
        webView.loadDataWithBaseURL(baseUrl, htmlContent, "text/html", "UTF-8", null)
    } else {
        webView.loadUrl(cleanUrl)
    }
}
