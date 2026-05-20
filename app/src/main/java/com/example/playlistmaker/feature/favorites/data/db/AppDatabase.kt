package com.example.playlistmaker.feature.favorites.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.playlistmaker.feature.favorites.data.db.dao.FavoritesDao
import com.example.playlistmaker.feature.favorites.data.db.entity.TrackEntity
import com.example.playlistmaker.feature.media.data.db.dao.PlaylistDao
import com.example.playlistmaker.feature.media.data.db.dao.PlaylistTrackDao
import com.example.playlistmaker.feature.media.data.db.entity.PlaylistEntity
import com.example.playlistmaker.feature.media.data.db.entity.PlaylistTrackEntity

@Database(
    entities = [
        TrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao

    companion object {
        private const val DATABASE_NAME = "playlist_maker_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}