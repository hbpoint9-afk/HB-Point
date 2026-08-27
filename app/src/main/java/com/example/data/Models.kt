package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val registeredAt: String,
    val isAdmin: Boolean = false
)

data class EpisodeItem(
    val title: String,
    val streamUrl: String,
    val downloadUrl: String = "",
    val size: String = "350 MB"
)

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "Movie", "TV Show", "Anime"
    val backdropUrl: String,
    val posterUrl: String,
    val description: String,
    val rating: String,
    val releaseYear: String,
    val category: String, // e.g. Action, Sci-Fi, Shonen, Anime, Drama
    val cast: String,
    val trailerLink: String,
    val isTrending: Boolean = false,
    val isRecentlyAdded: Boolean = false,
    val isBookmarked: Boolean = false,
    // Store server stream URLs as serialized format, e.g. "Server 1 BollyFast|url1;;Server 2 Firedrop|url2"
    val streamServers: String,
    val downloadLink: String = "",
    val fileSize: String = "1.4 GB",
    // Store episodes as serialized format, e.g. "Ep 1: Awakening|stream1|down1|400MB;;Ep 2: Battle|stream2|down2|420MB"
    val episodes: String = ""
) {
    fun getServerList(): List<Pair<String, String>> {
        if (streamServers.isBlank()) return emptyList()
        return streamServers.split(";;").mapNotNull {
            val parts = it.split("|")
            if (parts.size >= 2) parts[0] to parts[1] else null
        }
    }

    fun getEpisodeList(): List<EpisodeItem> {
        if (episodes.isBlank()) return emptyList()
        return episodes.split(";;").mapNotNull { entry ->
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
}

@Entity(tableName = "blogger_posts")
data class BloggerPost(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val published: String,
    val author: String,
    val url: String,
    val thumbnailUrl: String
)
