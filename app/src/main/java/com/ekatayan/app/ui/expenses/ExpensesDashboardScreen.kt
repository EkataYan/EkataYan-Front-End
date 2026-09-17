package com.ekatayan.app.ui.expenses

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.*
import com.ekatayan.app.core.designsystem.theme.*
import com.ekatayan.app.data.model.ExpenseCategoryTotal
import com.ekatayan.app.data.model.ExpensesData
import com.ekatayan.app.data.remote.api.ExpenseBalanceDto
import com.ekatayan.app.data.remote.api.ExpenseDto
import com.ekatayan.app.data.remote.api.ExpenseDebtDto
import com.ekatayan.app.data.remote.api.SettlementDto
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

private val AccommodationColor = EkataBlue
private val TransportColor = EkataSuccess
private val FoodColor = Color(0xFFE07A32)
private val ActivitiesColor = Color(0xFF8056C7)
private val ShoppingColor = Color(0xFF168C91)
private val OtherColor = EkataOutlineStrong

@Composable
fun ExpensesScreen(
    state: ExpensesData,
    onSwitchTrip: () -> Unit,
    onRetry: () -> Unit,
    onSetBudget: (String) -> Unit,
    onAdd: (String) -> Unit,
    onViewAll: (String) -> Unit,
    onBalances: (String) -> Unit,
    onDetails: (String, String) -> Unit,
    onHome: () -> Unit,
    onTrips: () -> Unit,
    onPlanner: () -> Unit,
    onExpenses: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
    onNotifications: () -> Unit,
    hasUnread: Boolean,
) {
    var showBudget by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = EkataBackground,
        bottomBar = { AppBottomNavigation(AppBottomNavItem.EXPENSES, onHome, onTrips, onPlanner, onExpenses, onProfile) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = EkataSpacing.pageHorizontal, vertical = EkataSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(EkataSpacing.sm),
        ) {
            item { ExpensesHeader(onNotifications, onSettings, hasUnread) }
            when {
                state.loading -> item { EkataLoadingState(stringResource(R.string.expenses_loading)) }
                state.error != null -> item { EkataErrorState(stringResource(R.string.expenses_load_error), state.error, actionLabel = stringResource(R.string.retry), onAction = onRetry) }
                state.availableTrips.isEmpty() -> item { EkataEmptyState(stringResource(R.string.expenses_no_trips), stringResource(R.string.expenses_no_trips_message)) }
                else -> {
                    item { TripSelector(state, onSwitchTrip) }
                    item { BudgetOverview(state) { showBudget = true } }
                    item { SpendingByCategory(state.categories, state.totalSpent) }
                    item {
                        QuickActions(
                            onAdd = { state.tripId?.let(onAdd) },
                            onBalances = { state.tripId?.let(onBalances) },
                            onSetBudget = { showBudget = true },
                            onSettleUp = { state.tripId?.let(onBalances) },
                            canSetBudget = state.currentUserRole in setOf("owner", "admin"),
                        )
                    }
                    item { RecentHeader(onViewAll = { state.tripId?.let(onViewAll) }, enabled = state.recentExpenses.isNotEmpty()) }
                    if (state.recentExpenses.isEmpty()) {
                        item { ExpenseEmptyState { state.tripId?.let(onAdd) } }
                    } else {
                        items(state.recentExpenses.take(3), key = ExpenseDto::id) { expense ->
                            ExpenseCard(expense) { state.tripId?.let { onDetails(it, expense.id) } }
                        }
                    }
                }
            }
        }
    }
    if(showBudget) BudgetDialog(state,onSetBudget){showBudget=false}
}

@Composable
internal fun ExpensesHeader(onNotifications: () -> Unit, onSettings: () -> Unit, hasUnread: Boolean) {
    Row(Modifier.fillMaxWidth().height(EkataComponentSize.pageHeaderTop), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AccountBalanceWallet, null, tint = EkataTextPrimary, modifier = Modifier.size(EkataIconSize.medium))
        Spacer(Modifier.width(EkataSpacing.xs))
        Text(stringResource(R.string.expenses_title), style = MaterialTheme.typography.headlineMedium, color = EkataTextPrimary)
        Spacer(Modifier.weight(1f))
        HeaderActions(onNotifications, onSettings, hasUnread)
    }
}

