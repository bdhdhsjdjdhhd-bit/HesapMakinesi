package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
  @Query("SELECT * FROM poki_games ORDER BY isFavorite DESC, playCount DESC, id ASC")
  fun getAllGames(): Flow<List<PokiGameEntity>>

  @Query("SELECT COUNT(*) FROM poki_games")
  suspend fun getGameCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGames(games: List<PokiGameEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertGame(game: PokiGameEntity)

  @Update
  suspend fun updateGame(game: PokiGameEntity)

  @Query("DELETE FROM poki_games WHERE id = :gameId")
  suspend fun deleteGameById(gameId: Int)

  @Query("SELECT * FROM calc_history ORDER BY timestamp DESC LIMIT 50")
  fun getCalcHistory(): Flow<List<CalcHistoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCalcHistory(entry: CalcHistoryEntity)

  @Query("DELETE FROM calc_history")
  suspend fun clearCalcHistory()

  @Query("SELECT * FROM vault_settings WHERE id = 1")
  fun getVaultSettings(): Flow<VaultSettingsEntity?>

  @Query("SELECT * FROM vault_settings WHERE id = 1")
  suspend fun getVaultSettingsOnce(): VaultSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveVaultSettings(settings: VaultSettingsEntity)
}
