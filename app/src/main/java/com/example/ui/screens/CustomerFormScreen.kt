package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Customer
import com.example.data.model.CustomerStage
import com.example.data.model.JobItem
import com.example.data.model.PaintingJobTypes
import com.example.data.model.SkywallFormatters
import com.example.ui.components.PaintColorPickerDialog
import com.example.ui.theme.LocalSkywallColors

private data class JobDraft(
  val key: String,
  val type: String,
  val colour: String,
  val hex: String,
  val areaText: String,
  val rateText: String,
) {
  val area: Double
    get() = areaText.trim().toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

  val rate: Double
    get() = rateText.trim().toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0

  val lineAmount: Double
    get() = area * rate

  fun toDomain(): JobItem =
    JobItem(type = type, colour = colour.trim(), hex = hex, area = area, rate = rate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(
  existingCustomer: Customer?,
  onBack: () -> Unit,
  onSave: (Customer) -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  val skywallColors = LocalSkywallColors.current
  val isNew = existingCustomer == null

  var name by remember(existingCustomer) { mutableStateOf(existingCustomer?.name.orEmpty()) }
  var phone by remember(existingCustomer) { mutableStateOf(existingCustomer?.phone.orEmpty()) }
  var place by remember(existingCustomer) { mutableStateOf(existingCustomer?.place.orEmpty()) }
  var stage by remember(existingCustomer) {
    mutableStateOf(existingCustomer?.stage ?: CustomerStage.ENQUIRY)
  }
  var visitDate by remember(existingCustomer) {
    mutableStateOf(existingCustomer?.visitDate.orEmpty())
  }
  var followDate by remember(existingCustomer) {
    mutableStateOf(
      if (isNew) SkywallFormatters.addDaysYmd(2) else existingCustomer?.followDate.orEmpty()
    )
  }
  var notes by remember(existingCustomer) { mutableStateOf(existingCustomer?.notes.orEmpty()) }
  var discountText by remember(existingCustomer) {
    mutableStateOf(
      existingCustomer?.discount?.takeIf { it > 0.0 }?.let {
        SkywallFormatters.formatNumberClean(it)
      }.orEmpty()
    )
  }

  val jobDrafts =
    remember(existingCustomer) {
      mutableStateListOf<JobDraft>().apply {
        val sourceJobs = existingCustomer?.jobs?.ifEmpty { listOf(JobItem()) } ?: listOf(JobItem())
        sourceJobs.forEachIndexed { idx, j ->
          add(
            JobDraft(
              key = "j_${idx}_${System.nanoTime()}",
              type = j.type,
              colour = j.colour,
              hex = j.hex.ifBlank { "#DDDDDD" },
              areaText = if (j.area > 0.0) SkywallFormatters.formatNumberClean(j.area) else "",
              rateText = if (j.rate > 0.0) SkywallFormatters.formatNumberClean(j.rate) else "",
            )
          )
        }
      }
    }

  var stageMenuExpanded by remember { mutableStateOf(false) }
  var activeDateTarget by remember { mutableStateOf<String?>(null) } // "visit" or "follow"
  var activeColorJobIndex by remember { mutableStateOf<Int?>(null) }
  var showValidationError by remember { mutableStateOf(false) }

  val subtotal = jobDrafts.sumOf { it.lineAmount }
  val discount = discountText.trim().toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
  val finalTotal = (subtotal - discount).coerceAtLeast(0.0)

  val cleanDigits = phone.filter { it.isDigit() }
  val isNameValid = name.trim().isNotEmpty()
  val isPhoneValid = cleanDigits.length in 10..15

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (isNew) "புதிய வாடிக்கையாளர்" else "திருத்து",
            style = MaterialTheme.typography.titleLarge,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("form_back_button"),
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
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("form_cancel_button"),
            shape = MaterialTheme.shapes.small,
          ) {
            Text(stringResource(R.string.action_cancel))
          }

          Button(
            onClick = {
              if (!isNameValid || !isPhoneValid) {
                showValidationError = true
                return@Button
              }
              val saved =
                Customer(
                  id = existingCustomer?.id ?: SkywallFormatters.generateUid(),
                  name = name.trim(),
                  phone = phone.trim(),
                  place = place.trim(),
                  stage = stage,
                  jobs = jobDrafts.map { it.toDomain() },
                  discount = discount,
                  visitDate = visitDate,
                  followDate = followDate,
                  notes = notes.trim(),
                  isSample = existingCustomer?.isSample ?: false,
                  createdAt = existingCustomer?.createdAt ?: System.currentTimeMillis(),
                  payments = existingCustomer?.payments ?: emptyList(),
                )
              onSave(saved)
            },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("form_save_button"),
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
        // Customer basics card
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
                label = { Text("பெயர் *") },
                isError = showValidationError && !isNameValid,
                supportingText =
                  if (showValidationError && !isNameValid) {
                    { Text("பெயரை உள்ளிடவும்") }
                  } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("form_name_input"),
              )

              OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("தொலைபேசி *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = showValidationError && !isPhoneValid,
                supportingText = {
                  Text(
                    if (showValidationError && !isPhoneValid) {
                      "சரியான 10 இலக்க தொலைபேசி எண்ணை உள்ளிடவும்"
                    } else {
                      "10 இலக்க எண்"
                    }
                  )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("form_phone_input"),
              )

              OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("இடம்") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("form_place_input"),
              )

              // Stage selector
              Box {
                Surface(
                  modifier =
                    Modifier.fillMaxWidth()
                      .heightIn(min = 52.dp)
                      .clip(MaterialTheme.shapes.small)
                      .clickable { stageMenuExpanded = true }
                      .testTag("form_stage_dropdown"),
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
                        text = "நிலை",
                        style = MaterialTheme.typography.labelSmall,
                        color = skywallColors.mutedText,
                      )
                      Text(
                        text = stage.labelTa,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                      )
                    }
                    Icon(
                      imageVector = Icons.Outlined.ArrowDropDown,
                      contentDescription = "நிலை",
                    )
                  }
                }
                DropdownMenu(
                  expanded = stageMenuExpanded,
                  onDismissRequest = { stageMenuExpanded = false },
                ) {
                  CustomerStage.entries.forEach { option ->
                    DropdownMenuItem(
                      text = { Text(option.labelTa) },
                      onClick = {
                        stage = option
                        stageMenuExpanded = false
                      },
                    )
                  }
                }
              }

              // Dates row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                DateSelectField(
                  label = "தள ஆய்வு தேதி",
                  ymd = visitDate,
                  onSelectClick = { activeDateTarget = "visit" },
                  onClearClick = { visitDate = "" },
                  modifier = Modifier.weight(1f).testTag("form_visit_date"),
                )
                DateSelectField(
                  label = "பின்தொடர்தல் தேதி",
                  ymd = followDate,
                  onSelectClick = { activeDateTarget = "follow" },
                  onClearClick = { followDate = "" },
                  modifier = Modifier.weight(1f).testTag("form_follow_date"),
                )
              }

              OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("குறிப்பு") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("form_notes_input"),
              )
            }
          }
        }

        // Jobs list card
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
              Text(
                text = "வேலைகள்",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )

              jobDrafts.forEachIndexed { index, job ->
                JobEditorItemCard(
                  job = job,
                  canRemove = jobDrafts.size > 1,
                  onUpdate = { updated -> jobDrafts[index] = updated },
                  onPickColor = { activeColorJobIndex = index },
                  onRemove = {
                    if (jobDrafts.size > 1) {
                      jobDrafts.removeAt(index)
                    }
                  },
                )
              }

              OutlinedButton(
                onClick = {
                  jobDrafts.add(
                    JobDraft(
                      key = "j_${System.nanoTime()}",
                      type = PaintingJobTypes.defaults.first(),
                      colour = "",
                      hex = "#DDDDDD",
                      areaText = "",
                      rateText = "",
                    )
                  )
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("add_job_button"),
                shape = MaterialTheme.shapes.small,
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("வேலை சேர்")
              }

              OutlinedTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = { Text("தள்ளுபடி (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("form_discount_input"),
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text("மொத்தம்", style = MaterialTheme.typography.bodyMedium)
                Text(
                  text = SkywallFormatters.formatInr(subtotal),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "இறுதி மதிப்பீடு",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = SkywallFormatters.formatInr(finalTotal),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
            }
          }
        }
      }
    }
  }

  activeDateTarget?.let { target ->
    val currentYmd = if (target == "visit") visitDate else followDate
    val pickerState =
      rememberDatePickerState(
        initialSelectedDateMillis =
          if (currentYmd.isNotBlank()) SkywallFormatters.ymdToEpochMillis(currentYmd)
          else System.currentTimeMillis()
      )
    DatePickerDialog(
      onDismissRequest = { activeDateTarget = null },
      confirmButton = {
        TextButton(
          onClick = {
            pickerState.selectedDateMillis?.let { millis ->
              val formatted = SkywallFormatters.epochMillisToYmd(millis)
              if (target == "visit") visitDate = formatted else followDate = formatted
            }
            activeDateTarget = null
          }
        ) {
          Text(stringResource(R.string.action_save))
        }
      },
      dismissButton = {
        TextButton(onClick = { activeDateTarget = null }) {
          Text(stringResource(R.string.action_cancel))
        }
      },
    ) {
      DatePicker(state = pickerState)
    }
  }

  activeColorJobIndex?.let { idx ->
    if (idx in jobDrafts.indices) {
      val currentJob = jobDrafts[idx]
      PaintColorPickerDialog(
        initialHex = currentJob.hex,
        onDismiss = { activeColorJobIndex = null },
        onColorSelected = { selectedHex, suggestedName ->
          val updatedColour =
            if (currentJob.colour.isBlank() && suggestedName != null) {
              suggestedName
            } else {
              currentJob.colour
            }
          jobDrafts[idx] = currentJob.copy(hex = selectedHex, colour = updatedColour)
          activeColorJobIndex = null
        },
      )
    }
  }
}

