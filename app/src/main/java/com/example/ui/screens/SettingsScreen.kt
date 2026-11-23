package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BusinessSettings
import com.example.data.model.ThemePreference
import com.example.ui.theme.LocalSkywallColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
  settings: BusinessSettings,
  onBack: () -> Unit,
  onSaveSettings: (BusinessSettings) -> Unit,
  onBackupJson: () -> Unit,
  onRestoreJson: () -> Unit,
  onExportCsv: () -> Unit,
  onWipeAllData: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  val skywallColors = LocalSkywallColors.current
  var name by remember(settings) { mutableStateOf(settings.name) }
  var address by remember(settings) { mutableStateOf(settings.address) }
  var phone by remember(settings) { mutableStateOf(settings.phone) }
  var theme by remember(settings) { mutableStateOf(settings.theme) }
  var themeMenuExpanded by remember { mutableStateOf(false) }
  var showWipeConfirm by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.action_settings),
            style = MaterialTheme.typography.titleLarge,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("settings_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.action_close),
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
          ),
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, skywallColors.cardBorder),
      ) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            shape = MaterialTheme.shapes.small,
          ) {
            Text(stringResource(R.string.action_close))
          }

          Button(
            onClick = {
              onSaveSettings(
                BusinessSettings(
                  name = name.trim().ifEmpty { BusinessSettings.DEFAULT_NAME },
                  address = address.trim().ifEmpty { BusinessSettings.DEFAULT_ADDRESS },
                  phone = phone.trim().ifEmpty { BusinessSettings.DEFAULT_PHONE },
                  theme = theme,
                )
              )
            },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("settings_save_button"),
            shape = MaterialTheme.shapes.small,
          ) {
            Text(stringResource(R.string.action_save))
          }
        }
      }
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 760.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, skywallColors.cardBorder),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("நிறுவனப் பெயர்") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("settings_name_input"),
              )

              OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("முகவரி") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("settings_address_input"),
              )

              OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("தொலைபேசி") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("settings_phone_input"),
              )

              Box {
                Surface(
                  modifier =
                    Modifier.fillMaxWidth()
                      .heightIn(min = 52.dp)
                      .clip(MaterialTheme.shapes.small)
                      .clickable { themeMenuExpanded = true }
                      .testTag("settings_theme_dropdown"),
                  shape = MaterialTheme.shapes.small,
                  color = skywallColors.surfaceElevated,
                  border = BorderStroke(1.dp, skywallColors.cardBorder),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column {
                      Text(
                        text = "தோற்றம்",
                        style = MaterialTheme.typography.labelSmall,
                        color = skywallColors.mutedText,
                      )
                      Text(
                        text = theme.labelTa,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                      )
                    }
                    Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = "தோற்றம்")
                  }
                }
                DropdownMenu(
                  expanded = themeMenuExpanded,
                  onDismissRequest = { themeMenuExpanded = false },
                ) {
                  ThemePreference.entries.forEach { pref ->
                    DropdownMenuItem(
                      text = { Text(pref.labelTa) },
                      onClick = {
                        theme = pref
                        themeMenuExpanded = false
                      },
                    )
                  }
                }
              }
            }
          }
        }

        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, skywallColors.cardBorder),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "காப்புப் பிரதி",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text =
                  "தரவு இந்தத் தொலைபேசியில் மட்டுமே பாதுகாப்பாகச் சேமிக்கப்படுகிறது. அடிக்கடி JSON காப்புப் பிரதி எடுத்துக் கொள்ளவும்.",
                style = MaterialTheme.typography.bodySmall,
                color = skywallColors.mutedText,
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                OutlinedButton(
                  onClick = onBackupJson,
                  modifier = Modifier.heightIn(min = 48.dp).testTag("backup_json_button"),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Backup,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(Modifier.width(6.dp))
                  Text("காப்புப் பிரதி (JSON)")
                }

                OutlinedButton(
                  onClick = onRestoreJson,
                  modifier = Modifier.heightIn(min = 48.dp).testTag("restore_json_button"),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(Modifier.width(6.dp))
                  Text("மீட்டெடு")
                }

                OutlinedButton(
                  onClick = onExportCsv,
                  modifier = Modifier.heightIn(min = 48.dp).testTag("settings_export_csv_button"),
                  shape = MaterialTheme.shapes.small,
                ) {
                  Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(Modifier.width(6.dp))
                  Text(stringResource(R.string.action_export_csv))
                }
              }
            }
          }
        }

        item {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "ஆபத்தான பகுதி",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
              )
              OutlinedButton(
                onClick = { showWipeConfirm = true },
                modifier = Modifier.heightIn(min = 48.dp).testTag("wipe_all_button"),
                shape = MaterialTheme.shapes.small,
                colors =
                  ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
              ) {
                Icon(
                  imageVector = Icons.Outlined.DeleteForever,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("அனைத்து தரவையும் நீக்கு")
              }
            }
          }
        }
      }
    }
  }

  if (showWipeConfirm) {
    AlertDialog(
      onDismissRequest = { showWipeConfirm = false },
      title = { Text("அனைத்து தரவையும் நீக்கவா?") },
      text = { Text("அனைத்து வாடிக்கையாளர் தரவும் நிரந்தரமாக நீக்கப்படும். உறுதியா?") },
      confirmButton = {
        Button(
          onClick = {
            showWipeConfirm = false
            onWipeAllData()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text(stringResource(R.string.action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = { showWipeConfirm = false }) {
          Text(stringResource(R.string.action_cancel))
        }
      },
    )
  }
}
