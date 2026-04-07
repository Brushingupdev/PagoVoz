package com.example.pagovoz

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import com.example.pagovoz.ui.theme.YapePurple

private val ActivationLightTop = Color(0xFFF8F4FC)
private val ActivationLightBottom = Color(0xFFF1F8FC)
private val ActivationCardColor = Color(0xFFFFFFFF)
private val ActivationCardBorder = Color(0xFFE3D9EE)
private val ActivationFieldColor = Color(0xFFF6F1FA)
private val ActivationFieldBorder = Color(0xFFD8CCE5)
private val ActivationDisabledButton = Color(0xFFE6E2EA)
private val ActivationDisabledText = Color(0xFF9B95A6)
private val ActivationBodyText = Color(0xFF746E7E)
private val PlinBlue = Color(0xFF34B7D7)
private val WhatsAppGreen = Color(0xFF25D366)
private val WhatsAppSoft = Color(0xFFEAF8EE)

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
                    colors = listOf(ActivationLightTop, ActivationLightBottom)
                )
            )
    ) {
        val compact = maxHeight < 760.dp

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (compact) 34.dp else 52.dp, end = if (compact) 12.dp else 20.dp)
                .size(if (compact) 120.dp else 156.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            YapePurple.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = if (compact) 6.dp else 14.dp, bottom = if (compact) 90.dp else 110.dp)
                .size(if (compact) 96.dp else 120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PlinBlue.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (compact) 18.dp else 24.dp, vertical = if (compact) 28.dp else 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ActivationHero(compact = compact)

            Spacer(modifier = Modifier.height(if (compact) 18.dp else 22.dp))

            ActivationFormCard(
                code = uiState.code,
                isCodeValid = uiState.isCodeFormatValid,
                isLoading = uiState.isLoading,
                error = uiState.error,
                compact = compact,
                onCodeChanged = viewModel::onCodeChanged,
                onActivate = viewModel::activate
            )

            Spacer(modifier = Modifier.height(if (compact) 16.dp else 18.dp))

            ActivationSupportButton(
                compact = compact,
                onClick = { openSupport(context) }
            )
        }
    }
}

@Composable
private fun ActivationHero(compact: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.activation_welcome_title),
            color = Color(0xFF1E1B24),
            fontSize = if (compact) 24.sp else 28.sp,
            lineHeight = if (compact) 28.sp else 32.sp,
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
            color = ActivationBodyText,
            fontSize = if (compact) 13.sp else 15.sp,
            lineHeight = if (compact) 19.sp else 21.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActivationFormCard(
    code: String,
    isCodeValid: Boolean,
    isLoading: Boolean,
    error: String,
    compact: Boolean,
    onCodeChanged: (String) -> Unit,
    onActivate: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = ActivationCardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, ActivationCardBorder),
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 18.dp else 22.dp, vertical = if (compact) 20.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Activa tu dispositivo",
                color = YapePurple,
                fontSize = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Ingresa tu codigo y empieza a usar HablaPago.",
                color = ActivationBodyText,
                fontSize = if (compact) 13.sp else 14.sp,
                lineHeight = if (compact) 18.sp else 19.sp,
                textAlign = TextAlign.Center
            )

            ActivationCodeField(
                code = code,
                isCodeValid = isCodeValid,
                onCodeChanged = onCodeChanged,
                compact = compact
            )

            if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(if (compact) 46.dp else 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = YapePurple)
                }
            } else {
                Button(
                    onClick = onActivate,
                    enabled = isCodeValid,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(if (compact) 46.dp else 48.dp),
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
                        fontSize = if (compact) 14.sp else 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivationSupportButton(
    compact: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.94f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(if (compact) 38.dp else 40.dp),
            shape = CircleShape,
            color = PlinBlue.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_benefit_whatsapp),
                    contentDescription = stringResource(R.string.a11y_contact_support),
                    tint = WhatsAppGreen,
                    modifier = Modifier.size(if (compact) 16.dp else 18.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Solicitar codigo por WhatsApp",
                color = Color(0xFF1E1B24),
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Escribenos y te ayudamos con la activacion.",
                color = ActivationBodyText,
                fontSize = if (compact) 11.sp else 12.sp,
                lineHeight = if (compact) 16.sp else 17.sp
            )
        }

        Button(
            onClick = onClick,
            shape = RoundedCornerShape(999.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF32D36B),
                contentColor = Color.White
            ),
            modifier = Modifier.height(if (compact) 38.dp else 40.dp)
        ) {
            Text(
                text = "WhatsApp",
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
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
    val supportingColor = if (isCodeValid) Color(0xFF1FA866) else ActivationBodyText

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
                color = Color(0xFF2A2433),
                fontSize = if (compact) 16.sp else 17.sp,
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
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF8F889A)
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
                focusedBorderColor = if (isCodeValid) PlinBlue else ActivationFieldBorder,
                unfocusedBorderColor = ActivationFieldBorder,
                focusedTextColor = Color(0xFF2A2433),
                unfocusedTextColor = Color(0xFF2A2433),
                focusedPlaceholderColor = Color(0xFF8F889A),
                unfocusedPlaceholderColor = Color(0xFF8F889A),
                focusedTrailingIconColor = PlinBlue,
                unfocusedTrailingIconColor = PlinBlue,
                cursorColor = YapePurple
            )
        )

        Text(
            text = stringResource(R.string.activation_code_format_hint),
            color = supportingColor,
            fontSize = if (compact) 11.sp else 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

private fun openSupport(context: android.content.Context) {
    val phoneNumber = "51983450723"
    val message = context.getString(R.string.activation_support_message)
    val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}
