package com.netspeedpro

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Int,
    val networkType: String,
    val carrier: String,
    val generation: String
)

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TestResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: TestResult)

    @Delete
    suspend fun delete(result: TestResult)

    @Query("DELETE FROM test_results")
    suspend fun deleteAll()
}

@Database(entities = [TestResult::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testResultDao(): TestResultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "netspeedpro.db"
                ).build().also { INSTANCE = it }
            }
    }
}
