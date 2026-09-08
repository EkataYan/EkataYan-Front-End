package com.ekatayan.app.ui.signup

import com.ekatayan.app.viewmodel.SignUpUiState

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AuthActionButton
import com.ekatayan.app.core.designsystem.component.AuthBackdrop
import com.ekatayan.app.core.designsystem.component.AuthContentWidth
import com.ekatayan.app.core.designsystem.component.AuthHeader
import com.ekatayan.app.core.designsystem.component.AuthOrDivider
import com.ekatayan.app.core.designsystem.component.AuthSocialButton
import com.ekatayan.app.core.designsystem.component.AuthTextField

private val SkyBlue = Color(0xFF2398EB)
private val LinkBlue = Color(0xFF006FCF)
private val FieldBorder = Color.Black.copy(alpha = 0.4f)

@Composable
fun SignUpScreen(
    uiState: SignUpUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onConfirmPasswordVisibilityClick: () -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AuthBackdrop()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding(),
            contentPadding = PaddingValues(top = 52.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(contentType = "header") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AuthHeader(
                        title = stringResource(R.string.signup_title),
                        subtitle = stringResource(R.string.signup_subtitle),
                    )
                }
            }
            item(contentType = "form") {
                SignUpForm(
                    uiState = uiState,
                    onNameChange = onNameChange,
                    onEmailChange = onEmailChange,
                    onPhoneNumberChange = onPhoneNumberChange,
                    onPasswordChange = onPasswordChange,
                    onConfirmPasswordChange = onConfirmPasswordChange,
                    onPasswordVisibilityClick = onPasswordVisibilityClick,
                    onConfirmPasswordVisibilityClick = onConfirmPasswordVisibilityClick,
                    onTermsAcceptedChange = onTermsAcceptedChange,
                    onSignUpClick = onSignUpClick,
                )
            }
            item(contentType = "form_divider_space") { Spacer(Modifier.height(4.dp)) }
            item(contentType = "divider") { AuthOrDivider() }
            item(contentType = "google") {
                AuthSocialButton(
                    text = stringResource(R.string.signup_google),
                    icon = R.drawable.signup_google,
                    onClick = onGoogleClick,
                )
            }
            item(contentType = "social_space") { Spacer(Modifier.height(11.dp)) }
            item(contentType = "apple") {
                AuthSocialButton(
                    text = stringResource(R.string.signup_apple),
                    icon = R.drawable.signup_apple,
                    onClick = onAppleClick,
                )
            }
            item(contentType = "login_space") { Spacer(Modifier.height(12.dp)) }
            item(contentType = "login") { LoginPrompt(onLoginClick = onLoginClick) }
        }
    }
}

@Composable
private fun SignUpForm(
    uiState: SignUpUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onConfirmPasswordVisibilityClick: () -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onSignUpClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .widthIn(max = AuthContentWidth)
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE9E9E9), RoundedCornerShape(15.dp))
            .background(Color.White, RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp, vertical = 18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            AuthTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                placeholder = stringResource(R.string.signup_name),
                leadingIcon = R.drawable.signup_profile,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            AuthTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.signup_email),
                leadingIcon = R.drawable.signup_email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            AuthTextField(
                value = uiState.phoneNumber,
                onValueChange = onPhoneNumberChange,
                placeholder = stringResource(R.string.signup_phone),
                leadingIcon = R.drawable.signup_phone,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                ),
            )
            AuthTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.signup_password),
                leadingIcon = R.drawable.signup_lock,
                isPassword = true,
                isPasswordVisible = uiState.isPasswordVisible,
                onPasswordVisibilityClick = onPasswordVisibilityClick,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
            )
            AuthTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = stringResource(R.string.signup_confirm_password),
                leadingIcon = R.drawable.signup_lock,
                isPassword = true,
                isPasswordVisible = uiState.isConfirmPasswordVisible,
                onPasswordVisibilityClick = onConfirmPasswordVisibilityClick,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            TermsRow(
                checked = uiState.hasAcceptedTerms,
                onCheckedChange = onTermsAcceptedChange,
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            AuthActionButton(
                text = stringResource(R.string.signup_action),
                onClick = onSignUpClick,
            )
        }
    }
}

@Composable
private fun TermsRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .heightIn(min = 32.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (checked) SkyBlue else Color.White)
                .border(1.dp, if (checked) SkyBlue else FieldBorder, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = termsText(),
            color = Color.Black,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
        )
    }
}

@Composable
private fun termsText(): AnnotatedString = buildAnnotatedString {
    append(stringResource(R.string.signup_terms_prefix))
    withStyle(SpanStyle(color = LinkBlue)) { append(stringResource(R.string.signup_terms)) }
    append(stringResource(R.string.signup_terms_joiner))
    withStyle(SpanStyle(color = LinkBlue)) { append(stringResource(R.string.signup_privacy)) }
}

@Composable
private fun LoginPrompt(onLoginClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.signup_existing),
            color = Color.Black,
            fontSize = 12.sp,
        )
        Text(
            text = stringResource(R.string.signup_login),
            color = LinkBlue,
            fontSize = 12.sp,
            modifier = Modifier.clickable(onClick = onLoginClick),
        )
    }
}
