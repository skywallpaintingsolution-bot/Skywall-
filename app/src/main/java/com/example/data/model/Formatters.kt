package com.example.data.model

import android.net.Uri
import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

object SkywallFormatters {
  private val tamilShortMonths =
    arrayOf(
      "ஜன.",
      "பிப்.",
      "மார்.",
      "ஏப்.",
      "மே",
      "ஜூன்",
      "ஜூலை",
      "ஆக.",
      "செப்.",
      "அக்.",
      "நவ.",
      "டிச.",
    )

  private val tamilFullMonths =
    arrayOf(
      "ஜனவரி",
      "பிப்ரவரி",
      "மார்ச்",
      "ஏப்ரல்",
      "மே",
      "ஜூன்",
      "ஜூலை",
      "ஆகஸ்ட்",
      "செப்டம்பர்",
      "அக்டோபர்",
      "நவம்பர்",
      "டிசம்பர்",
    )

  private val indianLocale = Locale("en", "IN")

  fun formatInr(amount: Double): String {
    val formatter = NumberFormat.getIntegerInstance(indianLocale)
    return "₹${formatter.format(amount.roundToLong())}"
  }

  fun formatCompactInr(amount: Double): String {
    if (amount <= 0.0) return "–"
    return if (amount >= 1000.0) {
      val k = amount / 1000.0
      val formatted =
        if (k % 1.0 == 0.0) k.toInt().toString()
        else String.format(Locale.US, "%.1f", k).trimEnd('0').trimEnd('.')
      "₹${formatted}K"
    } else {
      "₹${amount.roundToLong()}"
    }
  }

  fun formatNumberClean(value: Double): String {
    if (value == 0.0) return "0"
    return if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format(Locale.US, "%.2f", value).trimEnd('0').trimEnd('.')
  }

  fun todayYmd(): String = formatCalendar(Calendar.getInstance())

