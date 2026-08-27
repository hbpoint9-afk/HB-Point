package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MediaItem
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    Launch,
    Auth,
    Dashboard,
    Curator
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
                var currentScreen by remember { mutableStateOf(AppScreen.Launch) }
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
                            LaunchScreen(viewModel = viewModel) {
                                currentScreen = if (isLoggedIn) AppScreen.Dashboard else AppScreen.Auth
                            }
                        }
                        AppScreen.Auth -> {
                            AuthScreen(viewModel = viewModel) {
                                currentScreen = AppScreen.Dashboard
                            }
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
                                                    currentScreen = AppScreen.Curator
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                        AppScreen.Curator -> {
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
                        visible = currentScreen == AppScreen.Dashboard && selectedMedia != null,
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

    if (isTvMode) {
        // Horizontal side bar layout (Navigation Rail) for widescreen / Tablets / TVs
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = Color(0xFF0D0D0D),
                modifier = Modifier
                    .fillMaxHeight()
                    .windowInsetsPadding(WindowInsets.statusBars),
                header = {
                    Box(modifier = Modifier.padding(vertical = 16.dp)) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = "HB Logo",
                            tint = neonRed,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                NavigationRailItem(
                    selected = activeTab == DashboardTab.Home,
                    onClick = { onTabSelected(DashboardTab.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = neonRed,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationRailItem(
                    selected = activeTab == DashboardTab.Media,
                    onClick = { onTabSelected(DashboardTab.Media) },
                    icon = { Icon(Icons.Default.MovieFilter, contentDescription = "Media") },
                    label = { Text("Catalog", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = neonRed,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationRailItem(
                    selected = activeTab == DashboardTab.MyList,
                    onClick = { onTabSelected(DashboardTab.MyList) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks") },
                    label = { Text("My List", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = neonRed,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationRailItem(
                    selected = activeTab == DashboardTab.Blog,
                    onClick = { onTabSelected(DashboardTab.Blog) },
                    icon = { Icon(Icons.Default.Feed, contentDescription = "Blog") },
                    label = { Text("Blog", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = neonRed,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationRailItem(
                    selected = activeTab == DashboardTab.Settings,
                    onClick = { onTabSelected(DashboardTab.Settings) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Members") },
                    label = { Text("Members", fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = neonRed,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
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
                        label = { Text("Home", fontSize = 11.sp) },
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
                        label = { Text("Catalog", fontSize = 11.sp) },
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
                        label = { Text("My List", fontSize = 11.sp) },
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
                        label = { Text("Blog", fontSize = 11.sp) },
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
                        label = { Text("Members", fontSize = 11.sp) },
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

// -------------------------------------------------------------
// 9. DEDICATED TV SHOWS & MOVIES CATEGORIZED GRID WITH FILTERS
// -------------------------------------------------------------
@Composable
fun MediaCatalogGridScreen(
    viewModel: MainViewModel,
    onMediaSelected: (MediaItem) -> Unit
) {
    val allMedia by viewModel.allMedia.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") } // All, Movies, TV Shows

    val filteredItems = remember(allMedia, selectedFilter) {
        when (selectedFilter) {
            "Movies" -> allMedia.filter { it.type == "Movie" }
            "TV Shows" -> allMedia.filter { it.type == "TV Show" }
            else -> allMedia
        }
    }

    val neonRed = Color(0xFFEF4444)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
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
                text = "Premium categorized movies & TV show mirrors",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Subcategory filters (All, Movies, TV Shows)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val subFilters = listOf("All", "Movies", "TV Shows")
            for (filter in subFilters) {
                val active = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) neonRed else Color(0xFF0D0D0D))
                        .border(1.dp, if (active) neonRed else Color(0xFF1F1F1F), RoundedCornerShape(8.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    MediaThumbnailCard(media = item, onClick = onMediaSelected)
                }
            }
        }
    }
}
