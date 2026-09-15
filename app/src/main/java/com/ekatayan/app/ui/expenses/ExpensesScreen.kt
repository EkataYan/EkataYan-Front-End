package com.ekatayan.app.ui.expenses

import com.ekatayan.app.R

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ekatayan.app.core.designsystem.component.*
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.data.model.ExpensesData
import com.ekatayan.app.data.remote.api.ExpenseDto
import com.ekatayan.app.viewmodel.AddExpenseUiState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable fun ExpensesScreen(state:ExpensesData,onTrip:(String)->Unit,onRetry:()->Unit,onAdd:(String)->Unit,onHome:()->Unit,onTrips:()->Unit,onPlanner:()->Unit,onProfile:()->Unit,onSettings:()->Unit,onNotifications:()->Unit,hasUnread:Boolean){
    var expanded by remember{mutableStateOf(false)};var detail by remember{mutableStateOf<ExpenseDto?>(null)}
    Scaffold(bottomBar={AppBottomNavigation(AppBottomNavItem.EXPENSES,onHome,onTrips,onPlanner,{},onProfile)}){padding->LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(EkataSpacing.md),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.AccountBalanceWallet,null);Spacer(Modifier.width(8.dp));Text(stringResource(R.string.notifications_filter_expenses),style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.weight(1f));HeaderActions(onNotifications,onSettings,hasUnread)}}
        when{state.loading->item{EkataLoadingState(stringResource(R.string.expenses_loading))};state.error!=null->item{EkataErrorState(stringResource(R.string.expenses_load_error),state.error,actionLabel=stringResource(R.string.retry),onAction=onRetry)};state.availableTrips.isEmpty()->item{EkataEmptyState(stringResource(R.string.expenses_no_trips),stringResource(R.string.expenses_no_trips_message))};else->{
            item{Box{OutlinedButton({expanded=true},Modifier.fillMaxWidth()){Text(state.tripName.ifBlank{stringResource(R.string.expenses_select_trip)})};DropdownMenu(expanded,{expanded=false}){state.availableTrips.forEach{trip->trip.remoteId?.let{id->DropdownMenuItem({Text(trip.customName?:stringResource(R.string.trip_fallback_name))},onClick={expanded=false;onTrip(id)})}}}}}
            item{EkataCard(Modifier.fillMaxWidth()){Text(stringResource(R.string.expenses_total_spent_label),style=MaterialTheme.typography.labelMedium);Text(money(state.totalSpent.toBigDecimal()),style=MaterialTheme.typography.headlineSmall,color=MaterialTheme.colorScheme.primary);Text(pluralStringResource(R.plurals.expenses_across_count,state.recentExpenses.size,state.recentExpenses.size),color=MaterialTheme.colorScheme.onSurfaceVariant)}}
            if(state.categories.isNotEmpty())item{EkataCard(Modifier.fillMaxWidth()){Text(stringResource(R.string.expenses_spending_by_category),fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));state.categories.forEach{Row(Modifier.fillMaxWidth().padding(vertical=4.dp)){Text(categoryLabel(it.name),Modifier.weight(1f));Text(money(it.amount.toBigDecimal()));Text(stringResource(R.string.percentage_value,it.percentage),color=MaterialTheme.colorScheme.primary)}}}}
            item{EkataPrimaryButton(stringResource(R.string.expenses_action_add),{state.tripId?.let(onAdd)},Modifier.fillMaxWidth())}
            item{Text(stringResource(R.string.expenses_recent_expenses),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)}
            if(state.recentExpenses.isEmpty())item{EkataEmptyState(stringResource(R.string.expenses_empty),stringResource(R.string.expenses_empty_message))}
            items(state.recentExpenses,key={it.id}){expense->ExpenseRow(expense){detail=expense}}
            state.balanceError?.let { message -> item { EkataErrorState(stringResource(R.string.expenses_balances_unavailable), message, actionLabel=stringResource(R.string.retry), onAction=onRetry) } }
            if(state.balances.isNotEmpty())item{EkataCard(Modifier.fillMaxWidth()){Text(stringResource(R.string.ui_balances),fontWeight=FontWeight.Bold);state.balances.forEach{b->val net=b.netBalance.toBigDecimalOrNull()?:BigDecimal.ZERO;Row(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.weight(1f)){Text(b.displayName);Text("@${b.username}",style=MaterialTheme.typography.labelSmall)};Text(stringResource(if(net.signum()>0)R.string.expenses_owed else if(net.signum()<0)R.string.expenses_owes else R.string.expenses_settled,money(net.abs())),color=if(net.signum()>=0)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)}}}}
        }}
    }}
    detail?.let{ExpenseDetails(it){detail=null}}
}

