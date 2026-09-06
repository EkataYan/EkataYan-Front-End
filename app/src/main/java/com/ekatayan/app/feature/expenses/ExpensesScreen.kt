package com.ekatayan.app.feature.expenses

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding
import com.ekatayan.app.R
import java.text.NumberFormat
import java.util.Locale

private val Navy = Color(0xFF13234B)
private val Page = Color(0xFFFAFBFD)

@Composable
fun ExpensesScreen(
    uiState: ExpensesUiState,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    hasUnreadNotifications: Boolean,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(), containerColor = Page,
        bottomBar = {
            AppBottomNavigation(AppBottomNavItem.EXPENSES, onHomeClick, onTripsClick, onPlannerClick, {}, onProfileClick)
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Header(
                    onNotificationClick = onNotificationClick,
                    onSettingsClick = onSettingsClick,
                    hasUnreadNotifications = hasUnreadNotifications,
                    actionOffset = HeaderActionsTopPadding - padding.calculateTopPadding() - 12.dp,
                )
            }
            item { BudgetCard(uiState.budget) }
            item { SpendingCard(uiState.categories, uiState.budget.totalSpent) }
            item { QuickActions(uiState.quickActions) }
            item { RecentHeader() }
            items(uiState.recentExpenses, key = { it.titleRes }) { ExpenseRow(it) }
        }
    }
}

@Composable private fun Header(onNotificationClick: () -> Unit, onSettingsClick: () -> Unit, hasUnreadNotifications: Boolean, actionOffset: Dp) = Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Icon(Icons.Default.AccountBalanceWallet, null, tint = Color.Black, modifier = Modifier.size(27.dp))
    Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.expenses_title), fontSize = 29.sp, fontWeight = FontWeight.Medium)
    Spacer(Modifier.weight(1f)); HeaderActions(onNotificationClick = onNotificationClick, onSettingsClick = onSettingsClick, hasUnreadNotifications = hasUnreadNotifications, modifier = Modifier.offset(y = actionOffset))
}

@Composable private fun BudgetCard(data: BudgetSummary) = CardSection {
    Title(stringResource(R.string.expenses_budget_overview)); Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth()) {
        Metric(stringResource(R.string.expenses_total_budget), data.totalBudget, Icons.Default.AccountBalanceWallet, Color(0xFF367CF5))
        Metric(stringResource(R.string.expenses_total_spent), data.totalSpent, Icons.Default.Payments, Color(0xFF12B765))
        Metric(stringResource(R.string.expenses_remaining), data.remaining, Icons.Default.AccountBalanceWallet, Color(0xFF8651DB))
    }
    Spacer(Modifier.height(18.dp))
    LinearProgressIndicator({ data.usedFraction }, Modifier.fillMaxWidth().height(11.dp).clip(CircleShape), Color(0xFF1DB760), Color(0xFFE7EBF1))
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.expenses_budget_used, (data.usedFraction * 100).toInt()), color = Color(0xFF0BAA50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF367CF5), modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
        Text(stringResource(R.string.expenses_days_remaining, data.daysRemaining), color = Color.DarkGray, fontSize = 11.sp)
    }
}

@Composable private fun RowScope.Metric(label: String, amount: Long, icon: ImageVector, tint: Color) = Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
    Box(Modifier.size(38.dp).background(tint.copy(.12f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp)) }
    Spacer(Modifier.height(6.dp)); Text(label, color = Color.Gray, fontSize = 10.sp)
    Text(lkr(amount), color = tint, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
}

@Composable private fun SpendingCard(categories: List<ExpenseCategory>, total: Long) = CardSection {
    Title(stringResource(R.string.expenses_spending_by_category)); Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                var start = -90f
                categories.forEach { category ->
                    val sweep = category.percentage * 3.6f
                    drawArc(category.color, start, sweep, false, Offset(8.dp.toPx(), 8.dp.toPx()), Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()), style = Stroke(22.dp.toPx()))
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LKR", fontSize = 10.sp, color = Color.Gray); Text(number(total), fontWeight = FontWeight.Bold, color = Navy, fontSize = 17.sp); Text(stringResource(R.string.expenses_total_spent_label), fontSize = 10.sp, color = Color.Gray)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { categories.forEach { Legend(it) } }
    }
}

@Composable private fun Legend(data: ExpenseCategory) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(8.dp).background(data.color, CircleShape)); Spacer(Modifier.width(6.dp))
    Text(stringResource(data.nameRes), Modifier.weight(1f), fontSize = 9.sp, color = Navy, maxLines = 1)
    Text(number(data.amount), fontSize = 9.sp, color = Navy); Spacer(Modifier.width(6.dp))
    Text("${data.percentage}%", fontSize = 9.sp, color = data.color, fontWeight = FontWeight.Bold)
}

@Composable private fun QuickActions(actions: List<QuickAction>) = CardSection {
    Title(stringResource(R.string.expenses_quick_actions)); Spacer(Modifier.height(8.dp))
    actions.forEach { action ->
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(10.dp)).background(action.tint.copy(.09f)).clickable { }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(28.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) { Icon(action.icon, null, tint = action.tint, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(10.dp)); Text(stringResource(action.labelRes), Modifier.weight(1f), color = Navy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.ArrowForwardIos, null, tint = action.tint, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable private fun RecentHeader() = Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Title(stringResource(R.string.expenses_recent_expenses)); Spacer(Modifier.weight(1f)); Text(stringResource(R.string.expenses_view_all), color = Color(0xFF1677F2), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
}

@Composable private fun ExpenseRow(expense: Expense) = Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(expense.tint.copy(.055f), RoundedCornerShape(12.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically,
) {
    Box(Modifier.size(40.dp).background(expense.tint.copy(.13f), CircleShape), contentAlignment = Alignment.Center) { Icon(expense.icon, null, tint = expense.tint, modifier = Modifier.size(21.dp)) }
    Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(stringResource(expense.titleRes), color = Navy, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(stringResource(expense.categoryRes), color = Color.Gray, fontSize = 10.sp) }
    expense.participantCount?.let { Box(Modifier.size(27.dp).background(expense.tint.copy(.16f), CircleShape), contentAlignment = Alignment.Center) { Text(if (it > 1) "+$it" else "1", color = expense.tint, fontSize = 9.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(8.dp)) }
    Column(horizontalAlignment = Alignment.End) { Text(lkr(expense.amount), color = expense.tint, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text(stringResource(expense.dateTimeRes), color = Color.Gray, fontSize = 9.sp) }
}

@Composable private fun CardSection(content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(14.dp)).background(Color.White, RoundedCornerShape(14.dp)).padding(16.dp), content = content)
@Composable private fun Title(text: String) = Text(text, color = Navy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
private fun number(amount: Long) = NumberFormat.getIntegerInstance(Locale.US).format(amount)
private fun lkr(amount: Long) = "LKR ${number(amount)}"