@Composable
private fun TripSelector(state: ExpensesData, onSwitchTrip: () -> Unit) {
    Surface(
            modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).clickable(onClick = onSwitchTrip),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = EkataElevation.low,
            border = androidx.compose.foundation.BorderStroke(1.dp, EkataOutline),
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(38.dp).background(EkataLightBlue, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Luggage, null, tint = EkataBlue, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.expenses_selected_trip), style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary)
                    Text(state.tripName.ifBlank { stringResource(R.string.expenses_select_trip) }, style = MaterialTheme.typography.titleSmall, color = EkataTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Default.ExpandMore, null, tint = EkataBlue)
            }
        }
}

@Composable
private fun BudgetOverview(state: ExpensesData, onSetBudget: () -> Unit) = DashboardCard {
    SectionTitle(stringResource(R.string.expenses_budget_overview))
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth()) {
        BudgetMetric(stringResource(R.string.expenses_total_budget), state.totalBudget?.let(::money), Icons.Default.AccountBalanceWallet, EkataBlue, Modifier.weight(1f))
        BudgetMetric(stringResource(R.string.expenses_total_spent), money(state.totalSpent), Icons.Default.Payments, EkataSuccess, Modifier.weight(1f))
        val over=state.totalBudget?.let { state.totalSpent-it }?.takeIf { it>BigDecimal.ZERO }
        BudgetMetric(if(over!=null) stringResource(R.string.expenses_over_budget) else stringResource(R.string.expenses_remaining), over?.let(::money)?:state.totalBudget?.let { money(it-state.totalSpent) }, Icons.Default.Savings, if(over!=null) MaterialTheme.colorScheme.error else ActivitiesColor, Modifier.weight(1f))
    }
    Spacer(Modifier.height(14.dp))
    if (state.totalBudget != null) {
        val fraction = if (state.totalBudget.signum() == 0) 0f else state.totalSpent.divide(state.totalBudget, 4, java.math.RoundingMode.HALF_UP).toFloat()
        val percentage = if (state.totalBudget.signum() == 0) 0 else state.totalSpent.multiply(BigDecimal(100)).divide(state.totalBudget, 0, java.math.RoundingMode.HALF_UP).toInt()
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
            color = if (fraction > 1f) MaterialTheme.colorScheme.error else EkataSuccess,
            trackColor = EkataOutline.copy(alpha = .55f),
        )
        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.expenses_budget_used, percentage), style = MaterialTheme.typography.labelMedium, color = if (fraction > 1f) MaterialTheme.colorScheme.error else EkataSuccess)
            Spacer(Modifier.weight(1f))
            state.tripStatusText?.let {
                Icon(Icons.Default.CalendarMonth, null, tint = EkataBlue, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text(it, style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary)
            }
        }
    } else {
        Row(Modifier.fillMaxWidth().background(EkataLightBlue.copy(alpha = .55f), RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.expenses_budget_not_set_message), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary)
            TextButton(onSetBudget) { Text(stringResource(R.string.expenses_action_budget)) }
        }
    }
}

@Composable
private fun BudgetDialog(state:ExpensesData,onSave:(String)->Unit,onDismiss:()->Unit){
    var amount by remember(state.totalBudget){mutableStateOf(state.totalBudget?.toPlainString().orEmpty())}
    var submitted by remember{mutableStateOf(false)}
    LaunchedEffect(state.mutating,state.mutationError){if(submitted&&!state.mutating&&state.mutationError==null)onDismiss()}
    AlertDialog(
        onDismissRequest=onDismiss,
        title={Text(stringResource(if(state.totalBudget==null)R.string.expenses_set_trip_budget else R.string.expenses_edit_trip_budget))},
        text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text(state.tripName,style=MaterialTheme.typography.titleSmall,color=EkataTextSecondary)
            OutlinedTextField(amount,{amount=it.filter{c->c.isDigit()||c=='.'}},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.expenses_budget_amount))},prefix={Text("LKR ")},singleLine=true)
            state.mutationError?.let{Text(it,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)}
        }},
        confirmButton={Button({submitted=true;onSave(amount)},enabled=!state.mutating){Text(stringResource(if(state.totalBudget==null)R.string.expenses_save_budget else R.string.expenses_update_budget))}},
        dismissButton={TextButton(onDismiss){Text(stringResource(R.string.cancel))}},
    )
}

