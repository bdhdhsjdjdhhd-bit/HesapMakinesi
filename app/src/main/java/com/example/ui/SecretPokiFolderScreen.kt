package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PokiGameEntity
import com.example.data.VaultSettingsEntity
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosCyan
import com.example.ui.theme.IosElevatedCard
import com.example.ui.theme.IosElevatedSurface
import com.example.ui.theme.IosGlassBorder
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosPink
import com.example.ui.theme.IosSecondaryText
import com.example.ui.theme.IosSystemBackground
import com.example.ui.theme.IosYellow

private val FOLDER_CATEGORIES =
  listOf(
    "Tümü",
    "Favoriler",
    "Dahili 60FPS",
    "Popüler Poki",
    "Yarış & Aksiyon",
    "Zeka",
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretPokiFolderScreen(
  games: List<PokiGameEntity>,
  settings: VaultSettingsEntity,
  selectedCategory: String,
  searchQuery: String,
  onSelectCategory: (String) -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onOpenGame: (PokiGameEntity, Boolean) -> Unit,
  onOpenPokiPortal: () -> Unit,
  onToggleFavorite: (PokiGameEntity) -> Unit,
  onAddCustomGame: (String, String, String, String, String) -> Unit,
  onDeleteGame: (Int) -> Unit,
  onUpdateSettings: (String, String, String, Boolean) -> Unit,
  onLockToCalculator: () -> Unit,
) {
  BackHandler { onLockToCalculator() }

  var showSettingsSheet by remember { mutableStateOf(false) }
  var showAddGameSheet by remember { mutableStateOf(false) }

  val filteredGames =
    remember(games, selectedCategory, searchQuery) {
      games.filter { game ->
        val matchesCategory =
          when (selectedCategory) {
            "Tümü" -> true
            "Favoriler" -> game.isFavorite
            else -> game.category.equals(selectedCategory, ignoreCase = true)
          }
        val matchesQuery =
          searchQuery.isBlank() ||
            game.title.contains(searchQuery, ignoreCase = true) ||
            game.subtitle.contains(searchQuery, ignoreCase = true) ||
            game.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
      }
    }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors =
              listOf(
                Color(0xFF0E1324),
                IosSystemBackground,
                Color(0xFF050608),
              )
          )
        )
        .windowInsetsPadding(WindowInsets.statusBars)
        .windowInsetsPadding(WindowInsets.navigationBars)
        .testTag("secret_poki_folder_screen"),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
    ) {
      // iOS Secret Folder Top Navigation Bar
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Site / Folder Title (Editable on click)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier =
            Modifier.weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .clickable { showSettingsSheet = true }
              .padding(vertical = 4.dp, horizontal = 4.dp)
              .testTag("folder_title_header"),
        ) {
          Box(
            modifier =
              Modifier.size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(
                    colors = listOf(IosBlue, IosCyan),
                  )
                ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.SportsEsports,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(24.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = settings.siteName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Site Adını Düzenle",
                tint = IosCyan,
                modifier = Modifier.size(15.dp),
              )
            }
            Text(
              text = "${settings.folderSubtitle} • Şifre: ${settings.secretCode}",
              color = IosSecondaryText,
              fontSize = 12.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          IconButton(
            onClick = { showSettingsSheet = true },
            modifier =
              Modifier.size(44.dp)
                .clip(CircleShape)
                .background(IosElevatedCard)
                .testTag("vault_settings_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = stringResource(R.string.vault_settings),
              tint = Color.White,
              modifier = Modifier.size(20.dp),
            )
          }

          // Boss Key / Panic Button to immediately lock back to Calculator
          Button(
            onClick = onLockToCalculator,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = IosOrange,
                contentColor = Color.White,
              ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            modifier = Modifier.height(44.dp).testTag("panic_lock_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Hesap+",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      // Main Scrollable Grid Content inside iOS Frosted Folder Container
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 156.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().testTag("poki_games_grid"),
      ) {
        // Hero Banner Item
        item(span = { GridItemSpan(maxLineSpan) }) {
          PokiHeroBannerCard(
            siteName = settings.siteName,
            onOpenPokiPortal = onOpenPokiPortal,
            onAddGameClick = { showAddGameSheet = true },
          )
        }

        // iOS Search Bar & Category Filter Pills
        item(span = { GridItemSpan(maxLineSpan) }) {
          Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = searchQuery,
              onValueChange = onSearchQueryChange,
              placeholder = {
                Text(
                  text = stringResource(R.string.search_games_placeholder),
                  color = IosSecondaryText,
                  fontSize = 14.sp,
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  tint = IosSecondaryText,
                )
              },
              trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                  IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Temizle",
                      tint = IosSecondaryText,
                    )
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(16.dp),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedContainerColor = IosElevatedCard,
                  unfocusedContainerColor = IosElevatedCard,
                  focusedBorderColor = IosBlue,
                  unfocusedBorderColor = IosGlassBorder,
                  focusedTextColor = Color.White,
                  unfocusedTextColor = Color.White,
                ),
              modifier = Modifier.fillMaxWidth().testTag("search_games_input"),
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              contentPadding = PaddingValues(vertical = 2.dp),
            ) {
              items(FOLDER_CATEGORIES) { category ->
                val selected = category == selectedCategory
                FilterChip(
                  selected = selected,
                  onClick = { onSelectCategory(category) },
                  label = {
                    Text(
                      text = category,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  shape = RoundedCornerShape(50),
                  colors =
                    FilterChipDefaults.filterChipColors(
                      selectedContainerColor = IosBlue,
                      selectedLabelColor = Color.White,
                      containerColor = IosElevatedCard,
                      labelColor = IosSecondaryText,
                    ),
                  border =
                    FilterChipDefaults.filterChipBorder(
                      enabled = true,
                      selected = selected,
                      borderColor = IosGlassBorder,
                      selectedBorderColor = IosBlue,
                    ),
                )
              }
            }
          }
        }

        // Empty State if no games match search
        if (filteredGames.isEmpty()) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(24.dp))
                  .background(IosElevatedCard)
                  .padding(32.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🕹️", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Aradığınız kriterde oyun bulunamadı",
                  color = Color.White,
                  fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = onOpenPokiPortal,
                  colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                ) {
                  Text("Poki.com'da Ara")
                }
              }
            }
          }
        }

        // iOS Squircle Game Cards
        items(filteredGames, key = { it.id }) { game ->
          IosPokiGameCard(
            game = game,
            onPlayClick = { onOpenGame(game, false) },
            onWebModeClick = { onOpenGame(game, true) },
            onToggleFavorite = { onToggleFavorite(game) },
            onDeleteCustom =
              if (game.category == "Özel") {
                { onDeleteGame(game.id) }
              } else {
                null
              },
          )
        }
      }
    }
  }

  // Settings Modal Sheet (Site Name, Secret Code 67-, Calculator Hint)
  if (showSettingsSheet) {
    VaultSettingsBottomSheet(
      settings = settings,
      onDismiss = { showSettingsSheet = false },
      onSave = { siteName, subtitle, secretCode, showHint ->
        onUpdateSettings(siteName, subtitle, secretCode, showHint)
        showSettingsSheet = false
      },
    )
  }

  // Add Custom Poki / Web Game Modal Sheet
  if (showAddGameSheet) {
    AddCustomGameBottomSheet(
      onDismiss = { showAddGameSheet = false },
      onAdd = { title, subtitle, category, url, emoji ->
        onAddCustomGame(title, subtitle, category, url, emoji)
        showAddGameSheet = false
      },
    )
  }
}

