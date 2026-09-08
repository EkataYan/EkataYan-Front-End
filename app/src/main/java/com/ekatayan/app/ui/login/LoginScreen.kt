package com.ekatayan.app.ui.login

import com.ekatayan.app.viewmodel.LoginUiState

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AuthActionButton
import com.ekatayan.app.core.designsystem.component.AuthBackdrop
import com.ekatayan.app.core.designsystem.component.AuthContentWidth
import com.ekatayan.app.core.designsystem.component.AuthHeader
import com.ekatayan.app.core.designsystem.component.AuthLinkBlue
import com.ekatayan.app.core.designsystem.component.AuthOrDivider
import com.ekatayan.app.core.designsystem.component.AuthSocialButton
import com.ekatayan.app.core.designsystem.component.AuthTextField

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLogInClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onSignUpClick: () -> Unit,
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
                        title = stringResource(R.string.login_title),
                        subtitle = stringResource(R.string.login_subtitle),
                    )
                }
            }
            item(contentType = "form") {
                LoginForm(
                    uiState = uiState,
                    onEmailChange = onEmailChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityClick = onPasswordVisibilityClick,
                    onForgotPasswordClick = onForgotPasswordClick,
                    onLogInClick = onLogInClick,
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
            item(contentType = "signup_space") { Spacer(Modifier.height(12.dp)) }
            item(contentType = "signup") { SignUpPrompt(onSignUpClick = onSignUpClick) }
        }
    }
}

@Composable
private fun LoginForm(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLogInClick: () -> Unit,
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
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.signup_password),
                leadingIcon = R.drawable.signup_lock,
                isPassword = true,
                isPasswordVisible = uiState.isPasswordVisible,
                onPasswordVisibilityClick = onPasswordVisibilityClick,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .padding(top = 8.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = stringResource(R.string.login_forgot_password),
                color = AuthLinkBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.clickable(onClick = onForgotPasswordClick),
            )
        }
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            AuthActionButton(
                text = stringResource(R.string.login_action),
                onClick = onLogInClick,
            )
        }
    }
}

@Composable
private fun SignUpPrompt(onSignUpClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.login_new_account),
            color = Color.Black,
            fontSize = 12.sp,
        )
        Text(
            text = stringResource(R.string.login_signup),
            color = AuthLinkBlue,
            fontSize = 12.sp,
            modifier = Modifier.clickable(onClick = onSignUpClick),
        )
    }
}
