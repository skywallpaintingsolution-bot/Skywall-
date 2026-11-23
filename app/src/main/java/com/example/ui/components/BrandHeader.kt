package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BusinessSettings
import com.example.ui.theme.LocalSkywallColors
import com.example.ui.theme.MontserratFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrandHeader(
  settings: BusinessSettings,
  onOpenSettings: () -> Unit,
  onExportCsv: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, skywallColors.cardBorder),
    tonalElevation = 1.dp,
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          modifier = Modifier.size(width = 88.dp, height = 64.dp),
          shape = RoundedCornerShape(12.dp),
          color = Color.White,
          border = BorderStroke(1.dp, skywallColors.cardBorder),
        ) {
          Image(
            painter = painterResource(id = R.drawable.skywall_logo),
            contentDescription = stringResource(R.string.logo_content_desc),
            contentScale = ContentScale.Fit,
            modifier = Modifier.padding(4.dp).clip(RoundedCornerShape(8.dp)),
          )
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          Text(
            text =
              buildAnnotatedString {
                withStyle(SpanStyle(color = skywallColors.brandPrimary)) { append("SKY") }
                withStyle(SpanStyle(color = skywallColors.brandSecondary)) { append("WALL") }
              },
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 26.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.5.sp,
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Box(
              modifier =
                Modifier.weight(1f, fill = false)
                  .width(14.dp)
                  .height(1.5.dp)
                  .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            )
            Text(
              text = stringResource(R.string.brand_sub),
              fontFamily = MontserratFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.sp,
              letterSpacing = 1.4.sp,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Box(
              modifier =
                Modifier.weight(1f, fill = false)
                  .width(14.dp)
                  .height(1.5.dp)
                  .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            )
          }
          Text(
            text = stringResource(R.string.brand_tagline),
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            color = skywallColors.brandSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      HorizontalDivider(color = skywallColors.cardBorder.copy(alpha = 0.65f))

      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = settings.name,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text =
            buildString {
              append(settings.address)
              if (settings.phone.isNotBlank()) {
                append(" · ")
                append(settings.phone)
              }
            },
          style = MaterialTheme.typography.bodySmall,
          color = skywallColors.mutedText,
        )
      }

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(999.dp),
          color = skywallColors.accentSoft,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.CloudOff,
              contentDescription = null,
              modifier = Modifier.size(15.dp),
              tint = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = stringResource(R.string.offline_badge),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        OutlinedButton(
          onClick = onOpenSettings,
          modifier = Modifier.heightIn(min = 48.dp).testTag("settings_button"),
          shape = MaterialTheme.shapes.small,
          border = BorderStroke(1.dp, skywallColors.cardBorder),
        ) {
          Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(text = stringResource(R.string.action_settings))
        }

        FilledTonalButton(
          onClick = onExportCsv,
          modifier = Modifier.heightIn(min = 48.dp).testTag("export_csv_button"),
          shape = MaterialTheme.shapes.small,
        ) {
          Icon(
            imageVector = Icons.Outlined.FileDownload,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(text = stringResource(R.string.action_export_csv))
        }
      }
    }
  }
}
