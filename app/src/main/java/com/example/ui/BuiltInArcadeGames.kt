package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosCyan
import com.example.ui.theme.IosElevatedCard
import com.example.ui.theme.IosElevatedSurface
import com.example.ui.theme.IosGlassBorder
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosPink
import com.example.ui.theme.IosPurple
import com.example.ui.theme.IosSecondaryText
import com.example.ui.theme.IosYellow
import kotlin.math.abs
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun Game2048View(
  initialHighScore: Int,
  onNewHighScore: (Int) -> Unit,
) {
  var board by remember { mutableStateOf(init2048Board()) }
  var score by remember { mutableIntStateOf(0) }
  var bestScore by remember { mutableIntStateOf(initialHighScore) }
  var isGameOver by remember { mutableStateOf(false) }

  fun applyMove(direction: Int) {
    if (isGameOver) return
    val (nextBoard, gained, moved) = move2048(board, direction)
    if (moved) {
      val withSpawn = spawnRandom2048Tile(nextBoard)
      board = withSpawn
      score += gained
      if (score > bestScore) {
        bestScore = score
        onNewHighScore(bestScore)
      }
      if (!canMove2048(withSpawn)) {
        isGameOver = true
      }
    }
  }

  var dragX by remember { mutableFloatStateOf(0f) }
  var dragY by remember { mutableFloatStateOf(0f) }

  Column(
    modifier =
      Modifier.fillMaxSize()
        .padding(16.dp)
        .testTag("builtin_2048_game"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    // Score Header
    Row(
      modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ScorePill(label = "SKOR", value = score.toString(), color = IosOrange)
        ScorePill(label = "REKOR", value = bestScore.toString(), color = IosBlue)
      }

      IconButton(
        onClick = {
          board = init2048Board()
          score = 0
          isGameOver = false
        },
        modifier =
          Modifier.size(44.dp)
            .clip(CircleShape)
            .background(IosElevatedSurface)
            .testTag("restart_2048_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Yeniden Başlat",
          tint = Color.White,
        )
      }
    }

    // 4x4 Grid Board
    Box(
      modifier =
        Modifier.widthIn(max = 360.dp)
          .fillMaxWidth()
          .aspectRatio(1f)
          .clip(RoundedCornerShape(24.dp))
          .background(IosElevatedCard)
          .border(1.dp, IosGlassBorder, RoundedCornerShape(24.dp))
          .pointerInput(isGameOver) {
            detectDragGestures(
              onDragStart = {
                dragX = 0f
                dragY = 0f
              },
              onDrag = { _, dragAmount ->
                dragX += dragAmount.x
                dragY += dragAmount.y
              },
              onDragEnd = {
                if (abs(dragX) > abs(dragY)) {
                  if (abs(dragX) > 30f) {
                    applyMove(if (dragX > 0) 1 else 3) // 1=Right, 3=Left
                  }
                } else {
                  if (abs(dragY) > 30f) {
                    applyMove(if (dragY > 0) 2 else 0) // 2=Down, 0=Up
                  }
                }
              },
            )
          }
          .padding(12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        for (r in 0 until 4) {
          Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            for (c in 0 until 4) {
              val value = board[r * 4 + c]
              Box(
                modifier =
                  Modifier.weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(tile2048Color(value)),
                contentAlignment = Alignment.Center,
              ) {
                if (value > 0) {
                  Text(
                    text = value.toString(),
                    color = Color.White,
                    fontSize = if (value >= 1024) 20.sp else 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                  )
                }
              }
            }
          }
        }
      }

      if (isGameOver) {
        Box(
          modifier =
            Modifier.fillMaxSize()
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xD9000000)),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Oyun Bitti!",
              color = Color.White,
              fontSize = 28.sp,
              fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = {
                board = init2048Board()
                score = 0
                isGameOver = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = IosOrange),
            ) {
              Text("Tekrar Oyna")
            }
          }
        }
      }
    }

    // D-Pad Controls for easy one-handed tapping
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ArcadeControlButton(
        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        contentDesc = "Sol",
        onClick = { applyMove(3) },
      )
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArcadeControlButton(
          icon = Icons.Default.KeyboardArrowUp,
          contentDesc = "Yukarı",
          onClick = { applyMove(0) },
        )
        ArcadeControlButton(
          icon = Icons.Default.KeyboardArrowDown,
          contentDesc = "Aşağı",
          onClick = { applyMove(2) },
        )
      }
      ArcadeControlButton(
        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDesc = "Sağ",
        onClick = { applyMove(1) },
      )
    }
  }
}

