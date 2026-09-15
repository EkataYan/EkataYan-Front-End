package com.ekatayan.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.EkataComponentSize
import com.ekatayan.app.core.designsystem.theme.EkataIconSize
import com.ekatayan.app.core.designsystem.theme.EkataNavigationBackground
import com.ekatayan.app.core.designsystem.theme.EkataElevation
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.core.designsystem.theme.EkataRadius
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary

enum class AppBottomNavItem(val label: String, @param:DrawableRes val iconRes: Int) {
    HOME("Home", R.drawable.nav_home),
    TRIPS("Trips", R.drawable.nav_trips),
    PLANNER("AI Planner", R.drawable.nav_ai_planner),
    EXPENSES("Expenses", R.drawable.nav_expenses),
    PROFILE("Profile", R.drawable.nav_profile),
}

@Composable
fun AppBottomNavigation(
    selectedItem: AppBottomNavItem,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current).toFloat()

    val callbacks = listOf(onHomeClick, onTripsClick, onPlannerClick, onExpensesClick, onProfileClick)
    // Keep the parameter while callers migrate, but use one geometry everywhere.
    val outerPadding = EkataSpacing.sm
    val barHeight = EkataComponentSize.bottomNavigationCompactHeight
    val barRadius = EkataRadius.extraLarge
    val barElevation = EkataElevation.medium
    val itemSize = EkataComponentSize.bottomNavigationCompactIconContainer
    Row(
        modifier = modifier
            // adjustResize shortens the Compose window while the keyboard is open.
            // Offset by that lost height so the bar stays at the physical bottom
            // of the screen, underneath the IME, instead of jumping above it.
            .graphicsLayer { translationY = imeBottom }
            .padding(start = outerPadding, end = outerPadding, bottom = outerPadding)
            .fillMaxWidth()
            .height(barHeight)
            .shadow(barElevation, RoundedCornerShape(barRadius))
            .background(EkataNavigationBackground, RoundedCornerShape(barRadius))
            .padding(horizontal = 7.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppBottomNavItem.entries.forEachIndexed { index, item ->
            val selected = selectedItem == item
            Column(
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).clickable(onClick = callbacks[index]),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.size(itemSize).background(
                        if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                        CircleShape,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier
                            .size(EkataIconSize.medium)
                            .graphicsLayer { alpha = if (selected) 1f else 0.62f },
                    )
                }
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    ),
                    color = if (selected) MaterialTheme.colorScheme.primary else EkataTextSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}
