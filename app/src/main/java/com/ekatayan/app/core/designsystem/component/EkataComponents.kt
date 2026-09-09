package com.ekatayan.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ekatayan.app.core.designsystem.theme.EkataComponentSize
import com.ekatayan.app.core.designsystem.theme.EkataElevation
import com.ekatayan.app.core.designsystem.theme.EkataIconSize
import com.ekatayan.app.core.designsystem.theme.EkataImageScrim
import com.ekatayan.app.core.designsystem.theme.EkataOnImage
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.core.designsystem.theme.EkataSuccess
import androidx.compose.material3.MaterialTheme

@Composable
fun EkataPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = EkataComponentSize.buttonHeight),
        shape = MaterialTheme.shapes.medium,
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun EkataSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = EkataComponentSize.buttonHeight),
        shape = MaterialTheme.shapes.medium,
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun EkataTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.heightIn(min = EkataComponentSize.touchTarget)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun EkataQuickActionCard(
    label: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentIcon: ImageVector? = null,
    artwork: (@Composable BoxScope.() -> Unit)? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(EkataComponentSize.quickActionHeight),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = EkataElevation.none,
            pressedElevation = EkataElevation.none,
            focusedElevation = EkataElevation.none,
            hoveredElevation = EkataElevation.none,
            draggedElevation = EkataElevation.none,
            disabledElevation = EkataElevation.none,
        ),
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = EkataSpacing.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(EkataComponentSize.quickActionIconContainer)
                    .background(
                        if (artwork != null) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (artwork != null) {
                    artwork()
                } else {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(EkataIconSize.medium),
                        )
                    }
                    accentIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = EkataSuccess,
                            modifier = Modifier.align(Alignment.TopEnd).size(EkataComponentSize.quickActionAccentIcon),
                        )
                    }
                }
            }
            Spacer(Modifier.height(EkataSpacing.xxs))
            Box(
                modifier = Modifier.fillMaxWidth().height(EkataComponentSize.quickActionLabelHeight),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun EkataQuickActionCard(
    label: String,
    @DrawableRes artworkRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EkataQuickActionCard(
        label = label,
        icon = null,
        onClick = onClick,
        modifier = modifier,
        artwork = {
            Image(
                painter = painterResource(artworkRes),
                contentDescription = null,
                modifier = Modifier.size(EkataComponentSize.quickActionArtwork),
            )
        },
    )
}

@Composable
fun EkataPageIndicator(count: Int, selectedPage: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(EkataSpacing.xxs)) {
        repeat(count) { index ->
            Box(
                Modifier
                    .width(if (index == selectedPage) EkataComponentSize.pageIndicatorActiveWidth else EkataComponentSize.pageIndicator)
                    .height(EkataComponentSize.pageIndicator)
                    .background(
                        if (index == selectedPage) EkataOnImage else EkataOnImage.copy(alpha = 0.48f),
                        CircleShape,
                    ),
            )
        }
    }
}

@Composable
fun EkataImageTitle(text: String, modifier: Modifier = Modifier, maxLines: Int = 1) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            shadow = Shadow(
                color = EkataImageScrim.copy(alpha = 0.45f),
                offset = Offset(0f, 1f),
                blurRadius = 4f,
            ),
        ),
        color = EkataOnImage,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun EkataImageSupportingText(text: String, modifier: Modifier = Modifier, maxLines: Int = 2) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
            shadow = Shadow(
                color = EkataImageScrim.copy(alpha = 0.4f),
                offset = Offset(0f, 1f),
                blurRadius = 3f,
            ),
        ),
        color = EkataOnImage.copy(alpha = 0.92f),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun EkataCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low)
    if (onClick == null) {
        Card(modifier = modifier, shape = MaterialTheme.shapes.large, colors = colors, elevation = elevation) {
            Column(Modifier.padding(EkataSpacing.md), content = content)
        }
    } else {
        Card(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.large, colors = colors, elevation = elevation) {
            Column(Modifier.padding(EkataSpacing.md), content = content)
        }
    }
}

@Composable
fun EkataImageCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low),
    ) {
        Box(Modifier.fillMaxSize(), content = content)
    }
}

@Composable
fun EkataTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth().heightIn(min = EkataComponentSize.inputMinHeight),
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
fun EkataSectionHeading(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    action: (@Composable (() -> Unit))? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(EkataSpacing.xxs)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            supportingText?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        action?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EkataTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge)
        },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
fun EkataLoadingState(message: String, modifier: Modifier = Modifier) {
    EkataMessageState(message = message, modifier = modifier, progress = true)
}

@Composable
fun EkataEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) = EkataMessageState(title, message, modifier, icon, actionLabel, onAction)

@Composable
fun EkataErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) = EkataMessageState(title, message, modifier, actionLabel = actionLabel, onAction = onAction, error = true)

@Composable
fun EkataSuccessState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) = EkataMessageState(title, message, modifier, success = true)

@Composable
private fun EkataMessageState(
    title: String? = null,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    progress: Boolean = false,
    error: Boolean = false,
    success: Boolean = false,
) {
    val containerColor = when {
        error -> MaterialTheme.colorScheme.errorContainer
        success -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(
            Modifier.fillMaxWidth().padding(EkataSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EkataSpacing.sm),
        ) {
            if (progress) CircularProgressIndicator(Modifier.size(EkataIconSize.large), strokeWidth = 3.dp)
            icon?.let { Icon(it, contentDescription = null, modifier = Modifier.size(EkataIconSize.hero)) }
            title?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (actionLabel != null && onAction != null) EkataPrimaryButton(actionLabel, onAction)
        }
    }
}
