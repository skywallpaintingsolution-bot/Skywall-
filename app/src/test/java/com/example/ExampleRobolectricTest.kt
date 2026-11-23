package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BackupManager
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.CustomerStage
import com.example.data.model.JobItem
import com.example.data.model.PaymentItem
import com.example.data.model.SkywallFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app name matches Skywall`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Skywall", appName)
  }

  @Test
  fun `customer totals and whatsapp message calculate accurately`() {
    val customer =
      Customer(
        id = "c1",
        name = "முருகன்",
        phone = "9876543210",
        place = "மணவாளநகர்",
        stage = CustomerStage.QUOTE,
        jobs =
          listOf(
            JobItem(
              type = "உள் சுவர் பெயிண்டிங்",
              colour = "ஐவரி",
              hex = "#E8DCC8",
              area = 1000.0,
              rate = 12.0,
            )
          ),
        discount = 500.0,
        payments = listOf(PaymentItem(id = "p1", date = "2026-11-23", amount = 3000.0, note = "முன்பணம்")),
      )

    assertEquals(12000.0, customer.subtotal, 0.01)
    assertEquals(11500.0, customer.totalEstimate, 0.01)
    assertEquals(3000.0, customer.paidAmount, 0.01)
    assertEquals(8500.0, customer.dueAmount, 0.01)

    val msg = SkywallFormatters.buildWhatsAppMessage(customer, BusinessSettings())
    assertTrue(msg.contains("முருகன்"))
    assertTrue(msg.contains("₹11,500"))
    assertTrue(msg.contains("₹8,500"))

    val uri = SkywallFormatters.buildWhatsAppUri(customer.phone, msg)
    assertTrue(uri.toString().startsWith("https://wa.me/919876543210?text="))
  }

  @Test
  fun `json backup round trip and csv export preserve customer data`() {
    val customer =
      Customer(
        id = "ex1",
        name = "லதா",
        phone = "9000000002",
        place = "திருவள்ளூர்",
        stage = CustomerStage.PROGRESS,
        jobs =
          listOf(
            JobItem(
              type = "வெளிப்புற பெயிண்டிங்",
              colour = "டெரகோட்டா",
              hex = "#D9694A",
              area = 200.0,
              rate = 20.0,
            )
          ),
        discount = 200.0,
        visitDate = "2026-11-20",
        followDate = "2026-11-24",
        notes = "இரண்டு கோட்",
        isSample = false,
        createdAt = 123456789L,
        payments = listOf(PaymentItem(id = "p1", date = "2026-11-21", amount = 1000.0, note = "UPI")),
      )
    val settings = BusinessSettings()

    val json = BackupManager.exportJson(listOf(customer), settings)
    val restored = BackupManager.importJson(json)
    assertEquals(1, restored.customers.size)
    assertEquals("லதா", restored.customers.first().name)
    assertEquals(3800.0, restored.customers.first().totalEstimate, 0.01)
    assertEquals(2800.0, restored.customers.first().dueAmount, 0.01)

    val csv = BackupManager.exportCsv(listOf(customer))
    assertTrue(csv.startsWith("\uFEFF"))
    assertTrue(csv.contains("லதா"))
    assertTrue(csv.contains("வேலை நடக்கிறது"))
  }

  @Test
  fun `branded bill bitmap renders and message types format cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val customer =
      Customer(
        id = "c_bill",
        name = "Senivasan",
        phone = "9876543210",
        place = "Mapped",
        stage = com.example.data.model.CustomerStage.DONE,
        jobs =
          listOf(
            JobItem(
              type = "உள் சுவர் பெயிண்டிங்",
              colour = "",
              hex = "#E8DCC8",
              area = 31.0,
              rate = 2800.0,
            )
          ),
        payments =
          listOf(
            PaymentItem(id = "p1", date = "2026-11-01", amount = 57000.0, note = "முன்பணம்"),
            PaymentItem(id = "p2", date = "2026-11-15", amount = 10000.0, note = ""),
          ),
      )

    val billNumber = SkywallFormatters.generateBillNumber(customer)
    assertTrue(billNumber.startsWith("SW-"))
    assertEquals("23 நவம்பர், 2026", SkywallFormatters.formatFullDateTa("2026-11-23"))
    assertTrue(SkywallFormatters.isValidWhatsAppPhone("9876543210"))
    assertTrue(!SkywallFormatters.isValidWhatsAppPhone("12345"))

    val dueReminder =
      SkywallFormatters.buildWhatsAppMessage(
        customer,
        BusinessSettings(),
        com.example.data.model.WhatsAppMessageType.DUE_REMINDER,
      )
    assertTrue(dueReminder.contains("பாக்கி தொகை ₹19,800"))

    val bitmap =
      com.example.ui.components.BillImageExporter.renderBillBitmap(
        context = context,
        customer = customer,
        settings = BusinessSettings(),
        docType = com.example.data.model.BillDocType.INVOICE,
        billNumber = billNumber,
        issueDateYmd = "2026-11-23",
      )
    assertEquals(1080, bitmap.width)
    assertTrue(bitmap.height >= 900)
  }
}
