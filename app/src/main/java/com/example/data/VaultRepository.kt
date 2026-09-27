package com.example.data

import kotlinx.coroutines.flow.Flow

class VaultRepository(private val dao: VaultDao) {

  val allGames: Flow<List<PokiGameEntity>> = dao.getAllGames()
  val calcHistory: Flow<List<CalcHistoryEntity>> = dao.getCalcHistory()
  val vaultSettings: Flow<VaultSettingsEntity?> = dao.getVaultSettings()

  suspend fun ensureSeedData() {
    if (dao.getVaultSettingsOnce() == null) {
      dao.saveVaultSettings(
        VaultSettingsEntity(
          id = 1,
          siteName = "Poki",
          folderSubtitle = "Gizli Oyun Klasörü • iOS Arcade",
          secretCode = "67-",
          showCalculatorHint = true,
          defaultWebHomeUrl = "https://poki.com/tr",
        )
      )
    }
    if (dao.getGameCount() == 0) {
      dao.insertGames(defaultPokiGames())
    }
  }

  suspend fun toggleFavorite(game: PokiGameEntity) {
    dao.updateGame(game.copy(isFavorite = !game.isFavorite))
  }

  suspend fun recordGamePlay(game: PokiGameEntity) {
    dao.updateGame(
      game.copy(
        playCount = game.playCount + 1,
        lastPlayedAt = System.currentTimeMillis(),
      )
    )
  }

  suspend fun updateHighScore(game: PokiGameEntity, score: Int) {
    if (score > game.highScore) {
      dao.updateGame(game.copy(highScore = score))
    }
  }

  suspend fun addCustomGame(
    title: String,
    subtitle: String,
    category: String,
    url: String,
    emoji: String,
  ) {
    val cleanUrl =
      if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
    dao.insertGame(
      PokiGameEntity(
        title = title.trim(),
        subtitle = subtitle.trim().ifEmpty { "Özel Poki / Web Oyunu" },
        category = category,
        url = cleanUrl,
        isBuiltIn = false,
        builtInKey = "",
        accentColorHex = 0xFFFF2D55,
        secondaryColorHex = 0xFF5856D6,
        iconEmoji = emoji.ifEmpty { "🕹️" },
        rating = "5.0",
        isFavorite = true,
      )
    )
  }

  suspend fun deleteGame(gameId: Int) {
    dao.deleteGameById(gameId)
  }

  suspend fun addCalcHistory(expression: String, result: String) {
    dao.insertCalcHistory(
      CalcHistoryEntity(
        expression = expression,
        result = result,
        timestamp = System.currentTimeMillis(),
      )
    )
  }

  suspend fun clearCalcHistory() {
    dao.clearCalcHistory()
  }

  suspend fun updateSettings(settings: VaultSettingsEntity) {
    dao.saveVaultSettings(settings)
  }

