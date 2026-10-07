package de.morhenn.seven_wonders_calculator.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import de.morhenn.seven_wonders_calculator.domain.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName = "players", indices = [Index(value = ["name"], unique = true)])
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    /** Hidden players keep their history but are no longer offered in the setup. */
    val hidden: Boolean = false,
)

/** The whole score sheet is stored as JSON; it is small and always read as a unit. */
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val state: GameState,
)

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE hidden = 0 ORDER BY name COLLATE NOCASE")
    fun observeVisible(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun findByName(name: String): PlayerEntity?

    @Insert
    suspend fun insert(player: PlayerEntity): Long

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun get(id: Long): PlayerEntity?
}

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id")
    fun observe(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun get(id: Long): GameEntity?

    @Insert
    suspend fun insert(game: GameEntity): Long

    @Update
    suspend fun update(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE games SET state = :state WHERE id = :id")
    suspend fun updateState(id: Long, state: GameState)

    @Query("UPDATE games SET finishedAt = :finishedAt WHERE id = :id")
    suspend fun setFinished(id: Long, finishedAt: Long?)
}

class Converters {
    @TypeConverter
    fun fromState(state: GameState): String = json.encodeToString(GameState.serializer(), state)

    @TypeConverter
    fun toState(value: String): GameState = json.decodeFromString(GameState.serializer(), value)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}

@Database(entities = [PlayerEntity::class, GameEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun players(): PlayerDao
    abstract fun games(): GameDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "seven_wonders.db").build()
    }
}
