package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BillDocType
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.CustomerStage
import com.example.data.model.SkywallFormatters
import com.example.data.model.WhatsAppMessageType
import com.example.ui.components.BillPreviewCard
import com.example.ui.theme.LocalSkywallColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomerDetailScreen(
  customer: Customer,
  settings: BusinessSettings,
  snackbarHostState: SnackbarHostState,
  onBack: () -> Unit,
  onEdit: () -> Unit,
  onDeleteCustomer: () -> Unit,
  onStageChange: (CustomerStage) -> Unit,
  onAddPayment: (date: String, amount: Double, note: String) -> Unit,
  onDeletePayment: (paymentId: String) -> Unit,
  onCallPhone: (String) -> Unit,
  onSaveBillImage: (docType: BillDocType, billNumber: String, issueDateYmd: String) -> Unit,
  onShareBillImage: (docType: BillDocType, billNumber: String, issueDateYmd: String) -> Unit,
  onOpenWhatsAppChat: (phone: String, message: String) -> Unit,
  onCopyWhatsApp: (message: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  val skywallColors = LocalSkywallColors.current
  var stageMenuExpanded by remember { mutableStateOf(false) }
  var docTypeMenuExpanded by remember { mutableStateOf(false) }
  var msgTypeMenuExpanded by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf(false) }
  var showPaymentDatePicker by remember { mutableStateOf(false) }

  var payAmountText by remember { mutableStateOf("") }
  var payDate by remember(customer.id) { mutableStateOf(SkywallFormatters.todayYmd()) }
  var payNote by remember { mutableStateOf("") }

  var selectedDocType by remember(customer.id) {
    mutableStateOf(SkywallFormatters.defaultBillDocTypeFor(customer))
  }
  val billNumber = remember(customer.id, customer.createdAt) {
    SkywallFormatters.generateBillNumber(customer)
  }
  val issueDateYmd = remember { SkywallFormatters.todayYmd() }

  var selectedMsgType by remember(customer.id, customer.stage, customer.dueAmount) {
    mutableStateOf(SkywallFormatters.defaultMessageTypeFor(customer))
  }

  val generatedMessage =
    remember(
      customer.id,
      customer.stage,
      customer.jobs,
      customer.discount,
      customer.payments,
      settings,
      selectedMsgType,
    ) {
      SkywallFormatters.buildWhatsAppMessage(customer, settings, selectedMsgType)
    }
  var editableWaMessage by remember(customer.id) { mutableStateOf(generatedMessage) }
  LaunchedEffect(generatedMessage) { editableWaMessage = generatedMessage }

  val isPhoneValidForWhatsApp = SkywallFormatters.isValidWhatsAppPhone(customer.phone)

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = customer.name,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("detail_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.action_close),
            )
          }
        },
        actions = {
          OutlinedButton(
            onClick = onBack,
            modifier = Modifier.padding(end = 8.dp).heightIn(min = 40.dp),
            shape = MaterialTheme.shapes.small,
            border = BorderStroke(1.dp, skywallColors.cardBorder),
          ) {
            Text(stringResource(R.string.action_close))
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
            onClick = onEdit,
            modifier = Modifier.heightIn(min = 48.dp).testTag("detail_edit_button"),
            shape = MaterialTheme.shapes.small,
          ) {
            Icon(
              imageVector = Icons.Outlined.Edit,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.action_edit))
          }

          OutlinedButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("detail_delete_button"),
            shape = MaterialTheme.shapes.small,
            colors =
              ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
          ) {
            Icon(
              imageVector = Icons.Outlined.DeleteOutline,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text("வாடிக்கையாளரை நீக்கு")
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
        // Basic customer info section
        item {
          DetailSectionCard {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "நிலை",
                style = MaterialTheme.typography.bodyMedium,
                color = skywallColors.mutedText,
              )
              Box {
                Surface(
                  modifier =
                    Modifier.heightIn(min = 48.dp)
                      .clip(MaterialTheme.shapes.small)
                      .clickable { stageMenuExpanded = true }
                      .testTag("detail_stage_dropdown"),
                  shape = MaterialTheme.shapes.small,
                  color = skywallColors.surfaceElevated,
                  border = BorderStroke(1.dp, skywallColors.cardBorder),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Text(
                      text = customer.stage.labelTa,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = skywallColors.stageColor(customer.stage),
                    )
                    Icon(
                      imageVector = Icons.Outlined.ArrowDropDown,
                      contentDescription = "நிலை மாற்று",
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
            }

            HorizontalDivider(color = skywallColors.cardBorder.copy(alpha = 0.6f))

            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .heightIn(min = 48.dp)
                  .clip(MaterialTheme.shapes.small)
                  .clickable { onCallPhone(customer.phone) }
                  .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "தொலைபேசி",
                style = MaterialTheme.typography.bodyMedium,
                color = skywallColors.mutedText,
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.Call,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = customer.phone,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
            }

            DetailKeyValueRow("இடம்", customer.place.ifBlank { "–" })
            DetailKeyValueRow(
              "தள ஆய்வு",
              if (customer.visitDate.isNotBlank()) {
                SkywallFormatters.formatShortDateTa(customer.visitDate)
              } else {
                "–"
              },
            )
            DetailKeyValueRow(
              "பின்தொடர்தல்",
              if (customer.followDate.isNotBlank()) {
                SkywallFormatters.formatShortDateTa(customer.followDate)
              } else {
                "–"
              },
            )
            if (customer.notes.isNotBlank()) {
              DetailKeyValueRow("குறிப்பு", customer.notes)
            }
          }
        }

        // Work items & Estimate breakdown
        item {
          DetailSectionCard(title = "வேலை விவரம்") {
            customer.jobs.forEachIndexed { idx, job ->
              Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
              ) {
                if (job.hex.isNotBlank()) {
                  Box(
                    modifier =
                      Modifier.padding(top = 2.dp)
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SkywallFormatters.parseHexColor(job.hex))
                        .border(1.dp, skywallColors.cardBorder, RoundedCornerShape(6.dp))
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = job.type,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  if (job.colour.isNotBlank()) {
                    Text(
                      text = job.colour,
                      style = MaterialTheme.typography.bodySmall,
                      color = skywallColors.mutedText,
                    )
                  }
                  Text(
                    text =
                      "${SkywallFormatters.formatNumberClean(job.area)} ச.அடி × ₹${SkywallFormatters.formatNumberClean(job.rate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = skywallColors.mutedText,
                  )
                }
                Text(
                  text = SkywallFormatters.formatInr(job.lineAmount),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
              if (idx < customer.jobs.lastIndex) {
                HorizontalDivider(color = skywallColors.cardBorder.copy(alpha = 0.5f))
              }
            }

            HorizontalDivider(color = skywallColors.cardBorder)

            SummaryAmountRow("மொத்தம்", SkywallFormatters.formatInr(customer.subtotal))
            if (customer.discount > 0.0) {
              SummaryAmountRow(
                "தள்ளுபடி",
                "− ${SkywallFormatters.formatInr(customer.discount)}",
              )
            }
            SummaryAmountRow(
              "இறுதி மதிப்பீடு",
              SkywallFormatters.formatInr(customer.totalEstimate),
              isBold = true,
            )
            SummaryAmountRow("பெற்றது", SkywallFormatters.formatInr(customer.paidAmount))
            SummaryAmountRow(
              "பாக்கி",
              SkywallFormatters.formatInr(customer.dueAmount),
              isBold = true,
              isError = customer.dueAmount > 0.0,
            )
          }
        }

        // Payments section
        item {
          DetailSectionCard(title = "பெற்ற தொகை") {
            if (customer.payments.isEmpty()) {
              Text(
                text = "இன்னும் தொகை பெறவில்லை",
                style = MaterialTheme.typography.bodyMedium,
                color = skywallColors.mutedText,
              )
            } else {
              customer.payments.forEachIndexed { idx, payment ->
                Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = SkywallFormatters.formatShortDateTa(payment.date),
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (payment.note.isNotBlank()) {
                      Text(
                        text = payment.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = skywallColors.mutedText,
                      )
                    }
                  }
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text = SkywallFormatters.formatInr(payment.amount),
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = skywallColors.stageDone,
                    )
                    IconButton(
                      onClick = { onDeletePayment(payment.id) },
                      modifier = Modifier.size(48.dp).testTag("delete_payment_${payment.id}"),
                    ) {
                      Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "நீக்கு",
                        tint = MaterialTheme.colorScheme.error,
                      )
                    }
                  }
                }
                if (idx < customer.payments.lastIndex) {
                  HorizontalDivider(color = skywallColors.cardBorder.copy(alpha = 0.5f))
                }
              }
            }

            HorizontalDivider(color = skywallColors.cardBorder)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedTextField(
                value = payAmountText,
                onValueChange = { payAmountText = it },
                label = { Text("தொகை (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("payment_amount_input"),
              )

              Surface(
                modifier =
                  Modifier.weight(1f)
                    .heightIn(min = 56.dp)
                    .padding(top = 8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { showPaymentDatePicker = true }
                    .testTag("payment_date_button"),
                shape = MaterialTheme.shapes.small,
                color = skywallColors.surfaceElevated,
                border = BorderStroke(1.dp, skywallColors.cardBorder),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = "தேதி",
                    modifier = Modifier.size(16.dp),
                    tint = skywallColors.mutedText,
                  )
                  Text(
                    text = SkywallFormatters.formatShortDateTa(payDate).ifBlank { payDate },
                    style = MaterialTheme.typography.bodyMedium,
                  )
                }
              }
            }

            OutlinedTextField(
              value = payNote,
              onValueChange = { payNote = it },
              label = { Text("குறிப்பு") },
              placeholder = { Text("முன்பணம் / UPI / பணம்") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("payment_note_input"),
            )

            val parsedAmt = payAmountText.trim().toDoubleOrNull() ?: 0.0
            Button(
              onClick = {
                if (parsedAmt > 0.0) {
                  onAddPayment(payDate, parsedAmt, payNote)
                  payAmountText = ""
                  payNote = ""
                }
              },
              enabled = parsedAmt > 0.0,
              modifier =
                Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("add_payment_button"),
              shape = MaterialTheme.shapes.small,
            ) {
              Text("தொகை சேர்")
            }
          }
        }

        // Branded Bill / Estimate with Logo section
        item {
          DetailSectionCard(title = "பில் / மதிப்பீடு (லோகோவுடன்)") {
            Text(
              text = "ஆவண வகை",
              style = MaterialTheme.typography.bodySmall,
              color = skywallColors.mutedText,
            )

            Box {
              Surface(
                modifier =
                  Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { docTypeMenuExpanded = true }
                    .testTag("bill_doctype_dropdown"),
                shape = MaterialTheme.shapes.small,
                color = skywallColors.surfaceElevated,
                border = BorderStroke(1.dp, skywallColors.cardBorder),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = selectedDocType.dropdownLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "ஆவண வகை",
                  )
                }
              }
              DropdownMenu(
                expanded = docTypeMenuExpanded,
                onDismissRequest = { docTypeMenuExpanded = false },
              ) {
                BillDocType.entries.forEach { docType ->
                  DropdownMenuItem(
                    text = { Text(docType.dropdownLabel) },
                    onClick = {
                      selectedDocType = docType
                      docTypeMenuExpanded = false
                    },
                  )
                }
              }
            }

            BillPreviewCard(
              customer = customer,
              settings = settings,
              docType = selectedDocType,
              billNumber = billNumber,
              issueDateYmd = issueDateYmd,
            )

            Text(
              text = "எண் $billNumber · ${selectedDocType.titleTa}",
              style = MaterialTheme.typography.bodySmall,
              color = skywallColors.mutedText,
            )

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Button(
                onClick = { onSaveBillImage(selectedDocType, billNumber, issueDateYmd) },
                modifier = Modifier.heightIn(min = 48.dp).testTag("save_bill_image_button"),
                shape = MaterialTheme.shapes.small,
              ) {
                Icon(
                  imageVector = Icons.Outlined.Image,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("பில்லை படமாக சேமி")
              }

              OutlinedButton(
                onClick = { onShareBillImage(selectedDocType, billNumber, issueDateYmd) },
                modifier = Modifier.heightIn(min = 48.dp).testTag("share_bill_image_button"),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, skywallColors.cardBorder),
              ) {
                Icon(
                  imageVector = Icons.Outlined.Share,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("படத்தைப் பகிர்")
              }

              OutlinedButton(
                onClick = { onOpenWhatsAppChat(customer.phone, "") },
                enabled = isPhoneValidForWhatsApp,
                modifier = Modifier.heightIn(min = 48.dp).testTag("bill_open_whatsapp_button"),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, skywallColors.cardBorder),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Outlined.Chat,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                  if (isPhoneValidForWhatsApp) "WhatsApp திற"
                  else "WhatsApp எண் சரியில்லை"
                )
              }
            }

            Text(
              text =
                "படிகள்: 1) “பில்லை படமாக சேமி” அழுத்தி படத்தைச் சேமிக்கவும். 2) “WhatsApp திற” அழுத்தி உரையாடல் திறந்ததும் + (இணைப்பு) மூலம் சேமித்த பில் படத்தை இணைத்து அனுப்பவும் (அல்லது “படத்தைப் பகிர்” மூலம் நேரடியாக அனுப்பவும்).",
              style = MaterialTheme.typography.bodySmall,
              color = skywallColors.mutedText,
            )
          }
        }

        // Send to Customer / WhatsApp message section
        item {
          DetailSectionCard(title = "வாடிக்கையாளருக்கு அனுப்பு") {
            Text(
              text = "செய்தி வகை",
              style = MaterialTheme.typography.bodySmall,
              color = skywallColors.mutedText,
            )

            Box {
              Surface(
                modifier =
                  Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(MaterialTheme.shapes.small)
                    .clickable { msgTypeMenuExpanded = true }
                    .testTag("whatsapp_msg_type_dropdown"),
                shape = MaterialTheme.shapes.small,
                color = skywallColors.surfaceElevated,
                border = BorderStroke(1.dp, skywallColors.cardBorder),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = selectedMsgType.labelTa,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Icon(
                    imageVector = Icons.Outlined.ArrowDropDown,
                    contentDescription = "செய்தி வகை",
                  )
                }
              }
              DropdownMenu(
                expanded = msgTypeMenuExpanded,
                onDismissRequest = { msgTypeMenuExpanded = false },
              ) {
                WhatsAppMessageType.entries.forEach { msgType ->
                  DropdownMenuItem(
                    text = { Text(msgType.labelTa) },
                    onClick = {
                      selectedMsgType = msgType
                      msgTypeMenuExpanded = false
                    },
                  )
                }
              }
            }

            OutlinedTextField(
              value = editableWaMessage,
              onValueChange = { editableWaMessage = it },
              modifier = Modifier.fillMaxWidth().testTag("whatsapp_message_input"),
              minLines = 5,
              maxLines = 12,
            )

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Button(
                onClick = { onOpenWhatsAppChat(customer.phone, editableWaMessage) },
                enabled = isPhoneValidForWhatsApp,
                modifier = Modifier.heightIn(min = 48.dp).testTag("whatsapp_send_button"),
                shape = MaterialTheme.shapes.small,
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Outlined.Chat,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                  if (isPhoneValidForWhatsApp) stringResource(R.string.action_whatsapp_send)
                  else "WhatsApp எண் சரியில்லை"
                )
              }

              OutlinedButton(
                onClick = { onCopyWhatsApp(editableWaMessage) },
                modifier = Modifier.heightIn(min = 48.dp).testTag("whatsapp_copy_button"),
                shape = MaterialTheme.shapes.small,
              ) {
                Icon(
                  imageVector = Icons.Outlined.ContentCopy,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.action_copy))
              }
            }
          }
        }
      }
    }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("வாடிக்கையாளரை நீக்கவா?") },
      text = { Text("${customer.name} மற்றும் அனைத்து வேலை/பணம் பதிவுகளும் நீக்கப்படும்.") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirm = false
            onDeleteCustomer()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text(stringResource(R.string.action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text(stringResource(R.string.action_cancel))
        }
      },
    )
  }

  if (showPaymentDatePicker) {
    val datePickerState =
      rememberDatePickerState(
        initialSelectedDateMillis = SkywallFormatters.ymdToEpochMillis(payDate)
      )
    DatePickerDialog(
      onDismissRequest = { showPaymentDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            datePickerState.selectedDateMillis?.let { millis ->
              payDate = SkywallFormatters.epochMillisToYmd(millis)
            }
            showPaymentDatePicker = false
          }
        ) {
          Text(stringResource(R.string.action_save))
        }
      },
      dismissButton = {
        TextButton(onClick = { showPaymentDatePicker = false }) {
          Text(stringResource(R.string.action_cancel))
        }
      },
    ) {
      DatePicker(state = datePickerState)
    }
  }
}

@Composable
private fun DetailSectionCard(
  title: String? = null,
  content: @Composable () -> Unit,
) {
  val skywallColors = LocalSkywallColors.current
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
      if (title != null) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      content()
    }
  }
}

@Composable
private fun DetailKeyValueRow(label: String, value: String) {
  val skywallColors = LocalSkywallColors.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = skywallColors.mutedText,
      modifier = Modifier.padding(end = 16.dp),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun SummaryAmountRow(
  label: String,
  amount: String,
  isBold: Boolean = false,
  isError: Boolean = false,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style =
        if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
      color =
        if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
    )
    Text(
      text = amount,
      style =
        if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
      color =
        if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
    )
  }
}
