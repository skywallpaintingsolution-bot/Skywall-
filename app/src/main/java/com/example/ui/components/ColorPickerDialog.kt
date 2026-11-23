package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.PaintSwatchPresets
import com.example.data.model.SkywallFormatters
import com.example.ui.theme.LocalSkywallColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaintColorPickerDialog(
  initialHex: String,
  onDismiss: () -> Unit,
  onColorSelected: (hex: String, suggestedName: String?) -> Unit,
) {
  val skywallColors = LocalSkywallColors.current
  val initialColor = SkywallFormatters.parseHexColor(initialHex)
  val initialHsv = remember(initialHex) {
    FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
  }

  var hue by remember { mutableFloatStateOf(initialHsv[0]) }
  var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
  var brightness by remember { mutableFloatStateOf(initialHsv[2]) }
  var suggestedName by remember { mutableStateOf<String?>(null) }

  val currentColor = Color.hsv(hue, saturation, brightness)
  val currentHex = SkywallFormatters.colorToHex(currentColor)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "நிறத்தைத் தேர்ந்தெடுக்கவும்",
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Preview banner
        Surface(
          modifier = Modifier.fillMaxWidth().height(54.dp),
          shape = MaterialTheme.shapes.medium,
          color = currentColor,
          border = BorderStroke(1.dp, skywallColors.cardBorder),
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart,
          ) {
            Text(
              text =
                if (suggestedName != null) "$suggestedName ($currentHex)" else currentHex,
              style = MaterialTheme.typography.titleSmall,
              color = if (currentColor.luminance() > 0.5f) Color.Black else Color.White,
            )
          }
        }

        Text(
          text = "பிரபல நிறங்கள்",
          style = MaterialTheme.typography.labelMedium,
          color = skywallColors.mutedText,
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          PaintSwatchPresets.presets.forEach { preset ->
            val presetColor = SkywallFormatters.parseHexColor(preset.hex)
            val isSelected = currentHex.equals(preset.hex, ignoreCase = true)
            Box(
              modifier =
                Modifier.size(44.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(presetColor)
                  .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color =
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else skywallColors.cardBorder,
                    shape = RoundedCornerShape(10.dp),
                  )
                  .clickable {
                    val hsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(presetColor.toArgb(), hsv)
                    hue = hsv[0]
                    saturation = hsv[1]
                    brightness = hsv[2]
                    suggestedName = preset.nameTa
                  },
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = preset.nameTa,
                  tint = if (presetColor.luminance() > 0.5f) Color.Black else Color.White,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text("நிற வகை", style = MaterialTheme.typography.labelSmall)
            Text("${hue.toInt()}°", style = MaterialTheme.typography.labelSmall)
          }
          Slider(
            value = hue,
            onValueChange = {
              hue = it
              suggestedName = null
            },
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(thumbColor = currentColor),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text("நிற அடர்த்தி", style = MaterialTheme.typography.labelSmall)
            Text("${(saturation * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
          }
          Slider(
            value = saturation,
            onValueChange = {
              saturation = it
              suggestedName = null
            },
            valueRange = 0f..1f,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text("வெளிச்சம்", style = MaterialTheme.typography.labelSmall)
            Text("${(brightness * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
          }
          Slider(
            value = brightness,
            onValueChange = {
              brightness = it
              suggestedName = null
            },
            valueRange = 0.1f..1f,
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = { onColorSelected(currentHex, suggestedName) }) {
        Text(stringResource(R.string.action_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.action_cancel))
      }
    },
  )
}