@Composable
fun FlappyNeonView(
  initialHighScore: Int,
  onNewHighScore: (Int) -> Unit,
) {
  var isRunning by remember { mutableStateOf(false) }
  var isGameOver by remember { mutableStateOf(false) }
  var score by remember { mutableIntStateOf(0) }
  var bestScore by remember { mutableIntStateOf(initialHighScore) }

  var birdY by remember { mutableFloatStateOf(0.45f) }
  var birdVelocity by remember { mutableFloatStateOf(0f) }
  var pipeX by remember { mutableFloatStateOf(1.1f) }
  var gapCenterY by remember { mutableFloatStateOf(0.45f) }
  var passedPipe by remember { mutableStateOf(false) }

  LaunchedEffect(isRunning, isGameOver) {
    if (!isRunning || isGameOver) return@LaunchedEffect
    var lastNanos = 0L
    while (isRunning && !isGameOver) {
      withFrameNanos { nanos ->
        if (lastNanos == 0L) lastNanos = nanos
        val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
        lastNanos = nanos

        birdVelocity += 1.45f * dt
        birdY += birdVelocity * dt
        pipeX -= 0.48f * dt

        if (pipeX < -0.18f) {
          pipeX = 1.1f
          gapCenterY = Random.nextFloat() * 0.44f + 0.28f
          passedPipe = false
        }

        if (!passedPipe && pipeX < 0.25f) {
          passedPipe = true
          score += 1
          if (score > bestScore) {
            bestScore = score
            onNewHighScore(bestScore)
          }
        }

        val gapHalf = 0.15f
        val inPipeX = pipeX in 0.16f..0.34f
        val hitPipe = inPipeX && (birdY < gapCenterY - gapHalf || birdY > gapCenterY + gapHalf)
        val hitBounds = birdY < 0.04f || birdY > 0.94f

        if (hitPipe || hitBounds) {
          isGameOver = true
          isRunning = false
        }
      }
    }
  }

  fun flap() {
    if (isGameOver) {
      birdY = 0.45f
      birdVelocity = -0.52f
      pipeX = 1.1f
      score = 0
      passedPipe = false
      isGameOver = false
      isRunning = true
    } else if (!isRunning) {
      isRunning = true
      birdVelocity = -0.52f
    } else {
      birdVelocity = -0.52f
    }
  }

  Column(
    modifier =
      Modifier.fillMaxSize()
        .padding(16.dp)
        .testTag("builtin_flappy_game"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      ScorePill(label = "SKOR", value = score.toString(), color = IosGreen)
      ScorePill(label = "REKOR", value = bestScore.toString(), color = IosCyan)
    }

    Spacer(modifier = Modifier.height(12.dp))

    Box(
      modifier =
        Modifier.fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(26.dp))
          .background(Color(0xFF0A1020))
          .border(1.dp, IosGlassBorder, RoundedCornerShape(26.dp))
          .pointerInput(Unit) { detectTapGestures(onTap = { flap() }) },
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val gapHalfPx = h * 0.15f
        val gapY = h * gapCenterY
        val px = w * pipeX
        val pipeWidth = w * 0.14f

        // Top Pipe
        drawRoundRect(
          color = IosGreen,
          topLeft = Offset(px, 0f),
          size = Size(pipeWidth, (gapY - gapHalfPx).coerceAtLeast(0f)),
          cornerRadius = CornerRadius(18f, 18f),
        )
        // Bottom Pipe
        drawRoundRect(
          color = IosGreen,
          topLeft = Offset(px, gapY + gapHalfPx),
          size = Size(pipeWidth, (h - (gapY + gapHalfPx)).coerceAtLeast(0f)),
          cornerRadius = CornerRadius(18f, 18f),
        )

        // Neon Bird
        drawCircle(
          color = IosYellow,
          radius = w * 0.045f,
          center = Offset(w * 0.25f, h * birdY),
        )
      }

      if (!isRunning) {
        Surface(
          color = Color(0xCC1C1C1E),
          shape = RoundedCornerShape(20.dp),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = if (isGameOver) "Çarptın! Skor: $score" else "Flappy Neon Dash",
              color = Color.White,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Zıplamak için ekrana dokun",
              color = IosSecondaryText,
              fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = { flap() },
              colors = ButtonDefaults.buttonColors(containerColor = IosGreen, contentColor = Color.Black),
            ) {
              Text(if (isGameOver) "Tekrar Oyna" else "Başla", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun BrickBreakerView(
  initialHighScore: Int,
  onNewHighScore: (Int) -> Unit,
) {
  var isRunning by remember { mutableStateOf(false) }
  var score by remember { mutableIntStateOf(0) }
  var bestScore by remember { mutableIntStateOf(initialHighScore) }
  var paddleX by remember { mutableFloatStateOf(0.5f) }
  var ballX by remember { mutableFloatStateOf(0.5f) }
  var ballY by remember { mutableFloatStateOf(0.75f) }
  var ballVx by remember { mutableFloatStateOf(0.42f) }
  var ballVy by remember { mutableFloatStateOf(-0.52f) }
  val bricks = remember { mutableStateListOf<Boolean>().apply { repeat(24) { add(true) } } }

  fun resetBricks() {
    for (i in 0 until 24) bricks[i] = true
  }

  LaunchedEffect(isRunning) {
    if (!isRunning) return@LaunchedEffect
    var lastNanos = 0L
    while (isRunning) {
      withFrameNanos { nanos ->
        if (lastNanos == 0L) lastNanos = nanos
        val dt = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.04f)
        lastNanos = nanos

        ballX += ballVx * dt
        ballY += ballVy * dt

        if (ballX < 0.04f || ballX > 0.96f) ballVx = -ballVx
        if (ballY < 0.04f) ballVy = abs(ballVy)

        // Paddle collision
        if (ballY in 0.86f..0.91f && abs(ballX - paddleX) < 0.16f && ballVy > 0) {
          ballVy = -abs(ballVy)
          val offset = (ballX - paddleX) / 0.16f
          ballVx = (offset * 0.65f).coerceIn(-0.65f, 0.65f)
        }

        // Bricks collision (4 rows x 6 cols)
        for (r in 0 until 4) {
          for (c in 0 until 6) {
            val idx = r * 6 + c
            if (bricks[idx]) {
              val bLeft = c / 6f + 0.02f
              val bRight = (c + 1) / 6f - 0.02f
              val bTop = 0.08f + r * 0.065f
              val bBottom = bTop + 0.048f
              if (ballX in bLeft..bRight && ballY in bTop..bBottom) {
                bricks[idx] = false
                ballVy = -ballVy
                score += 15
                if (score > bestScore) {
                  bestScore = score
                  onNewHighScore(bestScore)
                }
                if (bricks.none { it }) {
                  resetBricks()
                }
              }
            }
          }
        }

        if (ballY > 0.97f) {
          isRunning = false
          ballX = 0.5f
          ballY = 0.75f
          ballVy = -0.52f
        }
      }
    }
  }

  Column(
    modifier =
      Modifier.fillMaxSize()
        .padding(16.dp)
        .testTag("builtin_brick_game"),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      ScorePill(label = "SKOR", value = score.toString(), color = IosPurple)
      ScorePill(label = "REKOR", value = bestScore.toString(), color = IosOrange)
    }

    Spacer(modifier = Modifier.height(12.dp))

    Box(
      modifier =
        Modifier.fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(26.dp))
          .background(Color(0xFF0B0A18))
          .border(1.dp, IosGlassBorder, RoundedCornerShape(26.dp))
          .pointerInput(Unit) {
            detectDragGestures { change, _ ->
              if (size.width > 0) {
                paddleX = (change.position.x / size.width).coerceIn(0.15f, 0.85f)
              }
            }
          }
          .clickable {
            if (!isRunning) {
              if (bricks.none { it }) resetBricks()
              score = 0
              resetBricks()
              isRunning = true
            }
          },
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val rowColors = listOf(IosPink, IosOrange, IosYellow, IosCyan)

        for (r in 0 until 4) {
          for (c in 0 until 6) {
            if (bricks[r * 6 + c]) {
              val left = (c / 6f + 0.015f) * w
              val top = (0.08f + r * 0.065f) * h
              drawRoundRect(
                color = rowColors[r],
                topLeft = Offset(left, top),
                size = Size(w / 6f - w * 0.03f, h * 0.045f),
                cornerRadius = CornerRadius(12f, 12f),
              )
            }
          }
        }

        // Paddle
        drawRoundRect(
          color = IosBlue,
          topLeft = Offset((paddleX - 0.14f) * w, 0.88f * h),
          size = Size(0.28f * w, h * 0.025f),
          cornerRadius = CornerRadius(20f, 20f),
        )

        // Ball
        drawCircle(
          color = Color.White,
          radius = w * 0.028f,
          center = Offset(ballX * w, ballY * h),
        )
      }

      if (!isRunning) {
        Surface(
          color = Color(0xCC1C1C1E),
          shape = RoundedCornerShape(20.dp),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = "Cyber Brick Breaker",
              color = Color.White,
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Parmağını kaydırarak raketi yönet",
              color = IosSecondaryText,
              fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = {
                score = 0
                resetBricks()
                isRunning = true
              },
              colors = ButtonDefaults.buttonColors(containerColor = IosPurple),
            ) {
              Text("Oyunu Başlat")
            }
          }
        }
      }
    }
  }
}

