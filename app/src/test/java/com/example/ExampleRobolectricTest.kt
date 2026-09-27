package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.VaultDatabase
import com.example.data.VaultRepository
import com.example.ui.CalculatorViewModel
import com.example.ui.ScreenMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: VaultDatabase
  private lateinit var viewModel: CalculatorViewModel

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db =
      Room.inMemoryDatabaseBuilder(context, VaultDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    viewModel = CalculatorViewModel(VaultRepository(db.vaultDao()))
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Hesap Makinesi", appName)
  }

  @Test
  fun `typing 67- unlocks secret poki folder and resets display`() {
    assertTrue(viewModel.screenMode.value is ScreenMode.Calculator)
    viewModel.onDigitClick("6")
    viewModel.onDigitClick("7")
    assertEquals("67", viewModel.uiState.value.displayText)

    viewModel.onOperatorClick("-")
    assertTrue(viewModel.screenMode.value is ScreenMode.SecretFolder)
    assertEquals("0", viewModel.uiState.value.displayText)
  }

  @Test
  fun `standard calculator addition works accurately`() {
    viewModel.onDigitClick("2")
    viewModel.onDigitClick("5")
    viewModel.onOperatorClick("+")
    viewModel.onDigitClick("1")
    viewModel.onDigitClick("7")
    viewModel.onEqualsClick()
    assertEquals("42", viewModel.uiState.value.displayText)
  }
}
