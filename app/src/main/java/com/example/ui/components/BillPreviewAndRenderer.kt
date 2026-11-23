package com.example.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.BillDocType
import com.example.data.model.BusinessSettings
import com.example.data.model.Customer
import com.example.data.model.SkywallFormatters
import com.example.ui.theme.MontserratFamily
import com.example.ui.theme.MuktaMalarFamily
import com.example.ui.theme.NotoSansTamilFamily
import java.io.File
import java.io.FileOutputStream

private val PaperWhite = Color(0xFFFFFFFF)
private val BillNavy = Color(0xFF0B2160)
private val BillSky = Color(0xFF0A7BE8)
private val BillInk = Color(0xFF13232B)
private val BillMuted = Color(0xFF586B73)
private val BillInfoBg = Color(0xFFF3F6FA)
private val BillLine = Color(0xFFE1E8EE)
private val BillDanger = Color(0xFFB3261E)

@Composable
fun BillPreviewCard(
  customer: Customer,
  settings: BusinessSettings,
  docType: BillDocType,
  billNumber: String,
  issueDateYmd: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    color = PaperWhite,
    border = BorderStroke(1.dp, Color(0xFFD6E0E2)),
    shadowElevation = 2.dp,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top sky-blue accent bar
      Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(BillSky))

      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Centered Skywall Logo + Brand Lockup
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Image(
            painter = painterResource(id = R.drawable.skywall_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 64.dp, height = 38.dp),
          )
          Text(
            text =
              buildAnnotatedString {
                withStyle(SpanStyle(color = BillNavy)) { append("SKY") }
                withStyle(SpanStyle(color = BillSky)) { append("WALL") }
              },
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = 0.5.sp,
          )
          Text(
            text = "— PAINTING SOLUTIONS —",
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 8.5.sp,
            letterSpacing = 1.6.sp,
            color = BillInk,
          )
          Text(
            text = "— WE PAINT YOUR DREAMS —",
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 7.5.sp,
            letterSpacing = 1.6.sp,
            color = BillSky,
          )
        }

        // Business Name & Address
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = settings.name,
            fontFamily = MuktaMalarFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp,
            color = BillInk,
          )
          Text(
            text =
              buildString {
                append(settings.address)
                if (settings.phone.isNotBlank()) append(" · ${settings.phone}")
              },
            fontFamily = NotoSansTamilFamily,
            fontSize = 9.5.sp,
            lineHeight = 13.sp,
            color = BillMuted,
          )
        }

        HorizontalDivider(color = BillLine)

        // Document Title & Number/Date
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top,
        ) {
          Column {
            Text(
              text = docType.titleTa,
              fontFamily = MuktaMalarFamily,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 17.sp,
              color = BillNavy,
            )
            Text(
              text = docType.titleEn,
              fontFamily = MontserratFamily,
              fontWeight = FontWeight.SemiBold,
              fontSize = 8.5.sp,
              letterSpacing = 1.5.sp,
              color = BillSky,
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "எண்: $billNumber",
              fontFamily = NotoSansTamilFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = BillInk,
            )
            Text(
              text = "தேதி: ${SkywallFormatters.formatFullDateTa(issueDateYmd)}",
              fontFamily = NotoSansTamilFamily,
              fontSize = 9.5.sp,
              color = BillMuted,
            )
          }
        }

        // Customer & Site Info Box
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(8.dp),
          color = BillInfoBg,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "வாடிக்கையாளர்",
                fontFamily = NotoSansTamilFamily,
                fontSize = 8.5.sp,
                color = BillMuted,
              )
              Text(
                text = customer.name,
                fontFamily = MuktaMalarFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = BillInk,
              )
              if (customer.place.isNotBlank()) {
                Text(
                  text = "இடம்: ${customer.place}",
                  fontFamily = NotoSansTamilFamily,
                  fontSize = 9.5.sp,
                  color = BillMuted,
                )
              }
              if (customer.phone.isNotBlank()) {
                Text(
                  text = customer.phone,
                  fontFamily = NotoSansTamilFamily,
                  fontSize = 9.5.sp,
                  color = BillMuted,
                )
              }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Column {
                Text(
                  text = "தள ஆய்வு தேதி",
                  fontFamily = NotoSansTamilFamily,
                  fontSize = 8.5.sp,
                  color = BillMuted,
                )
                Text(
                  text =
                    if (customer.visitDate.isNotBlank()) {
                      SkywallFormatters.formatFullDateTa(customer.visitDate)
                    } else "–",
                  fontFamily = NotoSansTamilFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 10.sp,
                  color = BillInk,
                )
              }
              Column {
                Text(
                  text = "நிலை",
                  fontFamily = NotoSansTamilFamily,
                  fontSize = 8.5.sp,
                  color = BillMuted,
                )
                Text(
                  text = customer.stage.labelTa,
                  fontFamily = NotoSansTamilFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = BillInk,
                )
              }
            }
          }
        }

        // Work Table Header
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(4.dp))
              .background(BillNavy)
              .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("#", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PaperWhite, modifier = Modifier.width(18.dp))
          Text("வேலை விவரம்", fontFamily = NotoSansTamilFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PaperWhite, modifier = Modifier.weight(1.9f))
          Text("அளவு", fontFamily = NotoSansTamilFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PaperWhite, modifier = Modifier.weight(0.95f), textAlign = TextAlign.End)
          Text("ரேட்", fontFamily = NotoSansTamilFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PaperWhite, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
          Text("தொகை", fontFamily = NotoSansTamilFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PaperWhite, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }

        // Work Table Rows
        customer.jobs.forEachIndexed { idx, job ->
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("${idx + 1}", fontSize = 9.5.sp, color = BillMuted, modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1.9f)) {
              Text(
                text = job.type,
                fontFamily = NotoSansTamilFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 9.5.sp,
                color = BillInk,
              )
              if (job.colour.isNotBlank()) {
                Text(
                  text = job.colour,
                  fontFamily = NotoSansTamilFamily,
                  fontSize = 8.5.sp,
                  color = BillMuted,
                )
              }
            }
            Text(
              text = "${SkywallFormatters.formatNumberClean(job.area)} ச.அடி",
              fontFamily = NotoSansTamilFamily,
              fontSize = 9.sp,
              color = BillInk,
              modifier = Modifier.weight(0.95f),
              textAlign = TextAlign.End,
            )
            Text(
              text = "₹${SkywallFormatters.formatNumberClean(job.rate)}",
              fontFamily = NotoSansTamilFamily,
              fontSize = 9.sp,
              color = BillInk,
              modifier = Modifier.weight(0.8f),
              textAlign = TextAlign.End,
            )
            Text(
              text = SkywallFormatters.formatInr(job.lineAmount),
              fontFamily = NotoSansTamilFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 9.5.sp,
              color = BillInk,
              modifier = Modifier.weight(1f),
              textAlign = TextAlign.End,
            )
          }
          HorizontalDivider(color = BillLine)
        }

        // Totals Right-Aligned Block
        Row(modifier = Modifier.fillMaxWidth()) {
          Spacer(modifier = Modifier.weight(0.45f))
          Column(
            modifier = Modifier.weight(0.55f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            BillTotalRow("மொத்தம்", SkywallFormatters.formatInr(customer.subtotal), BillMuted, BillInk)
            if (customer.discount > 0.0) {
              BillTotalRow(
                "தள்ளுபடி",
                "− ${SkywallFormatters.formatInr(customer.discount)}",
                BillMuted,
                BillInk,
              )
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = BillNavy,
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = if (docType == BillDocType.INVOICE) "இறுதி தொகை" else "இறுதி மதிப்பீடு",
                  fontFamily = MuktaMalarFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.5.sp,
                  color = PaperWhite,
                )
                Text(
                  text = SkywallFormatters.formatInr(customer.totalEstimate),
                  fontFamily = MuktaMalarFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp,
                  color = PaperWhite,
                )
              }
            }
            BillTotalRow("பெற்றது", SkywallFormatters.formatInr(customer.paidAmount), BillMuted, BillInk)
            BillTotalRow(
              "பாக்கி",
              SkywallFormatters.formatInr(customer.dueAmount),
              BillDanger,
              BillDanger,
              bold = true,
            )
          }
        }

        // Payments History
        if (customer.payments.isNotEmpty()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "பணம் பெற்ற பதிவுகள்",
            fontFamily = MuktaMalarFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = BillNavy,
          )
          customer.payments.forEach { payment ->
            HorizontalDivider(color = BillLine)
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = SkywallFormatters.formatFullDateTa(payment.date),
                fontFamily = NotoSansTamilFamily,
                fontSize = 9.sp,
                color = BillInk,
                modifier = Modifier.weight(1.1f),
              )
              Text(
                text = payment.note,
                fontFamily = NotoSansTamilFamily,
                fontSize = 9.sp,
                color = BillMuted,
                modifier = Modifier.weight(1f),
              )
              Text(
                text = SkywallFormatters.formatInr(payment.amount),
                fontFamily = NotoSansTamilFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = BillInk,
                textAlign = TextAlign.End,
              )
            }
          }
        }

        // Thank You & Tagline
        Column(
          modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Text(
            text = "நன்றி!",
            fontFamily = MuktaMalarFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = BillNavy,
          )
          Text(
            text = "———  WE PAINT YOUR DREAMS  ———",
            fontFamily = MontserratFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 8.sp,
            letterSpacing = 1.4.sp,
            color = BillSky,
          )
        }
      }

      // Dark Navy Bottom Address Bar
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .background(BillNavy)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text =
            buildString {
              append(settings.address)
              if (settings.phone.isNotBlank()) append(" · ${settings.phone}")
            },
          fontFamily = NotoSansTamilFamily,
          fontSize = 8.5.sp,
          lineHeight = 12.sp,
          color = PaperWhite,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
