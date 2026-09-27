package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.VaultDatabase
import com.example.data.VaultRepository
import com.example.ui.CalculatorViewModel
import com.example.ui.GamePlayerScreen
import com.example.ui.IosCalculatorScreen
import com.example.ui.ScreenMode
import com.example.ui.SecretPokiFolderScreen
import com.example.ui.theme.IosPureBlack
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
      navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
    )
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = IosPureBlack,
        ) {
          CalcVaultApp()
        }
      }
    }
  }
}

@Composable
fun CalcVaultApp() {
  val context = LocalContext.current
  val repository = remember {
    val db = VaultDatabase.getInstance(context)
    VaultRepository(db.vaultDao())
  }
  val viewModel: CalculatorViewModel =
    viewModel(factory = CalculatorViewModel.provideFactory(repository))

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val screenMode by viewModel.screenMode.collectAsStateWithLifecycle()
  val allGames by viewModel.allGames.collectAsStateWithLifecycle()
  val calcHistory by viewModel.calcHistory.collectAsStateWithLifecycle()
  val vaultSettings by viewModel.vaultSettings.collectAsStateWithLifecycle()

  AnimatedContent(
    targetState = screenMode,
    transitionSpec = {
      (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
          scaleIn(
            initialScale = 0.92f,
            animationSpec =
              spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
              ),
          ))
        .togetherWith(
          fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
            scaleOut(targetScale = 0.95f)
        )
    },
    label = "ScreenModeTransition",
  ) { mode ->
    when (mode) {
      is ScreenMode.Calculator -> {
        IosCalculatorScreen(
          uiState = uiState,
          settings = vaultSettings,
          history = calcHistory,
          onDigitClick = viewModel::onDigitClick,
          onDecimalClick = viewModel::onDecimalClick,
          onClearClick = viewModel::onClearClick,
          onToggleSignClick = viewModel::onToggleSignClick,
          onPercentClick = viewModel::onPercentClick,
          onOperatorClick = viewModel::onOperatorClick,
          onEqualsClick = viewModel::onEqualsClick,
          onBackspaceSwipe = viewModel::onBackspaceSwipe,
          onUseHistoryItem = viewModel::useHistoryItem,
          onClearHistory = viewModel::clearHistory,
          onHideHint = {
            viewModel.updateVaultSettings(
              siteName = vaultSettings.siteName,
              folderSubtitle = vaultSettings.folderSubtitle,
              secretCode = vaultSettings.secretCode,
              showCalculatorHint = false,
            )
          },
        )
      }

      is ScreenMode.SecretFolder -> {
        SecretPokiFolderScreen(
          games = allGames,
          settings = vaultSettings,
          selectedCategory = uiState.selectedCategory,
          searchQuery = uiState.searchQuery,
          onSelectCategory = viewModel::selectCategory,
          onSearchQueryChange = viewModel::updateSearchQuery,
          onOpenGame = viewModel::openGame,
          onOpenPokiPortal = viewModel::openPokiMainPortal,
          onToggleFavorite = viewModel::toggleFavorite,
          onAddCustomGame = viewModel::addCustomGame,
          onDeleteGame = viewModel::deleteGame,
          onUpdateSettings = viewModel::updateVaultSettings,
          onLockToCalculator = viewModel::lockBackToCalculator,
        )
      }

      is ScreenMode.GamePlayer -> {
        GamePlayerScreen(
          game = mode.game,
          forceWebMode = mode.forceWebMode,
          onSaveHighScore = { newBest -> viewModel.saveHighScore(mode.game, newBest) },
          onBackToFolder = viewModel::closeGameToFolder,
          onPanicLockToCalculator = viewModel::lockBackToCalculator,
        )
      }
    }
  }
}
