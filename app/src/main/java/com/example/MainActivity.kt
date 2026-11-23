package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.SkywallFormatters
import com.example.ui.components.BillImageExporter
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomerFormScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SkywallTheme
import com.example.ui.viewmodel.ActiveScreen
import com.example.ui.viewmodel.SkywallViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { SkywallApp() }
  }
}

@Composable
fun SkywallApp(viewModel: SkywallViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
  var pendingBillBitmap by remember { mutableStateOf<Bitmap?>(null) }

  LaunchedEffect(Unit) {
    viewModel.messages.collect { msg -> snackbarHostState.showSnackbar(msg) }
  }

  val jsonBackupLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
      uri ->
      if (uri != null) {
        val ok = writeTextToUri(context, uri, viewModel.buildJsonBackup())
        viewModel.notifyMessage(
          if (ok) "காப்புப் பிரதி சேமிக்கப்பட்டது" else "சேமிக்க முடியவில்லை"
        )
      }
    }

  val csvExportLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
      if (uri != null) {
        val ok = writeTextToUri(context, uri, viewModel.buildCsvExport())
        viewModel.notifyMessage(if (ok) "CSV சேமிக்கப்பட்டது" else "சேமிக்க முடியவில்லை")
      }
    }

  val billPngSaveLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
      val bmp = pendingBillBitmap
      pendingBillBitmap = null
      if (uri != null && bmp != null) {
        val ok = BillImageExporter.writeBitmapToUri(context, uri, bmp)
        viewModel.notifyMessage(if (ok) "பில் படம் சேமிக்கப்பட்டது" else "சேமிக்க முடியவில்லை")
      }
    }

  val restoreJsonLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        val text = readTextFromUri(context, uri)
        if (text != null) {
          pendingRestoreJson = text
        } else {
          viewModel.notifyMessage("கோப்பு சரியில்லை")
        }
      }
    }

  SkywallTheme(themePreference = uiState.settings.theme) {
    Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
      when (val screen = uiState.activeScreen) {
        is ActiveScreen.Dashboard -> {
          HomeScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onOpenSettings = { viewModel.openSettings() },
            onExportCsv = { csvExportLauncher.launch("skywall-customers.csv") },
            onClearSampleData = { viewModel.clearSampleData() },
            onOpenCustomer = { id -> viewModel.openCustomerDetail(id) },
            onFilterChange = { viewModel.setFilter(it) },
            onSearchChange = { viewModel.setSearchQuery(it) },
            onSortChange = { viewModel.setSortOption(it) },
            onStageChange = { id, stage -> viewModel.updateCustomerStage(id, stage) },
            onWhatsAppClick = { customer ->
              if (!SkywallFormatters.isValidWhatsAppPhone(customer.phone)) {
                viewModel.notifyMessage("WhatsApp எண் சரியில்லை")
              } else {
                val msg = SkywallFormatters.buildWhatsAppMessage(customer, uiState.settings)
                openWhatsApp(context, customer.phone, msg) {
                  viewModel.notifyMessage("WhatsApp திறக்க முடியவில்லை")
                }
              }
            },
            onCallClick = { customer -> openDialer(context, customer.phone) },
            onAddCustomerClick = { viewModel.openNewCustomerForm() },
          )
        }

        is ActiveScreen.Detail -> {
          val customer = uiState.selectedDetailCustomer
          if (customer == null) {
            LaunchedEffect(screen.customerId) { viewModel.openDashboard() }
          } else {
            CustomerDetailScreen(
              customer = customer,
              settings = uiState.settings,
              snackbarHostState = snackbarHostState,
              onBack = { viewModel.openDashboard() },
              onEdit = { viewModel.openEditCustomerForm(customer) },
              onDeleteCustomer = { viewModel.deleteCustomer(customer.id) },
              onStageChange = { stage -> viewModel.updateCustomerStage(customer.id, stage) },
              onAddPayment = { date, amount, note ->
                viewModel.addPayment(customer.id, date, amount, note)
              },
              onDeletePayment = { paymentId -> viewModel.deletePayment(paymentId) },
              onCallPhone = { phone -> openDialer(context, phone) },
              onSaveBillImage = { docType, billNumber, issueDateYmd ->
                val bmp =
                  BillImageExporter.renderBillBitmap(
                    context = context,
                    customer = customer,
                    settings = uiState.settings,
                    docType = docType,
                    billNumber = billNumber,
                    issueDateYmd = issueDateYmd,
                  )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                  val savedUri = BillImageExporter.saveBillToMediaStore(context, bmp, billNumber)
                  if (savedUri != null) {
                    viewModel.notifyMessage("பில் படம் Pictures/Skywall-ல் சேமிக்கப்பட்டது")
                  } else {
                    pendingBillBitmap = bmp
                    billPngSaveLauncher.launch("$billNumber.png")
                  }
                } else {
                  pendingBillBitmap = bmp
                  billPngSaveLauncher.launch("$billNumber.png")
                }
              },
              onShareBillImage = { docType, billNumber, issueDateYmd ->
                val bmp =
                  BillImageExporter.renderBillBitmap(
                    context = context,
                    customer = customer,
                    settings = uiState.settings,
                    docType = docType,
                    billNumber = billNumber,
                    issueDateYmd = issueDateYmd,
                  )
                val shared = BillImageExporter.shareBillBitmap(context, bmp, billNumber)
                if (!shared) {
                  viewModel.notifyMessage("படத்தைப் பகிர முடியவில்லை")
                }
              },
              onOpenWhatsAppChat = { phone, msg ->
                if (!SkywallFormatters.isValidWhatsAppPhone(phone)) {
                  viewModel.notifyMessage("WhatsApp எண் சரியில்லை")
                } else {
                  openWhatsApp(context, phone, msg) {
                    viewModel.notifyMessage("WhatsApp திறக்க முடியவில்லை")
                  }
                }
              },
              onCopyWhatsApp = { msg ->
                copyToClipboard(context, msg)
                viewModel.notifyMessage("நகலெடுக்கப்பட்டது")
              },
            )
          }
        }

        is ActiveScreen.Form -> {
          CustomerFormScreen(
            existingCustomer = screen.customer,
            onBack = {
              val existing = screen.customer
              if (existing != null) viewModel.openCustomerDetail(existing.id)
              else viewModel.openDashboard()
            },
            onSave = { saved -> viewModel.saveCustomer(saved, navigateToDetailAfterSave = true) },
          )
        }

        is ActiveScreen.Settings -> {
          SettingsScreen(
            settings = uiState.settings,
            onBack = { viewModel.openDashboard() },
            onSaveSettings = { viewModel.saveSettings(it) },
            onBackupJson = {
              jsonBackupLauncher.launch("skywall-backup-${SkywallFormatters.todayYmd()}.json")
            },
            onRestoreJson = {
              restoreJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            onExportCsv = { csvExportLauncher.launch("skywall-customers.csv") },
            onWipeAllData = { viewModel.wipeAllData() },
          )
        }
      }
    }

    pendingRestoreJson?.let { rawJson ->
      AlertDialog(
        onDismissRequest = { pendingRestoreJson = null },
        title = { Text("மீட்டெடுக்கவா?") },
        text = { Text("தற்போதைய தரவு மாற்றப்படும். தொடரவா?") },
        confirmButton = {
          Button(
            onClick = {
              pendingRestoreJson = null
              viewModel.restoreBackupJson(rawJson)
            }
          ) {
            Text("தொடர்")
          }
        },
        dismissButton = {
          TextButton(onClick = { pendingRestoreJson = null }) { Text("ரத்து") }
        },
      )
    }
  }
}

private fun writeTextToUri(context: Context, uri: Uri, content: String): Boolean {
  return try {
    context.contentResolver.openOutputStream(uri)?.use { out ->
      out.write(content.toByteArray(Charsets.UTF_8))
      out.flush()
    }
    true
  } catch (_: Exception) {
    false
  }
}

private fun readTextFromUri(context: Context, uri: Uri): String? {
  return try {
    context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
      it.readText()
    }
  } catch (_: Exception) {
    null
  }
}

private fun openDialer(context: Context, phone: String) {
  val clean = phone.trim()
  if (clean.isEmpty()) return
  val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
  runCatching { context.startActivity(intent) }
}

private fun openWhatsApp(
  context: Context,
  phone: String,
  message: String,
  onError: () -> Unit,
) {
  val uri = SkywallFormatters.buildWhatsAppUri(phone, message)
  val intent = Intent(Intent.ACTION_VIEW, uri)
  try {
    context.startActivity(intent)
  } catch (_: Exception) {
    onError()
  }
}

private fun copyToClipboard(context: Context, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  clipboard.setPrimaryClip(ClipData.newPlainText("Skywall WhatsApp", text))
}
