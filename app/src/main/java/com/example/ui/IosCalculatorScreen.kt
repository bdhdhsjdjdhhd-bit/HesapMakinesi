package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CalcHistoryEntity
import com.example.data.VaultSettingsEntity
import com.example.ui.theme.IosElevatedCard
import com.example.ui.theme.IosElevatedSurface
import com.example.ui.theme.IosFunctionButtonBg
import com.example.ui.theme.IosFunctionButtonPressed
import com.example.ui.theme.IosNumberButtonBg
import com.example.ui.theme.IosNumberButtonPressed
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosOrangePressed
import com.example.ui.theme.IosPureBlack
import com.example.ui.theme.IosSecondaryText
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IosCalculatorScreen(
  uiState: CalculatorUiState,
  settings: VaultSettingsEntity,
  history: List<CalcHistoryEntity>,
  onDigitClick: (String) -> Unit,
  onDecimalClick: () -> Unit,
  onClearClick: () -> Unit,
  onToggleSignClick: () -> Unit,
  onPercentClick: () -> Unit,
  onOperatorClick: (String) -> Unit,
  onEqualsClick: () -> Unit,
  onBackspaceSwipe: () -> Unit,
  onUseHistoryItem: (CalcHistoryEntity) -> Unit,
  onClearHistory: () -> Unit,
  onHideHint: () -> Unit,
) {
  val haptics = LocalHapticFeedback.current
  var showHistorySheet by remember { mutableStateOf(false) }
  var swipeAccumulated by remember { mutableStateOf(0f) }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(IosPureBlack)
        .windowInsetsPadding(WindowInsets.statusBars)
        .windowInsetsPadding(WindowInsets.navigationBars),
    contentAlignment = Alignment.BottomCenter,
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 460.dp)
          .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top iOS Utility Row (History Button + Optional Subtle Secret Code Pill)
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            showHistorySheet = true
          },
          modifier = Modifier.size(48.dp).testTag("calc_history_button"),
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = stringResource(R.string.calc_history_title),
            tint = IosOrange,
            modifier = Modifier.size(26.dp),
          )
        }

        AnimatedVisibility(
          visible = settings.showCalculatorHint,
          enter = fadeIn(),
          exit = fadeOut(),
        ) {
          Surface(
            color = IosElevatedCard.copy(alpha = 0.85f),
            shape = RoundedCornerShape(50),
            modifier = Modifier.testTag("secret_code_hint_pill"),
          ) {
            Row(
              modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                tint = IosOrange,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${settings.siteName}: ${settings.secretCode} tuşla",
                color = IosSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
              )
              Spacer(modifier = Modifier.width(4.dp))
              IconButton(
                onClick = onHideHint,
                modifier = Modifier.size(28.dp).testTag("hide_hint_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.VisibilityOff,
                  contentDescription = "İpucunu Gizle",
                  tint = IosSecondaryText,
                  modifier = Modifier.size(14.dp),
                )
              }
            }
          }
        }
      }

      // Display Area + Keypad Area
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
      ) {
        // Calculator Display with iOS Swipe-to-Delete Gesture
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 12.dp)
              .pointerInput(Unit) {
                detectHorizontalDragGestures(
                  onDragStart = { swipeAccumulated = 0f },
                  onHorizontalDrag = { _, dragAmount -> swipeAccumulated += dragAmount },
                  onDragEnd = {
                    if (abs(swipeAccumulated) > 40f) {
                      haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      onBackspaceSwipe()
                    }
                  },
                )
              }
              .testTag("calc_display_container"),
          horizontalAlignment = Alignment.End,
        ) {
          if (uiState.expressionPreview.isNotEmpty()) {
            Text(
              text = uiState.expressionPreview,
              color = IosSecondaryText,
              fontSize = 24.sp,
              fontWeight = FontWeight.Light,
              textAlign = TextAlign.End,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp).testTag("calc_expression_text"),
            )
          }

          val displayFontSize =
            when {
              uiState.displayText.length <= 6 -> 80.sp
              uiState.displayText.length == 7 -> 68.sp
              uiState.displayText.length == 8 -> 58.sp
              uiState.displayText.length == 9 -> 50.sp
              else -> 42.sp
            }

          Text(
            text = uiState.displayText,
            color = Color.White,
            fontSize = displayFontSize,
            fontWeight = FontWeight.Light,
            lineHeight = displayFontSize,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxWidth().testTag("calc_display_text"),
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // iOS 5x4 Circular Button Grid
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
          val spacing = 12.dp
          val maxGridWidth = maxWidth
          val btnByWidth = (maxGridWidth - (spacing * 3)) / 4
          val btnSize: Dp = min(btnByWidth, 82.dp)

          val clearLabel =
            if (uiState.displayText != "0" || uiState.expressionPreview.isNotEmpty()) "C" else "AC"

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            // Row 1: AC/C, +/-, %, ÷
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              IosCalcButton(
                label = clearLabel,
                testTag = "calc_btn_clear",
                size = btnSize,
                baseColor = IosFunctionButtonBg,
                pressedColor = IosFunctionButtonPressed,
                textColor = Color.Black,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onClearClick()
                },
              )
              IosCalcButton(
                label = "+/-",
                testTag = "calc_btn_sign",
                size = btnSize,
                baseColor = IosFunctionButtonBg,
                pressedColor = IosFunctionButtonPressed,
                textColor = Color.Black,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onToggleSignClick()
                },
              )
              IosCalcButton(
                label = "%",
                testTag = "calc_btn_percent",
                size = btnSize,
                baseColor = IosFunctionButtonBg,
                pressedColor = IosFunctionButtonPressed,
                textColor = Color.Black,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onPercentClick()
                },
              )
              IosOperatorButton(
                symbol = "÷",
                testTag = "calc_btn_div",
                size = btnSize,
                isActive = uiState.isOperatorHighlighted && uiState.pendingOperator == "÷",
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onOperatorClick("÷")
                },
              )
            }

            // Row 2: 7, 8, 9, ×
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              IosCalcButton(
                label = "7",
                testTag = "calc_btn_7",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("7")
                },
              )
              IosCalcButton(
                label = "8",
                testTag = "calc_btn_8",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("8")
                },
              )
              IosCalcButton(
                label = "9",
                testTag = "calc_btn_9",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("9")
                },
              )
              IosOperatorButton(
                symbol = "×",
                testTag = "calc_btn_mul",
                size = btnSize,
                isActive = uiState.isOperatorHighlighted && uiState.pendingOperator == "×",
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onOperatorClick("×")
                },
              )
            }

            // Row 3: 4, 5, 6, -
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              IosCalcButton(
                label = "4",
                testTag = "calc_btn_4",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("4")
                },
              )
              IosCalcButton(
                label = "5",
                testTag = "calc_btn_5",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("5")
                },
              )
              IosCalcButton(
                label = "6",
                testTag = "calc_btn_6",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("6")
                },
              )
              IosOperatorButton(
                symbol = "-",
                testTag = "calc_btn_sub",
                size = btnSize,
                isActive = uiState.isOperatorHighlighted && uiState.pendingOperator == "-",
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onOperatorClick("-")
                },
              )
            }

            // Row 4: 1, 2, 3, +
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              IosCalcButton(
                label = "1",
                testTag = "calc_btn_1",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("1")
                },
              )
              IosCalcButton(
                label = "2",
                testTag = "calc_btn_2",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("2")
                },
              )
              IosCalcButton(
                label = "3",
                testTag = "calc_btn_3",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("3")
                },
              )
              IosOperatorButton(
                symbol = "+",
                testTag = "calc_btn_add",
                size = btnSize,
                isActive = uiState.isOperatorHighlighted && uiState.pendingOperator == "+",
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onOperatorClick("+")
                },
              )
            }

            // Row 5: Wide 0 pill button, decimal ",", "="
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              val zeroButtonWidth = (btnSize * 2) + spacing
              IosZeroPillButton(
                width = zeroButtonWidth,
                height = btnSize,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDigitClick("0")
                },
              )
              IosCalcButton(
                label = ",",
                testTag = "calc_btn_dec",
                size = btnSize,
                baseColor = IosNumberButtonBg,
                pressedColor = IosNumberButtonPressed,
                textColor = Color.White,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onDecimalClick()
                },
              )
              IosOperatorButton(
                symbol = "=",
                testTag = "calc_btn_eq",
                size = btnSize,
                isActive = false,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  onEqualsClick()
                },
              )
            }
          }
        }
      }
    }
  }

  // iOS Calculation History Bottom Sheet
  if (showHistorySheet) {
    ModalBottomSheet(
      onDismissRequest = { showHistorySheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      containerColor = IosElevatedCard,
      contentColor = Color.White,
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("calc_history_sheet"),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.calc_history_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
          if (history.isNotEmpty()) {
            TextButton(
              onClick = onClearHistory,
              modifier = Modifier.testTag("clear_history_button"),
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = null,
                tint = IosOrange,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = stringResource(R.string.calc_clear_history),
                color = IosOrange,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
          Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = stringResource(R.string.calc_no_history),
              color = IosSecondaryText,
              fontSize = 16.sp,
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxWidth().height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(history, key = { it.id }) { item ->
              Column(
                modifier =
                  Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(IosElevatedSurface)
                    .clickable {
                      onUseHistoryItem(item)
                      showHistorySheet = false
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End,
              ) {
                Text(
                  text = item.expression,
                  color = IosSecondaryText,
                  fontSize = 15.sp,
                )
                Text(
                  text = "= ${item.result}",
                  color = Color.White,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Medium,
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun IosCalcButton(
  label: String,
  testTag: String,
  size: Dp,
  baseColor: Color,
  pressedColor: Color,
  textColor: Color,
  onClick: () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val bgColor by
    animateColorAsState(
      targetValue = if (isPressed) pressedColor else baseColor,
      label = "calcBtnColor",
    )
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.94f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessHigh),
      label = "calcBtnScale",
    )

  Box(
    modifier =
      Modifier.size(size)
        .scale(scale)
        .clip(CircleShape)
        .background(bgColor)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick,
        )
        .semantics { contentDescription = label }
        .testTag(testTag),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = if (label.length > 1) 26.sp else 34.sp,
      fontWeight = FontWeight.Normal,
    )
  }
}

@Composable
private fun IosOperatorButton(
  symbol: String,
  testTag: String,
  size: Dp,
  isActive: Boolean,
  onClick: () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val bgColor by
    animateColorAsState(
      targetValue =
        when {
          isActive -> Color.White
          isPressed -> IosOrangePressed
          else -> IosOrange
        },
      label = "opBgColor",
    )
  val contentColor by
    animateColorAsState(
      targetValue = if (isActive) IosOrange else Color.White,
      label = "opTextColor",
    )
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.94f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessHigh),
      label = "opScale",
    )

  Box(
    modifier =
      Modifier.size(size)
        .scale(scale)
        .clip(CircleShape)
        .background(bgColor)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick,
        )
        .semantics { contentDescription = symbol }
        .testTag(testTag),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = symbol,
      color = contentColor,
      fontSize = 38.sp,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun IosZeroPillButton(
  width: Dp,
  height: Dp,
  onClick: () -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val bgColor by
    animateColorAsState(
      targetValue = if (isPressed) IosNumberButtonPressed else IosNumberButtonBg,
      label = "zeroBtnColor",
    )
  val scale by
    animateFloatAsState(
      targetValue = if (isPressed) 0.96f else 1f,
      animationSpec = spring(stiffness = Spring.StiffnessHigh),
      label = "zeroBtnScale",
    )

  Box(
    modifier =
      Modifier.width(width)
        .height(height)
        .scale(scale)
        .clip(CircleShape)
        .background(bgColor)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick,
        )
        .padding(start = 30.dp)
        .semantics { contentDescription = "0" }
        .testTag("calc_btn_0"),
    contentAlignment = Alignment.CenterStart,
  ) {
    Text(
      text = "0",
      color = Color.White,
      fontSize = 34.sp,
      fontWeight = FontWeight.Normal,
    )
  }
}

@Composable
fun HorizontalDividerRow() {
  HorizontalDivider(color = IosElevatedSurface, thickness = 0.5.dp)
}