@Composable
private fun PokiHeroBannerCard(
  siteName: String,
  onOpenPokiPortal: () -> Unit,
  onAddGameClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .height(186.dp)
        .clip(RoundedCornerShape(26.dp))
        .border(1.dp, IosGlassBorder, RoundedCornerShape(26.dp)),
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_poki_banner),
      contentDescription = "$siteName Oyun Merkezi Banner",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.horizontalGradient(
              colors =
                listOf(
                  Color(0xE6080B14),
                  Color(0xB3080B14),
                  Color(0x4D080B14),
                )
            )
          )
          .padding(18.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Surface(
            color = IosBlue.copy(alpha = 0.9f),
            shape = RoundedCornerShape(50),
          ) {
            Text(
              text = "GİZLİ İOS KLASÖRÜ • 67- AKTİF",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "$siteName Oyun Merkezi",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
          )
          Text(
            text = "Popüler Poki oyunları ve 60FPS dahili iOS Arcade oyunları tek klasörde.",
            color = Color(0xFFD1D1D6),
            fontSize = 12.sp,
            maxLines = 2,
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            onClick = onOpenPokiPortal,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = IosGreen,
                contentColor = Color.Black,
              ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.height(40.dp).testTag("open_poki_portal_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Poki.com'u Aç",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
            )
          }

          Button(
            onClick = onAddGameClick,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = IosElevatedCard.copy(alpha = 0.9f),
                contentColor = Color.White,
              ),
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.height(40.dp).testTag("add_custom_game_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Oyun Ekle",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun IosPokiGameCard(
  game: PokiGameEntity,
  onPlayClick: () -> Unit,
  onWebModeClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDeleteCustom: (() -> Unit)?,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(IosElevatedCard.copy(alpha = 0.92f))
        .border(1.dp, IosGlassBorder, RoundedCornerShape(24.dp))
        .clickable { onPlayClick() }
        .padding(14.dp)
        .testTag("game_card_${game.id}"),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    // Top Row: Squircle App Icon + Favorite Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top,
    ) {
      Box(
        modifier =
          Modifier.size(62.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
              Brush.linearGradient(
                colors =
                  listOf(
                    Color(game.accentColorHex),
                    Color(game.secondaryColorHex),
                  )
              )
            ),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = game.iconEmoji,
          fontSize = 30.sp,
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier.size(36.dp).testTag("fav_btn_${game.id}"),
        ) {
          Icon(
            imageVector =
              if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favori",
            tint = if (game.isFavorite) IosPink else IosSecondaryText,
            modifier = Modifier.size(20.dp),
          )
        }

        Surface(
          color = if (game.isBuiltIn) IosGreen.copy(alpha = 0.2f) else IosBlue.copy(alpha = 0.2f),
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = if (game.isBuiltIn) "60FPS" else "POKI WEB",
            color = if (game.isBuiltIn) IosGreen else IosCyan,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Title & Subtitle
    Text(
      text = game.title,
      color = Color.White,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Text(
      text = game.subtitle,
      color = IosSecondaryText,
      fontSize = 12.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Rating / High Score Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = IosYellow,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
          text = if (game.highScore > 0) "Rekor: ${game.highScore}" else game.rating,
          color = IosSecondaryText,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
        )
      }

      if (onDeleteCustom != null) {
        IconButton(
          onClick = onDeleteCustom,
          modifier = Modifier.size(28.dp),
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Sil",
            tint = IosPink,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Action Buttons Row (Play + Optional Web Switch for Built-in games)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Button(
        onClick = onPlayClick,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = IosBlue,
            contentColor = Color.White,
          ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier.weight(1f).height(36.dp).testTag("play_game_btn_${game.id}"),
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = stringResource(R.string.play_now),
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
        )
      }

      if (game.isBuiltIn) {
        IconButton(
          onClick = onWebModeClick,
          modifier =
            Modifier.size(36.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(IosElevatedSurface)
              .testTag("web_mode_btn_${game.id}"),
        ) {
          Icon(
            imageVector = Icons.Default.Language,
            contentDescription = stringResource(R.string.open_poki_web),
            tint = IosCyan,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VaultSettingsBottomSheet(
  settings: VaultSettingsEntity,
  onDismiss: () -> Unit,
  onSave: (String, String, String, Boolean) -> Unit,
) {
  var siteName by remember(settings) { mutableStateOf(settings.siteName) }
  var subtitle by remember(settings) { mutableStateOf(settings.folderSubtitle) }
  var secretCode by remember(settings) { mutableStateOf(settings.secretCode) }
  var showHint by remember(settings) { mutableStateOf(settings.showCalculatorHint) }

  val quickSiteNames = listOf("Poki", "Poki Gizli Klasör", "iOS Arcade", "Oyun Kasası")
  val quickCodes = listOf("67-", "67-67-", "1234-", "00-")

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = IosElevatedCard,
    contentColor = Color.White,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp)
          .testTag("vault_settings_sheet"),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Gizli Klasör ve Site Ayarları",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
      )

      Text(
        text = "Sitenin / Klasörün Adı:",
        color = IosSecondaryText,
        fontSize = 13.sp,
      )

      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(quickSiteNames) { preset ->
          FilterChip(
            selected = siteName == preset,
            onClick = { siteName = preset },
            label = { Text(preset) },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = IosBlue,
                selectedLabelColor = Color.White,
              ),
          )
        }
      }

      OutlinedTextField(
        value = siteName,
        onValueChange = { siteName = it },
        label = { Text("Site / Klasör Adı") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("settings_site_name_input"),
      )

      OutlinedTextField(
        value = subtitle,
        onValueChange = { subtitle = it },
        label = { Text("Alt Başlık") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )

      Text(
        text = "Gizli Hesap Makinesi Şifresi (Varsayılan: 67-):",
        color = IosSecondaryText,
        fontSize = 13.sp,
      )

      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(quickCodes) { presetCode ->
          FilterChip(
            selected = secretCode == presetCode,
            onClick = { secretCode = presetCode },
            label = { Text(presetCode) },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = IosOrange,
                selectedLabelColor = Color.White,
              ),
          )
        }
      }

      OutlinedTextField(
        value = secretCode,
        onValueChange = { secretCode = it },
        label = { Text("Gizli Tuş Kombinasyonu (Örn: 67-)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("settings_secret_code_input"),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Hesap Makinesinde Şifre İpucunu Göster",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
          )
          Text(
            text = "Kapatırsanız hesap makinesinde hiçbir ipucu görünmez (Tam Gizli Mod).",
            color = IosSecondaryText,
            fontSize = 12.sp,
          )
        }
        Switch(
          checked = showHint,
          onCheckedChange = { showHint = it },
          colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosGreen),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        TextButton(onClick = onDismiss) {
          Text("İptal", color = IosSecondaryText)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = { onSave(siteName, subtitle, secretCode, showHint) },
          colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
          modifier = Modifier.testTag("save_settings_button"),
        ) {
          Text("Kaydet")
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomGameBottomSheet(
  onDismiss: () -> Unit,
  onAdd: (String, String, String, String, String) -> Unit,
) {
  var title by remember { mutableStateOf("") }
  var subtitle by remember { mutableStateOf("Poki Web Oyunu") }
  var url by remember { mutableStateOf("https://poki.com/tr") }
  var emoji by remember { mutableStateOf("🎮") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = IosElevatedCard,
    contentColor = Color.White,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp)
          .testTag("add_game_sheet"),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Gizli Klasöre Yeni Oyun / Site Ekle",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
      )

      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Oyun veya Site Adı") },
        placeholder = { Text("Örn: Venge.io veya Poki Futbol") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("new_game_title_input"),
      )

      OutlinedTextField(
        value = url,
        onValueChange = { url = it },
        label = { Text("Oyun Bağlantısı (URL)") },
        placeholder = { Text("https://poki.com/tr/g/...") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("new_game_url_input"),
      )

      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = emoji,
          onValueChange = { emoji = it },
          label = { Text("İkon Emoji") },
          singleLine = true,
          modifier = Modifier.width(110.dp),
        )
        OutlinedTextField(
          value = subtitle,
          onValueChange = { subtitle = it },
          label = { Text("Açıklama") },
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        TextButton(onClick = onDismiss) {
          Text("Vazgeç", color = IosSecondaryText)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = {
            if (title.isNotBlank() && url.isNotBlank()) {
              onAdd(title, subtitle, "Özel", url, emoji)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = IosGreen, contentColor = Color.Black),
          modifier = Modifier.testTag("confirm_add_game_button"),
        ) {
          Text("Klasöre Ekle", fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
