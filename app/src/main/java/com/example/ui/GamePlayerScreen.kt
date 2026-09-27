package com.example.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.PokiGameEntity
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosCyan
import com.example.ui.theme.IosElevatedCard
import com.example.ui.theme.IosElevatedSurface
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosSecondaryText
import com.example.ui.theme.IosSystemBackground

@Composable
fun GamePlayerScreen(
  game: PokiGameEntity,
  forceWebMode: Boolean,
  onSaveHighScore: (Int) -> Unit,
  onBackToFolder: () -> Unit,
  onPanicLockToCalculator: () -> Unit,
) {
  BackHandler { onBackToFolder() }

  var isWebMode by remember(game, forceWebMode) {
    mutableStateOf(forceWebMode || !game.isBuiltIn)
  }

  Column(
    modifier =
      Modifier.fillMaxSize()
        .background(IosSystemBackground)
        .windowInsetsPadding(WindowInsets.statusBars)
        .windowInsetsPadding(WindowInsets.navigationBars)
        .testTag("game_player_screen"),
  ) {
    // Top iOS Arcade / Safari Control Bar
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .background(IosElevatedCard)
          .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        IconButton(
          onClick = onBackToFolder,
          modifier =
            Modifier.size(40.dp)
              .clip(CircleShape)
              .background(IosElevatedSurface)
              .testTag("back_to_folder_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Klasöre Dön",
            tint = Color.White,
            modifier = Modifier.size(20.dp),
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${game.iconEmoji} ${game.title}",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = if (isWebMode) "Poki Canlı Web Modu" else "iOS 60FPS Dahili Mod",
            color = if (isWebMode) IosCyan else IosGreen,
            fontSize = 11.sp,
            maxLines = 1,
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        // Mode Switcher if game has a built-in 60FPS version
        if (game.isBuiltIn) {
          Button(
            onClick = { isWebMode = !isWebMode },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = IosElevatedSurface,
                contentColor = Color.White,
              ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp).testTag("toggle_play_mode_button"),
          ) {
            Icon(
              imageVector = if (isWebMode) Icons.Default.SportsEsports else Icons.Default.Language,
              contentDescription = null,
              tint = if (isWebMode) IosGreen else IosCyan,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isWebMode) "60FPS Mod" else "Poki Web",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        // Instant Boss Key / Panic Button -> Hides game & returns to Calculator
        Button(
          onClick = onPanicLockToCalculator,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = IosOrange,
              contentColor = Color.White,
            ),
          shape = RoundedCornerShape(50),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.height(36.dp).testTag("player_panic_lock_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Gizle",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }

    // Body: either Live Poki WebView or Built-In 60FPS Arcade
    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
      if (isWebMode) {
        PokiWebViewContainer(
          initialUrl = game.url,
          onSwitchToBuiltIn =
            if (game.isBuiltIn) {
              { isWebMode = false }
            } else {
              null
            },
        )
      } else {
        when (game.builtInKey) {
          "2048" ->
            Game2048View(
              initialHighScore = game.highScore,
              onNewHighScore = onSaveHighScore,
            )
          "flappy" ->
            FlappyNeonView(
              initialHighScore = game.highScore,
              onNewHighScore = onSaveHighScore,
            )
          "brick" ->
            BrickBreakerView(
              initialHighScore = game.highScore,
              onNewHighScore = onSaveHighScore,
            )
          "snake" ->
            SnakeClassicView(
              initialHighScore = game.highScore,
              onNewHighScore = onSaveHighScore,
            )
          else ->
            Game2048View(
              initialHighScore = game.highScore,
              onNewHighScore = onSaveHighScore,
            )
        }
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PokiWebViewContainer(
  initialUrl: String,
  onSwitchToBuiltIn: (() -> Unit)?,
) {
  val context = LocalContext.current
  var loadProgress by remember { mutableIntStateOf(0) }
  var currentUrl by remember { mutableStateOf(initialUrl) }

  val webView = remember {
    WebView(context).apply {
      layoutParams =
        ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT,
        )
      settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        mediaPlaybackRequiresUserGesture = false
        loadsImagesAutomatically = true
        useWideViewPort = true
        loadWithOverviewMode = true
        builtInZoomControls = false
        displayZoomControls = false
        cacheMode = WebSettings.LOAD_DEFAULT
        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        userAgentString =
          "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
      }
      CookieManager.getInstance().setAcceptCookie(true)
      CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

      webChromeClient =
        object : WebChromeClient() {
          override fun onProgressChanged(view: WebView?, newProgress: Int) {
            loadProgress = newProgress
          }
        }

      webViewClient =
        object : WebViewClient() {
          override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?,
          ): Boolean {
            val target = request?.url?.toString() ?: return false
            if (target.startsWith("http://") || target.startsWith("https://")) {
              currentUrl = target
              return false
            }
            return true
          }
        }
      loadUrl(initialUrl)
    }
  }

  DisposableEffect(webView) {
    onDispose {
      webView.stopLoading()
      webView.destroy()
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    if (loadProgress in 1..99) {
      LinearProgressIndicator(
        progress = { loadProgress / 100f },
        color = IosBlue,
        trackColor = IosElevatedCard,
        modifier = Modifier.fillMaxWidth().height(3.dp),
      )
    }

    // Mini iOS Safari URL & Quick Controls Bar
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .background(IosElevatedSurface)
          .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = currentUrl,
        color = IosSecondaryText,
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
          onClick = { webView.loadUrl("https://poki.com/tr") },
          modifier = Modifier.size(30.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Poki Ana Sayfa",
            tint = IosCyan,
            modifier = Modifier.size(16.dp),
          )
        }
        IconButton(
          onClick = { webView.reload() },
          modifier = Modifier.size(30.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Yenile",
            tint = Color.White,
            modifier = Modifier.size(16.dp),
          )
        }
        if (onSwitchToBuiltIn != null) {
          Button(
            onClick = onSwitchToBuiltIn,
            colors = ButtonDefaults.buttonColors(containerColor = IosGreen, contentColor = Color.Black),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp),
          ) {
            Text("Çevrimdışı 60FPS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    AndroidView(
      factory = { webView },
      modifier = Modifier.fillMaxSize().weight(1f).testTag("poki_webview"),
    )
  }
}
