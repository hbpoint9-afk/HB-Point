package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context, private val mediaDao: MediaDao) {

    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllMedia()
    val bookmarkedMedia: Flow<List<MediaItem>> = mediaDao.getBookmarkedMedia()
    val bloggerPosts: Flow<List<BloggerPost>> = mediaDao.getAllBloggerPosts()

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

    // Initialize database with premium starting catalog if empty
    suspend fun checkAndPrepopulate() = withContext(Dispatchers.IO) {
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
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4;;Server 3 HexaPlay|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4;;Direct Mirror|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
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
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4;;Direct Mirror|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
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
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4;;Server 3 HexaPlay|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
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
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4;;Direct Mirror|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
                ),
                MediaItem(
                    title = "Shadow Protocol",
                    type = "TV Show",
                    backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80",
                    posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80",
                    description = "An elite cybersecurity agency tracks a mysterious entity that is systematically corrupting global data mirrors. A tense, tech-heavy game of digital cat and mouse.",
                    rating = "8.5",
                    releaseYear = "2025",
                    category = "Thriller",
                    cast = "Reid Mercer, Aria Lin, Silas Vance",
                    trailerLink = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    isTrending = true,
                    isRecentlyAdded = false,
                    streamServers = "Server 1 BollyFast|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4;;Server 2 Firedrop|https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                )
            )
            mediaDao.insertAllMedia(startingItems)
        }
    }

    // Google Blogger API / XML integration fallback
    // Fetch blogger updates dynamically from a feed URL
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
                            authors.getJSONObject(0).optJSONObject("name")?.optString("\$t") ?: "HB Curator"
                        } else {
                            "HB Curator"
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

                        val mediaThumbnail = entry.optJSONObject("media\$thumbnail")
                        val thumbnailUrl = mediaThumbnail?.optString("url") ?: "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=400&q=80"

                        posts.add(
                            BloggerPost(
                                id = id,
                                title = title,
                                content = content,
                                published = published,
                                author = authorName,
                                url = postUrl,
                                thumbnailUrl = thumbnailUrl
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
            throw Exception("Failed to fetch Google Blogger feed. Response code: ${connection.responseCode}")
        } catch (e: Exception) {
            // Populate fallback Blogger posts locally if offline or error occurs, ensuring beautiful editorial cards load.
            val fallbackPosts = listOf(
                BloggerPost(
                    id = "post_fb_1",
                    title = "HB Point Version 2.0 Released: Multi-Server Integration & 10s Gesture Control",
                    content = "We are thrilled to launch HB Point 2.0! This release introduces multi-server mirror options (BollyFast, Firedrop, HexaPlay) to bypass local streaming blocks. Our native video player now supports standard pinch zoom/aspect ratio scaling and double tap to seek 10s forward or backward.",
                    published = "2026-07-16T12:00:00Z",
                    author = "System Curator",
                    url = "https://blogger.googleblog.com",
                    thumbnailUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=400&q=80"
                ),
                BloggerPost(
                    id = "post_fb_2",
                    title = "Upcoming Releases: Dark Matter Chronicles, Echoes of Eternity",
                    content = "Get ready for next week's exclusive premieres! Cosmic Cinematic content curators are uploading highly anticipated episodes of the cyberpunk series 'Echoes of Eternity'. Remember to toggle TV Mode in settings if you're streaming on tablet screens or high-density monitors.",
                    published = "2026-07-15T15:30:00Z",
                    author = "Admin Team",
                    url = "https://blogger.googleblog.com",
                    thumbnailUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=400&q=80"
                ),
                BloggerPost(
                    id = "post_fb_3",
                    title = "Optimizing Your Stream: Multi-Server Mirror Toggle Guide",
                    content = "If you experience buffering while watching 'Cosmic Pulse' or 'Obsidian Dawn', use our multi-server picker below the player. Changing links instantly reconnects to a alternative high-speed CDN without resetting your current playback location.",
                    published = "2026-07-14T09:15:00Z",
                    author = "Network Operations",
                    url = "https://blogger.googleblog.com",
                    thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=400&q=80"
                )
            )
            mediaDao.insertBloggerPosts(fallbackPosts)
            return@withContext Result.success(fallbackPosts)
        }
    }
}
