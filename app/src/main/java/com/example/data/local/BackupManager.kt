package com.example.data.local

import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.CustomerStage
import com.example.data.model.JobItem
import com.example.data.model.PaintingJobTypes
import com.example.data.model.PaymentItem
import com.example.data.model.SkywallFormatters
import com.example.data.model.ThemePreference
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToLong

object BackupManager {
  data class ParsedBackup(
    val customers: List<Customer>,
    val settings: BusinessSettings?,
  )

  fun exportJson(customers: List<Customer>, settings: BusinessSettings): String {
    val root = JSONObject()
    val customerArray = JSONArray()
    customers.forEach { c ->
      val cObj = JSONObject().apply {
        put("id", c.id)
        put("sample", c.isSample)
        put("name", c.name)
        put("phone", c.phone)
        put("place", c.place)
        put("stage", c.stage.key)
        put("discount", c.discount)
        put("visit", c.visitDate)
        put("follow", c.followDate)
        put("notes", c.notes)
        put("created", c.createdAt)
      }
      val jobsArray = JSONArray()
      c.jobs.forEach { j ->
        jobsArray.put(
          JSONObject().apply {
            put("type", j.type)
            put("colour", j.colour)
            put("hex", j.hex)
            put("area", j.area)
            put("rate", j.rate)
          }
        )
      }
      cObj.put("jobs", jobsArray)

      val paymentsArray = JSONArray()
      c.payments.forEach { p ->
        paymentsArray.put(
          JSONObject().apply {
            put("id", p.id)
            put("date", p.date)
            put("amt", p.amount)
            put("note", p.note)
          }
        )
      }
      cObj.put("payments", paymentsArray)
      customerArray.put(cObj)
    }

    val settingsObj = JSONObject().apply {
      put("name", settings.name)
      put("address", settings.address)
      put("phone", settings.phone)
      put("theme", settings.theme.key)
    }

    root.put("customers", customerArray)
    root.put("settings", settingsObj)
    return root.toString(2)
  }

  fun importJson(rawJson: String): ParsedBackup {
    val root = JSONObject(rawJson)
    val customersArray =
      root.optJSONArray("customers")
        ?: throw IllegalArgumentException("Missing customers array")

    val parsedCustomers =
      (0 until customersArray.length()).map { idx ->
        val obj = customersArray.getJSONObject(idx)
        val jobsArray = obj.optJSONArray("jobs") ?: JSONArray()
        val parsedJobs =
          (0 until jobsArray.length()).map { jIdx ->
            val jObj = jobsArray.getJSONObject(jIdx)
            JobItem(
              type = jObj.optString("type", PaintingJobTypes.defaults.first()),
              colour = jObj.optString("colour", ""),
              hex = jObj.optString("hex", "#DDDDDD").ifBlank { "#DDDDDD" },
              area = jObj.optDouble("area", 0.0).takeIf { !it.isNaN() } ?: 0.0,
              rate = jObj.optDouble("rate", 0.0).takeIf { !it.isNaN() } ?: 0.0,
            )
          }.ifEmpty { listOf(JobItem()) }

        val paymentsArray = obj.optJSONArray("payments") ?: JSONArray()
        val parsedPayments =
          (0 until paymentsArray.length()).map { pIdx ->
            val pObj = paymentsArray.getJSONObject(pIdx)
            PaymentItem(
              id = pObj.optString("id", "").ifBlank { SkywallFormatters.generateUid("p") },
              date = pObj.optString("date", SkywallFormatters.todayYmd()),
              amount = pObj.optDouble("amt", 0.0).takeIf { !it.isNaN() } ?: 0.0,
              note = pObj.optString("note", ""),
            )
          }

        Customer(
          id = obj.optString("id", "").ifBlank { SkywallFormatters.generateUid() },
          name = obj.optString("name", "").trim().ifEmpty { "பெயர் இல்லை" },
          phone = obj.optString("phone", ""),
          place = obj.optString("place", ""),
          stage = CustomerStage.fromKey(obj.optString("stage", "enquiry")),
          jobs = parsedJobs,
          discount = obj.optDouble("discount", 0.0).takeIf { !it.isNaN() } ?: 0.0,
          visitDate = obj.optString("visit", ""),
          followDate = obj.optString("follow", ""),
          notes = obj.optString("notes", ""),
          isSample = obj.optBoolean("sample", false),
          createdAt = obj.optLong("created", System.currentTimeMillis()),
          payments = parsedPayments,
        )
      }

    val settingsObj = root.optJSONObject("settings")
    val parsedSettings =
      settingsObj?.let { s ->
        BusinessSettings(
          name = s.optString("name", "").ifBlank { BusinessSettings.DEFAULT_NAME },
          address = s.optString("address", "").ifBlank { BusinessSettings.DEFAULT_ADDRESS },
          phone = s.optString("phone", "").ifBlank { BusinessSettings.DEFAULT_PHONE },
          theme = ThemePreference.fromKey(s.optString("theme", "auto")),
        )
      }

    return ParsedBackup(customers = parsedCustomers, settings = parsedSettings)
  }

  fun exportCsv(customers: List<Customer>): String {
    fun q(value: Any?): String {
      val s = value?.toString() ?: ""
      return "\"${s.replace("\"", "\"\"")}\""
    }

    val header =
      listOf(
        "பெயர்",
        "தொலைபேசி",
        "இடம்",
        "நிலை",
        "வேலை",
        "மதிப்பீடு",
        "பெற்றது",
        "பாக்கி",
        "தள ஆய்வு",
        "பின்தொடர்தல்",
        "குறிப்பு",
      )
    val rows = mutableListOf(header)
    customers.forEach { c ->
      val jobsSummary =
        c.jobs.joinToString("; ") { j ->
          val col = if (j.colour.isNotBlank()) " (${j.colour})" else ""
          "${j.type}$col ${SkywallFormatters.formatNumberClean(j.area)}x${SkywallFormatters.formatNumberClean(j.rate)}"
        }
      rows.add(
        listOf(
          c.name,
          c.phone,
          c.place,
          c.stage.labelTa,
          jobsSummary,
          c.totalEstimate.roundToLong().toString(),
          c.paidAmount.roundToLong().toString(),
          c.dueAmount.roundToLong().toString(),
          c.visitDate,
          c.followDate,
          c.notes,
        )
      )
    }

    return "\uFEFF" + rows.joinToString("\r\n") { row -> row.joinToString(",") { q(it) } }
  }
}
