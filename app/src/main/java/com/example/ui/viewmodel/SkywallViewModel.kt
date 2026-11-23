package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BackupManager
import com.example.data.local.SettingsDataStore
import com.example.data.local.SkywallDatabase
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.CustomerFilter
import com.example.data.model.CustomerStage
import com.example.data.model.DashboardSummary
import com.example.data.model.MonthCollection
import com.example.data.model.SkywallFormatters
import com.example.data.model.SortOption
import com.example.data.model.TodayTask
import com.example.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale

sealed interface ActiveScreen {
  data object Dashboard : ActiveScreen

  data class Detail(val customerId: String) : ActiveScreen

  data class Form(val customer: Customer? = null) : ActiveScreen

  data object Settings : ActiveScreen
}

data class SkywallUiState(
  val isLoading: Boolean = true,
  val settings: BusinessSettings = BusinessSettings(),
  val allCustomers: List<Customer> = emptyList(),
  val filteredCustomers: List<Customer> = emptyList(),
  val dashboard: DashboardSummary =
    DashboardSummary(
      totalCustomers = 0,
      newCount = 0,
      activeValue = 0.0,
      monthCollected = 0.0,
      totalDue = 0.0,
      tasks = emptyList(),
      monthCollections = emptyList(),
      dueCustomers = emptyList(),
      stageCounts = emptyMap(),
      followUpCount = 0,
      hasSampleData = false,
    ),
  val selectedFilter: CustomerFilter = CustomerFilter.ALL,
  val searchQuery: String = "",
  val sortOption: SortOption = SortOption.NEWEST,
  val activeScreen: ActiveScreen = ActiveScreen.Dashboard,
) {
  val selectedDetailCustomer: Customer?
    get() =
      (activeScreen as? ActiveScreen.Detail)?.let { detail ->
        allCustomers.firstOrNull { it.id == detail.customerId }
      }
}

class SkywallViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: CustomerRepository =
    CustomerRepository(
      customerDao = SkywallDatabase.getInstance(application).customerDao(),
      settingsDataStore = SettingsDataStore(application),
    )

  private val selectedFilter = MutableStateFlow(CustomerFilter.ALL)
  private val searchQuery = MutableStateFlow("")
  private val sortOption = MutableStateFlow(SortOption.NEWEST)
  private val activeScreen = MutableStateFlow<ActiveScreen>(ActiveScreen.Dashboard)
  private val isReady = MutableStateFlow(false)

  private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val messages: SharedFlow<String> = _messages.asSharedFlow()

  private val tamilCollator: Collator = Collator.getInstance(Locale("ta", "IN"))

  val uiState: StateFlow<SkywallUiState> =
    combine(
        repository.customersFlow,
        repository.settingsFlow,
        selectedFilter,
        searchQuery,
        sortOption,
        activeScreen,
        isReady,
      ) { values ->
        @Suppress("UNCHECKED_CAST")
        val customers = values[0] as List<Customer>
        val settings = values[1] as BusinessSettings
        val filter = values[2] as CustomerFilter
        val query = values[3] as String
        val sort = values[4] as SortOption
        val screen = values[5] as ActiveScreen
        val ready = values[6] as Boolean

        val dashboard = buildDashboardSummary(customers)
        val filtered = filterAndSortCustomers(customers, filter, query, sort)

        SkywallUiState(
          isLoading = !ready,
          settings = settings,
          allCustomers = customers,
          filteredCustomers = filtered,
          dashboard = dashboard,
          selectedFilter = filter,
          searchQuery = query,
          sortOption = sort,
          activeScreen = screen,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SkywallUiState(),
      )

  init {
    viewModelScope.launch {
      repository.ensureSampleSeededIfFirstLaunch()
      isReady.value = true
    }
  }

  fun setFilter(filter: CustomerFilter) {
    selectedFilter.value = filter
  }

  fun setSearchQuery(query: String) {
    searchQuery.value = query
  }

  fun setSortOption(option: SortOption) {
    sortOption.value = option
  }

  fun openDashboard() {
    activeScreen.value = ActiveScreen.Dashboard
  }

  fun openCustomerDetail(customerId: String) {
    activeScreen.value = ActiveScreen.Detail(customerId)
  }

  fun openNewCustomerForm() {
    activeScreen.value = ActiveScreen.Form(customer = null)
  }

  fun openEditCustomerForm(customer: Customer) {
    activeScreen.value = ActiveScreen.Form(customer = customer)
  }

  fun openSettings() {
    activeScreen.value = ActiveScreen.Settings
  }

  fun saveCustomer(customer: Customer, navigateToDetailAfterSave: Boolean = true) {
    viewModelScope.launch {
      repository.saveCustomer(customer)
      if (navigateToDetailAfterSave) {
        activeScreen.value = ActiveScreen.Detail(customer.id)
      } else {
        activeScreen.value = ActiveScreen.Dashboard
      }
      _messages.emit("சேமிக்கப்பட்டது")
    }
  }

  fun updateCustomerStage(customerId: String, stage: CustomerStage) {
    viewModelScope.launch {
      repository.updateStage(customerId, stage)
    }
  }

  fun deleteCustomer(customerId: String) {
    viewModelScope.launch {
      repository.deleteCustomer(customerId)
      if ((activeScreen.value as? ActiveScreen.Detail)?.customerId == customerId) {
        activeScreen.value = ActiveScreen.Dashboard
      }
      _messages.emit("வாடிக்கையாளர் நீக்கப்பட்டார்")
    }
  }

  fun addPayment(customerId: String, date: String, amount: Double, note: String) {
    if (amount <= 0.0) return
    viewModelScope.launch {
      repository.addPayment(customerId, date, amount, note)
      _messages.emit("தொகை சேர்க்கப்பட்டது")
    }
  }

  fun deletePayment(paymentId: String) {
    viewModelScope.launch {
      repository.deletePayment(paymentId)
      _messages.emit("பணம் பதிவு நீக்கப்பட்டது")
    }
  }

  fun clearSampleData() {
    viewModelScope.launch {
      repository.clearSampleCustomers()
      _messages.emit("மாதிரி தரவு நீக்கப்பட்டது")
    }
  }

  fun wipeAllData() {
    viewModelScope.launch {
      repository.wipeAllCustomers()
      activeScreen.value = ActiveScreen.Dashboard
      _messages.emit("அனைத்து தரவும் நீக்கப்பட்டது")
    }
  }

  fun saveSettings(settings: BusinessSettings) {
    viewModelScope.launch {
      repository.saveSettings(settings)
      activeScreen.value = ActiveScreen.Dashboard
      _messages.emit("சேமிக்கப்பட்டது")
    }
  }

  fun buildJsonBackup(): String {
    val state = uiState.value
    return BackupManager.exportJson(state.allCustomers, state.settings)
  }

  fun buildCsvExport(): String {
    return BackupManager.exportCsv(uiState.value.allCustomers)
  }

  fun restoreBackupJson(rawJson: String) {
    viewModelScope.launch {
      try {
        repository.restoreFromJson(rawJson)
        activeScreen.value = ActiveScreen.Dashboard
        _messages.emit("மீட்டெடுக்கப்பட்டது")
      } catch (_: Exception) {
        _messages.emit("கோப்பு சரியில்லை")
      }
    }
  }

  fun notifyMessage(text: String) {
    viewModelScope.launch { _messages.emit(text) }
  }

  private fun buildDashboardSummary(customers: List<Customer>): DashboardSummary {
    val today = SkywallFormatters.todayYmd()
    val currentMonthPrefix = today.take(7)

    val tasks = buildList {
      customers.forEach { c ->
        if (c.stage != CustomerStage.DONE && c.followDate.isNotBlank()) {
          val diff = SkywallFormatters.diffDaysFrom(c.followDate)
          if (diff != null && diff >= 0) {
            val txt =
              if (diff > 0) "பின்தொடர்தல் · $diff நாள் தாமதம்" else "பின்தொடர்தல் · இன்று"
            add(TodayTask(customer = c, isLate = diff > 0, label = txt))
          }
        }
        if (c.visitDate == today) {
          add(TodayTask(customer = c, isLate = false, label = "தள ஆய்வு · இன்று"))
        }
      }
    }

    val newCnt =
      customers.count {
        it.stage == CustomerStage.ENQUIRY || it.stage == CustomerStage.QUOTE
      }
    val activeVal =
      customers
        .filter { it.stage == CustomerStage.CONFIRMED || it.stage == CustomerStage.PROGRESS }
        .sumOf { it.totalEstimate }

    var monthCollected = 0.0
    val monthMap = mutableMapOf<String, Double>()
    customers.forEach { c ->
      c.payments.forEach { p ->
        val ym = p.date.take(7)
        if (ym == currentMonthPrefix) {
          monthCollected += p.amount
        }
        monthMap[ym] = (monthMap[ym] ?: 0.0) + p.amount
      }
    }

    val monthCollections =
      SkywallFormatters.recentSixMonths().map { slot ->
        MonthCollection(
          yearMonth = slot.yearMonth,
          shortLabel = slot.shortLabel,
          fullLabel = slot.fullLabel,
          amount = monthMap[slot.yearMonth] ?: 0.0,
          isCurrentMonth = slot.isCurrentMonth,
        )
      }

    val dueList =
      customers
        .filter { it.isInDueTracking && it.dueAmount > 0.0 }
        .sortedByDescending { it.dueAmount }
    val totalDue = dueList.sumOf { it.dueAmount }

    val stageCounts =
      CustomerStage.entries.associateWith { stage -> customers.count { it.stage == stage } }

    val followUpCount =
      customers.count { c ->
        c.stage != CustomerStage.DONE &&
          c.followDate.isNotBlank() &&
          (SkywallFormatters.diffDaysFrom(c.followDate) ?: -1) >= 0
      }

    return DashboardSummary(
      totalCustomers = customers.size,
      newCount = newCnt,
      activeValue = activeVal,
      monthCollected = monthCollected,
      totalDue = totalDue,
      tasks = tasks,
      monthCollections = monthCollections,
      dueCustomers = dueList,
      stageCounts = stageCounts,
      followUpCount = followUpCount,
      hasSampleData = customers.any { it.isSample },
    )
  }

  private fun filterAndSortCustomers(
    customers: List<Customer>,
    filter: CustomerFilter,
    query: String,
    sort: SortOption,
  ): List<Customer> {
    val q = query.trim().lowercase(Locale.getDefault())
    val filtered =
      customers.filter { c ->
        val stageMatches =
          when (filter) {
            CustomerFilter.ALL -> true
            CustomerFilter.FOLLOWUP ->
              c.stage != CustomerStage.DONE &&
                c.followDate.isNotBlank() &&
                (SkywallFormatters.diffDaysFrom(c.followDate) ?: -1) >= 0
            else -> c.stage == filter.toStageOrNull()
          }
        if (!stageMatches) return@filter false
        if (q.isEmpty()) return@filter true

        val hay =
          buildString {
            append(c.name).append(' ')
            append(c.phone).append(' ')
            append(c.place).append(' ')
            append(c.notes).append(' ')
            c.jobs.forEach { j -> append(j.type).append(' ').append(j.colour).append(' ') }
          }.lowercase(Locale.getDefault())
        hay.contains(q)
      }

    return when (sort) {
      SortOption.NEWEST -> filtered.sortedByDescending { it.createdAt }
      SortOption.DUE_DESC -> filtered.sortedByDescending { it.dueAmount }
      SortOption.VALUE_DESC -> filtered.sortedByDescending { it.totalEstimate }
      SortOption.FOLLOWUP_ASC ->
        filtered.sortedBy { it.followDate.ifBlank { "9999-99-99" } }
      SortOption.NAME_ASC -> filtered.sortedWith { a, b -> tamilCollator.compare(a.name, b.name) }
    }
  }
}