private fun BillTotalRow(
  label: String,
  amount: String,
  labelColor: Color,
  amountColor: Color,
  bold: Boolean = false,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      fontFamily = NotoSansTamilFamily,
      fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
      fontSize = 10.sp,
      color = labelColor,
    )
    Text(
      text = amount,
      fontFamily = NotoSansTamilFamily,
      fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
      fontSize = 10.sp,
      color = amountColor,
    )
  }
}

object BillImageExporter {
  fun renderBillBitmap(
    context: Context,
    customer: Customer,
    settings: BusinessSettings,
    docType: BillDocType,
    billNumber: String,
    issueDateYmd: String,
  ): Bitmap {
    val width = 1080
    val pad = 54f
    val contentW = (width - pad * 2).toInt()

    val mukta = ResourcesCompat.getFont(context, R.font.mukta_malar) ?: Typeface.DEFAULT_BOLD
    val noto = ResourcesCompat.getFont(context, R.font.noto_sans_tamil) ?: Typeface.DEFAULT
    val montserrat = ResourcesCompat.getFont(context, R.font.montserrat) ?: Typeface.DEFAULT_BOLD

    val navy = 0xFF0B2160.toInt()
    val sky = 0xFF0A7BE8.toInt()
    val ink = 0xFF13232B.toInt()
    val muted = 0xFF586B73.toInt()
    val infoBg = 0xFFF3F6FA.toInt()
    val lineColor = 0xFFE1E8EE.toInt()
    val danger = 0xFFB3261E.toInt()
    val white = 0xFFFFFFFF.toInt()

    fun tp(tf: Typeface, size: Float, color: Int, bold: Boolean = false): TextPaint =
      TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = if (bold) Typeface.create(tf, Typeface.BOLD) else tf
        textSize = size
        this.color = color
      }

