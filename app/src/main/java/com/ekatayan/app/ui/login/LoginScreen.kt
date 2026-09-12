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
import androidx.compose.material3.CircularProgressIndicator
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
        uiState.error?.let { error ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(when (error) {
                    com.ekatayan.app.data.repository.AuthenticationFailure.INVALID_CREDENTIALS -> R.string.login_error_invalid_credentials
                    com.ekatayan.app.data.repository.AuthenticationFailure.NETWORK -> R.string.login_error_network
                    com.ekatayan.app.data.repository.AuthenticationFailure.CONFIGURATION -> R.string.login_error_configuration
                    com.ekatayan.app.data.repository.AuthenticationFailure.GOOGLE_CANCELED -> R.string.auth_google_canceled
                    com.ekatayan.app.data.repository.AuthenticationFailure.GOOGLE_NO_CREDENTIAL -> R.string.auth_google_no_credential
                    com.ekatayan.app.data.repository.AuthenticationFailure.GOOGLE_INVALID_TOKEN -> R.string.auth_google_invalid_token
                    else -> R.string.login_error_generic
                }),
                color = Color(0xFFB3261E),
                fontSize = 11.sp,
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
                enabled = !uiState.isLoading,
            )
        }
        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.height(20.dp), strokeWidth = 2.dp)
            }
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