@Composable
private fun DateSelectField(
  label: String,
  ymd: String,
  onSelectClick: () -> Unit,
  onClearClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val skywallColors = LocalSkywallColors.current
  Surface(
    modifier =
      modifier
        .heightIn(min = 56.dp)
        .clip(MaterialTheme.shapes.small)
        .clickable(onClick = onSelectClick),
    shape = MaterialTheme.shapes.small,
    color = skywallColors.surfaceElevated,
    border = BorderStroke(1.dp, skywallColors.cardBorder),
  ) {
    Row(
      modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = skywallColors.mutedText,
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
          Icon(
            imageVector = Icons.Outlined.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = skywallColors.mutedText,
          )
          Text(
            text =
              if (ymd.isNotBlank()) SkywallFormatters.formatShortDateTa(ymd).ifBlank { ymd }
              else "–",
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
      if (ymd.isNotBlank()) {
        IconButton(onClick = onClearClick, modifier = Modifier.size(40.dp)) {
          Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = "தேதியை அழி",
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun JobEditorItemCard(
  job: JobDraft,
  canRemove: Boolean,
  onUpdate: (JobDraft) -> Unit,
  onPickColor: () -> Unit,
  onRemove: () -> Unit,
) {
  val skywallColors = LocalSkywallColors.current
  var typeMenuExpanded by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.small,
    color = skywallColors.surfaceElevated,
    border = BorderStroke(1.dp, skywallColors.cardBorder),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Job type dropdown
      Box {
        Surface(
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 48.dp)
              .clip(MaterialTheme.shapes.small)
              .clickable { typeMenuExpanded = true },
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, skywallColors.cardBorder),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "வேலை வகை",
                style = MaterialTheme.typography.labelSmall,
                color = skywallColors.mutedText,
              )
              Text(
                text = job.type,
                style = MaterialTheme.typography.bodyMedium,
              )
            }
            Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = "வேலை வகை")
          }
        }
        val options =
          if (PaintingJobTypes.defaults.contains(job.type)) PaintingJobTypes.defaults
          else PaintingJobTypes.defaults + job.type
        DropdownMenu(
          expanded = typeMenuExpanded,
          onDismissRequest = { typeMenuExpanded = false },
        ) {
          options.forEach { t ->
            DropdownMenuItem(
              text = { Text(t) },
              onClick = {
                typeMenuExpanded = false
                onUpdate(job.copy(type = t))
              },
            )
          }
        }
      }

      // Color swatch + color name
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier.size(48.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(SkywallFormatters.parseHexColor(job.hex))
              .border(1.5.dp, skywallColors.cardBorder, RoundedCornerShape(10.dp))
              .clickable(onClick = onPickColor),
          contentAlignment = Alignment.Center,
        ) {}

        OutlinedTextField(
          value = job.colour,
          onValueChange = { onUpdate(job.copy(colour = it)) },
          label = { Text("நிறம் (பெயர் / எண்)") },
          placeholder = { Text("எ.கா. ஸ்கை ப்ளூ 2207") },
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
      }

      // Area & Rate
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(
          value = job.areaText,
          onValueChange = { onUpdate(job.copy(areaText = it)) },
          label = { Text("ச.அடி") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
          value = job.rateText,
          onValueChange = { onUpdate(job.copy(rateText = it)) },
          label = { Text("ஒரு ச.அடி விலை (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (canRemove) {
          TextButton(
            onClick = onRemove,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.heightIn(min = 48.dp),
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteOutline,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.action_delete))
          }
        } else {
          Spacer(Modifier.width(1.dp))
        }

        Text(
          text = SkywallFormatters.formatInr(job.lineAmount),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
  }
}
