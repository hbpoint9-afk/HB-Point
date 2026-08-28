package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaRepository(
    private val context: Context,
    private val mediaDao: MediaDao,
    private val userDao: UserDao
) {

    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllMedia()
    val bookmarkedMedia: Flow<List<MediaItem>> = mediaDao.getBookmarkedMedia()
    val bloggerPosts: Flow<List<BloggerPost>> = mediaDao.getAllBloggerPosts()
    val allUsers: Flow<List<UserAccount>> = userDao.getAllUsers()

    fun getMediaById(id: Int): Flow<MediaItem?> = mediaDao.getMediaById(id)

    suspend fun insertMedia(item: MediaItem): Long = withContext(Dispatchers.IO) {
        mediaDao.insertMedia(item)
    }

    suspend fun updateMedia(item: MediaItem) = withContext(Dispatchers.IO) {
        mediaDao.updateMedia(item)
    }

    suspend fun deleteMedia(item: MediaItem) = withContext(Dispatchers.IO) {
        mediaDao.deleteMedia(item)
    }

    suspend fun deleteMediaById(id: Int) = withContext(Dispatchers.IO) {
        mediaDao.deleteMediaById(id)
    }

    suspend fun toggleBookmark(id: Int) = withContext(Dispatchers.IO) {
        val current = mediaDao.getMediaById(id).first()
        if (current != null) {
            mediaDao.updateMedia(current.copy(isBookmarked = !current.isBookmarked))
        }
    }

    // User Authentication & Management methods
    suspend fun registerUser(name: String, email: String, password: String, isAdmin: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(email.trim())
        if (existing != null) {
            return@withContext false // Email already registered
        }
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val newUser = UserAccount(
            name = name.trim(),
            email = email.trim(),
            password = password,
            registeredAt = dateFormat.format(Date()),
            isAdmin = isAdmin
        )
        userDao.insertUser(newUser)
        true
    }

    suspend fun authenticateUser(email: String, password: String): UserAccount? = withContext(Dispatchers.IO) {
        userDao.authenticateUser(email.trim(), password)
    }

    suspend fun getUserByEmail(email: String): UserAccount? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email.trim())
    }

    // Initialize database with premium starting catalog (Movies, TV Shows, and Anime)
    suspend fun checkAndPrepopulate() = withContext(Dispatchers.IO) {
        // Pre-populate admin and sample users if empty
        if (userDao.getUserCount() == 0) {
            val adminUser = UserAccount(
                name = "Admin HB",
                email = "hbpoint9@gmail.com",
                password = "@ansh0281",
                registeredAt = "Aug 27, 2026",
                isAdmin = true
            )
            val demoUser = UserAccount(
                name = "Rahul Sharma",
                email = "rahul.sharma@gmail.com",
                password = "password123",
                registeredAt = "Aug 26, 2026",
                isAdmin = false
            )
            val demoUser2 = UserAccount(
                name = "Sneha Patel",
                email = "sneha.patel@gmail.com",
                password = "password123",
                registeredAt = "Aug 27, 2026",
                isAdmin = false
            )
            userDao.insertUser(adminUser)
            userDao.insertUser(demoUser)
            userDao.insertUser(demoUser2)
        }

        val existing = mediaDao.getAllMedia().first()
        if (existing.isEmpty()) {
            val startingItems = listOf(
                MediaItem(
                    title = "Cosmic Pulse",
                    type = "Movie",
                    backdropUrl = "https://images.unsplash.com/photo-1539683255143-73a6b838b106?auto=format&fit=crop&w=800&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1440404653325-ab127d49abc1?auto=format&fit=crop&w=400&q=80",
                    description = "An immersive journey through distant soundwaves and glowing neon nebulae. Follow a group of acoustic surveyors who stumble upon an intelligence broadcasting from the center of a supermassive black hole.",
                    rating = "9.4",
                    releaseYear = "2026",
                    category = "Sci-Fi",
                    cast = "Kaelen Stark, Lyra Vance, Orion Cole",
                    trailerLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    isTrending = true,
                    isRecentlyAdded = true,
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4;;Server 3 HexaPlay|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4;;Direct Mirror|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    downloadLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    fileSize = "1.8 GB"
                ),
                MediaItem(
                    title = "Demon Blade: Infinity Realm",
                    type = "Anime",
                    backdropUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=800&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=400&q=80",
                    description = "A young demon slayer embarks on a treacherous pilgrimage through the shattered floating castles of the Upper Moons to reclaim his sibling's stolen soul.",
                    rating = "9.7",
                    releaseYear = "2026",
                    category = "Anime",
                    cast = "Tanjiro, Nezuko, Zenitsu, Inosuke",
                    trailerLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    isTrending = true,
                    isRecentlyAdded = true,
                    streamServers = "Server 1 AnimeFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    downloadLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    fileSize = "2.4 GB",
                    episodes = "Episode 1: Awakening Flame|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4|350 MB;;Episode 2: Shadow Slash|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4|360 MB;;Episode 3: Infinite Castle|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4|380 MB"
                ),
                MediaItem(
                    title = "Obsidian Dawn",
                    type = "Movie",
                    backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=800&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&w=400&q=80",
                    description = "When a global eclipse persists indefinitely, a lone mechanic must find the key to reignite a dying mechanical star. A cyberpunk thriller with breathtaking high-contrast visual depth.",
                    rating = "8.9",
                    releaseYear = "2025",
                    category = "Thriller",
                    cast = "Marcus Thorne, Elena Rhee, Dr. Julian Cross",
                    trailerLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    isTrending = true,
                    isRecentlyAdded = false,
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4;;Direct Mirror|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    downloadLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    fileSize = "1.4 GB"
                ),
                MediaItem(
                    title = "Cyber Rin: Ronin 2099",
                    type = "Anime",
                    backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80",
                    description = "In neon-soaked Neo-Tokyo, a masterless cyborg warrior wields a plasma katana against rogue AI syndicates threatening total digital singularity.",
                    rating = "9.2",
                    releaseYear = "2026",
                    category = "Anime",
                    cast = "Kenji Sato, Aoi Miyamoto, Kuro",
                    trailerLink = "https://player.mediadelivery.net/play/738595/f80feb1d-e6ab-4c25-94a5-2aac0ff7afb8",
                    isTrending = true,
                    isRecentlyAdded = true,
                    streamServers = "Server 1 Ultra HD|https://player.mediadelivery.net/play/738595/f80feb1d-e6ab-4c25-94a5-2aac0ff7afb8;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    downloadLink = "https://player.mediadelivery.net/play/738595/f80feb1d-e6ab-4c25-94a5-2aac0ff7afb8",
                    fileSize = "1.9 GB",
                    episodes = "Episode 1: The Plasma Katana|https://player.mediadelivery.net/play/738595/f80feb1d-e6ab-4c25-94a5-2aac0ff7afb8|https://player.mediadelivery.net/play/738595/f80feb1d-e6ab-4c25-94a5-2aac0ff7afb8|400 MB;;Episode 2: Syndicate Matrix|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4|410 MB"
                ),
                MediaItem(
                    title = "Chronicles of Neon",
                    type = "TV Show",
                    backdropUrl = "https://images.unsplash.com/photo-1511447333015-45b65e60f6d5?auto=format&fit=crop&w=800&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=400&q=80",
                    description = "In the mega-city of Neo-Calcutta, rogue grid runners hijack commercial streaming servers to broadcast secret truths about the ruling corporate monoliths.",
                    rating = "9.1",
                    releaseYear = "2026",
                    category = "Action",
                    cast = "Zephyr Cruz, Jax Ryder, Naomi Chen",
                    trailerLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    isTrending = false,
                    isRecentlyAdded = true,
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4;;Server 3 HexaPlay|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    downloadLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    fileSize = "2.2 GB",
                    episodes = "Episode 1: The Grid Breach|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4|450 MB;;Episode 2: Neon Signal|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4|420 MB"
                ),
                MediaItem(
                    title = "The Last Galaxy",
                    type = "Movie",
                    backdropUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?auto=format&fit=crop&w=800&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=400&q=80",
                    description = "Humanity's final Ark reaches the outer boundary of the universe, only to find an insurmountable mirror wall. The ship's navigator must unravel ancient cosmic codes.",
                    rating = "8.7",
                    releaseYear = "2026",
                    category = "Sci-Fi",
                    cast = "Cdr. Sarah Vance, Keith Miller, AI Voice E.V.E.",
                    trailerLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    isTrending = false,
                    isRecentlyAdded = true,
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4;;Direct Mirror|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    downloadLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    fileSize = "1.6 GB"
                )
            )
            mediaDao.insertAllMedia(startingItems)
        } else {
            // Update existing Cyber Rin item if present or add if missing
            val cyberRinUrl = "https://player.mediadelivery.net/play/738595/f80feb1d-e6ab-4c25-94a5-2aac0ff7afb8"
            val cyberItem = existing.find { it.title.contains("Cyber", ignoreCase = true) || it.title.contains("Rin", ignoreCase = true) }
            if (cyberItem != null) {
                val updated = cyberItem.copy(
                    title = "Cyber Rin: Ronin 2099",
                    streamServers = "Server 1 Ultra HD|$cyberRinUrl;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    downloadLink = cyberRinUrl,
                    trailerLink = cyberRinUrl,
                    episodes = "Episode 1: The Plasma Katana|$cyberRinUrl|$cyberRinUrl|400 MB;;Episode 2: Syndicate Matrix|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4|410 MB"
                )
                mediaDao.updateMedia(updated)
            }
        }
    }

    // Google Blogger API / XML integration fallback
    suspend fun syncBloggerFeed(customFeedUrl: String? = null): Result<List<BloggerPost>> = withContext(Dispatchers.IO) {
        try {
            val feedUrlStr = customFeedUrl ?: "https://blogger.googleblog.com/feeds/posts/default?alt=json"
            val url = URL(feedUrlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val stream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(stream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                val feed = jsonResponse.optJSONObject("feed")
                val entries = feed?.optJSONArray("entry")
                val posts = mutableListOf<BloggerPost>()

                if (entries != null) {
                    for (i in 0 until entries.length()) {
                        val entry = entries.getJSONObject(i)
                        val idObj = entry.optJSONObject("id")
                        val id = idObj?.optString("\$t") ?: "post_$i"
                        val titleObj = entry.optJSONObject("title")
                        val title = titleObj?.optString("\$t") ?: "No Title"
                        val contentObj = entry.optJSONObject("content") ?: entry.optJSONObject("summary")
                        val content = contentObj?.optString("\$t") ?: "No description available."
                        val publishedObj = entry.optJSONObject("published")
                        val published = publishedObj?.optString("\$t") ?: ""
                        
                        val authors = entry.optJSONArray("author")
                        val authorName = if (authors != null && authors.length() > 0) {
                            authors.getJSONObject(0).optJSONObject("name")?.optString("\$t") ?: "HB Point Editorial"
                        } else {
                            "HB Point Editorial"
                        }

                        val links = entry.optJSONArray("link")
                        var postUrl = ""
                        if (links != null) {
                            for (j in 0 until links.length()) {
                                val link = links.getJSONObject(j)
                                if (link.optString("rel") == "alternate") {
                                    postUrl = link.optString("href")
                                    break
                                }
                            }
                        }

                        posts.add(
                            BloggerPost(
                                id = id,
                                title = title,
                                content = content,
                                published = published.take(10),
                                author = authorName,
                                url = postUrl,
                                thumbnailUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=400&q=80"
                            )
                        )
                    }
                }

                if (posts.isNotEmpty()) {
                    mediaDao.clearBloggerPosts()
                    mediaDao.insertBloggerPosts(posts)
                    return@withContext Result.success(posts)
                }
            }
            Result.failure(Exception("HTTP error or empty feed"))
        } catch (e: Exception) {
            // Local fallback editorial posts if network unavailable
            val samplePosts = listOf(
                BloggerPost(
                    id = "local_1",
                    title = "HB Point 2026: Cinematic Masterpieces & Anime Releases",
                    content = "Discover our newly curated collection of Sci-Fi epics, high-octane blockbusters, and trending anime series with multi-mirror streams and instant downloads.",
                    published = "2026-08-27",
                    author = "Editorial Team",
                    url = "https://blogger.googleblog.com",
                    thumbnailUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=400&q=80"
                ),
                BloggerPost(
                    id = "local_2",
                    title = "Demon Blade: Infinity Realm - Episode Guide & Breakdown",
                    content = "A deep dive into the latest anime episodes, battle mechanics, sound design, and character arcs of the Infinity Castle arc.",
                    published = "2026-08-26",
                    author = "Anime Specialist",
                    url = "https://blogger.googleblog.com",
                    thumbnailUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=400&q=80"
                )
            )
            mediaDao.clearBloggerPosts()
            mediaDao.insertBloggerPosts(samplePosts)
            Result.success(samplePosts)
        }
    }
}
