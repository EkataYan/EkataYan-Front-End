package com.ekatayan.app.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.EkataCard
import com.ekatayan.app.core.designsystem.component.EkataErrorState
import com.ekatayan.app.core.designsystem.component.EkataLoadingState
import com.ekatayan.app.core.designsystem.component.EkataPrimaryButton
import com.ekatayan.app.core.designsystem.component.EkataSectionHeading
import com.ekatayan.app.core.designsystem.component.EkataSecondaryButton
import com.ekatayan.app.core.designsystem.component.EkataSuccessState
import com.ekatayan.app.core.designsystem.component.EkataTextField
import com.ekatayan.app.core.designsystem.component.EkataTopAppBar
import com.ekatayan.app.core.designsystem.theme.EkataIconSize
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.data.repository.ProfileFailure
import com.ekatayan.app.viewmodel.EditProfileUiState
import com.ekatayan.app.viewmodel.EditProfileValidationError

private val EditProfileAvatarSize = 96.dp

@Composable
fun EditProfileScreen(
    state: EditProfileUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onHomeCityChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onInterestsChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDiscardDialog by remember { mutableStateOf(false) }
    val requestBack = {
        if (state.isDirty) showDiscardDialog = true else onBackClick()
    }
    BackHandler(enabled = state.isDirty) { showDiscardDialog = true }

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EkataTopAppBar(
                title = stringResource(R.string.edit_profile_title),
                navigationIcon = {
                    IconButton(onClick = requestBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.edit_profile_back),
                        )
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        when {
            state.isLoading -> {
                EkataLoadingState(
                    message = stringResource(R.string.edit_profile_loading),
                    modifier = Modifier.padding(scaffoldPadding).padding(EkataSpacing.md),
                )
            }
            state.error != null && state.name.isBlank() -> {
                EkataErrorState(
                    title = stringResource(R.string.edit_profile_load_error_title),
                    message = profileErrorMessage(state.error),
                    actionLabel = stringResource(R.string.profile_retry),
                    onAction = onRetry,
                    modifier = Modifier.padding(scaffoldPadding).padding(EkataSpacing.md),
                )
            }
            else -> {
                EditProfileForm(
                    state = state,
                    onSave = onSave,
                    onNameChange = onNameChange,
                    onBioChange = onBioChange,
                    onHomeCityChange = onHomeCityChange,
                    onLanguageChange = onLanguageChange,
                    onInterestsChange = onInterestsChange,
                    onPhoneChange = onPhoneChange,
                    contentPadding = PaddingValues(
                        start = EkataSpacing.md,
                        top = EkataSpacing.md,
                        end = EkataSpacing.md,
                        bottom = EkataSpacing.xl,
                    ),
                    modifier = Modifier.padding(scaffoldPadding),
                )
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(R.string.edit_profile_discard_title)) },
            text = { Text(stringResource(R.string.edit_profile_discard_message)) },
            confirmButton = {
                TextButton(onClick = { showDiscardDialog = false; onBackClick() }) {
                    Text(stringResource(R.string.edit_profile_discard_action), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.edit_profile_keep_editing))
                }
            },
        )
    }
}

