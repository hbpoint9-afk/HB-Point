package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BloggerPost
import com.example.data.MediaItem
import com.example.data.MediaRepository
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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MediaRepository(application, database.mediaDao())
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

    // Filtered lists matching search query and category filters
    val filteredMedia: StateFlow<List<MediaItem>> = combine(
        allMedia,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { item ->
            val matchesQuery = item.title.contains(query, ignoreCase = true) || 
                               item.category.contains(query, ignoreCase = true) ||
                               item.cast.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || item.category.equals(category, ignoreCase = true) || item.type.equals(category, ignoreCase = true)
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
            // Check and pre-populate local media catalogs
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
        } else {
            _currentStreamUrl.value = ""
            _currentServerName.value = ""
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

    // PIN profiling log-in flow
    fun loginWithPin(pin: String): Boolean {
        val storedPin = preferences.getPin()
        val success = pin == storedPin
        if (success) {
            preferences.setLoggedIn(true)
        }
        return success
    }

    // Admin/Curator methods
    fun verifyCuratorPin(pin: String): Boolean {
        val success = pin == "8888" // Curator override PIN
        if (success) {
            preferences.setAdmin(true)
        }
        return success
    }

    fun saveCuratorMedia(
        title: String,
        type: String,
        backdrop: String,
        poster: String,
        desc: String,
        rating: String,
        year: String,
        category: String,
        cast: String,
        trailer: String,
        serversList: List<Pair<String, String>>
    ) {
        viewModelScope.launch {
            // Serialize server streams
            val serversString = serversList.joinToString(";;") { "${it.first}|${it.second}" }
            val item = MediaItem(
                title = title,
                type = type,
                backdropUrl = if (backdrop.isNotBlank()) backdrop else "https://images.unsplash.com/photo-1539683255143-73a6b838b106?auto=format&fit=crop&w=800&q=80",
                posterUrl = if (poster.isNotBlank()) poster else "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=400&q=80",
                description = desc,
                rating = if (rating.isNotBlank()) rating else "8.0",
                releaseYear = if (year.isNotBlank()) year else "2026",
                category = category,
                cast = cast,
                trailerLink = trailer,
                isRecentlyAdded = true,
                streamServers = serversString
            )
            repository.insertMedia(item)
        }
    }

    fun updateMediaItem(item: MediaItem) {
        viewModelScope.launch {
            repository.updateMedia(item)
        }
    }

    fun deleteMediaItem(id: Int) {
        viewModelScope.launch {
            repository.deleteMediaById(id)
            if (_selectedMedia.value?.id == id) {
                _selectedMedia.value = null
            }
        }
    }

    // Cinematic custom sound synthesizer for launch
    fun playLaunchSound() {
        if (_isMuted.value) return
        Thread {
            try {
                val sampleRate = 44100
                val duration = 2.0 // 2 seconds
                val numSamples = (duration * sampleRate).toInt()
                val sample = DoubleArray(numSamples)
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    
                    // Low cinematic bass sweep: 50Hz sweeps up to 130Hz
                    val freqBass = 50.0 + (80.0 * (t / duration))
                    val bass = Math.sin(2.0 * Math.PI * freqBass * t) * 0.45
                    
                    // Harmonic chord swells after 0.4s
                    var chord = 0.0
                    if (t > 0.4) {
                        val swellFactor = Math.sin(Math.PI * (t - 0.4) / (duration - 0.4))
                        // Low warm minor/suspended chord (C - Eb - G)
                        chord += Math.sin(2.0 * Math.PI * 130.81 * t) * 0.20 // C3
                        chord += Math.sin(2.0 * Math.PI * 155.56 * t) * 0.15 // Eb3 (warm minor)
                        chord += Math.sin(2.0 * Math.PI * 196.00 * t) * 0.12 // G3
                        chord += Math.sin(2.0 * Math.PI * 261.63 * t) * 0.08 // C4
                        chord *= swellFactor
                    }
                    
                    // Master envelope to fade in and fade out smoothly (prevent clicks)
                    val envelope = if (t < 0.15) {
                        t / 0.15
                    } else if (t > duration - 0.3) {
                        (duration - t) / 0.3
                    } else {
                        1.0
                    }
                    sample[i] = (bass + chord) * envelope
                }
                
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    buffer[i] = (sample[i] * 32767).toInt().toShort()
                }
                
                val audioTrack = android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    buffer.size * 2,
                    android.media.AudioTrack.MODE_STATIC
                )
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