@Composable
private fun BudgetMetric(label: String, value: String?, icon: ImageVector, tint: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(36.dp).background(tint.copy(alpha = .12f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary, maxLines = 1)
        Text(value ?: stringResource(R.string.expenses_not_set), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SpendingByCategory(categories: List<ExpenseCategoryTotal>, total: BigDecimal) = DashboardCard {
    SectionTitle(stringResource(R.string.expenses_spending_by_category))
    Spacer(Modifier.height(12.dp))
    if (categories.isEmpty()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            EmptyDonut()
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.expenses_no_category_spending), style = MaterialTheme.typography.titleSmall, color = EkataTextPrimary)
                Text(stringResource(R.string.expenses_chart_empty_message), style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary)
            }
        }
    } else {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    var start = -90f
                    categories.forEach { category ->
                        val sweep = category.percentage * 3.6f
                        drawArc(categoryStyle(category.name).color, start, sweep, false, Offset(7.dp.toPx(), 7.dp.toPx()), Size(size.width - 14.dp.toPx(), size.height - 14.dp.toPx()), style = Stroke(18.dp.toPx()))
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LKR", style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary)
                    Text(number(total), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = EkataTextPrimary)
                    Text(stringResource(R.string.expenses_total_spent_label), style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                categories.filter { it.amount > BigDecimal.ZERO }.forEach { CategoryLegend(it) }
            }
        }
    }
}

@Composable
private fun EmptyDonut() = Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
    Canvas(Modifier.fillMaxSize()) { drawCircle(EkataOutline.copy(alpha = .65f), style = Stroke(16.dp.toPx())) }
    Text(money(BigDecimal.ZERO), style = MaterialTheme.typography.labelMedium, color = EkataTextSecondary)
}

@Composable
private fun CategoryLegend(category: ExpenseCategoryTotal) {
    val style = categoryStyle(category.name)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(style.color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(categoryLabel(category.name), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = EkataTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(number(category.amount), style = MaterialTheme.typography.labelSmall, color = EkataTextPrimary)
        Spacer(Modifier.width(5.dp))
        Text(stringResource(R.string.expenses_percentage_compact, category.percentage), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = style.color)
    }
}

@Composable
private fun QuickActions(onAdd: () -> Unit, onBalances: () -> Unit, onSetBudget: () -> Unit, onSettleUp: () -> Unit, canSetBudget:Boolean) = DashboardCard {
    SectionTitle(stringResource(R.string.expenses_quick_actions))
    Spacer(Modifier.height(7.dp))
    QuickActionRow(stringResource(R.string.expenses_action_add), Icons.Default.Add, EkataBlue, onAdd)
    QuickActionRow(stringResource(R.string.ui_balances), Icons.Default.Balance, EkataSuccess, onBalances)
    if(canSetBudget) QuickActionRow(stringResource(R.string.expenses_action_budget), Icons.Default.Savings, ActivitiesColor, onSetBudget)
    QuickActionRow(stringResource(R.string.expenses_settle_up), Icons.Default.Handshake, FoodColor, onSettleUp)
}

@Composable
private fun QuickActionRow(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp).clip(RoundedCornerShape(11.dp)).background(tint.copy(alpha = .085f)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(30.dp).background(MaterialTheme.colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(10.dp))
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = EkataTextPrimary)
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = tint, modifier = Modifier.size(13.dp))
    }
}

@Composable
private fun RecentHeader(onViewAll: () -> Unit, enabled: Boolean) {
    Row(Modifier.fillMaxWidth().padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        SectionTitle(stringResource(R.string.expenses_recent_expenses))
        Spacer(Modifier.weight(1f))
        TextButton(onViewAll, enabled = enabled) { Text(stringResource(R.string.expenses_view_all)) }
    }
}

@Composable
private fun ExpenseEmptyState(onAdd: () -> Unit) = Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, EkataOutline)) {
    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(46.dp).background(EkataLightBlue, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.ReceiptLong, null, tint = EkataBlue) }
        Spacer(Modifier.height(9.dp))
        Text(stringResource(R.string.expenses_empty), style = MaterialTheme.typography.titleMedium, color = EkataTextPrimary)
        Text(stringResource(R.string.expenses_empty_message), style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary)
        Button(onClick = onAdd, modifier = Modifier.padding(top = 10.dp)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.expenses_action_add)) }
    }
}

