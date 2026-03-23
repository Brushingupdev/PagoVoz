package com.example.pagovoz

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pagovoz.ui.components.HablaPagoIconTile
import com.example.pagovoz.ui.components.hablaPagoPressable
import com.example.pagovoz.ui.theme.YapePurple

private val ActivationBackgroundTop = Color(0xFFF5F1FA)
private val ActivationBackgroundBottom = Color(0xFFFFFCFF)
private val ActivationGlowLavender = Color(0xFFEADCF8)
private val ActivationGlowRose = Color(0xFFF7EAF4)
private val ActivationGlowMint = Color(0xFFECF7F3)
private val ActivationPanelColor = Color(0xFFFFFFFF)
private val ActivationAccentSoft = Color(0xFFF3ECFB)
private val ActivationFieldColor = Color(0xFFF8F5FB)
private val ActivationFieldBorder = Color(0xFFD8D0E0)
private val ActivationDisabledButton = Color(0xFFE3E3E8)
private val ActivationDisabledText = Color(0xFF9D9DA6)
private val PlinBlue = Color(0xFF34B7D7)
private val WhatsAppGreen = Color(0xFF25D366)
private val WhatsAppSoft = Color(0xFFEAFBF2)

@Composable
fun ActivationScreen(
    onActivated: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ActivationViewModel = viewModel(
        factory = ActivationViewModelFactory(
            licenseRepository = defaultLicenseRepository(context),
            emptyCodeError = context.getString(R.string.activation_error_empty_code),
            codeFormatError = context.getString(R.string.activation_error_format_code),
            invalidCodeError = context.getString(R.string.activation_error_invalid_code)
        )
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is ActivationEvent.Activated) onActivated()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ActivationBackgroundTop, ActivationBackgroundBottom)
                )
            )
    ) {
        val compact = maxHeight < 760.dp
        val outerPadding = if (compact) 18.dp else 24.dp
        val topGap = if (compact) 12.dp else 20.dp
        val sectionGap = if (compact) 10.dp else 12.dp
        val heroTitleSize = if (compact) 27.sp else 30.sp
        val heroBodySize = if (compact) 15.sp else 16.sp
        val panelPadding = if (compact) 18.dp else 22.dp
        val panelSpacing = if (compact) 14.dp else 16.dp
        val iconSize = if (compact) 62.dp else 70.dp
        val scrollModifier = if (compact) Modifier.verticalScroll(rememberScrollState()) else Modifier

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (compact) 40.dp else 24.dp, end = if (compact) 6.dp else 0.dp)
                .size(if (compact) 168.dp else 220.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ActivationGlowLavender.copy(alpha = 0.82f),
                            ActivationGlowLavender.copy(alpha = 0.1f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = if (compact) 140.dp else 170.dp)
                .size(if (compact) 104.dp else 132.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ActivationGlowRose.copy(alpha = 0.72f),
                            ActivationGlowRose.copy(alpha = 0.08f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (compact) 120.dp else 140.dp)
                .fillMaxWidth()
                .height(if (compact) 220.dp else 260.dp)
                .clip(RoundedCornerShape(42.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ActivationGlowMint.copy(alpha = 0.24f),
                            Color.White.copy(alpha = 0f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = if (compact) 118.dp else 138.dp)
                .fillMaxWidth(if (compact) 0.86f else 0.8f)
                .height(if (compact) 130.dp else 152.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.42f),
                            ActivationGlowLavender.copy(alpha = 0.18f),
                            Color.White.copy(alpha = 0.16f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(scrollModifier)
                .padding(horizontal = outerPadding, vertical = if (compact) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (compact) {
                Arrangement.spacedBy(sectionGap)
            } else {
                Arrangement.spacedBy(sectionGap, Alignment.CenterVertically)
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 92.dp else 100.dp)
                        .clip(CircleShape)
                        .background(ActivationAccentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    HablaPagoIconTile(
                        iconRes = R.drawable.ic_activation_mic,
                        tint = Color.White,
                        containerColor = YapePurple,
                        size = iconSize,
                        iconSize = if (compact) 26.dp else 28.dp,
                        shape = RoundedCornerShape(22.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.activation_welcome_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = heroTitleSize,
                    lineHeight = if (compact) 31.sp else 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = buildAnnotatedString {
                        append("Asistente de voz para cobrar con ")
                        pushStyle(SpanStyle(color = YapePurple, fontWeight = FontWeight.Bold))
                        append("Yape")
                        pop()
                        append(" y ")
                        pushStyle(SpanStyle(color = PlinBlue, fontWeight = FontWeight.Bold))
                        append("Plin")
                        pop()
                        append(" sin tocar el celular.")
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = heroBodySize,
                    lineHeight = if (compact) 21.sp else 22.sp,
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = ActivationPanelColor,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = panelPadding, vertical = panelPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(panelSpacing)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Activa tu dispositivo",
                            color = YapePurple,
                            fontSize = if (compact) 17.sp else 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Ingresa tu código y empieza a usar HablaPago.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = if (compact) 14.sp else 15.sp,
                            lineHeight = if (compact) 20.sp else 21.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    ActivationCodeField(
                        code = uiState.code,
                        isCodeValid = uiState.isCodeFormatValid,
                        onCodeChanged = viewModel::onCodeChanged,
                        compact = compact
                    )

                    if (uiState.error.isNotEmpty()) {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = YapePurple)
                        }
                    } else {
                        Button(
                            onClick = viewModel::activate,
                            enabled = uiState.isCodeFormatValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YapePurple,
                                contentColor = Color.White,
                                disabledContainerColor = ActivationDisabledButton,
                                disabledContentColor = ActivationDisabledText
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.activation_button),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            ActivationSupportRow(
                compact = compact,
                onClick = { openSupport(context) }
            )
        }
    }
}

@Composable
private fun ActivationSupportRow(
    compact: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .hablaPagoPressable(interactionSource)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .padding(vertical = if (compact) 2.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 38.dp else 40.dp)
                    .clip(CircleShape)
                    .background(WhatsAppSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.a11y_contact_support),
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp)
            ) {
                Text(
                    text = "Solicitar codigo por WhatsApp",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Escribenos y te ayudamos con la activacion.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = if (compact) 11.sp else 12.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = WhatsAppGreen
            ) {
                Text(
                    text = "WhatsApp",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    color = Color.White,
                    fontSize = if (compact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ActivationCodeField(
    code: String,
    isCodeValid: Boolean,
    onCodeChanged: (String) -> Unit,
    compact: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val supportingColor = if (isCodeValid) Color(0xFF1FA866) else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChanged,
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = if (compact) 17.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Done,
                autoCorrectEnabled = false
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = stringResource(R.string.activation_code_placeholder),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            trailingIcon = {
                if (isCodeValid) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.a11y_activation_code_valid),
                        tint = Color(0xFF1FA866)
                    )
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ActivationFieldColor,
                unfocusedContainerColor = ActivationFieldColor,
                disabledContainerColor = ActivationFieldColor,
                focusedBorderColor = if (isCodeValid) Color(0xFF1FA866) else ActivationFieldBorder,
                unfocusedBorderColor = ActivationFieldBorder,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                focusedTrailingIconColor = Color(0xFF1FA866),
                unfocusedTrailingIconColor = Color(0xFF1FA866),
                cursorColor = YapePurple
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isCodeValid) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.a11y_activation_code_valid),
                    tint = Color(0xFF1FA866),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = stringResource(R.string.activation_code_format_hint),
                color = supportingColor,
                fontSize = if (compact) 12.sp else 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun openSupport(context: android.content.Context) {
    val phoneNumber = "51983450723"
    val message = context.getString(R.string.activation_support_message)
    val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}