  fun addDaysYmd(days: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, days)
    return formatCalendar(cal)
  }

  fun epochMillisToYmd(millis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    return formatCalendar(cal)
  }

  fun ymdToEpochMillis(ymd: String): Long? {
    val parts = parseYmd(ymd) ?: return null
    val cal = Calendar.getInstance()
    cal.clear()
    cal.set(parts.first, parts.second - 1, parts.third, 12, 0, 0)
    return cal.timeInMillis
  }

  fun formatShortDateTa(ymd: String): String {
    val parts = parseYmd(ymd) ?: return ""
    val monthIdx = (parts.second - 1).coerceIn(0, 11)
    return "${tamilShortMonths[monthIdx]} ${parts.third}"
  }

  fun formatFullDateTa(ymd: String): String {
    val parts = parseYmd(ymd) ?: return ""
    val monthIdx = (parts.second - 1).coerceIn(0, 11)
    return "${parts.third} ${tamilFullMonths[monthIdx]}, ${parts.first}"
  }

  fun generateBillNumber(customer: Customer): String {
    val cal = Calendar.getInstance().apply { timeInMillis = customer.createdAt }
    val yy = String.format(Locale.US, "%02d", cal.get(Calendar.YEAR) % 100)
    val mm = String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
    val dd = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
    val hash = (kotlin.math.abs(customer.id.hashCode()) % 46656).toString(36).uppercase(Locale.US).padStart(3, '0')
    return "SW-$yy$mm$dd-$hash"
  }

  fun isValidWhatsAppPhone(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    return digits.length in 10..15
  }

  fun defaultMessageTypeFor(customer: Customer): WhatsAppMessageType {
    return when (customer.stage) {
      CustomerStage.ENQUIRY -> WhatsAppMessageType.SITE_VISIT
      CustomerStage.QUOTE -> WhatsAppMessageType.ESTIMATE
      CustomerStage.CONFIRMED -> WhatsAppMessageType.CONFIRMED
      CustomerStage.PROGRESS ->
        if (customer.dueAmount > 0.0) WhatsAppMessageType.DUE_REMINDER
        else WhatsAppMessageType.ESTIMATE
      CustomerStage.DONE ->
        if (customer.dueAmount > 0.0) WhatsAppMessageType.DUE_REMINDER
        else WhatsAppMessageType.THANK_YOU
    }
  }

  fun defaultBillDocTypeFor(customer: Customer): BillDocType {
    return when (customer.stage) {
      CustomerStage.ENQUIRY, CustomerStage.QUOTE -> BillDocType.ESTIMATE
      CustomerStage.CONFIRMED, CustomerStage.PROGRESS, CustomerStage.DONE -> BillDocType.INVOICE
    }
  }

  fun diffDaysFrom(ymd: String): Int? {
    val parts = parseYmd(ymd) ?: return null
    val target = Calendar.getInstance().apply {
      clear()
      set(parts.first, parts.second - 1, parts.third, 0, 0, 0)
    }
    val now = Calendar.getInstance().apply {
      val y = get(Calendar.YEAR)
      val m = get(Calendar.MONTH)
      val d = get(Calendar.DAY_OF_MONTH)
      clear()
      set(y, m, d, 0, 0, 0)
    }
    val diffMillis = now.timeInMillis - target.timeInMillis
    return kotlin.math.round(diffMillis / 86_400_000.0).toInt()
  }

  fun recentSixMonths(): List<MonthSlot> {
    val now = Calendar.getInstance()
    return (5 downTo 0).map { offset ->
      val cal = (now.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, -offset)
      }
      val y = cal.get(Calendar.YEAR)
      val m = cal.get(Calendar.MONTH)
      val ym = String.format(Locale.US, "%04d-%02d", y, m + 1)
      MonthSlot(
        yearMonth = ym,
        shortLabel = tamilShortMonths[m],
        fullLabel = "${tamilFullMonths[m]} $y",
        isCurrentMonth = offset == 0,
      )
    }
  }

  fun buildWhatsAppMessage(
    customer: Customer,
    settings: BusinessSettings,
    messageType: WhatsAppMessageType = defaultMessageTypeFor(customer),
  ): String {
    val hi = "வணக்கம் ${customer.name},\n"
    val sig = "\n\n${settings.name}\n${settings.address}\nநன்றி."

    return when (messageType) {
      WhatsAppMessageType.SITE_VISIT -> {
        hi +
          "பெயிண்டிங் வேலைக்கான தள ஆய்வுக்கு வர விரும்புகிறோம். உங்களுக்கு வசதியான தேதி மற்றும் நேரத்தை தெரிவிக்கவும்." +
          sig
      }

      WhatsAppMessageType.DUE_REMINDER -> {
        val jobNames =
          customer.jobs.map { it.type.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
        hi +
          "உங்கள் $jobNames வேலைக்கான பாக்கி தொகை ${formatInr(customer.dueAmount)} " +
          "(இறுதி மதிப்பீடு ${formatInr(customer.totalEstimate)}, பெற்றது ${formatInr(customer.paidAmount)}). " +
          "வசதியான நேரத்தில் செலுத்துமாறு கேட்டுக்கொள்கிறோம்." +
          sig
      }

      WhatsAppMessageType.THANK_YOU -> {
        hi +
          "உங்கள் பெயிண்டிங் வேலை நல்ல முறையில் முடிவடைந்தது. எங்களைத் தேர்ந்தெடுத்ததற்கு நன்றி. வேலையில் ஏதேனும் குறை இருந்தால் தயங்காமல் தெரிவிக்கவும்." +
          sig
      }

      WhatsAppMessageType.CONFIRMED, WhatsAppMessageType.ESTIMATE -> {
        val lines =
          customer.jobs.mapIndexed { index, job ->
            val colourPart = if (job.colour.isNotBlank()) " (${job.colour})" else ""
            "${index + 1}) ${job.type}$colourPart – ${formatNumberClean(job.area)} ச.அடி × ₹${formatNumberClean(job.rate)} = ${formatInr(job.lineAmount)}"
          }.joinToString("\n")

        val intro =
          if (messageType == WhatsAppMessageType.CONFIRMED) {
            "${settings.name} சார்பாக உங்கள் பெயிண்டிங் வேலை உறுதியான விவரம்:\n\n"
          } else {
            "${settings.name} சார்பாக உங்கள் பெயிண்டிங் வேலைக்கான மதிப்பீடு:\n\n"
          }

        val builder = StringBuilder()
        builder.append(hi)
        builder.append(intro)
        builder.append(lines)
        builder.append("\n\nமொத்தம்: ${formatInr(customer.subtotal)}")
        if (customer.discount > 0.0) {
          builder.append("\nதள்ளுபடி: ${formatInr(customer.discount)}")
        }
        builder.append("\nஇறுதி மதிப்பீடு: ${formatInr(customer.totalEstimate)}")
        if (customer.paidAmount > 0.0) {
          builder.append("\nபெற்ற தொகை: ${formatInr(customer.paidAmount)}")
        }
        builder.append("\nபாக்கி: ${formatInr(customer.dueAmount)}")
        builder.append(sig)
        builder.toString()
      }
    }
  }

  fun buildWhatsAppUri(phone: String, message: String): Uri {
    var digits = phone.filter { it.isDigit() }
    if (digits.length == 10) {
      digits = "91$digits"
    }
    return Uri.parse("https://wa.me/$digits?text=${Uri.encode(message)}")
  }

  fun parseHexColor(hex: String?, fallback: Color = Color(0xFFDDDDDD)): Color {
    val clean = hex?.trim().orEmpty()
    if (clean.isEmpty()) return fallback
    return try {
      val normalized = if (clean.startsWith("#")) clean else "#$clean"
      Color(android.graphics.Color.parseColor(normalized))
    } catch (_: IllegalArgumentException) {
      fallback
    }
  }

  fun colorToHex(color: Color): String {
    val r = (color.red * 255f).toInt().coerceIn(0, 255)
    val g = (color.green * 255f).toInt().coerceIn(0, 255)
    val b = (color.blue * 255f).toInt().coerceIn(0, 255)
    return String.format(Locale.US, "#%02X%02X%02X", r, g, b)
  }

  fun generateUid(prefix: String = "c"): String {
    val timePart = System.currentTimeMillis().toString(36)
    val randPart = ((Math.random() * 1679616).toInt()).toString(36).padStart(4, '0')
    return "$prefix$timePart$randPart"
  }

  private fun formatCalendar(cal: Calendar): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(cal.time)
  }

  private fun parseYmd(ymd: String): Triple<Int, Int, Int>? {
    val parts = ymd.trim().split("-")
    if (parts.size != 3) return null
    val y = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    val d = parts[2].toIntOrNull() ?: return null
    if (m !in 1..12 || d !in 1..31) return null
    return Triple(y, m, d)
  }

  data class MonthSlot(
    val yearMonth: String,
    val shortLabel: String,
    val fullLabel: String,
    val isCurrentMonth: Boolean,
  )
}
