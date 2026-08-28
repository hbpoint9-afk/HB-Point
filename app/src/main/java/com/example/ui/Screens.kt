package com.example.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.BloggerPost
import com.example.data.EpisodeItem
import com.example.data.MediaItem
import com.example.data.UserAccount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val NeonRed = Color(0xFFEF4444)
val NeonRedDark = Color(0xFFDC2626)
val CardDark = Color(0xFF0D0D0D)
val BorderDark = Color(0xFF1F1F1F)
val GoldAccent = Color(0xFFFFB300)

// -------------------------------------------------------------
// 1. LAUNCH SCREEN (Cinematic Intro)
// -------------------------------------------------------------
@Composable
fun LaunchScreen(
    viewModel: MainViewModel,
    onFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.playLaunchSound()
        startAnimation = true
        delay(2400)
        onFinished()
    }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("launch_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = NeonRedDark.copy(alpha = 0.2f),
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 8.dp.toPx())
                    )
                    rotate(rotationAngle) {
                        drawArc(
                            color = NeonRed,
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
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "HB Point Logo Play",
                    tint = NeonRed,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(1000)) + expandVertically(animationSpec = tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HB POINT",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cinematic Movies, Anime & Series",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

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
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Mute Toggle",
                    tint = Color.White
                )
            }
            Button(
                onClick = onFinished,
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("ENTER", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 2. AUTHENTICATION (LOGIN & REGISTRATION SCREEN)
// -------------------------------------------------------------
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }

    // Form inputs
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Admin Verification Dialog State
    var showAdminCodeDialog by remember { mutableStateOf(false) }
    var adminCodeInput by remember { mutableStateOf("") }
    var adminCodeError by remember { mutableStateOf(false) }
    var pendingAdminEmail by remember { mutableStateOf("") }
    var pendingAdminName by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // App Branding Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, NeonRed, CircleShape)
                    .background(Color(0xFF141414)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "HB Point Logo",
                    tint = NeonRed,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HB POINT",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = if (isRegisterMode) "Create an account to start streaming" else "Sign in to access your entertainment hub",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Tab Switcher: Sign In vs Register
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isRegisterMode) NeonRed else Color.Transparent)
                            .clickable {
                                isRegisterMode = false
                                errorMessage = ""
                                successMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRegisterMode) NeonRed else Color.Transparent)
                            .clickable {
                                isRegisterMode = true
                                errorMessage = ""
                                successMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Inputs Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isRegisterMode) {
                        // Name Input for Registration
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it; errorMessage = "" },
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Name", tint = Color.Gray)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonRed,
                                unfocusedBorderColor = BorderDark
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("register_name_input")
                        )
                    }

                    // Gmail / Email Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it; errorMessage = "" },
                        label = { Text("Gmail / Email Address") },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Gmail", tint = Color.Gray)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                    )

                    // Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it; errorMessage = "" },
                        label = { Text(if (isRegisterMode) "Create Password" else "Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color.Gray)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = Color.Gray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                    )

                    // Error or Success Banner
                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            color = NeonRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (successMessage.isNotBlank()) {
                        Text(
                            text = successMessage,
                            color = Color.Green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Action Button
                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                viewModel.registerUser(
                                    name = nameInput,
                                    email = emailInput,
                                    password = passwordInput
                                ) { success, msg ->
                                    if (success) {
                                        if (msg == "ADMIN_CODE_REQUIRED") {
                                            pendingAdminEmail = emailInput.trim().lowercase()
                                            pendingAdminName = nameInput.trim()
                                            showAdminCodeDialog = true
                                        } else {
                                            successMessage = msg
                                            onLoginSuccess()
                                        }
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            } else {
                                viewModel.loginUser(
                                    email = emailInput,
                                    password = passwordInput
                                ) { result ->
                                    when (result) {
                                        is AuthResult.Success -> {
                                            onLoginSuccess()
                                        }
                                        is AuthResult.AdminCodeRequired -> {
                                            pendingAdminEmail = result.email
                                            pendingAdminName = result.name
                                            showAdminCodeDialog = true
                                        }
                                        is AuthResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_action_button")
                    ) {
                        Text(
                            text = if (isRegisterMode) "REGISTER & ENTER" else "SIGN IN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick member switch helper info
            Text(
                text = if (isRegisterMode) "Already registered? Switch to Sign In above." else "New here? Switch to Register to create a profile.",
                fontSize = 12.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }

        // -------------------------------------------------------------
        // ADMIN SECURITY CODE POPUP / DIALOG (Code: 0281)
        // -------------------------------------------------------------
        if (showAdminCodeDialog) {
            Dialog(
                onDismissRequest = {
                    showAdminCodeDialog = false
                    adminCodeInput = ""
                    adminCodeError = false
                },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
                    border = BorderStroke(1.5.dp, GoldAccent),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("admin_code_dialog")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Security Shield",
                                tint = GoldAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Admin Security Code",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Master Administrator credentials recognized for $pendingAdminEmail.\nEnter your 4-digit Admin Code to unlock.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = adminCodeInput,
                            onValueChange = {
                                if (it.length <= 4) {
                                    adminCodeInput = it
                                    adminCodeError = false
                                }
                            },
                            label = { Text("4-Digit Admin Code") },
                            placeholder = { Text("••••") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = BorderDark
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("admin_code_input")
                        )

                        if (adminCodeError) {
                            Text(
                                text = "Invalid Admin Code. Please try again.",
                                color = NeonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showAdminCodeDialog = false
                                    adminCodeInput = ""
                                    adminCodeError = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cancel", color = Color.Gray)
                            }

                            Button(
                                onClick = {
                                    val verified = viewModel.verifyAdminCode(
                                        code = adminCodeInput,
                                        email = pendingAdminEmail,
                                        name = pendingAdminName
                                    )
                                    if (verified) {
                                        showAdminCodeDialog = false
                                        onLoginSuccess()
                                    } else {
                                        adminCodeError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("admin_code_submit")
                            ) {
                                Text("VERIFY", color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. HOME DASHBOARD SCREEN
// -------------------------------------------------------------
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onMediaSelected: (MediaItem) -> Unit
) {
    val allMedia by viewModel.allMedia.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val profileName by viewModel.preferences.profileName
    val isAdmin by viewModel.preferences.isAdmin

    val filteredList by viewModel.filteredMedia.collectAsState()

    val featuredItem = remember(allMedia) {
        allMedia.firstOrNull { it.isTrending } ?: allMedia.firstOrNull()
    }

    val animeItems = remember(allMedia) {
        allMedia.filter { it.type.equals("Anime", ignoreCase = true) || it.category.contains("Anime", ignoreCase = true) }
    }

    val movieItems = remember(allMedia) {
        allMedia.filter { it.type.equals("Movie", ignoreCase = true) }
    }

    val tvItems = remember(allMedia) {
        allMedia.filter { it.type.equals("TV Show", ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Top Bar: Profile & Admin Badge
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isAdmin) GoldAccent else NeonRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = profileName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isAdmin) GoldAccent.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAdmin) "SUPER ADMIN" else "MEMBER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isAdmin) GoldAccent else NeonRed
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF141414))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "HB POINT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonRed
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search movies, anime, series, genres...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonRed,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("search_field")
                )
            }
        }

        // Genre / Category Filter Chips
        item {
            val categories = listOf("All", "Anime", "Movies", "TV Shows", "Sci-Fi", "Action", "Thriller")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) NeonRed else CardDark)
                            .border(1.dp, if (isSelected) NeonRed else BorderDark, RoundedCornerShape(20.dp))
                            .clickable { viewModel.setSelectedCategory(cat) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else Color.LightGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Search Results View if Searching or Filtering
        if (searchQuery.isNotBlank() || selectedCategory != "All") {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Results (${filteredList.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { media ->
                        MediaThumbnailCard(media = media, onClick = onMediaSelected)
                    }
                }
            }
        } else {
            // Hero Billboard Banner
            featuredItem?.let { hero ->
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onMediaSelected(hero) }
                    ) {
                        AsyncImage(
                            model = hero.backdropUrl,
                            contentDescription = hero.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.img_hbpoint_logo),
                            error = painterResource(id = R.drawable.img_hbpoint_logo)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                        startY = 60f
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonRed)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("FEATURED SPOTLIGHT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                            Text(hero.title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("${hero.releaseYear} • ${hero.category} • ⭐ ${hero.rating}", fontSize = 12.sp, color = Color.LightGray)
                            Text(hero.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // Anime Showcase Shelf
            if (animeItems.isNotEmpty()) {
                item {
                    MediaSectionShelf(
                        title = "🔥 Popular & Trending Anime",
                        subtitle = "Stream & download full episodes in HD",
                        items = animeItems,
                        onMediaSelected = onMediaSelected
                    )
                }
            }

            // Blockbuster Movies Shelf
            if (movieItems.isNotEmpty()) {
                item {
                    MediaSectionShelf(
                        title = "🎬 Blockbuster Movies",
                        subtitle = "High-speed multi-server streaming mirrors",
                        items = movieItems,
                        onMediaSelected = onMediaSelected
                    )
                }
            }

            // TV Series Shelf
            if (tvItems.isNotEmpty()) {
                item {
                    MediaSectionShelf(
                        title = "📺 Binge-Worthy TV Series",
                        subtitle = "Seasons and episodes ready for streaming",
                        items = tvItems,
                        onMediaSelected = onMediaSelected
                    )
                }
            }
        }
    }
}

@Composable
fun MediaSectionShelf(
    title: String,
    subtitle: String,
    items: List<MediaItem>,
    onMediaSelected: (MediaItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { media ->
                MediaThumbnailCard(media = media, onClick = onMediaSelected)
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
            .width(136.dp)
            .clickable { onClick(media) }
            .testTag("media_card_${media.id}"),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = media.posterUrl,
                    contentDescription = media.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.img_hbpoint_logo),
                    error = painterResource(id = R.drawable.img_hbpoint_logo)
                )

                // Rating overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = GoldAccent, modifier = Modifier.size(10.dp))
                        Text(media.rating, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Type Badge (e.g. Anime / Movie)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .background(
                            when (media.type) {
                                "Anime" -> Color(0xFF8B5CF6)
                                "TV Show" -> Color(0xFF3B82F6)
                                else -> NeonRed
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(media.type.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = media.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${media.releaseYear} • ${media.fileSize}",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
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
            .background(Color.Black)
            .statusBarsPadding()
            .testTag("my_list_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "My Watchlist",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Saved anime, movies & series for instant access",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

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
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Watchlist is Empty",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the bookmark icon on any movie or anime to save it here.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
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
// 5. GOOGLE BLOGGER RSS FEED (NEWS & EDITORIALS)
// -------------------------------------------------------------
@Composable
fun BloggerScreen(viewModel: MainViewModel) {
    val posts by viewModel.bloggerPosts.collectAsState()
    val syncing by viewModel.bloggerSyncing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .testTag("blogger_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HB Point News",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Release announcements & updates",
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
                    tint = if (syncing) Color.Gray else NeonRed
                )
            }
        }

        if (syncing && posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonRed)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
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
                    try {
                        uriHandler.openUri(post.url)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            .testTag("blogger_card_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, BorderDark),
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
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${post.published} • ${post.author}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = post.content,
                fontSize = 12.sp,
                color = Color.LightGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "READ MORE →",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonRed
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 6. STREAMING PLAYER OVERLAY & DETAILS (WITH DOWNLOADS & EPISODES)
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
    val isAdmin by viewModel.preferences.isAdmin

    var screenRatioMode by remember { mutableStateOf("FIT") }
    var isFullscreen by remember { mutableStateOf(false) }
    var showGestureIndicator by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }

    val episodeList = remember(media.episodes) {
        media.getEpisodeList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(
                if (!isFullscreen) {
                    Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                } else Modifier
            )
            .testTag("player_overlay")
    ) {
        if (isFullscreen) {
            // Fullscreen player taking 100% of the screen
            UniversalStreamingPlayer(
                rawUrl = currentUrl,
                title = media.title,
                modifier = Modifier.fillMaxSize(),
                screenRatioMode = screenRatioMode,
                isFullscreen = true,
                onToggleFullscreen = { isFullscreen = false },
                onToggleRatio = {
                    screenRatioMode = when (screenRatioMode) {
                        "FIT" -> "FILL"
                        "FILL" -> "STRETCH"
                        else -> "FIT"
                    }
                }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Player Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close player", tint = Color.White)
                        }
                        Column {
                            Text(media.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${media.type} • ${media.category}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Admin Quick Edit Button
                        if (isAdmin) {
                            IconButton(
                                onClick = { showEditDialog = true },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoldAccent.copy(alpha = 0.2f))
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Admin Edit Media", tint = GoldAccent, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Aspect Ratio Button
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

                // Universal Video Player Container Frame (ExoPlayer + BunnyStream & Embed HTML5 Web Engine)
                UniversalStreamingPlayer(
                    rawUrl = currentUrl,
                    title = media.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    screenRatioMode = screenRatioMode,
                    isFullscreen = false,
                    onToggleFullscreen = { isFullscreen = true },
                    onToggleRatio = {
                        screenRatioMode = when (screenRatioMode) {
                            "FIT" -> "FILL"
                            "FILL" -> "STRETCH"
                            else -> "FIT"
                        }
                    }
                )

                // Media Detail, Stream Mirror Selectors, Episode List & Download Action
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title and Meta tags
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(media.title, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.toggleBookmark(media.id) }) {
                                Icon(
                                    imageVector = if (media.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (media.isBookmarked) NeonRed else Color.White
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
                                    .background(NeonRed, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("⭐ ${media.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("${media.releaseYear} • ${media.type} • ${media.category}", fontSize = 12.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Size: ${media.fileSize}", fontSize = 10.sp, color = Color.LightGray)
                            }
                        }
                    }
                }

                // Download Button (Direct link action)
                item {
                    Button(
                        onClick = {
                            val downloadTarget = if (media.downloadLink.isNotBlank()) media.downloadLink else currentUrl
                            if (downloadTarget.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadTarget))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(1.dp, NeonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("download_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = NeonRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DOWNLOAD MOVIE (${media.fileSize})",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }

                // Multi-Server Stream Mirror Chips
                item {
                    Column {
                        Text(
                            text = "Stream Servers / Mirrors",
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
                            if (servers.isNotEmpty()) {
                                for (srv in servers) {
                                    val isActive = srv.first == serverName
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isActive) NeonRed else CardDark)
                                            .border(1.dp, if (isActive) NeonRed else BorderDark, RoundedCornerShape(8.dp))
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
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonRed)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("Primary Stream Mirror", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Episode List for Anime and TV Shows
                if (media.type == "Anime" || media.type == "TV Show" || episodeList.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Episodes & Downloads (${if (episodeList.isNotEmpty()) episodeList.size else 3} Episodes)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            if (episodeList.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    episodeList.forEachIndexed { index, ep ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CardDark),
                                            border = BorderStroke(1.dp, BorderDark),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = ep.title,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "Size: ${ep.size}",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                }

                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    // Stream Episode
                                                    IconButton(
                                                        onClick = {
                                                            if (ep.streamUrl.isNotBlank()) {
                                                                viewModel.selectServer(ep.title, ep.streamUrl)
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonRed)
                                                    ) {
                                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Episode", tint = Color.White, modifier = Modifier.size(18.dp))
                                                    }

                                                    // Download Episode
                                                    IconButton(
                                                        onClick = {
                                                            val dl = if (ep.downloadUrl.isNotBlank()) ep.downloadUrl else ep.streamUrl
                                                            if (dl.isNotBlank()) {
                                                                try {
                                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dl))
                                                                    context.startActivity(intent)
                                                                } catch (e: Exception) {
                                                                    e.printStackTrace()
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF1E1E1E))
                                                    ) {
                                                        Icon(Icons.Default.Download, contentDescription = "Download Episode", tint = Color.White, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Default anime sample episodes
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Episode 1: The Beginning", "Episode 2: The Battle", "Episode 3: Climax").forEachIndexed { idx, title ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CardDark),
                                            border = BorderStroke(1.dp, BorderDark),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                                    Text("Size: 350 MB", fontSize = 10.sp, color = Color.Gray)
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.selectServer(title, "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
                                                    },
                                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(NeonRed)
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Synopsis & Cast
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Synopsis & Detail", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(media.description, fontSize = 13.sp, color = Color.LightGray, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Starring Cast", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(media.cast, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    // Admin Edit Media Dialog
    if (showEditDialog) {
        MediaEditorDialog(
            viewModel = viewModel,
            initialMedia = media,
            onDismiss = { showEditDialog = false }
        )
    }
}
}

// -------------------------------------------------------------
// 7. MEMBERS & SETTINGS SCREEN (EQUAL MEMBERSHIP & ADMIN HUB)
// -------------------------------------------------------------
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onOpenCurator: () -> Unit
) {
    val profileName by viewModel.preferences.profileName
    val profileEmail by viewModel.preferences.profileEmail
    val isTvMode by viewModel.preferences.isTvMode
    val isAdmin by viewModel.preferences.isAdmin

    var newNameValue by remember { mutableStateOf(profileName) }
    var saveNameSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
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

        // Member Profile Card (Equal Status)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, if (isAdmin) GoldAccent else BorderDark),
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
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, if (isAdmin) GoldAccent else NeonRed, CircleShape)
                            .background(Color(0xFF141414)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = if (isAdmin) GoldAccent else NeonRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(profileName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        if (profileEmail.isNotBlank()) {
                            Text(profileEmail, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            text = if (isAdmin) "Super Administrator • Full Control" else "HB Community Member",
                            fontSize = 11.sp,
                            color = if (isAdmin) GoldAccent else NeonRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Admin Command Center Portal (Only for Admins)
        if (isAdmin) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161205)),
                    border = BorderStroke(1.5.dp, GoldAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = GoldAccent)
                            Text("ADMIN CONTROL PANEL", color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                        Text(
                            text = "Manage registered members, add & edit movies/anime, configure stream & download links, sizes, and anime episodes.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Button(
                            onClick = onOpenCurator,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("open_admin_panel_button")
                        ) {
                            Text("OPEN ADMIN PANEL", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // TV Mode Switch
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tablet & TV Rail Layout", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Switch to sidebar navigation for larger screens & tablets.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isTvMode,
                        onCheckedChange = { viewModel.preferences.setTvMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonRed,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF1E1E1E)
                        )
                    )
                }
            }
        }

        // Edit Profile Name
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, BorderDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Edit Display Name", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    OutlinedTextField(
                        value = newNameValue,
                        onValueChange = { newNameValue = it },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = BorderDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (newNameValue.isNotBlank()) {
                                viewModel.preferences.setProfileName(newNameValue.trim())
                                saveNameSuccess = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("SAVE NAME", fontWeight = FontWeight.Bold)
                    }

                    if (saveNameSuccess) {
                        Text("Name updated successfully!", color = Color.Green, fontSize = 12.sp)
                    }
                }
            }
        }

        // Logout Button
        item {
            Button(
                onClick = {
                    viewModel.logout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141414)),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = NeonRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOG OUT ACCOUNT", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// 8. ADMIN PANEL (MANAGE REGISTERED MEMBERS & MOVIES/ANIME CRUD)
// -------------------------------------------------------------
@Composable
fun CuratorScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val allMedia by viewModel.allMedia.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var selectedAdminTab by remember { mutableStateOf(0) } // 0: Registered Members, 1: Media Catalog
    var mediaTypeFilter by remember { mutableStateOf("All") } // All, Movies, TV Shows, Anime

    var memberSearchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var mediaToEdit by remember { mutableStateOf<MediaItem?>(null) }
    var mediaToDelete by remember { mutableStateOf<MediaItem?>(null) }

    val filteredUsers = remember(allUsers, memberSearchQuery) {
        if (memberSearchQuery.isBlank()) allUsers
        else allUsers.filter {
            it.name.contains(memberSearchQuery, ignoreCase = true) ||
            it.email.contains(memberSearchQuery, ignoreCase = true)
        }
    }

    val filteredMediaList = remember(allMedia, mediaTypeFilter) {
        when (mediaTypeFilter) {
            "Movies" -> allMedia.filter { it.type.equals("Movie", ignoreCase = true) }
            "TV Shows" -> allMedia.filter { it.type.equals("TV Show", ignoreCase = true) }
            "Anime" -> allMedia.filter { it.type.equals("Anime", ignoreCase = true) || it.category.contains("Anime", ignoreCase = true) }
            else -> allMedia
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .testTag("admin_panel_screen")
    ) {
        // Admin Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Settings", tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("HB Admin Command Center", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Icon(Icons.Default.Verified, contentDescription = "Verified Admin", tint = GoldAccent, modifier = Modifier.size(16.dp))
                }
                Text("Logged in as: hbpoint9@gmail.com", fontSize = 11.sp, color = GoldAccent)
            }
        }

        // Top Navigation Tabs: Registered Members vs Movie & Anime Manager
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAdminTab == 0) GoldAccent else Color.Transparent)
                        .clickable { selectedAdminTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👥 Members (${allUsers.size})",
                        color = if (selectedAdminTab == 0) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAdminTab == 1) GoldAccent else Color.Transparent)
                        .clickable { selectedAdminTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎬 Movies & Anime (${allMedia.size})",
                        color = if (selectedAdminTab == 1) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB 1: REGISTERED MEMBERS SECTION
        if (selectedAdminTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Member Input
                OutlinedTextField(
                    value = memberSearchQuery,
                    onValueChange = { memberSearchQuery = it },
                    placeholder = { Text("Search member by name or Gmail...", color = Color.Gray, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderDark,
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("search_members_input")
                )

                Text(
                    text = "Newly Registered Members (${filteredUsers.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (filteredUsers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No registered members found.", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredUsers) { user ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CardDark),
                                border = BorderStroke(1.dp, if (user.isAdmin) GoldAccent else BorderDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("user_item_${user.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(if (user.isAdmin) GoldAccent else Color(0xFF1F1F1F)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (user.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                                contentDescription = "User avatar",
                                                tint = if (user.isAdmin) Color.Black else Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = user.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = user.email,
                                                fontSize = 12.sp,
                                                color = Color.LightGray
                                            )
                                            Text(
                                                text = "Joined: ${user.registeredAt}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (user.isAdmin) GoldAccent.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (user.isAdmin) "ADMIN" else "MEMBER",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (user.isAdmin) GoldAccent else NeonRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: MOVIE & ANIME MANAGEMENT (ADD, EDIT, DELETE)
        if (selectedAdminTab == 1) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Media Button & Subfilters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            mediaToEdit = null
                            showAddEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("admin_add_media_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ADD MOVIE / ANIME", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Filter chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("All", "Movies", "Anime").forEach { f ->
                            val active = mediaTypeFilter == f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) Color(0xFF1F1F1F) else Color.Transparent)
                                    .border(1.dp, if (active) GoldAccent else BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { mediaTypeFilter = f }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = f,
                                    fontSize = 11.sp,
                                    color = if (active) GoldAccent else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Media Inventory List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredMediaList) { media ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            border = BorderStroke(1.dp, BorderDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("admin_media_item_${media.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column {
                                        Text(
                                            text = media.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${media.type} • ${media.category} • ${media.fileSize}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "⭐ ${media.rating} • ${media.getServerList().size} Mirrors • ${media.getEpisodeList().size} Eps",
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Edit Button
                                    IconButton(
                                        onClick = {
                                            mediaToEdit = media
                                            showAddEditDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Media", tint = GoldAccent)
                                    }

                                    // Delete Button
                                    IconButton(
                                        onClick = {
                                            mediaToDelete = media
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Media", tint = NeonRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Media Full Sheet Dialog
        if (showAddEditDialog) {
            MediaEditorDialog(
                viewModel = viewModel,
                initialMedia = mediaToEdit,
                onDismiss = {
                    showAddEditDialog = false
                    mediaToEdit = null
                }
            )
        }

        // Delete Media Confirmation Dialog
        mediaToDelete?.let { item ->
            AlertDialog(
                onDismissRequest = { mediaToDelete = null },
                containerColor = Color(0xFF141414),
                title = { Text("Delete Media", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete '${item.title}' (${item.type}) from the database? This action is permanent.", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteMediaItem(item.id) {
                                mediaToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                    ) {
                        Text("DELETE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mediaToDelete = null }) {
                        Text("CANCEL", color = Color.Gray)
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 9. MEDIA EDITOR DIALOG (ADD & EDIT MOVIES, ANIME, EPISODES, STREAMS, SIZES)
// -------------------------------------------------------------
@Composable
fun MediaEditorDialog(
    viewModel: MainViewModel,
    initialMedia: MediaItem?,
    onDismiss: () -> Unit
) {
    val isEditing = initialMedia != null

    var title by remember { mutableStateOf(initialMedia?.title ?: "") }
    var type by remember { mutableStateOf(initialMedia?.type ?: "Movie") } // Movie, TV Show, Anime
    var category by remember { mutableStateOf(initialMedia?.category ?: if (initialMedia?.type == "Anime") "Anime" else "Action") }
    var description by remember { mutableStateOf(initialMedia?.description ?: "") }
    var rating by remember { mutableStateOf(initialMedia?.rating ?: "9.0") }
    var releaseYear by remember { mutableStateOf(initialMedia?.releaseYear ?: "2026") }
    var cast by remember { mutableStateOf(initialMedia?.cast ?: "") }
    var posterUrl by remember { mutableStateOf(initialMedia?.posterUrl ?: "") }
    var backdropUrl by remember { mutableStateOf(initialMedia?.backdropUrl ?: "") }
    var trailerLink by remember { mutableStateOf(initialMedia?.trailerLink ?: "") }
    var downloadLink by remember { mutableStateOf(initialMedia?.downloadLink ?: "") }
    var fileSize by remember { mutableStateOf(initialMedia?.fileSize ?: "1.4 GB") }

    // Stream servers setup
    var streamLink1 by remember {
        mutableStateOf(initialMedia?.getServerList()?.getOrNull(0)?.second ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
    }
    var streamLink2 by remember {
        mutableStateOf(initialMedia?.getServerList()?.getOrNull(1)?.second ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
    }

    // Anime / Series Episodes setup
    var episodesRaw by remember { mutableStateOf(initialMedia?.episodes ?: "") }

    // New Episode inputs for Anime
    var newEpTitle by remember { mutableStateOf("") }
    var newEpStream by remember { mutableStateOf("") }
    var newEpDownload by remember { mutableStateOf("") }
    var newEpSize by remember { mutableStateOf("350 MB") }

    var statusMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D0D0D))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            text = if (isEditing) "Edit ${initialMedia?.title}" else "Add New Media / Anime",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                statusMessage = "Please enter a valid title"
                                return@Button
                            }

                            val s1 = cleanStreamUrl(streamLink1)
                            val s2 = cleanStreamUrl(streamLink2)
                            val servers = mutableListOf<String>()
                            if (s1.isNotBlank()) servers.add("Server 1 BollyFast|$s1")
                            if (s2.isNotBlank()) servers.add("Server 2 Firedrop|$s2")
                            val serversSerialized = servers.joinToString(";;")

                            viewModel.saveMedia(
                                id = initialMedia?.id ?: 0,
                                title = title,
                                type = type,
                                category = category,
                                poster = posterUrl,
                                backdrop = backdropUrl,
                                desc = description,
                                rating = rating,
                                year = releaseYear,
                                cast = cast,
                                trailer = trailerLink,
                                streamServers = serversSerialized,
                                downloadLink = if (downloadLink.isNotBlank()) cleanStreamUrl(downloadLink) else s1,
                                fileSize = fileSize,
                                episodes = episodesRaw,
                                isTrending = initialMedia?.isTrending ?: true,
                                onComplete = onDismiss
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_media_button")
                    ) {
                        Text(if (isEditing) "SAVE CHANGES" else "PUBLISH", fontWeight = FontWeight.Bold)
                    }
                }
            },
            containerColor = Color.Black
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                if (statusMessage.isNotBlank()) {
                    item {
                        Text(statusMessage, color = NeonRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // 1. Basic Media Metadata
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("1. Media Identity & Classification", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Title *") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                modifier = Modifier.fillMaxWidth().testTag("edit_title_input")
                            )

                            // Media Type Selector: Movie, TV Show, Anime
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                listOf("Movie", "Anime", "TV Show").forEach { t ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = type == t,
                                            onClick = {
                                                type = t
                                                if (t == "Anime" && category == "Action") category = "Anime"
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = NeonRed)
                                        )
                                        Text(t, color = Color.White, fontSize = 13.sp)
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = { category = it },
                                    label = { Text("Genre / Category") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = rating,
                                    onValueChange = { rating = it },
                                    label = { Text("Rating (e.g. 9.4)") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = releaseYear,
                                    onValueChange = { releaseYear = it },
                                    label = { Text("Release Year") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = fileSize,
                                    onValueChange = { fileSize = it },
                                    label = { Text("Movie Size (e.g. 1.4 GB)") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                    modifier = Modifier.weight(1f).testTag("edit_filesize_input")
                                )
                            }

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Detail / Synopsis Description") },
                                minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                modifier = Modifier.fillMaxWidth().testTag("edit_desc_input")
                            )

                            OutlinedTextField(
                                value = cast,
                                onValueChange = { cast = it },
                                label = { Text("Cast / Voice Actors") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 2. Stream Links & Download Links
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, BorderDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("2. Movie Stream Links & Download Mirror", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)

                            OutlinedTextField(
                                value = streamLink1,
                                onValueChange = { streamLink1 = it },
                                label = { Text("Stream Server 1 (Primary Stream Link)") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                modifier = Modifier.fillMaxWidth().testTag("edit_stream1_input")
                            )

                            OutlinedTextField(
                                value = streamLink2,
                                onValueChange = { streamLink2 = it },
                                label = { Text("Stream Server 2 (Backup Mirror)") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = downloadLink,
                                onValueChange = { downloadLink = it },
                                label = { Text("Download Link (Direct MP4 / MKV link)") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                modifier = Modifier.fillMaxWidth().testTag("edit_download_input")
                            )

                            OutlinedTextField(
                                value = posterUrl,
                                onValueChange = { posterUrl = it },
                                label = { Text("Poster Image URL") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = backdropUrl,
                                onValueChange = { backdropUrl = it },
                                label = { Text("Backdrop Banner URL") },
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 3. Anime / TV Series Episode Manager
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, if (type == "Anime") GoldAccent else BorderDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "3. Anime / Series Episode Manager",
                                fontWeight = FontWeight.Bold,
                                color = if (type == "Anime") GoldAccent else Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Add and manage individual episode stream links, download links, and sizes for Anime & TV Series.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            // Add New Episode Form
                            Column(
                                modifier = Modifier
                                    .background(Color(0xFF141414), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Add Episode", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)

                                OutlinedTextField(
                                    value = newEpTitle,
                                    onValueChange = { newEpTitle = it },
                                    label = { Text("Episode Title (e.g. Episode 1: Awakening)") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = newEpStream,
                                    onValueChange = { newEpStream = it },
                                    label = { Text("Episode Stream URL") },
                                    placeholder = { Text("https://.../sample.mp4") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newEpDownload,
                                        onValueChange = { newEpDownload = it },
                                        label = { Text("Download Link") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = newEpSize,
                                        onValueChange = { newEpSize = it },
                                        label = { Text("Size (e.g. 350 MB)") },
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = NeonRed, unfocusedBorderColor = BorderDark),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (newEpTitle.isNotBlank()) {
                                            val stream = if (newEpStream.isNotBlank()) cleanStreamUrl(newEpStream) else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
                                            val dl = if (newEpDownload.isNotBlank()) cleanStreamUrl(newEpDownload) else stream
                                            val newEntry = "${newEpTitle.trim()}|$stream|$dl|${newEpSize.trim()}"
                                            episodesRaw = if (episodesRaw.isBlank()) newEntry else "$episodesRaw;;$newEntry"
                                            newEpTitle = ""
                                            newEpStream = ""
                                            newEpDownload = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (type == "Anime") GoldAccent else NeonRed),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("+ ADD EPISODE", color = if (type == "Anime") Color.Black else Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Current Episode List
                            val currentEps = remember(episodesRaw) {
                                if (episodesRaw.isBlank()) emptyList()
                                else episodesRaw.split(";;").mapNotNull { entry ->
                                    val parts = entry.split("|")
                                    if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                                        EpisodeItem(
                                            title = parts[0],
                                            streamUrl = if (parts.size > 1) parts[1] else "",
                                            downloadUrl = if (parts.size > 2) parts[2] else "",
                                            size = if (parts.size > 3) parts[3] else "350 MB"
                                        )
                                    } else null
                                }
                            }

                            if (currentEps.isNotEmpty()) {
                                Text("Current Episodes (${currentEps.size}):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    currentEps.forEachIndexed { index, ep ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF161616), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(ep.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("Size: ${ep.size}", color = Color.Gray, fontSize = 10.sp)
                                            }
                                            IconButton(
                                                onClick = {
                                                    val epList = episodesRaw.split(";;").toMutableList()
                                                    if (index < epList.size) {
                                                        epList.removeAt(index)
                                                        episodesRaw = epList.joinToString(";;")
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Episode", tint = NeonRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
