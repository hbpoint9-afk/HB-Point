package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BloggerPost
import com.example.data.MediaItem
import com.example.data.MediaRepository
import com.example.data.UserAccount
import com.example.data.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val name: String, val email: String, val isAdmin: Boolean) : AuthResult()
    data class AdminCodeRequired(val email: String, val name: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MediaRepository(application, database.mediaDao(), database.userDao())
    val preferences = UserPreferences(application)

    // Streaming item and player states
    private val _selectedMedia = MutableStateFlow<MediaItem?>(null)
    val selectedMedia: StateFlow<MediaItem?> = _selectedMedia.asStateFlow()

    private val _currentStreamUrl = MutableStateFlow("")
    val currentStreamUrl: StateFlow<String> = _currentStreamUrl.asStateFlow()

    private val _currentServerName = MutableStateFlow("")
    val currentServerName: StateFlow<String> = _currentServerName.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Filter and search states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Expose all media items directly from repository
    val allMedia: StateFlow<List<MediaItem>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All registered users for Admin panel
    val allUsers: StateFlow<List<UserAccount>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists matching search query and category filters
    val filteredMedia: StateFlow<List<MediaItem>> = combine(
        allMedia,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.cast.contains(query, ignoreCase = true) ||
                    item.type.contains(query, ignoreCase = true)

            val matchesCategory = when (category) {
                "All" -> true
                "Movies" -> item.type.equals("Movie", ignoreCase = true)
                "TV Shows" -> item.type.equals("TV Show", ignoreCase = true)
                "Anime" -> item.type.equals("Anime", ignoreCase = true) || item.category.contains("Anime", ignoreCase = true)
                else -> item.category.contains(category, ignoreCase = true) || item.type.equals(category, ignoreCase = true)
            }
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedMedia: StateFlow<List<MediaItem>> = repository.bookmarkedMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bloggerPosts: StateFlow<List<BloggerPost>> = repository.bloggerPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _bloggerSyncing = MutableStateFlow(false)
    val bloggerSyncing: StateFlow<Boolean> = _bloggerSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            // Check and pre-populate local media catalogs and initial users
            repository.checkAndPrepopulate()
            // Sync RSS Blogger feed initially
            syncBlogger()
        }
    }

    fun syncBlogger(customUrl: String? = null) {
        viewModelScope.launch {
            _bloggerSyncing.value = true
            repository.syncBloggerFeed(customUrl)
            _bloggerSyncing.value = false
        }
    }

    fun selectMedia(item: MediaItem?) {
        _selectedMedia.value = item
        if (item == null) {
            _currentStreamUrl.value = ""
            _currentServerName.value = ""
            return
        }
        val servers = item.getServerList()
        if (servers.isNotEmpty()) {
            _currentStreamUrl.value = servers[0].second
            _currentServerName.value = servers[0].first
        } else if (item.getEpisodeList().isNotEmpty()) {
            val firstEp = item.getEpisodeList()[0]
            _currentStreamUrl.value = firstEp.streamUrl
            _currentServerName.value = firstEp.title
        } else {
            _currentStreamUrl.value = item.downloadLink
            _currentServerName.value = "Direct Stream"
        }
    }

    fun selectServer(name: String, url: String) {
        _currentServerName.value = name
        _currentStreamUrl.value = url
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleBookmark(id: Int) {
        viewModelScope.launch {
            repository.toggleBookmark(id)
        }
    }

    fun toggleMuteSound() {
        _isMuted.value = !_isMuted.value
    }

    // -------------------------------------------------------------
    // USER REGISTRATION & LOGIN LOGIC
    // -------------------------------------------------------------
    fun registerUser(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            onResult(false, "Please fill in all fields (Name, Gmail, Password)")
            return
        }
        if (!email.contains("@") || !email.contains(".")) {
            onResult(false, "Please enter a valid Gmail / Email address")
            return
        }
        if (password.length < 4) {
            onResult(false, "Password must be at least 4 characters")
            return
        }

        viewModelScope.launch {
            val normalizedEmail = email.trim().lowercase()
            val isAdminAccount = normalizedEmail == "hbpoint9@gmail.com"
            val success = repository.registerUser(
                name = name.trim(),
                email = normalizedEmail,
                password = password,
                isAdmin = isAdminAccount
            )
            if (success) {
                // If the registered user is the designated admin account, require code or log in
                if (isAdminAccount && password == "@ansh0281") {
                    onResult(true, "ADMIN_CODE_REQUIRED")
                } else {
                    preferences.setLoggedInUser(name = name.trim(), email = normalizedEmail, isAdmin = false)
                    onResult(true, "Registration successful!")
                }
            } else {
                onResult(false, "This Gmail address is already registered. Please sign in.")
            }
        }
    }

    fun loginUser(
        email: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(AuthResult.Error("Please enter both Gmail and Password"))
            return
        }

        val normalizedEmail = email.trim().lowercase()

        // Admin designated check: hbpoint9@gmail.com and @ansh0281
        if (normalizedEmail == "hbpoint9@gmail.com" && password == "@ansh0281") {
            onResult(AuthResult.AdminCodeRequired(email = normalizedEmail, name = "Admin HB"))
            return
        }

        viewModelScope.launch {
            val user = repository.authenticateUser(normalizedEmail, password)
            if (user != null) {
                if (user.isAdmin || (normalizedEmail == "hbpoint9@gmail.com" && password == "@ansh0281")) {
                    onResult(AuthResult.AdminCodeRequired(email = user.email, name = user.name))
                } else {
                    preferences.setLoggedInUser(name = user.name, email = user.email, isAdmin = false)
                    onResult(AuthResult.Success(name = user.name, email = user.email, isAdmin = false))
                }
            } else {
                onResult(AuthResult.Error("Invalid Gmail or Password. Please try again or create an account."))
            }
        }
    }

    fun verifyAdminCode(code: String, email: String, name: String): Boolean {
        return if (code.trim() == "0281") {
            preferences.setLoggedInUser(
                name = if (name.isNotBlank()) name else "Admin HB",
                email = if (email.isNotBlank()) email else "hbpoint9@gmail.com",
                isAdmin = true
            )
            true
        } else {
            false
        }
    }

    fun logout() {
        preferences.logout()
        _selectedMedia.value = null
    }

    // -------------------------------------------------------------
    // ADMIN MEDIA MANAGEMENT (Add, Edit, Delete Movie/Anime/TV Show)
    // -------------------------------------------------------------
    fun saveMedia(
        id: Int = 0,
        title: String,
        type: String, // Movie, TV Show, Anime
        category: String,
        poster: String,
        backdrop: String,
        desc: String,
        rating: String,
        year: String,
        cast: String,
        trailer: String,
        streamServers: String,
        downloadLink: String,
        fileSize: String,
        episodes: String,
        isTrending: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val fallbackPoster = when (type) {
                "Anime" -> "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=400&q=80"
                "TV Show" -> "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=400&q=80"
                else -> "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=400&q=80"
            }
            val fallbackBackdrop = when (type) {
                "Anime" -> "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=800&q=80"
                "TV Show" -> "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?auto=format&fit=crop&w=800&q=80"
                else -> "https://images.unsplash.com/photo-1539683255143-73a6b838b106?auto=format&fit=crop&w=800&q=80"
            }

            val item = MediaItem(
                id = id,
                title = title.trim(),
                type = type,
                category = if (category.isNotBlank()) category.trim() else if (type == "Anime") "Anime" else "Action",
                posterUrl = if (poster.isNotBlank()) poster.trim() else fallbackPoster,
                backdropUrl = if (backdrop.isNotBlank()) backdrop.trim() else fallbackBackdrop,
                description = if (desc.isNotBlank()) desc.trim() else "No detailed description provided.",
                rating = if (rating.isNotBlank()) rating.trim() else "9.0",
                releaseYear = if (year.isNotBlank()) year.trim() else "2026",
                cast = if (cast.isNotBlank()) cast.trim() else "HB Studios Cast",
                trailerLink = if (trailer.isNotBlank()) trailer.trim() else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                streamServers = streamServers.trim(),
                downloadLink = downloadLink.trim(),
                fileSize = if (fileSize.isNotBlank()) fileSize.trim() else "1.4 GB",
                episodes = episodes.trim(),
                isTrending = isTrending,
                isRecentlyAdded = true
            )

            if (id == 0) {
                repository.insertMedia(item)
            } else {
                repository.updateMedia(item)
                if (_selectedMedia.value?.id == id) {
                    _selectedMedia.value = item
                }
            }
            onComplete()
        }
    }

    fun deleteMediaItem(id: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteMediaById(id)
            if (_selectedMedia.value?.id == id) {
                _selectedMedia.value = null
            }
            onComplete()
        }
    }

    // Cinematic custom sound synthesizer for launch (Loud, Crystal-Clear & Resonant)
    fun playLaunchSound() {
        if (_isMuted.value) return
        Thread {
            try {
                val sampleRate = 44100
                val duration = 2.2 // 2.2 seconds
                val numSamples = (duration * sampleRate).toInt()
                val sample = DoubleArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate

                    // 1. Crisp Opening Strike & Whoosh (t: 0 -> 0.45s)
                    var strike = 0.0
                    if (t < 0.45) {
                        val strikeEnv = Math.exp(-t * 9.0)
                        val strikeFreq = 650.0 * Math.exp(-t * 8.0) + 180.0
                        strike = Math.sin(2.0 * Math.PI * strikeFreq * t) * 0.45 * strikeEnv
                        // High sparkle shimmer
                        strike += Math.sin(2.0 * Math.PI * 1800.0 * t) * 0.12 * strikeEnv
                    }

                    // 2. Punchy Mid & Warm Bass (audible on both phone speakers & earphones)
                    val bassFreq = 90.0 + 35.0 * Math.exp(-t * 3.0)
                    val bassEnv = if (t < 0.08) t / 0.08 else Math.exp(-(t - 0.08) * 1.2)
                    val bass = (Math.sin(2.0 * Math.PI * bassFreq * t) * 0.55 +
                               Math.sin(2.0 * Math.PI * (bassFreq * 2.0) * t) * 0.35) * bassEnv

                    // 3. Cinematic Majestic Chord Swell (t: 0.15 -> 2.2s)
                    var chord = 0.0
                    if (t > 0.12) {
                        val chordTime = t - 0.12
                        val swell = if (chordTime < 0.35) {
                            chordTime / 0.35
                        } else {
                            Math.exp(-(chordTime - 0.35) * 1.4)
                        }

                        // Rich layered harmonic chord: C3 (130.8Hz), G3 (196.0Hz), C4 (261.6Hz), E4 (329.6Hz), G4 (392.0Hz), C5 (523.25Hz)
                        val c3 = Math.sin(2.0 * Math.PI * 130.81 * t) * 0.35
                        val g3 = Math.sin(2.0 * Math.PI * 196.00 * t) * 0.30
                        val c4 = Math.sin(2.0 * Math.PI * 261.63 * t) * 0.35
                        val e4 = Math.sin(2.0 * Math.PI * 329.63 * t) * 0.28
                        val g4 = Math.sin(2.0 * Math.PI * 392.00 * t) * 0.22
                        val c5 = Math.sin(2.0 * Math.PI * 523.25 * t) * 0.15

                        chord = (c3 + g3 + c4 + e4 + g4 + c5) * swell
                    }

                    // Master output sum
                    sample[i] = strike + bass + chord
                }

                // Find peak for full normalization to maximize loudness without clipping
                var maxPeak = 0.0001
                for (s in sample) {
                    val abs = Math.abs(s)
                    if (abs > maxPeak) maxPeak = abs
                }

                val buffer = ShortArray(numSamples)
                val targetMax = 31500.0 // Near max 16-bit PCM for loud, clear volume
                val scale = targetMax / maxPeak

                for (i in 0 until numSamples) {
                    val normalized = (sample[i] * scale).coerceIn(-32767.0, 32767.0)
                    buffer[i] = normalized.toInt().toShort()
                }

                val audioTrack = android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    android.media.AudioTrack.MODE_STATIC
                )
                audioTrack.setVolume(1.0f)
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                Thread.sleep((duration * 1000).toLong())
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
