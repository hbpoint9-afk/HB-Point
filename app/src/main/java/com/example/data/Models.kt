package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "Movie" or "TV Show"
    val backdropUrl: String,
    val posterUrl: String,
    val description: String,
    val rating: String,
    val releaseYear: String,
    val category: String, // e.g. Action, Sci-Fi, Drama
    val cast: String,
    val trailerLink: String,
    val isTrending: Boolean = false,
    val isRecentlyAdded: Boolean = false,
    val isBookmarked: Boolean = false,
    // Store server stream URLs as serialized format, e.g. "Server 1|url1;;Server 2|url2"
    val streamServers: String 
) {
    fun getServerList(): List<Pair<String, String>> {
        if (streamServers.isBlank()) return emptyList()
        return streamServers.split(";;").mapNotNull {
            val parts = it.split("|")
            if (parts.size == 2) parts[0] to parts[1] else null
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
