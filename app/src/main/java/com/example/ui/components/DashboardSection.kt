package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Customer
import com.example.data.model.CustomerStage
import com.example.data.model.DashboardSummary
import com.example.data.model.MonthCollection
import com.example.data.model.SkywallFormatters
import com.example.data.model.TodayTask
import com.example.ui.theme.LocalSkywallColors

@Composable
fun SampleDataBanner(
  onClearSample: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = skywallColors.accentSoft,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp),
      )
      Text(
        text = stringResource(R.string.sample_banner_text),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      OutlinedButton(
        onClick = onClearSample,
        modifier = Modifier.heightIn(min = 48.dp).testTag("clear_sample_button"),
        shape = MaterialTheme.shapes.small,
      ) {
        Icon(
          imageVector = Icons.Outlined.DeleteSweep,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
          text = stringResource(R.string.sample_banner_clear),
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayTasksPanel(
  tasks: List<TodayTask>,
  onOpenCustomer: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (tasks.isEmpty()) return
  val skywallColors = LocalSkywallColors.current

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, skywallColors.cardBorder),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Outlined.EventAvailable,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = "இன்றைய பணிகள் (${tasks.size})",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        tasks.forEach { task ->
          val containerColor =
            if (task.isLate) skywallColors.dangerSoft else skywallColors.surfaceElevated
          val borderColor =
            if (task.isLate) MaterialTheme.colorScheme.error else skywallColors.cardBorder

          Surface(
            modifier =
              Modifier.heightIn(min = 48.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { onOpenCustomer(task.customer.id) }
                .testTag("today_task_${task.customer.id}"),
            shape = MaterialTheme.shapes.small,
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              if (task.isLate) {
                Icon(
                  imageVector = Icons.Outlined.WarningAmber,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(16.dp),
                )
              }
              Text(
                text = task.customer.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = task.label,
                style = MaterialTheme.typography.bodySmall,
                color =
                  if (task.isLate) MaterialTheme.colorScheme.error
                  else skywallColors.mutedText,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatsGrid(
  dashboard: DashboardSummary,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current
  val items =
    listOf(
      StatItem("மொத்த வாடிக்கையாளர்கள்", dashboard.totalCustomers.toString(), false),
      StatItem("புதிய விசாரணை / மதிப்பீடு", dashboard.newCount.toString(), false),
      StatItem("நடப்பு வேலை மதிப்பு", SkywallFormatters.formatInr(dashboard.activeValue), false),
      StatItem("இந்த மாதம் பெற்றது", SkywallFormatters.formatInr(dashboard.monthCollected), false),
      StatItem("மொத்த பாக்கி", SkywallFormatters.formatInr(dashboard.totalDue), true),
    )

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val columns =
      when {
        maxWidth >= 780.dp -> 5
        maxWidth >= 480.dp -> 3
        else -> 2
      }
    val rows = items.chunked(columns)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      rows.forEach { rowItems ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          rowItems.forEach { stat ->
            Surface(
              modifier = Modifier.weight(1f),
              shape = MaterialTheme.shapes.medium,
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, skywallColors.cardBorder),
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = stat.label,
                  style = MaterialTheme.typography.bodySmall,
                  color = skywallColors.mutedText,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = stat.value,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  color =
                    if (stat.isHighlight && dashboard.totalDue > 0.0) {
                      MaterialTheme.colorScheme.error
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    },
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }
          if (rowItems.size < columns) {
            repeat(columns - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
          }
        }
      }
    }
  }
}

private data class StatItem(
  val label: String,
  val value: String,
  val isHighlight: Boolean,
)

@Composable
fun AnalyticsSection(
  dashboard: DashboardSummary,
  onOpenCustomer: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val twoColumns = maxWidth >= 680.dp
    if (twoColumns) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
      ) {
        CollectionsChartCard(
          collections = dashboard.monthCollections,
          modifier = Modifier.weight(1.15f),
        )
        DuesListCard(
          dueCustomers = dashboard.dueCustomers,
          onOpenCustomer = onOpenCustomer,
          modifier = Modifier.weight(1f),
        )
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CollectionsChartCard(collections = dashboard.monthCollections)
        DuesListCard(
          dueCustomers = dashboard.dueCustomers,
          onOpenCustomer = onOpenCustomer,
        )
      }
    }
  }
}

@Composable
private fun CollectionsChartCard(
  collections: List<MonthCollection>,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current
  val primaryColor = MaterialTheme.colorScheme.primary
  val lineColor = skywallColors.cardBorder
  val maxAmount = (collections.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(1.0)
  val chartDescription =
    "மாத வாரியாகப் பெற்ற தொகை: " +
      collections.joinToString(", ") {
        "${it.fullLabel} ${SkywallFormatters.formatInr(it.amount)}"
      }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, skywallColors.cardBorder),
  ) {
    Column(
      modifier = Modifier.padding(16.dp).semantics { contentDescription = chartDescription },
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "கடந்த 6 மாத வசூல்",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        collections.forEach { slot ->
          Text(
            text = SkywallFormatters.formatCompactInr(slot.amount),
            style = MaterialTheme.typography.labelSmall,
            color =
              if (slot.isCurrentMonth && slot.amount > 0.0) primaryColor
              else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )
        }
      }

      Canvas(modifier = Modifier.fillMaxWidth().height(92.dp)) {
        val count = collections.size.coerceAtLeast(1)
        val slotWidth = size.width / count
        val barWidth = (slotWidth * 0.56f).coerceAtMost(42.dp.toPx())
        val radius = 4.dp.toPx()

        drawLine(
          color = lineColor,
          start = Offset(0f, size.height),
          end = Offset(size.width, size.height),
          strokeWidth = 1.dp.toPx(),
        )

        collections.forEachIndexed { index, slot ->
          val ratio = (slot.amount / maxAmount).toFloat().coerceIn(0f, 1f)
          val minHeight = 2.dp.toPx()
          val barHeight = (size.height * ratio).coerceAtLeast(minHeight)
          val left = index * slotWidth + (slotWidth - barWidth) / 2f
          val top = size.height - barHeight
          val barColor = if (slot.isCurrentMonth) primaryColor else primaryColor.copy(alpha = 0.58f)

          drawRoundRect(
            color = barColor,
            topLeft = Offset(left, top),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(radius, radius),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        collections.forEach { slot ->
          Text(
            text = slot.shortLabel,
            style = MaterialTheme.typography.labelSmall,
            color = skywallColors.mutedText,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
          )
        }
      }
    }
  }
}

@Composable
private fun DuesListCard(
  dueCustomers: List<Customer>,
  onOpenCustomer: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, skywallColors.cardBorder),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Outlined.AccountBalanceWallet,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(18.dp),
        )
        Text(
          text = "பாக்கி வசூலிக்க வேண்டியவர்கள்",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      if (dueCustomers.isEmpty()) {
        Text(
          text = "பாக்கி எதுவும் இல்லை",
          style = MaterialTheme.typography.bodyMedium,
          color = skywallColors.mutedText,
          modifier = Modifier.padding(vertical = 8.dp),
        )
      } else {
        dueCustomers.forEachIndexed { index, customer ->
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { onOpenCustomer(customer.id) }
                .padding(vertical = 8.dp, horizontal = 4.dp)
                .testTag("due_item_${customer.id}"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = customer.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = customer.stage.labelTa,
                style = MaterialTheme.typography.bodySmall,
                color = skywallColors.mutedText,
              )
            }
            Spacer(Modifier.width(10.dp))
            Text(
              text = SkywallFormatters.formatInr(customer.dueAmount),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.error,
            )
          }
          if (index < dueCustomers.lastIndex) {
            HorizontalDivider(color = skywallColors.cardBorder.copy(alpha = 0.6f))
          }
        }
      }
    }
  }
}

@Composable
fun StageDistributionBar(
  totalCustomers: Int,
  stageCounts: Map<CustomerStage, Int>,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current
  val denom = totalCustomers.coerceAtLeast(1).toFloat()

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(10.dp)
        .clip(CircleShape)
        .background(skywallColors.cardBorder)
        .semantics { contentDescription = "நிலை வாரியான வாடிக்கையாளர்கள்" },
  ) {
    CustomerStage.entries.forEach { stage ->
      val count = stageCounts[stage] ?: 0
      if (count > 0) {
        Box(
          modifier =
            Modifier.weight(count / denom)
              .height(10.dp)
              .background(skywallColors.stageColor(stage))
        )
      }
    }
  }
}
