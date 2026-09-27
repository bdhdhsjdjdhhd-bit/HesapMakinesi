package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "poki_games")
data class PokiGameEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val title: String,
  val subtitle: String,
  val category: String,
  val url: String,
  val isBuiltIn: Boolean = false,
  val builtInKey: String = "",
  val accentColorHex: Long = 0xFF00C6FF,
  val secondaryColorHex: Long = 0xFF0072FF,
  val iconEmoji: String = "🎮",
  val rating: String = "4.9",
  val isFavorite: Boolean = false,
  val playCount: Int = 0,
  val lastPlayedAt: Long = 0L,
  val highScore: Int = 0,
)

@Entity(tableName = "calc_history")
data class CalcHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val expression: String,
  val result: String,
  val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "vault_settings")
data class VaultSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val siteName: String = "Poki",
  val folderSubtitle: String = "Gizli Oyun Klasörü • iOS Arcade",
  val secretCode: String = "67-",
  val showCalculatorHint: Boolean = true,
  val defaultWebHomeUrl: String = "https://poki.com/tr",
)
