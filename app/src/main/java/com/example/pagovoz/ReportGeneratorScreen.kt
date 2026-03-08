package com.example.pagovoz

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagovoz.ui.theme.YapeCyan
import com.example.pagovoz.ui.theme.YapePurple
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportGeneratorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: ReportsViewModel = viewModel(
        factory = ReportsViewModelFactory(
            sessionRepository = defaultSessionRepository(context)
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reportTitle = if (uiState.isToday) stringResource(R.string.reports_today) else stringResource(R.string.reports_yesterday)

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { generatePdfDirect(context, it, uiState.reportDate, uiState.reportTotal, uiState.reportHistory) }
    }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.reports_title), color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = YapePurple)
                )
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = YapePurple,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = YapeCyan
                        )
                    }
                ) {
                    Tab(selected = uiState.selectedTab == 0, onClick = { viewModel.onTabSelected(0) }, text = { Text(stringResource(R.string.reports_tab_today), fontWeight = FontWeight.Bold) })
                    Tab(selected = uiState.selectedTab == 1, onClick = { viewModel.onTabSelected(1) }, text = { Text(stringResource(R.string.reports_tab_yesterday), fontWeight = FontWeight.Bold) })
                }
            }
        }
    ) { padding ->
        if (uiState.selectedTab == 1 && !uiState.hasYesterdayData) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.reports_no_yesterday_data), color = Color.Gray)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    reportTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = YapePurple
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.reports_label_date), fontWeight = FontWeight.Bold)
                            Text(uiState.reportDate)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.reports_label_total_sales), fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", uiState.reportTotal)), color = YapePurple, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.reports_label_transactions), fontWeight = FontWeight.Bold)
                            Text("${uiState.reportCount}")
                        }
                    }
                }

                Text(
                    stringResource(R.string.reports_preview_title),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    if (uiState.reportHistory.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.reports_empty_transactions), color = Color.Gray)
                        }
                    } else {
                        Column(Modifier.padding(12.dp)) {
                            uiState.reportHistory.takeLast(5).reversed().forEach { record ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(record.sender, fontSize = 12.sp, maxLines = 1)
                                    Text(stringResource(R.string.currency_amount, String.format(Locale.US, "%.2f", record.amount)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (uiState.reportHistory.size > 5) {
                                Text(stringResource(R.string.reports_and_more, uiState.reportHistory.size - 5), fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { sharePdfCustom(context, uiState.reportDate, uiState.reportTotal, uiState.reportHistory) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reports_share_whatsapp), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val fileName = "Reporte_Pagos_${uiState.reportDate.replace("-", "")}.pdf"
                        createDocumentLauncher.launch(fileName)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, YapePurple)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = YapePurple)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reports_download_pdf), color = YapePurple, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun sharePdfCustom(context: Context, date: String, total: Float, history: List<PaymentRecord>) {
    try {
        val fileName = "Reporte_Pagos_${date.replace("-", "")}.pdf"
        val cacheFile = File(context.cacheDir, fileName)
        generatePdfToFileCustom(cacheFile, date, total, history)

        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
        val totalText = String.format(Locale.US, "%.2f", total)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.reports_share_text, date, totalText))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.whatsapp")
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.reports_share_chooser)))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.reports_share_error, e.message ?: ""), Toast.LENGTH_LONG).show()
    }
}

fun generatePdfToFileCustom(file: File, date: String, total: Float, history: List<PaymentRecord>) {
    val document = PdfDocument()
    val paint = Paint()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 50f

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page = document.startPage(pageInfo)
    var canvas = page.canvas

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Reporte de Ventas - PagoVoz Pro", margin, 50f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    canvas.drawText("Fecha del reporte: $date", margin, 80f, paint)
    canvas.drawText("Total Neto Recaudado: S/ ${String.format(Locale.US, "%.2f", total)}", margin, 100f, paint)
    canvas.drawLine(margin, 120f, pageWidth - margin, 120f, paint)

    var y = 150f
    paint.textSize = 12f
    for (record in history) {
        if (y > 800f) {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = margin

            paint.isUnderlineText = true
            canvas.drawText("(Continuación reporte $date - pág $pageNumber)", margin, 30f, paint)
            paint.isUnderlineText = false
            y += 30f
        }

        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))
        canvas.drawText("$time - ${record.sender}", margin, y, paint)
        canvas.drawText("S/ ${String.format(Locale.US, "%.2f", record.amount)}", pageWidth - 150f, y, paint)
        y += 25f
    }

    document.finishPage(page)
    FileOutputStream(file).use { out -> document.writeTo(out) }
    document.close()
}

fun generatePdfDirect(context: Context, uri: Uri, date: String, total: Float, history: List<PaymentRecord>) {
    val document = PdfDocument()
    val paint = Paint()
    val pageWidth = 595
    val pageHeight = 842
    val margin = 50f

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page = document.startPage(pageInfo)
    var canvas = page.canvas

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Reporte de Ventas - PagoVoz Pro", margin, 50f, paint)

    paint.textSize = 14f
    paint.isFakeBoldText = false
    canvas.drawText("Fecha del reporte: $date", margin, 80f, paint)
    canvas.drawText("Total Neto Recaudado: S/ ${String.format(Locale.US, "%.2f", total)}", margin, 100f, paint)
    canvas.drawLine(margin, 120f, pageWidth - margin, 120f, paint)

    var y = 150f
    paint.textSize = 12f
    for (record in history) {
        if (y > 800f) {
            document.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = margin

            paint.isUnderlineText = true
            canvas.drawText("(Continuación reporte $date - pág $pageNumber)", margin, 30f, paint)
            paint.isUnderlineText = false
            y += 30f
        }

        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(record.timestamp))
        canvas.drawText("$time - ${record.sender}", margin, y, paint)
        canvas.drawText("S/ ${String.format(Locale.US, "%.2f", record.amount)}", pageWidth - 150f, y, paint)
        y += 25f
    }

    document.finishPage(page)

    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream -> document.writeTo(outputStream) }
        Toast.makeText(context, context.getString(R.string.reports_pdf_download_ok), Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, context.getString(R.string.reports_pdf_download_error), Toast.LENGTH_LONG).show()
    } finally {
        document.close()
    }
}
