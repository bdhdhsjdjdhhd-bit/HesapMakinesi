package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalcHistoryEntity
import com.example.data.PokiGameEntity
import com.example.data.VaultRepository
import com.example.data.VaultSettingsEntity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScreenMode {
  data object Calculator : ScreenMode

  data object SecretFolder : ScreenMode

  data class GamePlayer(val game: PokiGameEntity, val forceWebMode: Boolean = false) : ScreenMode
}

data class CalculatorUiState(
  val displayText: String = "0",
  val expressionPreview: String = "",
  val pendingOperator: String? = null,
  val isOperatorHighlighted: Boolean = false,
  val shouldClearDisplayOnNextDigit: Boolean = false,
  val keySequenceBuffer: String = "",
  val selectedCategory: String = "Tümü",
  val searchQuery: String = "",
)

class CalculatorViewModel(private val repository: VaultRepository) : ViewModel() {

  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  private val _screenMode = MutableStateFlow<ScreenMode>(ScreenMode.Calculator)
  val screenMode: StateFlow<ScreenMode> = _screenMode.asStateFlow()

  val allGames: StateFlow<List<PokiGameEntity>> =
    repository.allGames.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val calcHistory: StateFlow<List<CalcHistoryEntity>> =
    repository.calcHistory.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val vaultSettings: StateFlow<VaultSettingsEntity> =
    repository.vaultSettings
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultSettingsEntity(),
      )
      .let { flow ->
        val mapped = MutableStateFlow(VaultSettingsEntity())
        viewModelScope.launch {
          flow.collect { value ->
            if (value != null) {
              mapped.value = value
            }
          }
        }
        mapped.asStateFlow()
      }

  private var storedOperand: Double? = null
  private var lastRightOperand: Double? = null
  private var lastCompletedOperator: String? = null

  init {
    viewModelScope.launch { repository.ensureSeedData() }
  }

  fun onDigitClick(digit: String) {
    val state = _uiState.value
    val newBuffer = (state.keySequenceBuffer + digit).takeLast(16)

    if (checkSecretUnlock(newBuffer)) {
      unlockSecretVault()
      return
    }

    val currentDisplay = state.displayText
    val nextDisplay =
      if (state.shouldClearDisplayOnNextDigit || currentDisplay == "0" || currentDisplay == "Hata") {
        digit
      } else {
        val rawDigitsCount = currentDisplay.count { it.isDigit() }
        if (rawDigitsCount >= 9) currentDisplay else currentDisplay + digit
      }

    _uiState.value =
      state.copy(
        displayText = nextDisplay,
        isOperatorHighlighted = false,
        shouldClearDisplayOnNextDigit = false,
        keySequenceBuffer = newBuffer,
      )
  }

  fun onDecimalClick() {
    val state = _uiState.value
    val newBuffer = (state.keySequenceBuffer + ",").takeLast(16)
    if (state.shouldClearDisplayOnNextDigit) {
      _uiState.value =
        state.copy(
          displayText = "0,",
          isOperatorHighlighted = false,
          shouldClearDisplayOnNextDigit = false,
          keySequenceBuffer = newBuffer,
        )
      return
    }
    if (!state.displayText.contains(",")) {
      _uiState.value =
        state.copy(
          displayText = state.displayText + ",",
          keySequenceBuffer = newBuffer,
        )
    }
  }

  fun onClearClick() {
    val state = _uiState.value
    // Just like iOS Calculator: if display is not "0", C clears current display; AC clears all
    if (state.displayText != "0" && state.pendingOperator != null && !state.isOperatorHighlighted) {
      _uiState.value =
        state.copy(
          displayText = "0",
          isOperatorHighlighted = true,
          shouldClearDisplayOnNextDigit = true,
          keySequenceBuffer = "",
        )
    } else {
      storedOperand = null
      lastRightOperand = null
      lastCompletedOperator = null
      _uiState.value =
        state.copy(
          displayText = "0",
          expressionPreview = "",
          pendingOperator = null,
          isOperatorHighlighted = false,
          shouldClearDisplayOnNextDigit = false,
          keySequenceBuffer = "",
        )
    }
  }

  fun onToggleSignClick() {
    val state = _uiState.value
    val current = parseDisplayToDouble(state.displayText)
    if (current == 0.0) return
    val toggled = -current
    _uiState.value = state.copy(displayText = formatNumber(toggled))
  }

  fun onPercentClick() {
    val state = _uiState.value
    val current = parseDisplayToDouble(state.displayText)
    val percentValue =
      if (storedOperand != null && (state.pendingOperator == "+" || state.pendingOperator == "-")) {
        storedOperand!! * (current / 100.0)
      } else {
        current / 100.0
      }
    _uiState.value =
      state.copy(
        displayText = formatNumber(percentValue),
        shouldClearDisplayOnNextDigit = true,
      )
  }

  fun onBackspaceSwipe() {
    val state = _uiState.value
    if (state.shouldClearDisplayOnNextDigit || state.displayText == "0" || state.displayText == "Hata") {
      return
    }
    val shortened = state.displayText.dropLast(1)
    val nextText =
      if (shortened.isEmpty() || shortened == "-") {
        "0"
      } else {
        shortened
      }
    _uiState.value = state.copy(displayText = nextText)
  }

  fun onOperatorClick(operator: String) {
    val state = _uiState.value
    val newBuffer = (state.keySequenceBuffer + operator).takeLast(16)

    // Check secret trigger: e.g., typing "67-" or when display shows "67" and "-" is pressed
    val configuredSecret = vaultSettings.value.secretCode.ifEmpty { "67-" }
    if (
      checkSecretUnlock(newBuffer) ||
        (operator == "-" && state.displayText == "67" && configuredSecret.startsWith("67-"))
    ) {
      if (configuredSecret == "67-" || newBuffer.endsWith(configuredSecret)) {
        unlockSecretVault()
        return
      }
    }

    val currentNumber = parseDisplayToDouble(state.displayText)

    if (storedOperand != null && state.pendingOperator != null && !state.shouldClearDisplayOnNextDigit) {
      val intermediate = calculate(storedOperand!!, currentNumber, state.pendingOperator)
      storedOperand = intermediate
      val formatted = intermediate?.let { formatNumber(it) } ?: "Hata"
      _uiState.value =
        state.copy(
          displayText = formatted,
          expressionPreview = "$formatted $operator",
          pendingOperator = operator,
          isOperatorHighlighted = true,
          shouldClearDisplayOnNextDigit = true,
          keySequenceBuffer = newBuffer,
        )
    } else {
      storedOperand = currentNumber
      _uiState.value =
        state.copy(
          expressionPreview = "${formatNumber(currentNumber)} $operator",
          pendingOperator = operator,
          isOperatorHighlighted = true,
          shouldClearDisplayOnNextDigit = true,
          keySequenceBuffer = newBuffer,
        )
    }
  }

  fun onEqualsClick() {
    val state = _uiState.value
    val newBuffer = (state.keySequenceBuffer + "=").takeLast(16)
    if (checkSecretUnlock(newBuffer)) {
      unlockSecretVault()
      return
    }

    val currentNumber = parseDisplayToDouble(state.displayText)
    val activeOperator = state.pendingOperator ?: lastCompletedOperator
    val leftOperand =
      if (state.pendingOperator != null) {
        storedOperand ?: currentNumber
      } else {
        currentNumber
      }
    val rightOperand =
      if (state.pendingOperator != null) {
        lastRightOperand = currentNumber
        currentNumber
      } else {
        lastRightOperand ?: return
      }

    if (activeOperator == null) return

    val result = calculate(leftOperand, rightOperand, activeOperator)
    val formattedLeft = formatNumber(leftOperand)
    val formattedRight = formatNumber(rightOperand)
    val formattedResult = result?.let { formatNumber(it) } ?: "Hata"
    val fullExpression = "$formattedLeft $activeOperator $formattedRight"

    lastCompletedOperator = activeOperator
    storedOperand = result

    viewModelScope.launch {
      repository.addCalcHistory(expression = fullExpression, result = formattedResult)
    }

    _uiState.value =
      state.copy(
        displayText = formattedResult,
        expressionPreview = "$fullExpression =",
        pendingOperator = null,
        isOperatorHighlighted = false,
        shouldClearDisplayOnNextDigit = true,
        keySequenceBuffer = "",
      )
  }

  fun useHistoryItem(item: CalcHistoryEntity) {
    _uiState.value =
      _uiState.value.copy(
        displayText = item.result,
        expressionPreview = item.expression,
        shouldClearDisplayOnNextDigit = true,
      )
  }

  fun clearHistory() {
    viewModelScope.launch { repository.clearCalcHistory() }
  }

  private fun checkSecretUnlock(buffer: String): Boolean {
    val customCode = vaultSettings.value.secretCode.trim().ifEmpty { "67-" }
    return buffer.endsWith("67-") || buffer.endsWith("67-67-") || buffer.endsWith(customCode)
  }

  fun unlockSecretVault() {
    storedOperand = null
    lastRightOperand = null
    lastCompletedOperator = null
    _uiState.value =
      _uiState.value.copy(
        displayText = "0",
        expressionPreview = "",
        pendingOperator = null,
        isOperatorHighlighted = false,
        shouldClearDisplayOnNextDigit = false,
        keySequenceBuffer = "",
      )
    _screenMode.value = ScreenMode.SecretFolder
  }

  fun lockBackToCalculator() {
    _screenMode.value = ScreenMode.Calculator
  }

  fun openGame(game: PokiGameEntity, forceWebMode: Boolean = false) {
    viewModelScope.launch { repository.recordGamePlay(game) }
    _screenMode.value = ScreenMode.GamePlayer(game = game, forceWebMode = forceWebMode)
  }

  fun openPokiMainPortal() {
    val homeUrl = vaultSettings.value.defaultWebHomeUrl.ifEmpty { "https://poki.com/tr" }
    val portalGame =
      allGames.value.firstOrNull { it.url == homeUrl }
        ?: PokiGameEntity(
          id = -1,
          title = vaultSettings.value.siteName.ifEmpty { "Poki" },
          subtitle = "Canlı Poki Oyun Portalı",
          category = "Popüler Poki",
          url = homeUrl,
          isBuiltIn = false,
          iconEmoji = "🌐",
        )
    _screenMode.value = ScreenMode.GamePlayer(game = portalGame, forceWebMode = true)
  }

  fun closeGameToFolder() {
    _screenMode.value = ScreenMode.SecretFolder
  }

  fun selectCategory(category: String) {
    _uiState.value = _uiState.value.copy(selectedCategory = category)
  }

  fun updateSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun toggleFavorite(game: PokiGameEntity) {
    viewModelScope.launch { repository.toggleFavorite(game) }
  }

  fun saveHighScore(game: PokiGameEntity, score: Int) {
    viewModelScope.launch { repository.updateHighScore(game, score) }
  }

  fun addCustomGame(
    title: String,
    subtitle: String,
    category: String,
    url: String,
    emoji: String,
  ) {
    if (title.isBlank() || url.isBlank()) return
    viewModelScope.launch {
      repository.addCustomGame(
        title = title,
        subtitle = subtitle,
        category = category,
        url = url,
        emoji = emoji,
      )
    }
  }

  fun deleteGame(gameId: Int) {
    viewModelScope.launch { repository.deleteGame(gameId) }
  }

  fun updateVaultSettings(
    siteName: String,
    folderSubtitle: String,
    secretCode: String,
    showCalculatorHint: Boolean,
  ) {
    val current = vaultSettings.value
    viewModelScope.launch {
      repository.updateSettings(
        current.copy(
          siteName = siteName.trim().ifEmpty { "Poki" },
          folderSubtitle = folderSubtitle.trim().ifEmpty { "Gizli Oyun Klasörü • iOS Arcade" },
          secretCode = secretCode.trim().ifEmpty { "67-" },
          showCalculatorHint = showCalculatorHint,
        )
      )
    }
  }

  private fun calculate(left: Double, right: Double, operator: String): Double? {
    return when (operator) {
      "+" -> left + right
      "-" -> left - right
      "×" -> left * right
      "÷" -> if (right == 0.0) null else left / right
      else -> right
    }
  }

  private fun parseDisplayToDouble(text: String): Double {
    val normalized = text.replace(".", "").replace(",", ".")
    return normalized.toDoubleOrNull() ?: 0.0
  }

  private fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "Hata"
    if (value == 0.0) return "0"
    val absVal = kotlin.math.abs(value)
    if (absVal >= 1e9 || (absVal > 0 && absVal < 1e-6)) {
      return String.format(Locale.US, "%.3e", value).replace(".", ",")
    }
    val symbols =
      DecimalFormatSymbols(Locale.forLanguageTag("tr-TR")).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
      }
    val formatter = DecimalFormat("#,##0.######", symbols)
    return formatter.format(value)
  }

  companion object {
    fun provideFactory(repository: VaultRepository): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return CalculatorViewModel(repository) as T
        }
      }
  }
}