@Composable
internal fun ExpenseCard(expense: ExpenseDto, onClick: () -> Unit) {
    val style = categoryStyle(expense.category)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(13.dp),
        color = style.color.copy(alpha = .055f),
        border = androidx.compose.foundation.BorderStroke(1.dp, style.color.copy(alpha = .14f)),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(style.color.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) { Icon(style.icon, null, tint = style.color, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(expense.title, style = MaterialTheme.typography.titleSmall, color = EkataTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(categoryLabel(expense.category), style = MaterialTheme.typography.labelSmall, color = style.color)
                Text(stringResource(R.string.expenses_paid_by_split, expense.payer.displayName, expense.participants.size), style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(money(expense.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = style.color, maxLines = 1)
                Text(expense.expenseDate, style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary)
            }
        }
    }
}

@Composable
fun ExpenseHistoryScreen(state: ExpensesData, onBack: () -> Unit, onRetry: () -> Unit, onDetails:(String,String)->Unit) {
    var filter by remember { mutableStateOf("All") }
    Scaffold(
        containerColor = EkataBackground,
        topBar = { EkataTopAppBar(stringResource(R.string.expenses_history), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.wishlist_back)) } }) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(EkataSpacing.md), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text(state.tripName, style = MaterialTheme.typography.titleSmall, color = EkataTextSecondary) }
            item { Row(Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("All","Accommodation","Transport","Food & Drinks","Activities","Shopping","Other").forEach{category->FilterChip(filter==category,{filter=category},{Text(if(category=="All")stringResource(R.string.ui_all) else categoryLabel(category))})}} }
            when {
                state.loading -> item { EkataLoadingState(stringResource(R.string.expenses_loading)) }
                state.error != null -> item { EkataErrorState(stringResource(R.string.expenses_load_error), state.error, actionLabel = stringResource(R.string.retry), onAction = onRetry) }
                state.recentExpenses.isEmpty() -> item { EkataEmptyState(stringResource(R.string.expenses_empty), stringResource(R.string.expenses_empty_message)) }
                else -> items(state.recentExpenses.filter{filter=="All"||it.category==filter}, key = ExpenseDto::id) { expense -> ExpenseCard(expense) { state.tripId?.let{onDetails(it,expense.id)} } }
            }
        }
    }
}

@Composable
fun BalancesScreen(state: ExpensesData, onBack: () -> Unit, onRetry: () -> Unit, onSettle:(String,String,String,String)->Unit) {
    var selectedDebt by remember { mutableStateOf<ExpenseDebtDto?>(null) }
    Scaffold(
        containerColor = EkataBackground,
        topBar = { EkataTopAppBar(stringResource(R.string.ui_balances), navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.wishlist_back)) } }) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(EkataSpacing.md), verticalArrangement = Arrangement.spacedBy(EkataSpacing.sm)) {
            item { Text(state.tripName, style = MaterialTheme.typography.titleSmall, color = EkataTextSecondary) }
            when {
                state.loading -> item { EkataLoadingState(stringResource(R.string.expenses_loading)) }
                state.error != null -> item { EkataErrorState(stringResource(R.string.expenses_load_error), state.error, actionLabel = stringResource(R.string.retry), onAction = onRetry) }
                state.balanceError != null -> item { EkataErrorState(stringResource(R.string.expenses_balances_unavailable), state.balanceError, actionLabel = stringResource(R.string.retry), onAction = onRetry) }
                else -> {
                    item { YourBalanceCard(state.balances.firstOrNull { it.userId == state.currentUserId }) }
                    item { SectionTitle(stringResource(R.string.expenses_member_balances)) }
                    val relationships=state.debts.filter{it.paidBy==state.currentUserId||it.paidTo==state.currentUserId}
                    if (relationships.isEmpty()) item { EkataEmptyState(stringResource(R.string.expenses_you_are_settled), stringResource(R.string.expenses_all_settled_message)) }
                    else items(relationships,key={it.paidBy+it.paidTo}){debt->DebtRow(debt,state.currentUserId){selectedDebt=debt}}
                    item { SectionTitle(stringResource(R.string.expenses_settlement_history)) }
                    if(state.settlements.isEmpty()) item{Text(stringResource(R.string.expenses_no_settlements),style=MaterialTheme.typography.bodyMedium,color=EkataTextSecondary)}
                    else items(state.settlements,key=SettlementDto::id){SettlementRow(it)}
                    state.mutationError?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}}
                }
            }
        }
    }
    selectedDebt?.let{debt->RecordPaymentDialog(debt,state.mutating,{selectedDebt=null}){amount,method,note->onSettle(debt.paidTo,amount,method,note);selectedDebt=null}}
}

