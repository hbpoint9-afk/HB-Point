package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun authenticateUser(email: String, password: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Delete
    suspend fun deleteUser(user: UserAccount)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY id DESC")
    fun getAllMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getMediaById(id: Int): Flow<MediaItem?>

    @Query("SELECT * FROM media_items WHERE isBookmarked = 1 ORDER BY id DESC")
    fun getBookmarkedMedia(): Flow<List<MediaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItem): Long

    @Update
    suspend fun updateMedia(item: MediaItem)

    @Delete
    suspend fun deleteMedia(item: MediaItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedia(items: List<MediaItem>)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Int)

    // Blogger query methods
    @Query("SELECT * FROM blogger_posts ORDER BY published DESC")
    fun getAllBloggerPosts(): Flow<List<BloggerPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloggerPosts(posts: List<BloggerPost>)

    @Query("DELETE FROM blogger_posts")
    suspend fun clearBloggerPosts()
}

@Database(
    entities = [MediaItem::class, BloggerPost::class, UserAccount::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hbpoint_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
