package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Customer
import com.example.data.model.CustomerStage
import com.example.data.model.SkywallFormatters
import com.example.ui.theme.LocalSkywallColors

@Composable
fun CustomerCard(
  customer: Customer,
  onOpenDetail: () -> Unit,
  onStageChange: (CustomerStage) -> Unit,
  onWhatsAppClick: () -> Unit,
  onCallClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current
  val stageColor = skywallColors.stageColor(customer.stage)
  var stageMenuExpanded by remember { mutableStateOf(false) }

  val followUpDiff =
    if (customer.stage != CustomerStage.DONE && customer.followDate.isNotBlank()) {
      SkywallFormatters.diffDaysFrom(customer.followDate)
    } else {
      null
    }

  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onOpenDetail)
        .testTag("customer_card_${customer.id}"),
    shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, skywallColors.cardBorder),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header row: Name + contact meta on left, Stage pill on right
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
          Text(
            text = customer.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          val metaParts = buildList {
            if (customer.phone.isNotBlank()) add(customer.phone)
            if (customer.place.isNotBlank()) add(customer.place)
            if (customer.visitDate.isNotBlank()) {
              add("தள ஆய்வு ${SkywallFormatters.formatShortDateTa(customer.visitDate)}")
            }
          }
          if (metaParts.isNotEmpty()) {
            Text(
              text = metaParts.joinToString(" · "),
              style = MaterialTheme.typography.bodySmall,
              color = skywallColors.mutedText,
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(999.dp),
          color = stageColor.copy(alpha = 0.14f),
        ) {
          Text(
            text = customer.stage.labelTa,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = stageColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
          )
        }
      }

      // Paint swatches + Job summary
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        val hexes = customer.jobs.map { it.hex }.filter { it.isNotBlank() }.take(4)
        if (hexes.isNotEmpty()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            hexes.forEachIndexed { idx, hex ->
              Box(
                modifier =
                  Modifier.offset(x = (-6 * idx).dp)
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SkywallFormatters.parseHexColor(hex))
                    .border(1.dp, skywallColors.cardBorder, RoundedCornerShape(6.dp))
              )
            }
          }
        }
        Text(
          text = customer.jobSummaryTitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
      }

      // 3-column metrics strip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MetricBox(
          label = "மதிப்பீடு",
          value = SkywallFormatters.formatInr(customer.totalEstimate),
          valueColor = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
        MetricBox(
          label = "பெற்றது",
          value = SkywallFormatters.formatInr(customer.paidAmount),
          valueColor = skywallColors.stageDone,
          modifier = Modifier.weight(1f),
        )
        MetricBox(
          label = "பாக்கி",
          value = SkywallFormatters.formatInr(customer.dueAmount),
          valueColor =
            if (customer.dueAmount > 0.0) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
      }

      // Payment progress bar
      LinearProgressIndicator(
        progress = { customer.paymentProgress },
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
        color = skywallColors.stageDone,
        trackColor = skywallColors.cardBorder,
        strokeCap = StrokeCap.Round,
      )

      // Follow-up badge
      if (followUpDiff != null) {
        val isLate = followUpDiff > 0
        val badgeText =
          when {
            followUpDiff > 0 -> "பின்தொடர்தல் $followUpDiff நாள் தாமதம்"
            followUpDiff == 0 -> "பின்தொடர்தல் இன்று"
            else -> "பின்தொடர்தல் ${SkywallFormatters.formatShortDateTa(customer.followDate)}"
          }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = if (isLate) skywallColors.dangerSoft else skywallColors.surfaceElevated,
        ) {
          Text(
            text = badgeText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isLate) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isLate) MaterialTheme.colorScheme.error else skywallColors.mutedText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      // Footer actions row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(modifier = Modifier.weight(1f)) {
          Surface(
            modifier =
              Modifier.fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { stageMenuExpanded = true }
                .testTag("stage_dropdown_${customer.id}"),
            shape = MaterialTheme.shapes.small,
            color = skywallColors.surfaceElevated,
            border = BorderStroke(1.dp, skywallColors.cardBorder),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = customer.stage.labelTa,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
              )
              Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = "நிலை மாற்று",
                tint = skywallColors.mutedText,
              )
            }
          }

          DropdownMenu(
            expanded = stageMenuExpanded,
            onDismissRequest = { stageMenuExpanded = false },
          ) {
            CustomerStage.entries.forEach { stage ->
              DropdownMenuItem(
                text = { Text(stage.labelTa) },
                onClick = {
                  stageMenuExpanded = false
                  onStageChange(stage)
                },
              )
            }
          }
        }

        IconButton(
          onClick = onCallClick,
          modifier =
            Modifier.size(48.dp)
              .border(1.dp, skywallColors.cardBorder, MaterialTheme.shapes.small)
              .testTag("call_button_${customer.id}"),
        ) {
          Icon(
            imageVector = Icons.Outlined.Call,
            contentDescription = "அழைக்க",
            tint = MaterialTheme.colorScheme.primary,
          )
        }

        OutlinedButton(
          onClick = onWhatsAppClick,
          modifier = Modifier.heightIn(min = 48.dp).testTag("whatsapp_button_${customer.id}"),
          shape = MaterialTheme.shapes.small,
          border = BorderStroke(1.dp, skywallColors.cardBorder),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Outlined.Chat,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = skywallColors.stageDone,
          )
          Spacer(Modifier.width(4.dp))
          Text(
            text = stringResource(R.string.action_whatsapp),
            style = MaterialTheme.typography.labelMedium,
          )
        }

        IconButton(
          onClick = onOpenDetail,
          modifier =
            Modifier.size(48.dp)
              .border(1.dp, skywallColors.cardBorder, MaterialTheme.shapes.small)
              .testTag("detail_button_${customer.id}"),
        ) {
          Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = stringResource(R.string.action_detail),
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Composable
private fun MetricBox(
  label: String,
  value: String,
  valueColor: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.small,
    color = skywallColors.surfaceElevated,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        color = skywallColors.mutedText,
        maxLines = 1,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = valueColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