  companion object {
    fun defaultPokiGames(): List<PokiGameEntity> =
      listOf(
        PokiGameEntity(
          title = "Poki.com Ana Sayfa",
          subtitle = "Tüm Poki Oyunları • Canlı Portal",
          category = "Popüler Poki",
          url = "https://poki.com/tr",
          isBuiltIn = false,
          accentColorHex = 0xFF00C6FF,
          secondaryColorHex = 0xFF0072FF,
          iconEmoji = "🌐",
          rating = "5.0",
          isFavorite = true,
        ),
        PokiGameEntity(
          title = "2048 Poki iOS",
          subtitle = "Anında Açılır • 60FPS Bulmaca",
          category = "Dahili 60FPS",
          url = "https://poki.com/tr/g/2048",
          isBuiltIn = true,
          builtInKey = "2048",
          accentColorHex = 0xFFFF9F0A,
          secondaryColorHex = 0xFFFF375F,
          iconEmoji = "🔢",
          rating = "4.9",
          isFavorite = true,
        ),
        PokiGameEntity(
          title = "Flappy Neon Dash",
          subtitle = "Anında Açılır • Refleks Arcade",
          category = "Dahili 60FPS",
          url = "https://poki.com/tr",
          isBuiltIn = true,
          builtInKey = "flappy",
          accentColorHex = 0xFF30D158,
          secondaryColorHex = 0xFF0A84FF,
          iconEmoji = "🐦",
          rating = "4.9",
          isFavorite = true,
        ),
        PokiGameEntity(
          title = "Cyber Brick Breaker",
          subtitle = "Anında Açılır • Klasik Blok Kırma",
          category = "Dahili 60FPS",
          url = "https://poki.com/tr/g/brick-breaker",
          isBuiltIn = true,
          builtInKey = "brick",
          accentColorHex = 0xFFBF5AF2,
          secondaryColorHex = 0xFF5E5CE6,
          iconEmoji = "🧱",
          rating = "4.8",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Snake.io Classic",
          subtitle = "Anında Açılır • Neon Yılan",
          category = "Dahili 60FPS",
          url = "https://poki.com/tr/g/snake-is-mlg-edition",
          isBuiltIn = true,
          builtInKey = "snake",
          accentColorHex = 0xFF32D74B,
          secondaryColorHex = 0xFF00C7BE,
          iconEmoji = "🐍",
          rating = "4.9",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Subway Surfers",
          subtitle = "Poki Hit • Sonsuz Koşu",
          category = "Popüler Poki",
          url = "https://poki.com/tr/g/subway-surfers",
          isBuiltIn = false,
          accentColorHex = 0xFFFF3B30,
          secondaryColorHex = 0xFFFF9500,
          iconEmoji = "🛹",
          rating = "4.9",
          isFavorite = true,
        ),
        PokiGameEntity(
          title = "Moto X3M",
          subtitle = "Poki Yarış • Akrobasi Motoru",
          category = "Yarış & Aksiyon",
          url = "https://poki.com/tr/g/moto-x3m",
          isBuiltIn = false,
          accentColorHex = 0xFFFF9F0A,
          secondaryColorHex = 0xFFFF2D55,
          iconEmoji = "🏍️",
          rating = "4.9",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Stickman Hook",
          subtitle = "Poki Fizik • Ağ Sallanma",
          category = "Popüler Poki",
          url = "https://poki.com/tr/g/stickman-hook",
          isBuiltIn = false,
          accentColorHex = 0xFF5856D6,
          secondaryColorHex = 0xFF007AFF,
          iconEmoji = "🕸️",
          rating = "4.8",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Drive Mad",
          subtitle = "Poki Araç • Engelli Parkur",
          category = "Yarış & Aksiyon",
          url = "https://poki.com/tr/g/drive-mad",
          isBuiltIn = false,
          accentColorHex = 0xFF64D2FF,
          secondaryColorHex = 0xFF0A84FF,
          iconEmoji = "🚙",
          rating = "4.8",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Monkey Mart",
          subtitle = "Poki Simülasyon • Market Yönet",
          category = "Popüler Poki",
          url = "https://poki.com/tr/g/monkey-mart",
          isBuiltIn = false,
          accentColorHex = 0xFFFFD60A,
          secondaryColorHex = 0xFFFF9F0A,
          iconEmoji = "🐵",
          rating = "4.9",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Temple Run 2",
          subtitle = "Poki Macera • Tapınak Kaçışı",
          category = "Yarış & Aksiyon",
          url = "https://poki.com/tr/g/temple-run-2",
          isBuiltIn = false,
          accentColorHex = 0xFFAC8E68,
          secondaryColorHex = 0xFFFF9F0A,
          iconEmoji = "🗿",
          rating = "4.7",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Who Is? Bilmece",
          subtitle = "Poki Zeka • Dedektif Bulmacası",
          category = "Zeka",
          url = "https://poki.com/tr/g/who-is",
          isBuiltIn = false,
          accentColorHex = 0xFFFF2D55,
          secondaryColorHex = 0xFFAF52DE,
          iconEmoji = "🕵️",
          rating = "4.8",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Crossy Road",
          subtitle = "Poki Arcade • Karşıdan Karşıya",
          category = "Popüler Poki",
          url = "https://poki.com/tr/g/crossy-road",
          isBuiltIn = false,
          accentColorHex = 0xFF34C759,
          secondaryColorHex = 0xFF30B0C7,
          iconEmoji = "🐔",
          rating = "4.8",
          isFavorite = false,
        ),
        PokiGameEntity(
          title = "Fruit Ninja",
          subtitle = "Poki Aksiyon • Meyve Kesme",
          category = "Yarış & Aksiyon",
          url = "https://poki.com/tr/g/fruit-ninja",
          isBuiltIn = false,
          accentColorHex = 0xFFFF375F,
          secondaryColorHex = 0xFFFF9F0A,
          iconEmoji = "🍉",
          rating = "4.8",
          isFavorite = false,
        ),
      )
  }
}
