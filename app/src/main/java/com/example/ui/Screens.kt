package com.example.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.BloggerPost
import com.example.data.MediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -------------------------------------------------------------
// 1. LAUNCH SCREEN (Cinematic Intro)
// -------------------------------------------------------------
@Composable
fun LaunchScreen(
    viewModel: MainViewModel,
    onFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var startAnimation by remember { mutableStateOf(false) }

    // Sound synthesize on launch
    LaunchedEffect(Unit) {
        viewModel.playLaunchSound()
        startAnimation = true
        delay(2600) // Beautiful 2.6s cinematic flow
        onFinished()
    }

    // Circular rotating ring states
    val infiniteTransition = rememberInfiniteTransition(label = "neon_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "neon_rotation_angle"
    )

    // Crimson neon glow color definitions
    val neonRed = Color(0xFFEF4444)
    val neonRedDark = Color(0xFFDC2626)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .testTag("launch_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Neon rotating custom geometric circles
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Outer neon glowing solid circle
                    drawCircle(
                        color = neonRedDark.copy(alpha = 0.2f),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 8.dp.toPx())
                    )
                    // Inner dashed rotation ring
                    rotate(rotationAngle) {
                        drawArc(
                            color = neonRed,
                            startAngle = 0f,
                            sweepAngle = 280f,
                            useCenter = false,
                            style = Stroke(
                                width = 4.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(15f, 15f), 0f
                                )
                            )
                        )
                    }
                }
                // Centered neon inner ring
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "HB Point Logo Play",
                    tint = neonRed,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand head text with intense neon dropshadow
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(1000)) + expandVertically(animationSpec = tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HB POINT",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.drawBehind {
                            // Immersive glowing blood red shadow effect
                            drawRect(
                                color = Color.Transparent
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unlimited Entertainment",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Skip / Mute Floating Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isMuted by viewModel.isMuted.collectAsState()
            IconButton(
                onClick = { viewModel.toggleMuteSound() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF141414))
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = "Mute Toggle",
                    tint = Color.White
                )
            }
            Button(
                onClick = onFinished,
                colors = ButtonDefaults.buttonColors(containerColor = neonRed),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("SKIP INTRO", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 2. AUTHENTICATION (PIN Gatekeeper Screen)
// -------------------------------------------------------------
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    var pinValue by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }
    val profileName by viewModel.preferences.profileName
    val neonRed = Color(0xFFEF4444)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Circular profile avatar selection
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(2.dp, neonRed, CircleShape)
                    .background(Color(0xFF141414)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hbpoint_logo),
                    contentDescription = "HB Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome, $profileName",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Enter profile lock PIN to stream",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Custom PIN Dots Visualizer
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                for (i in 0 until 4) {
                    val active = i < pinValue.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) neonRed else Color(0xFF222222)
                            )
                            .border(1.dp, if (active) neonRed else Color.Gray, CircleShape)
                    )
                }
            }

            if (loginError) {
                Text(
                    text = "Incorrect PIN code. Try again.",
                    color = neonRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // High fidelity numerical input keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "OK")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (key in row) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (key) {
                                            "Clear" -> Color(0xFF1E1E1E)
                                            "OK" -> neonRed
                                            else -> Color(0xFF141414)
                                        }
                                    )
                                    .clickable {
                                        loginError = false
                                        when (key) {
                                            "Clear" -> {
                                                if (pinValue.isNotEmpty()) {
                                                    pinValue = pinValue.dropLast(1)
                                                }
                                            }
                                            "OK" -> {
                                                if (viewModel.loginWithPin(pinValue)) {
                                                    onLoginSuccess()
                                                } else {
                                                    loginError = true
                                                    pinValue = ""
                                                }
                                            }
                                            else -> {
                                                if (pinValue.length < 4) {
                                                    pinValue += key
                                                    // Auto trigger login if 4 digits are completed
                                                    if (pinValue.length == 4) {
                                                        if (viewModel.loginWithPin(pinValue)) {
                                                            onLoginSuccess()
                                                        } else {
                                                            loginError = true
                                                            pinValue = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("keypad_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = if (key.length > 1) 14.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Default PIN is 1234. Change it in settings.",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    }
}

// -------------------------------------------------------------
// 3. HOME DASHBOARD & MEDIA LISTS
// -------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onMediaSelected: (MediaItem) -> Unit
) {
    val allMedia by viewModel.allMedia.collectAsState()
    val filteredMedia by viewModel.filteredMedia.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val trendingMedia = allMedia.filter { it.isTrending }
    val tvShows = allMedia.filter { it.type == "TV Show" }
    val movies = allMedia.filter { it.type == "Movie" }
    val recentlyAdded = allMedia.filter { it.isRecentlyAdded }

    val categories = listOf("All", "Movie", "TV Show", "Sci-Fi", "Action", "Thriller", "Drama")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Cinematic Banner with sliding Carousel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                // Background Cover
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Cosmic Cinematic Hero Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark Gradient overlay for text legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black
                                )
                            )
                        )
                )

                // Foreground Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TRENDING FEATURED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "COSMIC PULSE",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "A rich journey through distant soundwaves and glowing neon nebulae.",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val cosmicPulse = allMedia.find { it.title.contains("Cosmic Pulse") }
                                if (cosmicPulse != null) onMediaSelected(cosmicPulse)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play icon", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("STREAM NOW", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val cosmicPulse = allMedia.find { it.title.contains("Cosmic Pulse") }
                                if (cosmicPulse != null) {
                                    viewModel.toggleBookmark(cosmicPulse.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141414)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add List", tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("MY LIST", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search Bar and Genres filter Layout
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search title, genre, cast...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0D0D0D),
                        unfocusedContainerColor = Color(0xFF0D0D0D),
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color(0xFF1F1F1F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Genres Filter Horizontal Scroll Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val active = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (active) Color(0xFFEF4444) else Color(0xFF0D0D0D))
                                .border(1.dp, if (active) Color(0xFFEF4444) else Color(0xFF1F1F1F), RoundedCornerShape(20.dp))
                                .clickable { viewModel.setSelectedCategory(category) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("chip_$category"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                color = if (active) Color.White else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Search Results layout (if searching/filtering is active)
        if (searchQuery.isNotEmpty() || selectedCategory != "All") {
            item {
                Text(
                    text = "Search Results (${filteredMedia.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            if (filteredMedia.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching movies or series found.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filteredMedia) { media ->
                            MediaThumbnailCard(media, onMediaSelected)
                        }
                    }
                }
            }
        } else {
            // Standard lists layout
            item {
                MediaRowSection(title = "Trending Content", items = trendingMedia, onMediaSelected = onMediaSelected)
            }
            item {
                MediaRowSection(title = "TV Shows", items = tvShows, onMediaSelected = onMediaSelected)
            }
            item {
                MediaRowSection(title = "Blockbuster Movies", items = movies, onMediaSelected = onMediaSelected)
            }
            item {
                MediaRowSection(title = "Recently Added", items = recentlyAdded, onMediaSelected = onMediaSelected)
            }
        }
    }
}