@Composable private fun ExpenseRow(expense:ExpenseDto,onClick:()->Unit)=EkataCard(Modifier.fillMaxWidth().clickable(onClick=onClick)){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(expense.title,fontWeight=FontWeight.Bold);Text(expense.category,color=MaterialTheme.colorScheme.primary);Text(stringResource(R.string.expenses_paid_by_split,expense.payer.displayName,expense.participants.size),style=MaterialTheme.typography.bodySmall)};Text(money(expense.amount.toBigDecimalOrNull()?:BigDecimal.ZERO),fontWeight=FontWeight.Bold)}}
@Composable private fun ExpenseDetails(expense:ExpenseDto,onDismiss:()->Unit)=AlertDialog(onDismissRequest=onDismiss,confirmButton={TextButton(onDismiss){Text(stringResource(R.string.wishlist_close))}},title={Text(expense.title)},text={Column{Text(stringResource(R.string.expenses_total_value,money(expense.amount.toBigDecimalOrNull()?:BigDecimal.ZERO)),fontWeight=FontWeight.Bold);Text(stringResource(R.string.expenses_paid_by,expense.payer.displayName));Spacer(Modifier.height(12.dp));Text(stringResource(R.string.ui_equal_split),fontWeight=FontWeight.Bold);expense.participants.forEach{Row(Modifier.fillMaxWidth().padding(vertical=3.dp)){Text(it.displayName,Modifier.weight(1f));Text(money(it.shareAmount.toBigDecimalOrNull()?:BigDecimal.ZERO))}};if(expense.notes.isNotBlank()){Spacer(Modifier.height(8.dp));Text(expense.notes)}}})

@Composable fun AddExpenseScreen(state:AddExpenseUiState,onTitle:(String)->Unit,onAmount:(String)->Unit,onCategory:(String)->Unit,onPayer:(String)->Unit,onParticipant:(String)->Unit,onAll:()->Unit,onDate:(String)->Unit,onNotes:(String)->Unit,onSave:()->Unit,onBack:()->Unit){
    val categories=listOf("Accommodation","Transport","Food & Drinks","Activities","Shopping","Other");var catMenu by remember{mutableStateOf(false)};var payerMenu by remember{mutableStateOf(false)}
    Scaffold(topBar={EkataTopAppBar(stringResource(R.string.expenses_action_add),navigationIcon={IconButton(onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,null)}})}){padding->LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(EkataSpacing.md),verticalArrangement=Arrangement.spacedBy(12.dp)){
        if(state.loading)item{EkataLoadingState(stringResource(R.string.trip_members_loading))}else{
            item{OutlinedTextField(state.title,onTitle,Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_title))},singleLine=true)};item{OutlinedTextField(state.amount,onAmount,Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_amount_lkr))},singleLine=true)}
            item{Box{OutlinedButton({catMenu=true},Modifier.fillMaxWidth()){Text(categoryLabel(state.category))};DropdownMenu(catMenu,{catMenu=false}){categories.forEach{c->DropdownMenuItem({Text(categoryLabel(c))},{catMenu=false;onCategory(c)})}}}}
            item{OutlinedTextField(state.date,onDate,Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_date_yyyy_mm_dd))},singleLine=true)}
            item{Box{OutlinedButton({payerMenu=true},Modifier.fillMaxWidth()){Text(stringResource(R.string.expenses_paid_by,state.members.firstOrNull{it.userId==state.paidBy}?.displayName?:stringResource(R.string.select)))};DropdownMenu(payerMenu,{payerMenu=false}){state.members.forEach{m->DropdownMenuItem({Text("${m.displayName}  @${m.username}")},{payerMenu=false;onPayer(m.userId)})}}}}
            item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text(stringResource(R.string.ui_split_between_equal),Modifier.weight(1f),fontWeight=FontWeight.Bold);TextButton(onAll){Text(stringResource(R.string.ui_select_all))}}}
            items(state.members,key={it.userId}){m->Row(Modifier.fillMaxWidth().clickable{onParticipant(m.userId)},verticalAlignment=Alignment.CenterVertically){Checkbox(m.userId in state.participantIds,{onParticipant(m.userId)});Column{Text(m.displayName);Text("@${m.username}",style=MaterialTheme.typography.labelSmall)}}}
            item{OutlinedTextField(state.notes,onNotes,Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_notes_optional))},minLines=2)};state.error?.let{item{Text(it,color=MaterialTheme.colorScheme.error)}};item{EkataPrimaryButton(if(state.saving)stringResource(R.string.saving) else stringResource(R.string.expenses_save),onSave,Modifier.fillMaxWidth(),enabled=!state.saving)}
        }
    }}
}
private fun money(amount:BigDecimal)="LKR "+NumberFormat.getNumberInstance().format(amount)
@Composable private fun categoryLabel(value:String):String=stringResource(when(value){"Accommodation"->R.string.expense_category_accommodation;"Transport"->R.string.expense_category_transport;"Food & Drinks"->R.string.expense_category_food;"Activities"->R.string.expense_category_activities;"Shopping"->R.string.expense_category_shopping;else->R.string.expense_category_other})