@Composable
fun SnakeClassicView(
  initialHighScore: Int,
  onNewHighScore: (Int) -> Unit,
) {
  val gridSize = 15
  var snake by remember { mutableStateOf(listOf(Pair(7, 7), Pair(7, 8), Pair(7, 9))) }
  var dir by remember { mutableStateOf(Pair(0, -1)) }
  var food by remember { mutableStateOf(Pair(4, 4)) }
  var isRunning by remember { mutableStateOf(true) }
  var score by remember { mutableIntStateOf(0) }
  var bestScore by remember { mutableIntStateOf(initialHighScore) }

  LaunchedEffect(isRunning, dir) {
    while (isRunning) {
      delay(165L)
      val head = snake.first()
      val nextHead =
        Pair(
          (head.first + dir.first + gridSize) % gridSize,
          (head.second + dir.second + gridSize) % gridSize,
        )
      if (snake.contains(nextHead)) {
        isRunning = false
      } else {
        if (nextHead == food) {
          snake = listOf(nextHead) + snake
          score += 10
          if (score > bestScore) {
            bestScore = score
            onNewHighScore(bestScore)
          }
          var nextFood = Pair(Random.nextInt(gridSize), Random.nextInt(gridSize))
          while (snake.contains(nextFood)) {
            nextFood = Pair(Random.nextInt(gridSize), Random.nextInt(gridSize))
          }
          food = nextFood
        } else {
          snake = listOf(nextHead) + snake.dropLast(1)
        }
      }
    }
  }

  Column(
    modifier =
      Modifier.fillMaxSize()
        .padding(16.dp)
        .testTag("builtin_snake_game"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      ScorePill(label = "SKOR", value = score.toString(), color = IosGreen)
      ScorePill(label = "REKOR", value = bestScore.toString(), color = IosYellow)
    }

    Box(
      modifier =
        Modifier.widthIn(max = 360.dp)
          .fillMaxWidth()
          .aspectRatio(1f)
          .clip(RoundedCornerShape(24.dp))
          .background(Color(0xFF0B1410))
          .border(1.dp, IosGlassBorder, RoundedCornerShape(24.dp))
          .padding(10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val cellW = size.width / gridSize
        val cellH = size.height / gridSize

        // Food
        drawCircle(
          color = IosPink,
          radius = cellW * 0.42f,
          center = Offset((food.first + 0.5f) * cellW, (food.second + 0.5f) * cellH),
        )

        // Snake segments
        snake.forEachIndexed { index, part ->
          drawRoundRect(
            color = if (index == 0) IosGreen else IosCyan,
            topLeft = Offset(part.first * cellW + 2f, part.second * cellH + 2f),
            size = Size(cellW - 4f, cellH - 4f),
            cornerRadius = CornerRadius(8f, 8f),
          )
        }
      }

      if (!isRunning) {
        Surface(color = Color(0xD91C1C1E), shape = RoundedCornerShape(18.dp)) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text("Yılan Çarptı!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
              onClick = {
                snake = listOf(Pair(7, 7), Pair(7, 8), Pair(7, 9))
                dir = Pair(0, -1)
                score = 0
                isRunning = true
              },
              colors = ButtonDefaults.buttonColors(containerColor = IosGreen, contentColor = Color.Black),
            ) {
              Text("Tekrar Oyna", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // D-Pad
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ArcadeControlButton(
        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
        contentDesc = "Sol",
        onClick = { if (dir.first == 0) dir = Pair(-1, 0) },
      )
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ArcadeControlButton(
          icon = Icons.Default.KeyboardArrowUp,
          contentDesc = "Yukarı",
          onClick = { if (dir.second == 0) dir = Pair(0, -1) },
        )
        ArcadeControlButton(
          icon = Icons.Default.KeyboardArrowDown,
          contentDesc = "Aşağı",
          onClick = { if (dir.second == 0) dir = Pair(0, 1) },
        )
      }
      ArcadeControlButton(
        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDesc = "Sağ",
        onClick = { if (dir.first == 0) dir = Pair(1, 0) },
      )
    }
  }
}

@Composable
private fun ScorePill(label: String, value: String, color: Color) {
  Surface(
    color = IosElevatedCard,
    shape = RoundedCornerShape(14.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "$label: ",
        color = IosSecondaryText,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = value,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
      )
    }
  }
}

@Composable
private fun ArcadeControlButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  contentDesc: String,
  onClick: () -> Unit,
) {
  Button(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    colors = ButtonDefaults.buttonColors(containerColor = IosElevatedSurface),
    contentPadding = PaddingValues(0.dp),
    modifier = Modifier.size(56.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDesc,
      tint = Color.White,
      modifier = Modifier.size(28.dp),
    )
  }
}

