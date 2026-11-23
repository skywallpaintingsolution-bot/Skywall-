package com.example.data.repository

import com.example.data.local.BackupManager
import com.example.data.local.CustomerDao
import com.example.data.local.PaymentEntity
import com.example.data.local.SettingsDataStore
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.CustomerStage
import com.example.data.model.JobItem
import com.example.data.model.PaymentItem
import com.example.data.model.SkywallFormatters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CustomerRepository(
  private val customerDao: CustomerDao,
  private val settingsDataStore: SettingsDataStore,
) {
  val customersFlow: Flow<List<Customer>> =
    customerDao.observeAllCustomers().map { list -> list.map { it.toDomain() } }

  val settingsFlow: Flow<BusinessSettings> = settingsDataStore.settingsFlow

  suspend fun ensureSampleSeededIfFirstLaunch() {
    if (!settingsDataStore.hasSeededSample()) {
      if (customerDao.countCustomers() == 0) {
        customerDao.replaceAllCustomers(createSampleCustomers())
      }
      settingsDataStore.markSampleSeeded()
    }
  }

  suspend fun saveCustomer(customer: Customer) {
    customerDao.upsertCustomerWithJobsAndPayments(customer)
  }

  suspend fun updateStage(customerId: String, stage: CustomerStage) {
    customerDao.updateStage(customerId, stage.key)
  }

  suspend fun deleteCustomer(customerId: String) {
    customerDao.deleteCustomerById(customerId)
  }

  suspend fun clearSampleCustomers() {
    customerDao.deleteSampleCustomers()
  }

  suspend fun wipeAllCustomers() {
    customerDao.deleteAllCustomers()
  }

  suspend fun addPayment(customerId: String, date: String, amount: Double, note: String) {
    customerDao.insertPayment(
      PaymentEntity(
        id = SkywallFormatters.generateUid("p"),
        customerId = customerId,
        date = date,
        amount = amount,
        note = note.trim(),
      )
    )
  }

  suspend fun deletePayment(paymentId: String) {
    customerDao.deletePaymentById(paymentId)
  }

  suspend fun saveSettings(settings: BusinessSettings) {
    settingsDataStore.saveSettings(settings)
  }

  suspend fun restoreFromJson(rawJson: String) {
    val parsed = BackupManager.importJson(rawJson)
    customerDao.replaceAllCustomers(parsed.customers)
    parsed.settings?.let { settingsDataStore.saveSettings(it) }
    settingsDataStore.markSampleSeeded()
  }

  private fun createSampleCustomers(): List<Customer> {
    val today = SkywallFormatters.todayYmd()
    val baseTime = System.currentTimeMillis()
    fun mk(
      id: String,
      name: String,
      phone: String,
      place: String,
      stage: CustomerStage,
      jobs: List<JobItem>,
      paid: Double,
      visit: String = "",
      follow: String = "",
      offsetOrder: Long,
    ): Customer {
      return Customer(
        id = id,
        name = "$name (மாதிரி)",
        phone = phone,
        place = place,
        stage = stage,
        jobs = jobs,
        discount = 0.0,
        visitDate = visit,
        followDate = follow,
        notes = "",
        isSample = true,
        createdAt = baseTime - offsetOrder * 10_000L,
        payments =
          if (paid > 0.0) {
            listOf(
              PaymentItem(
                id = SkywallFormatters.generateUid("p"),
                date = today,
                amount = paid,
                note = "முன்பணம்",
              )
            )
          } else {
            emptyList()
          },
      )
    }

    return listOf(
      mk(
        id = "ex1",
        name = "முருகன்",
        phone = "9000000001",
        place = "மணவாளநகர்",
        stage = CustomerStage.PROGRESS,
        jobs =
          listOf(
            JobItem(
              type = "உள் சுவர் பெயிண்டிங்",
              colour = "ஐவரி",
              hex = "#E8DCC8",
              area = 1900.0,
              rate = 12.0,
            ),
            JobItem(
              type = "வெளிப்புற பெயிண்டிங்",
              colour = "க்ரீம்",
              hex = "#D8D2C4",
              area = 1000.0,
              rate = 23.0,
            ),
          ),
        paid = 15000.0,
        visit = SkywallFormatters.addDaysYmd(-4),
        follow = SkywallFormatters.addDaysYmd(1),
        offsetOrder = 1,
      ),
      mk(
        id = "ex2",
        name = "லதா",
        phone = "9000000002",
        place = "திருவள்ளூர்",
        stage = CustomerStage.QUOTE,
        jobs =
          listOf(
            JobItem(
              type = "வெளிப்புற பெயிண்டிங்",
              colour = "டெரகோட்டா 4412",
              hex = "#D9694A",
              area = 2400.0,
              rate = 18.0,
            )
          ),
        paid = 0.0,
        visit = SkywallFormatters.addDaysYmd(-3),
        follow = SkywallFormatters.addDaysYmd(-1),
        offsetOrder = 2,
      ),
      mk(
        id = "ex3",
        name = "ரமேஷ்",
        phone = "9000000003",
        place = "கடம்பத்தூர்",
        stage = CustomerStage.DONE,
        jobs =
          listOf(
            JobItem(
              type = "வாட்டர் ப்ரூஃபிங்",
              colour = "",
              hex = "#7FAE8A",
              area = 900.0,
              rate = 22.0,
            )
          ),
        paid = 19800.0,
        visit = SkywallFormatters.addDaysYmd(-60),
        follow = "",
        offsetOrder = 3,
      ),
      mk(
        id = "ex4",
        name = "செல்வி",
        phone = "9000000004",
        place = "பூந்தமல்லி",
        stage = CustomerStage.CONFIRMED,
        jobs =
          listOf(
            JobItem(
              type = "டெக்ஸ்சர் / டிசைன்",
              colour = "ஸ்கை ப்ளூ 2207",
              hex = "#0A6F93",
              area = 600.0,
              rate = 35.0,
            )
          ),
        paid = 5000.0,
        visit = SkywallFormatters.addDaysYmd(-1),
        follow = "",
        offsetOrder = 4,
      ),
      mk(
        id = "ex5",
        name = "கார்த்திக்",
        phone = "9000000005",
        place = "மணவாளநகர்",
        stage = CustomerStage.ENQUIRY,
        jobs =
          listOf(
            JobItem(
              type = "மர வேலை பாலிஷ்",
              colour = "",
              hex = "#8A5A2B",
              area = 1.0,
              rate = 14000.0,
            )
          ),
        paid = 0.0,
        visit = "",
        follow = SkywallFormatters.addDaysYmd(3),
        offsetOrder = 5,
      ),
    )
  }
}
