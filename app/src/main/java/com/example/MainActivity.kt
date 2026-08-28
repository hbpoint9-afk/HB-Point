package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MediaItem
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    Launch,
    Auth,
    Dashboard,
    AdminPanel
}

enum class DashboardTab {
    Home,
    Media,
    MyList,
    Blog,
    Settings
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Default directly to Dashboard so the full streaming catalog, trending hero, and player are immediately open and previewable
                var currentScreen by remember { mutableStateOf(AppScreen.Dashboard) }
                var activeTab by remember { mutableStateOf(DashboardTab.Home) }

                val isLoggedIn by viewModel.preferences.isLoggedIn
                val isTvMode by viewModel.preferences.isTvMode
                val selectedMedia by viewModel.selectedMedia.collectAsState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    when (currentScreen) {
                        AppScreen.Launch -> {
                            LaunchScreen(
                                viewModel = viewModel,
                                onFinished = {
                                    currentScreen = AppScreen.Dashboard
                                },
                                onOpenAuth = {
                                    currentScreen = AppScreen.Auth
                                }
                            )
                        }
                        AppScreen.Auth -> {
                            AuthScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    currentScreen = AppScreen.Dashboard
                                },
                                onGuestContinue = {
                                    currentScreen = AppScreen.Dashboard
                                }
                            )
                        }
                        AppScreen.Dashboard -> {
                            AdaptiveScaffold(
                                activeTab = activeTab,
                                isTvMode = isTvMode,
                                onTabSelected = { activeTab = it },
                                content = {
                                    when (activeTab) {
                                        DashboardTab.Home -> {
                                            HomeScreen(viewModel = viewModel) { media ->
                                                viewModel.selectMedia(media)
                                            }
                                        }
                                        DashboardTab.Media -> {
                                            MediaCatalogGridScreen(viewModel = viewModel) { media ->
                                                viewModel.selectMedia(media)
                                            }
                                        }
                                        DashboardTab.MyList -> {
                                            MyListScreen(viewModel = viewModel) { media ->
                                                viewModel.selectMedia(media)
                                            }
                                        }
                                        DashboardTab.Blog -> {
                                            BloggerScreen(viewModel = viewModel)
                                        }
                                        DashboardTab.Settings -> {
                                            SettingsScreen(
                                                viewModel = viewModel,
                                                onOpenCurator = {
                                                    currentScreen = AppScreen.AdminPanel
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        AppScreen.AdminPanel -> {
                            CuratorScreen(
                                viewModel = viewModel,
                                onBack = {
                                    currentScreen = AppScreen.Dashboard
                                    activeTab = DashboardTab.Settings
                                }
                            )
                        }
                    }

                    // Floating Built-In Streaming Player view overlay
                    AnimatedVisibility(
                        visible = (currentScreen == AppScreen.Dashboard || currentScreen == AppScreen.AdminPanel) && selectedMedia != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        selectedMedia?.let { media ->
                            PlayerOverlay(
                                media = media,
                                viewModel = viewModel,
                                onClose = {
                                    viewModel.selectMedia(null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdaptiveScaffold(
    activeTab: DashboardTab,
    isTvMode: Boolean,
    onTabSelected: (DashboardTab) -> Unit,
    content: @Composable () -> Unit
) {
    val neonRed = Color(0xFFEF4444)
    val goldAccent = Color(0xFFFFD700)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        val isWideScreen = totalWidth >= 680.dp || isTvMode

        if (isWideScreen) {
            // High-Performance Desktop & TV Sidebar Navigation with full vertical scroll
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Desktop Sidebar Navigation
                Column(
                    modifier = Modifier
                        .width(if (totalWidth >= 900.dp) 220.dp else 180.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF0D0D0D))
                        .border(BorderStroke(1.dp, Color(0xFF1E1E1E)))
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // App Logo Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(neonRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "HB Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "HB POINT",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "DESKTOP STREAM",
                                color = neonRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E1E1E), modifier = Modifier.padding(bottom = 8.dp))

                    // Menu Items List (Guaranteed 100% visible & scrollable)
                    val navItems = listOf(
                        Triple(DashboardTab.Home, "Home", Icons.Default.Home),
                        Triple(DashboardTab.Media, "Catalog", Icons.Default.MovieFilter),
                        Triple(DashboardTab.MyList, "My Watchlist", Icons.Default.Bookmark),
                        Triple(DashboardTab.Blog, "News & Blog", Icons.Default.Feed),
                        Triple(DashboardTab.Settings, "Members & Hub", Icons.Default.Person)
                    )

                    navItems.forEach { (tab, title, icon) ->
                        val isSelected = activeTab == tab
                        Surface(
                            onClick = { onTabSelected(tab) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) neonRed.copy(alpha = 0.15f) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, neonRed.copy(alpha = 0.6f)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("nav_rail_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) neonRed else Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Quick TV / Desktop badge at sidebar bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF141414))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "HD Multi-Mirror",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldAccent
                            )
                            Text(
                                text = "Ultra 4K & MP4 Player Ready",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Main Content View Area (Smooth mouse & touch scrolling)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.Black)
                ) {
                    content()
                }
            }
        } else {
            // Bottom Navigation Bar for Compact Phones
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = Color(0xFF0D0D0D),
                        tonalElevation = 8.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.Home,
                            onClick = { onTabSelected(DashboardTab.Home) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = neonRed,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.Media,
                            onClick = { onTabSelected(DashboardTab.Media) },
                            icon = { Icon(Icons.Default.MovieFilter, contentDescription = "Media") },
                            label = { Text("Catalog", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = neonRed,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_media")
                        )
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.MyList,
                            onClick = { onTabSelected(DashboardTab.MyList) },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks") },
                            label = { Text("My List", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = neonRed,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_mylist")
                        )
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.Blog,
                            onClick = { onTabSelected(DashboardTab.Blog) },
                            icon = { Icon(Icons.Default.Feed, contentDescription = "Blog") },
                            label = { Text("Blog", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = neonRed,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_blog")
                        )
                        NavigationBarItem(
                            selected = activeTab == DashboardTab.Settings,
                            onClick = { onTabSelected(DashboardTab.Settings) },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Members") },
                            label = { Text("Members", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = neonRed,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            modifier = Modifier.testTag("nav_members")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    content()
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DEDICATED TV SHOWS, MOVIES & ANIME CATEGORIZED GRID WITH FILTERS
// -------------------------------------------------------------
@Composable
fun MediaCatalogGridScreen(
    viewModel: MainViewModel,
    onMediaSelected: (MediaItem) -> Unit
) {
    val allMedia by viewModel.allMedia.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") } // All, Movies, TV Shows, Anime

    val filteredItems = remember(allMedia, selectedFilter) {
        when (selectedFilter) {
            "Movies" -> allMedia.filter { it.type.equals("Movie", ignoreCase = true) }
            "TV Shows" -> allMedia.filter { it.type.equals("TV Show", ignoreCase = true) }
            "Anime" -> allMedia.filter { it.type.equals("Anime", ignoreCase = true) || it.category.contains("Anime", ignoreCase = true) }
            else -> allMedia
        }
    }

    val neonRed = Color(0xFFEF4444)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .testTag("catalog_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "Cinematic Catalog",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Premium categorized movies, anime & TV show mirrors",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Subcategory filters (All, Movies, TV Shows, Anime)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val subFilters = listOf("All", "Movies", "Anime", "TV Shows")
            for (filter in subFilters) {
                val active = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) neonRed else Color(0xFF0D0D0D))
                        .border(1.dp, if (active) neonRed else Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No items match this category catalog.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    MediaThumbnailCard(media = item, modifier = Modifier.fillMaxWidth(), onClick = onMediaSelected)
                }
            }
        }
    }
}
