package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Trade
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Trade Management", appName)
  }

  @Test
  fun `test trade profit calculation for BUY`() {
    // Buy 10 RELIANCE @ 2450, Exit @ 2510, Charges 80
    val (gross, net, status) = Trade.calculatePnL(
      buySell = "BUY",
      entryPrice = 2450.0,
      exitPrice = 2510.0,
      quantity = 10,
      totalCharges = 80.0
    )
    assertEquals(600.0, gross, 0.001)
    assertEquals(520.0, net, 0.001)
    assertEquals("PROFIT", status)
  }

  @Test
  fun `test trade loss calculation for BUY`() {
    val (gross, net, status) = Trade.calculatePnL(
      buySell = "BUY",
      entryPrice = 120.0,
      exitPrice = 96.0,
      quantity = 50,
      totalCharges = 60.0
    )
    assertEquals(-1200.0, gross, 0.001)
    assertEquals(-1260.0, net, 0.001)
    assertEquals("LOSS", status)
  }
}