@Composable
fun MediaRowSection(
    title: String,
    items: List<MediaItem>,
    onMediaSelected: (MediaItem) -> Unit
) {
    if (items.isEmpty()) return
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "SEE ALL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items) { media ->
                MediaThumbnailCard(media, onMediaSelected)
            }
        }
    }
}

@Composable
fun MediaThumbnailCard(
    media: MediaItem,
    onClick: (MediaItem) -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick(media) }
            .testTag("media_card_${media.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = media.posterUrl,
                    contentDescription = media.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.img_hbpoint_logo),
                    error = painterResource(id = R.drawable.img_hbpoint_logo)
                )

                // Rating overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFB300), modifier = Modifier.size(10.dp))
                        Text(media.rating, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = media.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Text(
                text = "${media.releaseYear} • ${media.category}",
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// 4. MY LIST (BOOKMARKS)
// -------------------------------------------------------------
@Composable
fun MyListScreen(
    viewModel: MainViewModel,
    onMediaSelected: (MediaItem) -> Unit
) {
    val bookmarkedItems by viewModel.bookmarkedMedia.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .testTag("my_list_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "My Caching Bookmarks",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        if (bookmarkedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = "Empty Bookmark",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Watch List is Empty",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Save series & movies to stream offline instantly on sluggish cell networks.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bookmarkedItems) { item ->
                    MediaThumbnailCard(media = item, onClick = onMediaSelected)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. GOOGLE BLOGGER RSS FEED (NEWS)
// -------------------------------------------------------------
@Composable
fun BloggerScreen(viewModel: MainViewModel) {
    val posts by viewModel.bloggerPosts.collectAsState()
    val syncing by viewModel.bloggerSyncing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .testTag("blogger_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HB Blogger News",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Live feeds from Google Blogger",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { viewModel.syncBlogger() },
                enabled = !syncing,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF141414))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Feed",
                    tint = if (syncing) Color.Gray else Color(0xFFEF4444)
                )
            }
        }

        if (syncing && posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFEF4444))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { post ->
                    BloggerNewsCard(post)
                }
            }
        }
    }
}

