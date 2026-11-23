package com.example.data.model

enum class CustomerStage(val key: String, val labelTa: String) {
  ENQUIRY("enquiry", "விசாரணை"),
  QUOTE("quote", "மதிப்பீடு அனுப்பப்பட்டது"),
  CONFIRMED("confirmed", "உறுதியானது"),
  PROGRESS("progress", "வேலை நடக்கிறது"),
  DONE("done", "முடிந்தது");

  companion object {
    fun fromKey(key: String?): CustomerStage =
      entries.firstOrNull { it.key == key } ?: ENQUIRY
  }
}

enum class CustomerFilter(val key: String, val labelTa: String) {
  ALL("all", "அனைத்தும்"),
  ENQUIRY("enquiry", "விசாரணை"),
  QUOTE("quote", "மதிப்பீடு அனுப்பப்பட்டது"),
  CONFIRMED("confirmed", "உறுதியானது"),
  PROGRESS("progress", "வேலை நடக்கிறது"),
  DONE("done", "முடிந்தது"),
  FOLLOWUP("followup", "பின்தொடர்தல்");

  fun toStageOrNull(): CustomerStage? = CustomerStage.entries.firstOrNull { it.key == key }
}

enum class SortOption(val key: String, val labelTa: String) {
  NEWEST("new", "புதியது முதல்"),
  DUE_DESC("due", "பாக்கி அதிகம்"),
  VALUE_DESC("value", "மதிப்பீடு அதிகம்"),
  FOLLOWUP_ASC("follow", "பின்தொடர்தல் தேதி"),
  NAME_ASC("name", "பெயர் வரிசை"),
}

enum class ThemePreference(val key: String, val labelTa: String) {
  AUTO("auto", "தானியங்கி"),
  LIGHT("light", "வெளிச்சம்"),
  DARK("dark", "இருண்டது");

  companion object {
    fun fromKey(key: String?): ThemePreference =
      entries.firstOrNull { it.key == key } ?: AUTO
  }
}

enum class BillDocType(val titleTa: String, val titleEn: String, val dropdownLabel: String) {
  INVOICE("பில்", "INVOICE", "பில் (Invoice)"),
  ESTIMATE("மதிப்பீடு", "ESTIMATE", "மதிப்பீடு (Estimate)"),
}

enum class WhatsAppMessageType(val labelTa: String) {
  SITE_VISIT("தள ஆய்வு அழைப்பு"),
  ESTIMATE("மதிப்பீடு விவரம்"),
  CONFIRMED("வேலை உறுதி செய்தி"),
  DUE_REMINDER("பாக்கி நினைவூட்டல்"),
  THANK_YOU("நன்றி செய்தி"),
}

object PaintingJobTypes {
  val defaults =
    listOf(
      "உள் சுவர் பெயிண்டிங்",
      "வெளிப்புற பெயிண்டிங்",
      "டெக்ஸ்சர் / டிசைன்",
      "வாட்டர் ப்ரூஃபிங்",
      "மர வேலை பாலிஷ்",
      "மற்றவை",
    )
}

data class PaintSwatchPreset(val nameTa: String, val hex: String)

object PaintSwatchPresets {
  val presets =
    listOf(
      PaintSwatchPreset("ஐவரி", "#E8DCC8"),
      PaintSwatchPreset("க்ரீம்", "#D8D2C4"),
      PaintSwatchPreset("ஸ்கை ப்ளூ 2207", "#0A6F93"),
      PaintSwatchPreset("ராயல் நேவி", "#0B2160"),
      PaintSwatchPreset("டெரகோட்டா 4412", "#D9694A"),
      PaintSwatchPreset("மின்ட் க்ரீன்", "#7FAE8A"),
      PaintSwatchPreset("சான்ட் ஸ்டோன்", "#D6B88C"),
      PaintSwatchPreset("டீக் வுட்", "#8A5A2B"),
      PaintSwatchPreset("பியூர் வைட்", "#F5F5F2"),
      PaintSwatchPreset("சார்கோல் கிரே", "#4A525A"),
      PaintSwatchPreset("லாவண்டர் மிஸ்ட்", "#B8B0D4"),
      PaintSwatchPreset("சன்ஷைன் யெல்லோ", "#EBC14D"),
    )
}

data class JobItem(
  val id: Long = 0L,
  val type: String = PaintingJobTypes.defaults.first(),
  val colour: String = "",
  val hex: String = "#DDDDDD",
  val area: Double = 0.0,
  val rate: Double = 0.0,
) {
  val lineAmount: Double
    get() = area.coerceAtLeast(0.0) * rate.coerceAtLeast(0.0)
}

data class PaymentItem(
  val id: String,
  val date: String,
  val amount: Double,
  val note: String = "",
)

data class Customer(
  val id: String,
  val name: String,
  val phone: String,
  val place: String = "",
  val stage: CustomerStage = CustomerStage.ENQUIRY,
  val jobs: List<JobItem> = listOf(JobItem()),
  val discount: Double = 0.0,
  val visitDate: String = "",
  val followDate: String = "",
  val notes: String = "",
  val isSample: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val payments: List<PaymentItem> = emptyList(),
) {
  val subtotal: Double
    get() = jobs.sumOf { it.lineAmount }

  val totalEstimate: Double
    get() = (subtotal - discount.coerceAtLeast(0.0)).coerceAtLeast(0.0)

  val paidAmount: Double
    get() = payments.sumOf { it.amount.coerceAtLeast(0.0) }

  val dueAmount: Double
    get() = (totalEstimate - paidAmount).coerceAtLeast(0.0)

  val paymentProgress: Float
    get() =
      if (totalEstimate <= 0.0) 0f
      else (paidAmount / totalEstimate).toFloat().coerceIn(0f, 1f)

  val isInDueTracking: Boolean
    get() =
      stage == CustomerStage.CONFIRMED ||
        stage == CustomerStage.PROGRESS ||
        stage == CustomerStage.DONE

  val jobSummaryTitle: String
    get() {
      val types = jobs.map { it.type.trim() }.filter { it.isNotEmpty() }
      if (types.isEmpty()) return "வேலை குறிப்பிடப்படவில்லை"
      return if (types.size > 1) "${types.first()} +${types.size - 1}" else types.first()
    }
}

data class BusinessSettings(
  val name: String = DEFAULT_NAME,
  val address: String = DEFAULT_ADDRESS,
  val phone: String = DEFAULT_PHONE,
  val theme: ThemePreference = ThemePreference.AUTO,
) {
  companion object {
    const val DEFAULT_NAME = "ஸ்கைவால் பெயிண்டிங் சொல்யூஷன்ஸ்"
    const val DEFAULT_ADDRESS =
      "எண் 5, பொன்னியம்மன் கோவில் பின்புறம், பட்டறை கிராமம், அத்திகத்தூர் ரோடு, மணவாளநகர் 602002"
    const val DEFAULT_PHONE = "6380817101"
  }
}

data class TodayTask(
  val customer: Customer,
  val isLate: Boolean,
  val label: String,
)

data class MonthCollection(
  val yearMonth: String,
  val shortLabel: String,
  val fullLabel: String,
  val amount: Double,
  val isCurrentMonth: Boolean,
)

data class DashboardSummary(
  val totalCustomers: Int,
  val newCount: Int,
  val activeValue: Double,
  val monthCollected: Double,
  val totalDue: Double,
  val tasks: List<TodayTask>,
  val monthCollections: List<MonthCollection>,
  val dueCustomers: List<Customer>,
  val stageCounts: Map<CustomerStage, Int>,
  val followUpCount: Int,
  val hasSampleData: Boolean,
)