@Composable private fun DebtRow(debt:ExpenseDebtDto,currentUserId:String?,onSettle:()->Unit){
    val userOwes=debt.paidBy==currentUserId;val person=if(userOwes)debt.recipient else debt.payer
    Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(13.dp),color=MaterialTheme.colorScheme.surface,border=androidx.compose.foundation.BorderStroke(1.dp,EkataOutline)){
        Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){
            Box(Modifier.size(38.dp).background((if(userOwes)MaterialTheme.colorScheme.error else EkataSuccess).copy(alpha=.12f),CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Default.Person,null,tint=if(userOwes)MaterialTheme.colorScheme.error else EkataSuccess)}
            Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(person.displayName,fontWeight=FontWeight.SemiBold);Text("@${person.username}",style=MaterialTheme.typography.labelSmall,color=EkataTextSecondary);Text(stringResource(if(userOwes)R.string.expenses_you_owe_person else R.string.expenses_person_owes_you,person.displayName,money(debt.amount.toBigDecimalOrNull()?:BigDecimal.ZERO)),style=MaterialTheme.typography.labelSmall,color=if(userOwes)MaterialTheme.colorScheme.error else EkataSuccess)}
            if(userOwes) TextButton(onSettle){Text(stringResource(R.string.expenses_settle_up))}
        }
    }
}

@Composable private fun SettlementRow(item:SettlementDto)=Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(13.dp),color=MaterialTheme.colorScheme.surface,border=androidx.compose.foundation.BorderStroke(1.dp,EkataOutline)){
    Row(Modifier.padding(13.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Handshake,null,tint=EkataBlue);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(stringResource(R.string.expenses_paid_person,item.payer.displayName,item.recipient.displayName),fontWeight=FontWeight.SemiBold);Text(item.paymentMethod,style=MaterialTheme.typography.labelSmall,color=EkataTextSecondary);Text(item.settledAt.take(10),style=MaterialTheme.typography.labelSmall,color=EkataTextSecondary)};Text(money(item.amount.toBigDecimalOrNull()?:BigDecimal.ZERO),fontWeight=FontWeight.Bold,color=EkataBlue)}
}

@Composable private fun RecordPaymentDialog(debt:ExpenseDebtDto,saving:Boolean,onDismiss:()->Unit,onSave:(String,String,String)->Unit){
    var amount by remember{mutableStateOf(debt.amount)};var method by remember{mutableStateOf("Cash")};var note by remember{mutableStateOf("")}
    AlertDialog(onDismissRequest=onDismiss,title={Text(stringResource(R.string.expenses_record_payment))},text={Column(verticalArrangement=Arrangement.spacedBy(9.dp)){
        DetailLine(stringResource(R.string.expenses_paying),debt.payer.displayName);DetailLine(stringResource(R.string.expenses_paying_to),debt.recipient.displayName);DetailLine(stringResource(R.string.expenses_outstanding),money(debt.amount.toBigDecimalOrNull()?:BigDecimal.ZERO))
        OutlinedTextField(amount,{amount=it.filter{c->c.isDigit()||c=='.'}},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_amount))},prefix={Text("LKR ")},singleLine=true)
        Text(stringResource(R.string.expenses_payment_method),fontWeight=FontWeight.SemiBold);Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){listOf("Cash","Bank Transfer","Other").forEach{value->FilterChip(method==value,{method=value},{Text(paymentMethodLabel(value))})}}
        OutlinedTextField(note,{note=it},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_notes_optional))},maxLines=3)
    }},confirmButton={Button({onSave(amount,method,note)},enabled=!saving){Text(stringResource(R.string.expenses_record_payment))}},dismissButton={TextButton(onDismiss){Text(stringResource(R.string.cancel))}})
}