private fun init2048Board(): List<Int> {
  val empty = List(16) { 0 }
  return spawnRandom2048Tile(spawnRandom2048Tile(empty))
}

private fun spawnRandom2048Tile(board: List<Int>): List<Int> {
  val zeroIndices = board.indices.filter { board[it] == 0 }
  if (zeroIndices.isEmpty()) return board
  val target = zeroIndices[Random.nextInt(zeroIndices.size)]
  val mutable = board.toMutableList()
  mutable[target] = if (Random.nextFloat() < 0.88f) 2 else 4
  return mutable
}

private fun move2048(board: List<Int>, direction: Int): Triple<List<Int>, Int, Boolean> {
  val result = board.toMutableList()
  var totalGained = 0
  var moved = false

  for (i in 0 until 4) {
    val indices =
      when (direction) {
        0 -> listOf(i, i + 4, i + 8, i + 12) // Up
        1 -> listOf(i * 4 + 3, i * 4 + 2, i * 4 + 1, i * 4) // Right
        2 -> listOf(i + 12, i + 8, i + 4, i) // Down
        else -> listOf(i * 4, i * 4 + 1, i * 4 + 2, i * 4 + 3) // Left
      }
    val line = indices.map { result[it] }.filter { it != 0 }.toMutableList()
    val merged = mutableListOf<Int>()
    var idx = 0
    while (idx < line.size) {
      if (idx + 1 < line.size && line[idx] == line[idx + 1]) {
        val doubled = line[idx] * 2
        merged.add(doubled)
        totalGained += doubled
        idx += 2
      } else {
        merged.add(line[idx])
        idx += 1
      }
    }
    while (merged.size < 4) merged.add(0)
    for (pos in 0 until 4) {
      if (result[indices[pos]] != merged[pos]) moved = true
      result[indices[pos]] = merged[pos]
    }
  }
  return Triple(result, totalGained, moved)
}

private fun canMove2048(board: List<Int>): Boolean {
  if (board.any { it == 0 }) return true
  for (r in 0 until 4) {
    for (c in 0 until 4) {
      val v = board[r * 4 + c]
      if (c + 1 < 4 && board[r * 4 + c + 1] == v) return true
      if (r + 1 < 4 && board[(r + 1) * 4 + c] == v) return true
    }
  }
  return false
}

private fun tile2048Color(value: Int): Color =
  when (value) {
    0 -> Color(0xFF2C2C2E)
    2 -> Color(0xFF3A3A3C)
    4 -> Color(0xFF48484A)
    8 -> Color(0xFFFF9F0A)
    16 -> Color(0xFFFF6B00)
    32 -> Color(0xFFFF375F)
    64 -> Color(0xFFFF2D55)
    128 -> Color(0xFFBF5AF2)
    256 -> Color(0xFF5E5CE6)
    512 -> Color(0xFF0A84FF)
    1024 -> Color(0xFF30D158)
    else -> Color(0xFFFFD60A)
  }
