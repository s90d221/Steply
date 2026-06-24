package com.steply.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.steply.app.data.local.dao.ChairStandResultDao
import com.steply.app.data.local.dao.ExerciseRecommendationDao
import com.steply.app.data.local.dao.ScreeningSessionDao
import com.steply.app.data.local.dao.UserProfileDao
import com.steply.app.data.local.entities.ChairStandResultEntity
import com.steply.app.data.local.entities.ExerciseRecommendationEntity
import com.steply.app.data.local.entities.ScreeningSessionEntity
import com.steply.app.data.local.entities.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        ScreeningSessionEntity::class,
        ChairStandResultEntity::class,
        ExerciseRecommendationEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SteplyDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun screeningSessionDao(): ScreeningSessionDao
    abstract fun chairStandResultDao(): ChairStandResultDao
    abstract fun exerciseRecommendationDao(): ExerciseRecommendationDao

    companion object {
        @Volatile
        private var instance: SteplyDatabase? = null

        fun getInstance(context: Context): SteplyDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SteplyDatabase::class.java,
                    "steply_local.db",
                ).build().also { instance = it }
            }
        }
    }
}