@Composable private fun paymentMethodLabel(value:String)=stringResource(when(value){"Bank Transfer"->R.string.expenses_bank_transfer;"Other"->R.string.expense_category_other;else->R.string.expenses_cash})

@Composable
private fun YourBalanceCard(balance: ExpenseBalanceDto?) = DashboardCard {
    SectionTitle(stringResource(R.string.expenses_your_balance))
    Spacer(Modifier.height(13.dp))
    if (balance == null) {
        Text(stringResource(R.string.expenses_balance_unavailable_message), style = MaterialTheme.typography.bodyMedium, color = EkataTextSecondary)
    } else {
        val net = balance.netBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val tint = when { net.signum() > 0 -> EkataSuccess; net.signum() < 0 -> MaterialTheme.colorScheme.error; else -> EkataBlue }
        Text(stringResource(when { net.signum() > 0 -> R.string.expenses_you_are_owed; net.signum() < 0 -> R.string.expenses_you_owe; else -> R.string.expenses_you_are_settled }), style = MaterialTheme.typography.labelMedium, color = EkataTextSecondary)
        Text(money(net.abs()), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = tint)
    }
}

@Composable
private fun MemberBalanceRow(balance: ExpenseBalanceDto) {
    val net = balance.netBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val tint = if (net.signum() >= 0) EkataSuccess else MaterialTheme.colorScheme.error
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, EkataOutline)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).background(tint.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = tint) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) { Text(balance.displayName, style = MaterialTheme.typography.titleSmall); Text("@${balance.username}", style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary) }
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(if (net.signum() > 0) R.string.expenses_owed else if (net.signum() < 0) R.string.expenses_owes else R.string.expenses_settled, money(net.abs())), color = tint, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Text(stringResource(R.string.expenses_paid_owed_summary, money(balance.amountPaid.toBigDecimalOrNull() ?: BigDecimal.ZERO), money(balance.amountOwed.toBigDecimalOrNull() ?: BigDecimal.ZERO)), style = MaterialTheme.typography.labelSmall, color = EkataTextSecondary)
            }
        }
    }
}

@Composable
internal fun ExpenseDetailsDialog(expense: ExpenseDto, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onDismiss) { Text(stringResource(R.string.wishlist_close)) } },
        title = { Text(expense.title) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(money(expense.amount.toBigDecimalOrNull() ?: BigDecimal.ZERO), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = categoryStyle(expense.category).color)
                DetailLine(stringResource(R.string.ui_category), categoryLabel(expense.category))
                DetailLine(stringResource(R.string.ui_date), expense.expenseDate)
                DetailLine(stringResource(R.string.ui_paid_by), expense.payer.displayName)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.expenses_participants), style = MaterialTheme.typography.titleSmall)
                expense.participants.forEach { participant ->
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        Text(participant.displayName, Modifier.weight(1f), color = EkataTextPrimary)
                        Text(money(participant.shareAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO), fontWeight = FontWeight.SemiBold)
                    }
                }
                if (expense.notes.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.ui_notes), style = MaterialTheme.typography.titleSmall)
                    Text(expense.notes, style = MaterialTheme.typography.bodyMedium, color = EkataTextSecondary)
                }
            }
        },
    )
}

