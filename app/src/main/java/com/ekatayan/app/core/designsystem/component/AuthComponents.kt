package com.ekatayan.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.AppLocaleManager

val AuthActionBlue = Color(0xFF2398EB)
val AuthLinkBlue = Color(0xFF006FCF)
val AuthContentWidth = 303.dp

private val BrandGreen = Color(0xFF126527)
private val BrandBlue = Color(0xFF0A7B9C)
private val AuthFieldBorder = Color.Black.copy(alpha = 0.4f)
private val AuthSocialBorder = Color(0xFFF5E8E8)

@Composable
fun AuthBackdrop() {
    Image(
        painter = painterResource(R.drawable.signup_background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        // This layer is measured only from the stable auth root. IME handling belongs
        // exclusively to the sibling form container in LoginScreen/SignUpScreen.
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
fun AuthHeader(title: String, subtitle: String) {
    Image(
        painter = painterResource(R.drawable.signup_logo),
        contentDescription = stringResource(R.string.signup_brand),
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(width = 123.dp, height = 150.dp),
    )
    Spacer(Modifier.height(6.dp))
    AuthBrandLockup()
    Spacer(Modifier.height(14.dp))
    Text(
        text = title,
        color = Color.Black,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp,
    )
    Spacer(Modifier.height(3.dp))
    Text(
        text = subtitle,
        color = Color.Black.copy(alpha = 0.75f),
        fontSize = 13.sp,
        lineHeight = 19.sp,
    )
    Spacer(Modifier.height(16.dp))
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    @DrawableRes leadingIcon: Int,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityClick: (() -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (isPassword && !isPasswordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, AuthFieldBorder, RoundedCornerShape(9.dp))
                    .padding(start = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 11.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Black.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                    innerTextField()
                }
                if (isPassword && onPasswordVisibilityClick != null) {
                    Image(
                        painter = painterResource(R.drawable.signup_eye),
                        contentDescription = stringResource(
                            if (isPasswordVisible) R.string.signup_hide_password
                            else R.string.signup_show_password,
                        ),
                        modifier = Modifier
                            .size(42.dp)
                            .padding(start = 10.dp, top = 13.dp, end = 14.dp, bottom = 13.dp)
                            .alpha(if (isPasswordVisible) 1f else 0.72f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onPasswordVisibilityClick,
                            ),
                    )
                } else {
                    Spacer(Modifier.width(14.dp))
                }
            }
        },
    )
}

@Composable
fun AuthActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(2.dp, RoundedCornerShape(9.dp))
            .clip(RoundedCornerShape(9.dp))
            .background(AuthActionBlue)
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.28f),
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun AuthOrDivider() {
    Row(
        modifier = Modifier
            .widthIn(max = 281.dp)
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(Color.Black.copy(alpha = 0.45f)))
        Text(
            text = stringResource(R.string.signup_or),
            color = Color.Black.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp),
        )
        Box(Modifier.weight(1f).height(1.dp).background(Color.Black.copy(alpha = 0.45f)))
    }
}

@Composable
fun AuthSocialButton(text: String, @DrawableRes icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .widthIn(max = AuthContentWidth)
            .fillMaxWidth()
            .height(46.dp)
            .shadow(2.dp, RoundedCornerShape(9.dp))
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White)
            .border(1.dp, AuthSocialBorder, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = text,
            color = Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
fun AuthLanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        AppLocaleManager.ENGLISH to R.string.language_english,
        AppLocaleManager.SINHALA to R.string.language_sinhala,
        AppLocaleManager.TAMIL to R.string.language_tamil,
    )
    val selectedLabel = options.firstOrNull { it.first == selectedLanguage }?.second
        ?: R.string.language_english

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(50.dp), ambientColor = Color.Black.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .clickable(enabled = onLanguageSelected != null) { expanded = true }
                .padding(horizontal = 5.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.signup_globe),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(selectedLabel),
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp),
            )
            Image(
                painter = painterResource(R.drawable.signup_chevron),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 7.dp, end = 1.dp)
                    .size(width = 16.dp, height = 7.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White,
        ) {
            options.forEach { (languageCode, label) ->
                DropdownMenuItem(
                    text = { Text(stringResource(label), color = Color.Black) },
                    onClick = {
                        expanded = false
                        if (languageCode != selectedLanguage) onLanguageSelected?.invoke(languageCode)
                    },
                )
            }
        }
    }
}

@Composable
fun AuthBrandLockup() {
    val localizedTagline = stringResource(R.string.signup_tagline)
    Text(
        text = buildAnnotatedString {
            append("Ekata")
            withStyle(SpanStyle(color = BrandGreen)) { append("Yan") }
        },
        color = Color.Black,
        fontFamily = FontFamily.Serif,
        fontSize = 36.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
    )
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = BrandBlue)) { append(localizedTagline.take(2)) }
            append(localizedTagline.drop(2))
        },
        color = Color.Black,
        fontFamily = FontFamily.Serif,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
    )
}