@Composable
private fun EditProfileForm(
    state: EditProfileUiState,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onHomeCityChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onInterestsChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().imePadding(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(EkataSpacing.lg),
    ) {
        item { EditProfileAvatar() }
        item {
            EkataSectionHeading(
                title = stringResource(R.string.edit_profile_personal_information),
                supportingText = stringResource(R.string.edit_profile_personal_information_subtitle),
            )
        }
        item {
            EkataCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(EkataSpacing.md)) {
                    EkataTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = stringResource(R.string.edit_profile_name),
                        leadingIcon = Icons.Outlined.Badge,
                        isError = state.validationError in setOf(
                            EditProfileValidationError.NAME_REQUIRED,
                            EditProfileValidationError.NAME_TOO_LONG,
                        ),
                        supportingText = validationMessage(state.validationError, EditProfileValidationError.NAME_REQUIRED, EditProfileValidationError.NAME_TOO_LONG),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    )
                    EkataTextField(
                        value = state.phone,
                        onValueChange = onPhoneChange,
                        label = stringResource(R.string.edit_profile_phone),
                        leadingIcon = Icons.Outlined.Phone,
                        isError = state.validationError == EditProfileValidationError.PHONE_INVALID,
                        supportingText = validationMessage(state.validationError, EditProfileValidationError.PHONE_INVALID),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    )
                    EkataTextField(
                        value = state.email,
                        onValueChange = {},
                        label = stringResource(R.string.edit_profile_account_email),
                        readOnly = true,
                        leadingIcon = Icons.Outlined.Email,
                        supportingText = stringResource(R.string.edit_profile_email_read_only),
                    )
                    EkataTextField(
                        value = state.bio,
                        onValueChange = onBioChange,
                        label = stringResource(R.string.edit_profile_bio),
                        leadingIcon = Icons.AutoMirrored.Outlined.ShortText,
                        singleLine = false,
                        minLines = 3,
                        isError = state.validationError == EditProfileValidationError.BIO_TOO_LONG,
                        supportingText = validationMessage(state.validationError, EditProfileValidationError.BIO_TOO_LONG),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    )
                    EkataTextField(
                        value = state.homeCity,
                        onValueChange = onHomeCityChange,
                        label = stringResource(R.string.edit_profile_city),
                        leadingIcon = Icons.Outlined.Home,
                        isError = state.validationError == EditProfileValidationError.CITY_TOO_LONG,
                        supportingText = validationMessage(state.validationError, EditProfileValidationError.CITY_TOO_LONG),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    )
                    EkataTextField(
                        value = state.language,
                        onValueChange = onLanguageChange,
                        label = stringResource(R.string.edit_profile_language),
                        leadingIcon = Icons.Outlined.Language,
                        isError = state.validationError == EditProfileValidationError.LANGUAGE_INVALID,
                        supportingText = validationMessage(state.validationError, EditProfileValidationError.LANGUAGE_INVALID),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                    EkataTextField(
                        value = state.interests,
                        onValueChange = onInterestsChange,
                        label = stringResource(R.string.edit_profile_interests),
                        leadingIcon = Icons.Outlined.TravelExplore,
                        isError = state.validationError == EditProfileValidationError.INTERESTS_INVALID,
                        supportingText = validationMessage(state.validationError, EditProfileValidationError.INTERESTS_INVALID)
                            ?: stringResource(R.string.edit_profile_interests_hint),
                    )
                }
            }
        }
        if (state.error != null) {
            item {
                EkataErrorState(
                    title = stringResource(R.string.edit_profile_save_error_title),
                    message = profileErrorMessage(state.error),
                )
            }
        }
        if (state.saved) {
            item {
                EkataSuccessState(
                    title = stringResource(R.string.edit_profile_saved_title),
                    message = stringResource(R.string.edit_profile_saved_message),
                )
            }
        }
        item {
            EkataPrimaryButton(
                text = stringResource(if (state.isSaving) R.string.edit_profile_saving else R.string.edit_profile_save),
                onClick = onSave,
                enabled = state.isDirty && !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun EditProfileAvatar() {
    EkataCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(EditProfileAvatarSize),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Image(
                    painter = painterResource(R.drawable.profile_avatar_placeholder),
                    contentDescription = stringResource(R.string.profile_avatar_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.clip(CircleShape),
                )
            }
            Spacer(Modifier.width(EkataSpacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(EkataSpacing.xs)) {
                Text(
                    text = stringResource(R.string.edit_profile_photo_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.edit_profile_photo_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                EkataSecondaryButton(
                    text = stringResource(R.string.edit_profile_change_photo),
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}

@Composable
private fun validationMessage(
    actual: EditProfileValidationError?,
    vararg expected: EditProfileValidationError,
): String? = if (actual !in expected) null else stringResource(
    when (actual) {
        EditProfileValidationError.NAME_REQUIRED -> R.string.edit_profile_name_required
        EditProfileValidationError.NAME_TOO_LONG -> R.string.edit_profile_name_too_long
        EditProfileValidationError.BIO_TOO_LONG -> R.string.edit_profile_bio_too_long
        EditProfileValidationError.CITY_TOO_LONG -> R.string.edit_profile_city_too_long
        EditProfileValidationError.LANGUAGE_INVALID -> R.string.edit_profile_language_invalid
        EditProfileValidationError.INTERESTS_INVALID -> R.string.edit_profile_interests_invalid
        EditProfileValidationError.PHONE_INVALID -> R.string.edit_profile_phone_invalid
        null -> R.string.edit_profile_invalid
    },
)

@Composable
private fun profileErrorMessage(error: ProfileFailure): String = stringResource(
    when (error) {
        ProfileFailure.AUTHENTICATION -> R.string.edit_profile_auth_error
        ProfileFailure.NETWORK -> R.string.edit_profile_network_error
        ProfileFailure.INVALID_RESPONSE -> R.string.edit_profile_validation_error
        ProfileFailure.FORBIDDEN -> R.string.edit_profile_forbidden_error
        else -> R.string.edit_profile_server_error
    },
)
