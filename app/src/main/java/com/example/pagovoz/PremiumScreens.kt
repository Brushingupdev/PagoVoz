package com.example.pagovoz

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.theme.AppColors
import com.example.pagovoz.ui.theme.AppElevation
import com.example.pagovoz.ui.theme.AppIconSizes
import com.example.pagovoz.ui.theme.AppRadii
import com.example.pagovoz.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumStatusScreen(
    onBack: () -> Unit,
    onShowReports: () -> Unit,
    onShowHistory: () -> Unit,
    onShowPayments: () -> Unit,
    onShowVoiceSettings: () -> Unit,
    onShowProfile: () -> Unit
) {
    BackHandler { onBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomBar(selectedTab = DashboardTab.Premium) { tab ->
                when (tab) {
                    DashboardTab.Home -> onBack()
                    DashboardTab.History -> onShowHistory()
                    DashboardTab.Payments -> onShowPayments()
                    DashboardTab.Reports -> onShowReports()
                    DashboardTab.Premium -> Unit
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            AppColors.BackgroundAccent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier.clickable(onClick = onBack),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.a11y_navigate_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PREMIUM ACTIVO",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.7.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "HablaPago Premium\nActivo",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.displaySmall,
                        lineHeight = 42.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.premium_active_subtitle_new),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumMetricCard(
                            title = stringResource(R.string.premium_benefit_title_reports),
                            value = "PDF + WhatsApp"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PremiumMetricCard(
                            title = stringResource(R.string.premium_benefit_title_voice),
                            value = "Voz Pro"
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumReferenceBenefitItem("Reportes ilimitados")
                    PremiumReferenceBenefitItem("Envío por WhatsApp")
                    PremiumReferenceBenefitItem("Sin anuncios")
                    PremiumReferenceBenefitItem("Soporte prioritario")
                    PremiumReferenceBenefitItem("Voz configurable")
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.premium_linked_device),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.premium_status_supporting),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = onShowReports,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = "Abrir reportes",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Button(
                        onClick = onShowVoiceSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = "Ajustar voz",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumInfoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    BackHandler { onBack() }
    val phoneNumber = "51983450723"
    val message = context.getString(R.string.premium_whatsapp_message)
    val whatsappUrl = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
    val openWhatsApp = {
        val intent = Intent(Intent.ACTION_VIEW, whatsappUrl.toUri())
        context.startActivity(intent)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            AppColors.BackgroundAccent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier.clickable(onClick = onBack),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.a11y_navigate_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Desbloquea\nHablaPago Premium",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.displaySmall,
                        lineHeight = 42.sp,
                        textAlign = TextAlign.Center
                    )

                    PremiumReferenceBenefitItem("Voz ilimitada")
                    PremiumReferenceBenefitItem("Reportes avanzados")
                    PremiumReferenceBenefitItem("Sin anuncios")
                    PremiumReferenceBenefitItem("Transferencias rápidas")
                    PremiumReferenceBenefitItem("Soporte prioritario")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumPlanCard(
                        modifier = Modifier.weight(1f),
                        title = "Mensual",
                        price = "S/ 9.90",
                        supporting = null,
                        highlighted = false
                    )
                    PremiumPlanCard(
                        modifier = Modifier.weight(1f),
                        title = "Anual",
                        price = "S/ 89",
                        supporting = "Mejor valor",
                        highlighted = true
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    onClick = openWhatsApp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = "Activar Ahora",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Text(
                    text = stringResource(R.string.premium_contact_whatsapp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = openWhatsApp)
                )
            }
        }
    }
}

@Composable
private fun PremiumReferenceBenefitItem(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PremiumPlanCard(
    modifier: Modifier = Modifier,
    title: String,
    price: String,
    supporting: String?,
    highlighted: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (highlighted) 0.98f else 0.88f),
        border = BorderStroke(
            1.dp,
            if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = price,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun PremiumMinimalHero(
    onRequestAccess: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        AppColors.BackgroundAccent,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        val heroMinHeight = if (maxHeight > 0.dp) 430.dp else 430.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroMinHeight)
                .padding(horizontal = 24.dp, vertical = 26.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(152.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(102.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            )

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(
                        text = stringResource(R.string.premium_info_badge),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.7.sp
                    )

                    Text(
                        text = stringResource(R.string.premium_unlock_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displaySmall,
                        lineHeight = 44.sp
                    )

                    Text(
                        text = stringResource(R.string.premium_unlock_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumMetricCard(
                                title = stringResource(R.string.premium_benefit_title_reports),
                                value = "PDF + WhatsApp"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PremiumMetricCard(
                                title = stringResource(R.string.premium_benefit_title_voice),
                                value = "Voz Pro"
                            )
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = onRequestAccess,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.premium_request_access),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumMetricCard(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            lineHeight = 16.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PremiumMinimalBenefitRow(
    title: String,
    subtitle: String,
    iconRes: Int,
    trailingLabel: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.SurfaceBrand
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            Text(
                text = trailingLabel,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        )
    }
}

@Composable
private fun PremiumMinimalWhy() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.premium_why_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.premium_why_body),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun PremiumMinimalActions(
    onRequestAccess: () -> Unit,
    onContactWhatsApp: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = onRequestAccess,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.premium_request_access),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Button(
            onClick = onContactWhatsApp,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            contentPadding = PaddingValues(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.premium_contact_whatsapp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PremiumStatusHeroCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.SurfaceBrand,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.premium_active_badge_refined),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.7.sp
                )

                Text(
                    text = stringResource(R.string.premium_active_title_new),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                )

                Text(
                    text = stringResource(R.string.premium_active_subtitle_new),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.SuccessContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_benefit_voice),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                PremiumMetricCard(
                    title = stringResource(R.string.premium_benefit_title_reports),
                    value = "PDF + WhatsApp"
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                PremiumMetricCard(
                    title = stringResource(R.string.premium_benefit_title_voice),
                    value = "Voz Pro"
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.premium_linked_device),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.premium_status_supporting),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PremiumBenefitsPanel(isStatus: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DashboardSectionHeader(
            title = if (isStatus) {
                stringResource(R.string.premium_status_benefits_title)
            } else {
                stringResource(R.string.premium_info_benefits_title)
            },
            subtitle = if (isStatus) {
                stringResource(R.string.premium_status_benefits_subtitle)
            } else {
                stringResource(R.string.premium_info_benefits_subtitle)
            }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            PremiumMinimalBenefitRow(
                title = stringResource(R.string.premium_benefit_title_reports),
                subtitle = stringResource(R.string.premium_benefit_subtitle_reports),
                iconRes = R.drawable.ic_benefit_pdf,
                trailingLabel = stringResource(R.string.premium_benefit_tag)
            )
            PremiumMinimalBenefitRow(
                title = stringResource(R.string.premium_benefit_title_whatsapp),
                subtitle = stringResource(R.string.premium_benefit_subtitle_whatsapp),
                iconRes = R.drawable.ic_benefit_whatsapp,
                trailingLabel = stringResource(R.string.premium_benefit_tag)
            )
            PremiumMinimalBenefitRow(
                title = stringResource(R.string.premium_benefit_title_no_ads),
                subtitle = stringResource(R.string.premium_benefit_subtitle_no_ads),
                iconRes = R.drawable.ic_benefit_no_ads,
                trailingLabel = stringResource(R.string.premium_benefit_tag)
            )
            PremiumMinimalBenefitRow(
                title = stringResource(R.string.premium_benefit_title_priority),
                subtitle = stringResource(R.string.premium_benefit_subtitle_priority),
                iconRes = R.drawable.ic_benefit_support,
                trailingLabel = stringResource(R.string.premium_benefit_tag)
            )
            PremiumMinimalBenefitRow(
                title = stringResource(R.string.premium_benefit_title_voice),
                subtitle = stringResource(R.string.premium_benefit_subtitle_voice),
                iconRes = R.drawable.ic_benefit_voice,
                trailingLabel = stringResource(R.string.premium_benefit_tag)
            )
        }
    }
}

@Composable
private fun PremiumWhyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.flat)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.premium_why_title),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.premium_why_body),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PremiumActionPanel(
    onRequestAccess: () -> Unit,
    onContactWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.md)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = onRequestAccess,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.premium_request_access),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Button(
                onClick = onContactWhatsApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.premium_contact_whatsapp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PremiumHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AppColors.BrandPrimaryStrong, MaterialTheme.colorScheme.primary)
                )
            )
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .height(148.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PremiumCircleIcon(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumCircleIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        HablaPagoIconTile(
            icon = icon,
            tint = Color.White,
            containerColor = Color.White.copy(alpha = 0.18f),
            size = AppIconSizes.tileSm,
            iconSize = AppIconSizes.lg,
            shape = CircleShape
        )
    }
}

@Composable
private fun PremiumBenefitCard(
    title: String,
    subtitle: String,
    iconRes: Int,
    trailingLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadii.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.flat)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HablaPagoIconTile(
                iconRes = iconRes,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                size = 48.dp,
                iconSize = 22.dp,
                shape = RoundedCornerShape(18.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(AppRadii.pill),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = trailingLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = Color(0xFF1FA866),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}
