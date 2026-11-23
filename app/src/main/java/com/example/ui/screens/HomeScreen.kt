package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Customer
import com.example.data.model.CustomerFilter
import com.example.data.model.CustomerStage
import com.example.data.model.SortOption
import com.example.ui.components.AnalyticsSection
import com.example.ui.components.BrandHeader
import com.example.ui.components.CustomerCard
import com.example.ui.components.SampleDataBanner
import com.example.ui.components.StageDistributionBar
import com.example.ui.components.StatsGrid
import com.example.ui.components.TodayTasksPanel
import com.example.ui.theme.LocalSkywallColors
import com.example.ui.viewmodel.SkywallUiState

@Composable
fun HomeScreen(
  uiState: SkywallUiState,
  snackbarHostState: SnackbarHostState,
  onOpenSettings: () -> Unit,
  onExportCsv: () -> Unit,
  onClearSampleData: () -> Unit,
  onOpenCustomer: (String) -> Unit,
  onFilterChange: (CustomerFilter) -> Unit,
  onSearchChange: (String) -> Unit,
  onSortChange: (SortOption) -> Unit,
  onStageChange: (String, CustomerStage) -> Unit,
  onWhatsAppClick: (Customer) -> Unit,
  onCallClick: (Customer) -> Unit,
  onAddCustomerClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onAddCustomerClick,
        modifier = Modifier.testTag("add_customer_fab"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(999.dp),
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
        text = { Text(text = stringResource(R.string.action_add_customer)) },
      )
    },
  ) { innerPadding ->
    if (uiState.isLoading) {
      Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
      return@Scaffold
    }

    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      BoxWithConstraints(modifier = Modifier.fillMaxSize().widthIn(max = 1100.dp)) {
        val gridColumns =
          when {
            maxWidth >= 920.dp -> 3
            maxWidth >= 620.dp -> 2
            else -> 1
          }
        val customerRows = uiState.filteredCustomers.chunked(gridColumns)

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 104.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          item {
            BrandHeader(
              settings = uiState.settings,
              onOpenSettings = onOpenSettings,
              onExportCsv = onExportCsv,
            )
          }

          if (uiState.dashboard.hasSampleData) {
            item { SampleDataBanner(onClearSample = onClearSampleData) }
          }

          if (uiState.dashboard.tasks.isNotEmpty()) {
            item {
              TodayTasksPanel(
                tasks = uiState.dashboard.tasks,
                onOpenCustomer = onOpenCustomer,
              )
            }
          }

          item { StatsGrid(dashboard = uiState.dashboard) }

          item {
            AnalyticsSection(
              dashboard = uiState.dashboard,
              onOpenCustomer = onOpenCustomer,
            )
          }

          item {
            StageDistributionBar(
              totalCustomers = uiState.dashboard.totalCustomers,
              stageCounts = uiState.dashboard.stageCounts,
            )
          }

          item {
            FilterChipStrip(
              selectedFilter = uiState.selectedFilter,
              totalCount = uiState.dashboard.totalCustomers,
              stageCounts = uiState.dashboard.stageCounts,
              followUpCount = uiState.dashboard.followUpCount,
              onFilterChange = onFilterChange,
            )
          }

          item {
            SearchAndSortBar(
              searchQuery = uiState.searchQuery,
              sortOption = uiState.sortOption,
              onSearchChange = onSearchChange,
              onSortChange = onSortChange,
            )
          }

          if (uiState.filteredCustomers.isEmpty()) {
            item {
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, skywallColors.cardBorder),
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.PeopleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = skywallColors.mutedText,
                  )
                  Text(
                    text = stringResource(R.string.empty_customers_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text =
                      if (uiState.allCustomers.isNotEmpty()) {
                        stringResource(R.string.empty_customers_filter_hint)
                      } else {
                        stringResource(R.string.empty_customers_add_hint)
                      },
                    style = MaterialTheme.typography.bodyMedium,
                    color = skywallColors.mutedText,
                    textAlign = TextAlign.Center,
                  )
                }
              }
            }
          } else {
            items(customerRows.size, key = { idx -> customerRows[idx].first().id }) { rowIndex ->
              val rowItems = customerRows[rowIndex]
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                rowItems.forEach { customer ->
                  CustomerCard(
                    customer = customer,
                    onOpenDetail = { onOpenCustomer(customer.id) },
                    onStageChange = { stage -> onStageChange(customer.id, stage) },
                    onWhatsAppClick = { onWhatsAppClick(customer) },
                    onCallClick = { onCallClick(customer) },
                    modifier = Modifier.weight(1f),
                  )
                }
                if (rowItems.size < gridColumns) {
                  repeat(gridColumns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun FilterChipStrip(
  selectedFilter: CustomerFilter,
  totalCount: Int,
  stageCounts: Map<CustomerStage, Int>,
  followUpCount: Int,
  onFilterChange: (CustomerFilter) -> Unit,
) {
  val skywallColors = LocalSkywallColors.current
  val scrollState = rememberScrollState()

  Row(
    modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CustomerFilter.entries.forEach { filter ->
      val selected = selectedFilter == filter
      val count =
        when (filter) {
          CustomerFilter.ALL -> totalCount
          CustomerFilter.FOLLOWUP -> followUpCount
          else -> stageCounts[filter.toStageOrNull()] ?: 0
        }
      val dotColor = filter.toStageOrNull()?.let { skywallColors.stageColor(it) }

      Surface(
        modifier =
          Modifier.heightIn(min = 48.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable { onFilterChange(filter) }
            .testTag("filter_${filter.key}"),
        shape = RoundedCornerShape(999.dp),
        color =
          if (selected) MaterialTheme.colorScheme.onBackground
          else MaterialTheme.colorScheme.surface,
        border =
          BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.onBackground else skywallColors.cardBorder,
          ),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (dotColor != null) {
            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(dotColor))
          }
          Text(
            text = filter.labelTa,
            style = MaterialTheme.typography.bodySmall,
            color =
              if (selected) MaterialTheme.colorScheme.background
              else MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color =
              if (selected) MaterialTheme.colorScheme.background
              else MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Composable
private fun SearchAndSortBar(
  searchQuery: String,
  sortOption: SortOption,
  onSearchChange: (String) -> Unit,
  onSortChange: (SortOption) -> Unit,
) {
  val skywallColors = LocalSkywallColors.current
  var sortMenuExpanded by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      modifier = Modifier.weight(1f).testTag("search_input"),
      placeholder = {
        Text(
          text = stringResource(R.string.search_placeholder),
          style = MaterialTheme.typography.bodySmall,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Outlined.Search,
          contentDescription = "தேடல்",
          tint = skywallColors.mutedText,
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(
            onClick = { onSearchChange("") },
            modifier = Modifier.size(48.dp),
          ) {
            Icon(imageVector = Icons.Outlined.Close, contentDescription = "தேடலை அழி")
          }
        }
      },
      singleLine = true,
      shape = MaterialTheme.shapes.small,
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedBorderColor = skywallColors.cardBorder,
        ),
    )

    Box {
      Surface(
        modifier =
          Modifier.height(54.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable { sortMenuExpanded = true }
            .testTag("sort_dropdown"),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, skywallColors.cardBorder),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = sortOption.labelTa,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
          )
          Icon(
            imageVector = Icons.Outlined.ArrowDropDown,
            contentDescription = "வரிசை",
            tint = skywallColors.mutedText,
          )
        }
      }

      DropdownMenu(
        expanded = sortMenuExpanded,
        onDismissRequest = { sortMenuExpanded = false },
      ) {
        SortOption.entries.forEach { option ->
          DropdownMenuItem(
            text = { Text(option.labelTa) },
            onClick = {
              sortMenuExpanded = false
              onSortChange(option)
            },
          )
        }
      }
    }
  }
}
