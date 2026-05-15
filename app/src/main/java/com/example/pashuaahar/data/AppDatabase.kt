package com.example.pashuaahar.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM cow_profiles ORDER BY id DESC")
    fun getAllCowProfiles(): Flow<List<CowProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCowProfile(profile: CowProfile)

    @androidx.room.Delete
    suspend fun deleteCowProfile(profile: CowProfile)

    @Query("SELECT * FROM recipe_history ORDER BY date DESC")
    fun getRecipeHistory(): Flow<List<RecipeHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeHistory(history: RecipeHistory)

    @Query("SELECT * FROM veterinary_tips")
    fun getAllTips(): Flow<List<VeterinaryTip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: List<VeterinaryTip>)
}

@Database(entities = [CowProfile::class, RecipeHistory::class, VeterinaryTip::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pashu_aahar_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