    fun layout(text: String, paint: TextPaint, w: Int, align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL): StaticLayout =
      StaticLayout.Builder.obtain(text, 0, text.length, paint, w.coerceAtLeast(10))
        .setAlignment(align)
        .setLineSpacing(4f, 1f)
        .build()

    val logoBitmap = runCatching { BitmapFactory.decodeResource(context.resources, R.drawable.skywall_logo) }.getOrNull()

    val bizNameLayout = layout(settings.name, tp(mukta, 28f, ink, true), contentW)
    val bizAddrFull = buildString {
      append(settings.address)
      if (settings.phone.isNotBlank()) append(" · ${settings.phone}")
    }
    val bizAddrLayout = layout(bizAddrFull, tp(noto, 22f, muted), contentW)
    val footerAddrLayout = layout(bizAddrFull, tp(noto, 20f, white), contentW, Layout.Alignment.ALIGN_CENTER)

    val custNameLayout = layout(customer.name, tp(mukta, 28f, ink, true), (contentW * 0.52f).toInt())
    val custPlaceLayout =
      if (customer.place.isNotBlank()) layout("இடம்: ${customer.place}", tp(noto, 22f, muted), (contentW * 0.52f).toInt())
      else null
    val custPhoneLayout =
      if (customer.phone.isNotBlank()) layout(customer.phone, tp(noto, 22f, muted), (contentW * 0.52f).toInt())
      else null

    val jobColW = (contentW * 0.43f).toInt()
    val jobLayouts =
      customer.jobs.map { job ->
        val txt = buildString {
          append(job.type)
          if (job.colour.isNotBlank()) append("\n(${job.colour})")
        }
        layout(txt, tp(noto, 22f, ink), jobColW)
      }