@Composable
fun ExpenseDetailsScreen(state:ExpensesData,expenseId:String?,onBack:()->Unit,onEdit:(String,String)->Unit,onDelete:(String)->Unit){
    val expense=state.recentExpenses.firstOrNull{it.id==expenseId};var confirmDelete by remember{mutableStateOf(false)};var deleteRequested by remember{mutableStateOf(false)}
    LaunchedEffect(expense,state.loading){if(deleteRequested&&!state.loading&&expense==null)onBack()}
    Scaffold(containerColor=EkataBackground,topBar={EkataTopAppBar(stringResource(R.string.expenses_details),navigationIcon={IconButton(onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,stringResource(R.string.wishlist_back))}})}){padding->
        when{state.loading->Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.Center){EkataLoadingState(stringResource(R.string.expenses_loading))}
            expense==null->Box(Modifier.fillMaxSize().padding(padding),contentAlignment=Alignment.Center){EkataErrorState(stringResource(R.string.expenses_details_unavailable),state.error?:stringResource(R.string.expenses_details_unavailable_message),actionLabel=stringResource(R.string.wishlist_back),onAction=onBack)}
            else->LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(EkataSpacing.md),verticalArrangement=Arrangement.spacedBy(EkataSpacing.sm)){
                item{DashboardCard{Text(expense.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(money(expense.amount.toBigDecimalOrNull()?:BigDecimal.ZERO),style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold,color=categoryStyle(expense.category).color);Text(categoryLabel(expense.category),color=categoryStyle(expense.category).color);Spacer(Modifier.height(8.dp));DetailLine(stringResource(R.string.ui_paid_by),expense.payer.displayName);DetailLine(stringResource(R.string.ui_date),expense.expenseDate)}}
                item{DashboardCard{SectionTitle(stringResource(R.string.expenses_participants));expense.participants.forEach{participant->Row(Modifier.fillMaxWidth().padding(top=8.dp)){Text(participant.displayName,Modifier.weight(1f));Text(money(participant.shareAmount.toBigDecimalOrNull()?:BigDecimal.ZERO),fontWeight=FontWeight.SemiBold)}}}}
                if(expense.notes.isNotBlank())item{DashboardCard{SectionTitle(stringResource(R.string.ui_notes));Spacer(Modifier.height(6.dp));Text(expense.notes,color=EkataTextSecondary)}}
                if(state.currentUserId==expense.createdBy||state.currentUserRole in setOf("owner","admin"))item{OutlinedButton({state.tripId?.let{onEdit(it,expense.id)}},Modifier.fillMaxWidth()){Icon(Icons.Default.Edit,null);Spacer(Modifier.width(6.dp));Text(stringResource(R.string.expenses_edit_expense))}}
                if(state.currentUserRole=="owner")item{Button({confirmDelete=true},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error)){Icon(Icons.Default.Delete,null);Spacer(Modifier.width(6.dp));Text(stringResource(R.string.expenses_delete_expense))}}
                state.mutationError?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}}
            }
        }
    }
    if(confirmDelete&&expense!=null)AlertDialog(onDismissRequest={confirmDelete=false},title={Text(stringResource(R.string.expenses_delete_question))},text={Column{Text(expense.title,fontWeight=FontWeight.SemiBold);Text(money(expense.amount.toBigDecimalOrNull()?:BigDecimal.ZERO));Spacer(Modifier.height(8.dp));Text(stringResource(R.string.expenses_delete_warning))}},confirmButton={Button({confirmDelete=false;deleteRequested=true;onDelete(expense.id)},colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error)){Text(stringResource(R.string.delete))}},dismissButton={TextButton({confirmDelete=false}){Text(stringResource(R.string.cancel))}})
}

@Composable
private fun DetailLine(label: String, value: String) = Row(Modifier.fillMaxWidth().padding(top = 7.dp)) {
    Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = EkataTextSecondary)
    Text(value, style = MaterialTheme.typography.bodyMedium, color = EkataTextPrimary)
}

@Composable
private fun DashboardCard(content: @Composable ColumnScope.() -> Unit) = Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    shadowElevation = EkataElevation.low,
    border = androidx.compose.foundation.BorderStroke(1.dp, EkataOutline.copy(alpha = .65f)),
) { Column(Modifier.padding(EkataSpacing.md), content = content) }

@Composable
private fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = EkataTextPrimary)

private data class CategoryStyle(val color: Color, val icon: ImageVector)
private fun categoryStyle(category: String) = when (category) {
    "Accommodation" -> CategoryStyle(AccommodationColor, Icons.Default.Hotel)
    "Transport" -> CategoryStyle(TransportColor, Icons.Default.DirectionsBus)
    "Food & Drinks" -> CategoryStyle(FoodColor, Icons.Default.Restaurant)
    "Activities" -> CategoryStyle(ActivitiesColor, Icons.Default.LocalActivity)
    "Shopping" -> CategoryStyle(ShoppingColor, Icons.Default.ShoppingBag)
    else -> CategoryStyle(OtherColor, Icons.Default.MoreHoriz)
}

private fun number(amount: BigDecimal) = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
    minimumFractionDigits = 0
    maximumFractionDigits = 2
    roundingMode = java.math.RoundingMode.HALF_UP
}.format(amount)
private fun money(amount: BigDecimal) = "LKR ${number(amount)}"