@Composable
fun BloggerNewsCard(post: BloggerPost) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (post.url.isNotBlank()) {
                    uriHandler.openUri(post.url)
                }
            }
            .testTag("blogger_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
        border = BorderStroke(1.dp, Color(0xFF1F1F1F)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = post.thumbnailUrl,
                    contentDescription = post.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.img_hbpoint_logo),
                    error = painterResource(id = R.drawable.img_hbpoint_logo)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Published by ${post.author} • ${post.published.take(10)}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.content,
                fontSize = 12.sp,
                color = Color.LightGray,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "READ MORE IN BROWSER →",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 6. PORTABLE SCREEN PLAYER OVERLAY (DYNAMIC STREAMING PLAYER)
// -------------------------------------------------------------
@Composable
fun PlayerOverlay(
    media: MediaItem,
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val currentUrl by viewModel.currentStreamUrl.collectAsState()
    val serverName by viewModel.currentServerName.collectAsState()
    var isPlaying by remember { mutableStateOf(false) }

    // Advanced video controls states
    var screenRatioMode by remember { mutableStateOf("FIT") } // FIT, FILL, STRETCH
    var showGestureIndicator by remember { mutableStateOf("") } // "-10s" or "+10s"
    var isCastingConnected by remember { mutableStateOf(false) }
    var showCastingDialog by remember { mutableStateOf(false) }

    // Lock Screen to landscape if desired
    val activity = context as? Activity
    LaunchedEffect(Unit) {
        // Optional: lock landscape or keep rotation
        // activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    DisposableEffect(Unit) {
        onDispose {
            // Restore portrait when exiting
            // activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("player_overlay")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Player Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close player", tint = Color.White)
                    }
                    Column {
                        Text(media.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(media.category, fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Casting toggle Icon
                    IconButton(onClick = { showCastingDialog = true }) {
                        Icon(
                            imageVector = if (isCastingConnected) Icons.Default.CastConnected else Icons.Default.Cast,
                            contentDescription = "Chromecast Airplay Selector",
                            tint = if (isCastingConnected) Color(0xFFEF4444) else Color.White
                        )
                    }
                    // Aspect ratio controller Icon
                    IconButton(
                        onClick = {
                            screenRatioMode = when (screenRatioMode) {
                                "FIT" -> "FILL"
                                "FILL" -> "STRETCH"
                                else -> "FIT"
                            }
                        }
                    ) {
                        Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio Toggle", tint = Color.White)
                    }
                }
            }

            // Video Player Container Frame with gestural overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFF070707))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                val halfWidth = size.width / 2
                                if (offset.x < halfWidth) {
                                    // Rewind double tap (Left half)
                                    showGestureIndicator = "-10s"
                                } else {
                                    // Fastforward double tap (Right half)
                                    showGestureIndicator = "+10s"
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Wrap a native android video view to load the true MP4 streaming mirrors!
                if (currentUrl.isNotBlank()) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mediaController = MediaController(ctx)
                                mediaController.setAnchorView(this)
                                setMediaController(mediaController)
                                setVideoPath(currentUrl)
                                setOnPreparedListener {
                                    isPlaying = true
                                    start()
                                }
                            }
                        },
                        update = { view ->
                            // Update URL instantly when server changes
                            if (view.tag != currentUrl) {
                                view.setVideoPath(currentUrl)
                                view.tag = currentUrl
                                view.start()
                            }
                            // Apply screen scaling ratio programmatically
                            when (screenRatioMode) {
                                "FIT" -> {
                                    view.scaleX = 1.0f
                                    view.scaleY = 1.0f
                                }
                                "FILL" -> {
                                    view.scaleX = 1.2f
                                    view.scaleY = 1.2f
                                }
                                "STRETCH" -> {
                                    view.scaleX = 1.4f
                                    view.scaleY = 1.0f
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFEF4444))
                    }
                }

                // 10s Gesture popup HUD
                if (showGestureIndicator.isNotEmpty()) {
                    LaunchedEffect(showGestureIndicator) {
                        delay(600)
                        showGestureIndicator = ""
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (showGestureIndicator == "-10s") Icons.Default.Replay10 else Icons.Default.Forward10,
                                contentDescription = "Fast-forward",
                                tint = Color(0xFFEF4444)
                            )
                            Text(showGestureIndicator, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Media Detail Descriptions, Season selectors & Server picker
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF000000))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title and basic meta tags
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(media.title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                            IconButton(onClick = { viewModel.toggleBookmark(media.id) }) {
                                Icon(
                                    imageVector = if (media.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save Bookmarks",
                                    tint = if (media.isBookmarked) Color(0xFFEF4444) else Color.White
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(media.rating, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("${media.releaseYear} • ${media.type} • ${media.category}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                // MULTI-SERVER STREAM SELECTOR CHIPS
                item {
                    Column {
                        Text(
                            text = "Stream Servers (Alternate CDN Mirrors)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            val servers = media.getServerList()
                            for (srv in servers) {
                                val isActive = srv.first == serverName
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isActive) Color(0xFFEF4444) else Color(0xFF141414))
                                        .border(1.dp, if (isActive) Color(0xFFEF4444) else Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.selectServer(srv.first, srv.second) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Layers,
                                            contentDescription = srv.first,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(srv.first, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Season/Episode selector (For TV Shows only)
                if (media.type == "TV Show") {
                    item {
                        Column {
                            Text(
                                text = "Episodes Selector (Season 1)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                for (ep in 1..10) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (ep == 1) Color(0xFFEF4444) else Color(0xFF141414))
                                            .border(1.dp, if (ep == 1) Color(0xFFEF4444) else Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                                            .clickable {
                                                // Simulated switching link
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("EP", fontSize = 9.sp, color = Color.Gray)
                                            Text("$ep", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Rich Descriptions & Cast tags
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Synopsis", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(media.description, fontSize = 13.sp, color = Color.LightGray, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Starring Cast", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(media.cast, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    // Dynamic casting dialog selector
    if (showCastingDialog) {
        Dialog(onDismissRequest = { showCastingDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
                border = BorderStroke(1.dp, Color(0xFF1F1F1F)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Cast, contentDescription = "Cast logo", tint = Color(0xFFEF4444), modifier = Modifier.size(56.dp))
                    Text("Select Cast Mirror Destination", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val devices = listOf("Living Room TV (Chromecast)", "Master Bedroom (AirPlay)", "Family Hub Display", "Direct FireStick Receiver")
                        for (dev in devices) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141414))
                                    .clickable {
                                        isCastingConnected = true
                                        showCastingDialog = false
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Tv, contentDescription = "TV", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    Text(dev, color = Color.White, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. MEMBERS & SETTINGS (PROFILING & CURATOR BYPASS ENTRANCE)
// -------------------------------------------------------------
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onOpenCurator: () -> Unit
) {
    val context = LocalContext.current
    val profileName by viewModel.preferences.profileName
    val isTvMode by viewModel.preferences.isTvMode
    val isAdmin by viewModel.preferences.isAdmin

    var newPinValue by remember { mutableStateOf("") }
    var curatorPinValue by remember { mutableStateOf("") }
    var changePinSuccess by remember { mutableStateOf(false) }
    var curatorSuccess by remember { mutableStateOf(false) }

    val neonRed = Color(0xFFEF4444)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Members & Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        // Beautiful profiling Avatar and membership badge
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
                border = BorderStroke(1.dp, Color(0xFF1F1F1F)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .border(2.dp, neonRed, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hbpoint_logo),
                            contentDescription = "HB stream logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(profileName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(Icons.Default.Verified, contentDescription = "VIP Badge", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        }
                        Text("Cosmic Platinum VIP Member", fontSize = 11.sp, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)
                        Text("Unlimited Speed • Server Mirror Bypass Active", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }

        // TV Mode / Layout Customization Switch
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("App Customization", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tablet TV Mode Layout", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Enables sidebar navigation rail for tablets & TV screens.", fontSize = 11.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = isTvMode,
                            onCheckedChange = { viewModel.preferences.setTvMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = neonRed,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFF1E1E1E)
                            )
                        )
                    }
                }
            }
        }

        // Profile Customizer forms (Change profile name)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Edit Profile & Lock PIN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Profile name field
                        OutlinedTextField(
                            value = profileName,
                            onValueChange = { viewModel.preferences.setProfileName(it) },
                            label = { Text("Profile Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = neonRed,
                                unfocusedBorderColor = Color(0xFF1F1F1F)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 4 digit Reset PIN
                        OutlinedTextField(
                            value = newPinValue,
                            onValueChange = { if (it.length <= 4) newPinValue = it },
                            label = { Text("Update 4-Digit Profile PIN") },
                            placeholder = { Text("Currently: ${viewModel.preferences.getPin()}") },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = neonRed,
                                unfocusedBorderColor = Color(0xFF1F1F1F)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = {
                                if (newPinValue.length == 4) {
                                    viewModel.preferences.setPin(newPinValue)
                                    changePinSuccess = true
                                    newPinValue = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = neonRed),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("SAVE PIN", fontWeight = FontWeight.Bold)
                        }

                        if (changePinSuccess) {
                            Text("Profile Lock PIN updated successfully!", color = Color.Green, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Live Admin Curator Gatekeeper Bypass Form
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Curator & Publisher Dashboard Portal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Admin bypass PIN is required to add, update, or remove movies and streaming servers.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        if (isAdmin) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("CURATOR PRIVILEGES ACTIVE", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onOpenCurator,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                                    ) {
                                        Text("OPEN CURATOR WORKSPACE", color = Color.Black, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = curatorPinValue,
                                onValueChange = { curatorPinValue = it },
                                label = { Text("Enter Curator Admin PIN (8888)") },
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = neonRed,
                                    unfocusedBorderColor = Color(0xFF1F1F1F)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (viewModel.verifyCuratorPin(curatorPinValue)) {
                                        curatorSuccess = true
                                        onOpenCurator()
                                    } else {
                                        curatorSuccess = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = neonRed),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("VERIFY ADMIN PORTAL", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Log out profile button
        item {
            Button(
                onClick = {
                    viewModel.preferences.setLoggedIn(false)
                    viewModel.preferences.setAdmin(false)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141414)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = neonRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOG OUT PROFILE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 8. LIVE CURATOR PORTAL (ADMIN DASHBOARD)
// -------------------------------------------------------------
@Composable
fun CuratorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allMedia by viewModel.allMedia.collectAsState()

    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Movie") } // Movie or TV Show
    var backdropUrl by remember { mutableStateOf("") }
    var posterUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("9.0") }
    var releaseYear by remember { mutableStateOf("2026") }
    var category by remember { mutableStateOf("Action") }
    var cast by remember { mutableStateOf("") }
    var trailerLink by remember { mutableStateOf("") }

    // Multi-server forms stream URLs
    var server1Name by remember { mutableStateOf("Server 1 BollyFast") }
    var server1Url by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }
    var server2Name by remember { mutableStateOf("Server 2 Firedrop") }
    var server2Url by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4") }
    var server3Name by remember { mutableStateOf("Server 3 HexaPlay") }
    var server3Url by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4") }

    var saveStatusMsg by remember { mutableStateOf("") }
    val neonRed = Color(0xFFEF4444)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .testTag("curator_screen")
    ) {
        // Portal Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Settings", tint = Color.White)
            }
            Column {
                Text("HB Curator Workspace", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Realtime media manager database syncing", fontSize = 11.sp, color = Color(0xFFFFB300))
            }
        }

        // Forms tab and current catalog inventory list side-by-side or scroll layout
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text("1. Publish New Movie or Series", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Forms Layout parameters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Cinematic Title *") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type Selectors Option
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = type == "Movie",
                                onClick = { type = "Movie" },
                                colors = RadioButtonDefaults.colors(selectedColor = neonRed)
                            )
                            Text("Movie", color = Color.White, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = type == "TV Show",
                                onClick = { type = "TV Show" },
                                colors = RadioButtonDefaults.colors(selectedColor = neonRed)
                            )
                            Text("TV Show", color = Color.White, fontSize = 14.sp)
                        }
                    }

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Genre Category (Action, Sci-Fi, Thriller, Drama)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Synopsis Rich Description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = rating,
                            onValueChange = { rating = it },
                            label = { Text("Rating (e.g. 9.1)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = releaseYear,
                            onValueChange = { releaseYear = it },
                            label = { Text("Release Year") },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = cast,
                        onValueChange = { cast = it },
                        label = { Text("Cast starring (comma separated)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = posterUrl,
                        onValueChange = { posterUrl = it },
                        label = { Text("Poster URL (Portrait Image)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = backdropUrl,
                        onValueChange = { backdropUrl = it },
                        label = { Text("Backdrop URL (Landscape Banner)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Streaming alternative CDNs Server configurations
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Multi-Server Mirror Configurations", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Column(
                        modifier = Modifier
                            .background(Color(0xFF0D0D0D), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Server 1
                        Column {
                            Text("Mirror 1 Stream Link", fontSize = 11.sp, color = Color.Gray)
                            OutlinedTextField(
                                value = server1Url,
                                onValueChange = { server1Url = it },
                                placeholder = { Text("MP4 or HLS URL Stream link") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Server 2
                        Column {
                            Text("Mirror 2 Stream Link", fontSize = 11.sp, color = Color.Gray)
                            OutlinedTextField(
                                value = server2Url,
                                onValueChange = { server2Url = it },
                                placeholder = { Text("MP4 or HLS URL Stream link") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Server 3
                        Column {
                            Text("Mirror 3 Stream Link", fontSize = 11.sp, color = Color.Gray)
                            OutlinedTextField(
                                value = server3Url,
                                onValueChange = { server3Url = it },
                                placeholder = { Text("MP4 or HLS URL Stream link") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = neonRed, unfocusedBorderColor = Color(0xFF1F1F1F)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            saveStatusMsg = "Title cannot be blank!"
                        } else {
                            val serversList = mutableListOf<Pair<String, String>>()
                            if (server1Url.isNotBlank()) serversList.add(server1Name to server1Url)
                            if (server2Url.isNotBlank()) serversList.add(server2Name to server2Url)
                            if (server3Url.isNotBlank()) serversList.add(server3Name to server3Url)
                            
                            // fallback server if empty
                            if (serversList.isEmpty()) {
                                serversList.add("Direct Mirror" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                            }

                            viewModel.saveCuratorMedia(
                                title = title,
                                type = type,
                                backdrop = backdropUrl,
                                poster = posterUrl,
                                desc = description,
                                rating = rating,
                                year = releaseYear,
                                category = category,
                                cast = cast,
                                trailer = trailerLink,
                                serversList = serversList
                            )

                            // Reset forms
                            title = ""
                            description = ""
                            cast = ""
                            posterUrl = ""
                            backdropUrl = ""
                            saveStatusMsg = "Successfully published to the persistent stream database!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("PUBLISH CATALOG MEDIA", fontWeight = FontWeight.Black)
                }

                if (saveStatusMsg.isNotEmpty()) {
                    Text(
                        text = saveStatusMsg,
                        color = Color.Green,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Catalog list items to delete/manage
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("3. Manage Media Inventory (${allMedia.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            items(allMedia) { media ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0D0D), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = media.posterUrl,
                            contentDescription = media.title,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.img_hbpoint_logo),
                            error = painterResource(id = R.drawable.img_hbpoint_logo)
                        )
                        Column {
                            Text(media.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.dp.value.sp)
                            Text("${media.type} • ${media.category}", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    IconButton(
                        onClick = { viewModel.deleteMediaItem(media.id) }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = neonRed)
                    }
                }
            }
        }
    }
}