    // First pass: measure height
    var y = 12f + 26f // top stripe + top padding
    if (logoBitmap != null) y += 92f
    y += 42f + 26f + 26f + 24f // SKYWALL + sub + tagline + gap
    y += bizNameLayout.height + 6f + bizAddrLayout.height + 22f
    y += 16f + 78f + 20f // divider + title/number row

    val infoBoxH =
      maxOf(
        52f + custNameLayout.height + (custPlaceLayout?.height ?: 0) + (custPhoneLayout?.height ?: 0),
        148f,
      )
    y += infoBoxH + 24f
    y += 52f // table header
    jobLayouts.forEach { jl -> y += maxOf(jl.height.toFloat(), 32f) + 28f }
    y += 20f
    y += 40f // subtotal
    if (customer.discount > 0.0) y += 40f
    y += 62f // final pill
    y += 40f // paid
    y += 42f // due

    if (customer.payments.isNotEmpty()) {
      y += 28f + 40f
      y += customer.payments.size * 52f
    }

    y += 38f + 46f + 34f + 36f // thank you + tagline + bottom margin
    val footerBarH = footerAddrLayout.height + 34f
    val totalHeight = (y + footerBarH).toInt().coerceAtLeast(900)

    val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(white)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Top Sky stripe
    paint.color = sky
    canvas.drawRect(0f, 0f, width.toFloat(), 12f, paint)

    var curY = 36f
    if (logoBitmap != null) {
      val targetH = 84f
      val aspect = logoBitmap.width.toFloat() / logoBitmap.height.coerceAtLeast(1).toFloat()
      val targetW = targetH * aspect
      val left = (width - targetW) / 2f
      canvas.drawBitmap(
        logoBitmap,
        Rect(0, 0, logoBitmap.width, logoBitmap.height),
        RectF(left, curY, left + targetW, curY + targetH),
        paint,
      )
      curY += targetH + 10f
    }

    // SKYWALL wordmark centered
    val skyPaint = tp(montserrat, 42f, navy, true)
    val wallPaint = tp(montserrat, 42f, sky, true)
    val skyW = skyPaint.measureText("SKY")
    val wallW = wallPaint.measureText("WALL")
    val wordStartX = (width - (skyW + wallW)) / 2f
    canvas.drawText("SKY", wordStartX, curY + 34f, skyPaint)
    canvas.drawText("WALL", wordStartX + skyW, curY + 34f, wallPaint)
    curY += 46f

