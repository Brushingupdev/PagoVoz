package com.example.pagovoz

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.pagovoz.ui.theme.YapeCyan
import com.example.pagovoz.ui.theme.YapePurple

@Composable
fun PremiumLogo() {
    Box(
        modifier = Modifier.size(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(YapeCyan.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        Surface(
            modifier = Modifier.size(110.dp),
            shape = CircleShape,
            shadowElevation = 12.dp,
            tonalElevation = 6.dp,
            border = BorderStroke(3.dp, Brush.linearGradient(listOf(YapeCyan, YapePurple)))
        ) {
            Image(
                painter = painterResource(id = R.drawable.mi_logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Surface(
            modifier = Modifier
                .size(38.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-8).dp),
            shape = CircleShape,
            color = YapePurple,
            border = BorderStroke(2.dp, YapeCyan),
            shadowElevation = 6.dp
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = YapeCyan,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumStatusScreen(onBack: () -> Unit) {
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.premium_plan_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YapePurple)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PremiumLogo()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.premium_status_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = YapePurple
            )
            Text(
                text = stringResource(R.string.premium_status_subtitle),
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(stringResource(R.string.premium_benefits_active), fontWeight = FontWeight.Bold, color = YapePurple)
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_sales_today_yesterday))
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_unlimited_pdf))
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_whatsapp_send))
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_no_ads))
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_priority_support))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.premium_linked_device),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = YapePurple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.premium_back_panel), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumInfoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.premium_info_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YapePurple)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PremiumLogo()
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.premium_business_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = YapePurple
            )
            Text(
                text = stringResource(R.string.premium_business_subtitle),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, YapePurple.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_unlimited_reports))
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_detailed_history))
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_real_time_management))
                    PremiumBenefitItem(stringResource(R.string.premium_benefit_support_247))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                onClick = {
                    val phoneNumber = "51983450723"
                    val message = context.getString(R.string.premium_whatsapp_message)
                    val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.premium_request_access), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.premium_back_main), color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PremiumBenefitItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = YapeCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