    val subPaint = tp(montserrat, 18f, ink, true).apply {
      letterSpacing = 0.16f
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("— PAINTING SOLUTIONS —", width / 2f, curY + 18f, subPaint)
    curY += 26f

    val tagPaint = tp(montserrat, 16f, sky, true).apply {
      letterSpacing = 0.16f
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText("— WE PAINT YOUR DREAMS —", width / 2f, curY + 16f, tagPaint)
    curY += 34f

    canvas.save()
    canvas.translate(pad, curY)
    bizNameLayout.draw(canvas)
    canvas.translate(0f, bizNameLayout.height + 6f)
    bizAddrLayout.draw(canvas)
    canvas.restore()
    curY += bizNameLayout.height + 6f + bizAddrLayout.height + 20f

    paint.color = lineColor
    canvas.drawRect(pad, curY, width - pad, curY + 2f, paint)
    curY += 18f

    // Title + Number/Date
    canvas.drawText(docType.titleTa, pad, curY + 36f, tp(mukta, 38f, navy, true))
    canvas.drawText(
      docType.titleEn,
      pad,
      curY + 62f,
      tp(montserrat, 18f, sky, true).apply { letterSpacing = 0.16f },
    )

    val rightNumPaint = tp(noto, 22f, ink, true).apply { textAlign = Paint.Align.RIGHT }
    val rightDatePaint = tp(noto, 21f, muted).apply { textAlign = Paint.Align.RIGHT }
    canvas.drawText("எண்: $billNumber", width - pad, curY + 28f, rightNumPaint)
    canvas.drawText(
      "தேதி: ${SkywallFormatters.formatFullDateTa(issueDateYmd)}",
      width - pad,
      curY + 58f,
      rightDatePaint,
    )
    curY += 86f

    // Customer & Site Box
    paint.color = infoBg
    canvas.drawRoundRect(RectF(pad, curY, width - pad, curY + infoBoxH), 16f, 16f, paint)
    val leftBoxX = pad + 24f
    val rightBoxX = pad + contentW * 0.57f
    var boxY = curY + 34f
    canvas.drawText("வாடிக்கையாளர்", leftBoxX, boxY, tp(noto, 19f, muted))
    canvas.drawText("தள ஆய்வு தேதி", rightBoxX, boxY, tp(noto, 19f, muted))
    boxY += 12f
    canvas.save()
    canvas.translate(leftBoxX, boxY)
    custNameLayout.draw(canvas)
    var cOff = custNameLayout.height.toFloat()
    custPlaceLayout?.let {
      canvas.translate(0f, cOff + 4f)
      it.draw(canvas)
      cOff = it.height.toFloat()
    }
    custPhoneLayout?.let {
      canvas.translate(0f, cOff + 2f)
      it.draw(canvas)
    }
    canvas.restore()

    val visitText =
      if (customer.visitDate.isNotBlank()) SkywallFormatters.formatFullDateTa(customer.visitDate)
      else "–"
    canvas.drawText(visitText, rightBoxX, boxY + 26f, tp(noto, 22f, ink, true))
    canvas.drawText("நிலை", rightBoxX, boxY + 62f, tp(noto, 19f, muted))
    canvas.drawText(customer.stage.labelTa, rightBoxX, boxY + 90f, tp(noto, 22f, ink, true))

    curY += infoBoxH + 24f

    // Work Table Header
    paint.color = navy
    canvas.drawRoundRect(RectF(pad, curY, width - pad, curY + 50f), 10f, 10f, paint)
    val thLeft = tp(noto, 20f, white, true)
    val thRight = tp(noto, 20f, white, true).apply { textAlign = Paint.Align.RIGHT }
    val colIdxX = pad + 18f
    val colJobX = pad + 56f
    val colAreaRight = pad + contentW * 0.67f
    val colRateRight = pad + contentW * 0.82f
    val colAmtRight = width - pad - 18f

    canvas.drawText("#", colIdxX, curY + 32f, thLeft)
    canvas.drawText("வேலை விவரம்", colJobX, curY + 32f, thLeft)
    canvas.drawText("அளவு", colAreaRight, curY + 32f, thRight)
    canvas.drawText("ரேட்", colRateRight, curY + 32f, thRight)
    canvas.drawText("தொகை", colAmtRight, curY + 32f, thRight)
    curY += 56f

    // Work Table Rows
    val tdMuted = tp(noto, 21f, muted)
    val tdRight = tp(noto, 21f, ink).apply { textAlign = Paint.Align.RIGHT }
    val tdRightBold = tp(noto, 21f, ink, true).apply { textAlign = Paint.Align.RIGHT }
    customer.jobs.forEachIndexed { idx, job ->
      val jl = jobLayouts[idx]
      val rowH = maxOf(jl.height.toFloat(), 32f)
      canvas.drawText("${idx + 1}", colIdxX, curY + 24f, tdMuted)
      canvas.save()
      canvas.translate(colJobX, curY + 2f)
      jl.draw(canvas)
      canvas.restore()

      canvas.drawText(
        "${SkywallFormatters.formatNumberClean(job.area)} ச.அடி",
        colAreaRight,
        curY + 24f,
        tdRight,
      )
      canvas.drawText(
        "₹${SkywallFormatters.formatNumberClean(job.rate)}",
        colRateRight,
        curY + 24f,
        tdRight,
      )
      canvas.drawText(
        SkywallFormatters.formatInr(job.lineAmount),
        colAmtRight,
        curY + 24f,
        tdRightBold,
      )
      curY += rowH + 18f
      paint.color = lineColor
      canvas.drawRect(pad, curY, width - pad, curY + 1.5f, paint)
      curY += 10f
    }

    curY += 14f

    // Right-aligned Totals
    val totLabelX = pad + contentW * 0.48f
    val totValRight = width - pad - 14f
    val lblPaint = tp(noto, 22f, muted)
    val valPaint = tp(noto, 22f, ink, true).apply { textAlign = Paint.Align.RIGHT }

    canvas.drawText("மொத்தம்", totLabelX, curY + 24f, lblPaint)
    canvas.drawText(SkywallFormatters.formatInr(customer.subtotal), totValRight, curY + 24f, valPaint)
    curY += 40f

    if (customer.discount > 0.0) {
      canvas.drawText("தள்ளுபடி", totLabelX, curY + 24f, lblPaint)
      canvas.drawText(
        "− ${SkywallFormatters.formatInr(customer.discount)}",
        totValRight,
        curY + 24f,
        valPaint,
      )
      curY += 40f
    }

    // Highlighted final pill
    paint.color = navy
    canvas.drawRoundRect(
      RectF(totLabelX - 16f, curY, width - pad, curY + 52f),
      12f,
      12f,
      paint,
    )
    canvas.drawText(
      if (docType == BillDocType.INVOICE) "இறுதி தொகை" else "இறுதி மதிப்பீடு",
      totLabelX,
      curY + 34f,
      tp(mukta, 24f, white, true),
    )
    canvas.drawText(
      SkywallFormatters.formatInr(customer.totalEstimate),
      totValRight,
      curY + 35f,
      tp(mukta, 26f, white, true).apply { textAlign = Paint.Align.RIGHT },
    )
    curY += 62f

    canvas.drawText("பெற்றது", totLabelX, curY + 24f, lblPaint)
    canvas.drawText(
      SkywallFormatters.formatInr(customer.paidAmount),
      totValRight,
      curY + 24f,
      valPaint,
    )
    curY += 40f

    canvas.drawText("பாக்கி", totLabelX, curY + 24f, tp(noto, 23f, danger, true))
    canvas.drawText(
      SkywallFormatters.formatInr(customer.dueAmount),
      totValRight,
      curY + 24f,
      tp(noto, 23f, danger, true).apply { textAlign = Paint.Align.RIGHT },
    )
    curY += 42f

    // Payments log
    if (customer.payments.isNotEmpty()) {
      curY += 20f
      canvas.drawText("பணம் பெற்ற பதிவுகள்", pad, curY + 24f, tp(mukta, 23f, navy, true))
      curY += 40f
      customer.payments.forEach { payment ->
        paint.color = lineColor
        canvas.drawRect(pad, curY, width - pad, curY + 1.5f, paint)
        curY += 10f
        canvas.drawText(
          SkywallFormatters.formatFullDateTa(payment.date),
          pad,
          curY + 24f,
          tp(noto, 20f, ink),
        )
        canvas.drawText(
          payment.note,
          pad + contentW * 0.36f,
          curY + 24f,
          tp(noto, 20f, muted),
        )
        canvas.drawText(
          SkywallFormatters.formatInr(payment.amount),
          width - pad,
          curY + 24f,
          tp(noto, 20f, ink, true).apply { textAlign = Paint.Align.RIGHT },
        )
        curY += 42f
      }
    }

    // Thank you + Tagline
    curY += 24f
    canvas.drawText(
      "நன்றி!",
      width / 2f,
      curY + 30f,
      tp(mukta, 30f, navy, true).apply { textAlign = Paint.Align.CENTER },
    )
    curY += 48f
    canvas.drawText(
      "———  WE PAINT YOUR DREAMS  ———",
      width / 2f,
      curY + 18f,
      tp(montserrat, 17f, sky, true).apply {
        letterSpacing = 0.14f
        textAlign = Paint.Align.CENTER
      },
    )

    // Bottom Navy Address Bar
    val barTop = totalHeight - footerBarH
    paint.color = navy
    canvas.drawRect(0f, barTop, width.toFloat(), totalHeight.toFloat(), paint)
    canvas.save()
    canvas.translate(pad, barTop + 17f)
    footerAddrLayout.draw(canvas)
    canvas.restore()

    return bitmap
  }

  fun saveBillToMediaStore(context: Context, bitmap: Bitmap, fileName: String): Uri? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    val values =
      ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Skywall")
        put(MediaStore.Images.Media.IS_PENDING, 1)
      }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
    return try {
      resolver.openOutputStream(uri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
      }
      values.clear()
      values.put(MediaStore.Images.Media.IS_PENDING, 0)
      resolver.update(uri, values, null, null)
      uri
    } catch (_: Exception) {
      resolver.delete(uri, null, null)
      null
    }
  }

  fun writeBitmapToUri(context: Context, uri: Uri, bitmap: Bitmap): Boolean {
    return try {
      context.contentResolver.openOutputStream(uri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
      }
      true
    } catch (_: Exception) {
      false
    }
  }

  fun shareBillBitmap(context: Context, bitmap: Bitmap, fileName: String): Boolean {
    return try {
      val dir = File(context.cacheDir, "bills").apply { mkdirs() }
      val file = File(dir, "$fileName.png")
      FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
      }
      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val shareIntent =
        Intent(Intent.ACTION_SEND).apply {
          type = "image/png"
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
      context.startActivity(Intent.createChooser(shareIntent, "பில் படத்தைப் பகிர்"))
      true
    } catch (_: Exception) {
      false
    }
  }
}
