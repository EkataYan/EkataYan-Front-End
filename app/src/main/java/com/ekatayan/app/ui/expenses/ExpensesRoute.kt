package com.ekatayan.app.ui.expenses

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.*
import kotlinx.coroutines.flow.StateFlow

@Composable fun ExpensesRoute(onHomeClick:()->Unit,onTripsClick:()->Unit,onPlannerClick:()->Unit,onExpensesClick:()->Unit,onProfileClick:()->Unit,onSettingsClick:()->Unit,onNotificationClick:()->Unit,notificationsUiState:StateFlow<NotificationsUiState>,onSwitchTrip:()->Unit,onAdd:(String)->Unit,onViewAll:(String)->Unit,onBalances:(String)->Unit,onDetails:(String,String)->Unit,viewModel:ExpensesViewModel=hiltViewModel()){
    val state by viewModel.uiState.collectAsStateWithLifecycle();val notifications by notificationsUiState.collectAsStateWithLifecycle()
    ExpensesScreen(state,onSwitchTrip,viewModel::refresh,viewModel::setBudget,onAdd,onViewAll,onBalances,onDetails,onHomeClick,onTripsClick,onPlannerClick,onExpensesClick,onProfileClick,onSettingsClick,onNotificationClick,notifications.hasUnreadNotifications)
}
@Composable fun ExpenseHistoryRoute(onBack:()->Unit,onDetails:(String,String)->Unit,viewModel:ExpensesViewModel=hiltViewModel()){
    val state by viewModel.uiState.collectAsStateWithLifecycle();ExpenseHistoryScreen(state,onBack,viewModel::refresh,onDetails)
}
@Composable fun ExpenseBalancesRoute(onBack:()->Unit,viewModel:ExpensesViewModel=hiltViewModel()){
    val state by viewModel.uiState.collectAsStateWithLifecycle();BalancesScreen(state,onBack,viewModel::refresh,viewModel::recordSettlement)
}
@Composable fun ExpenseDetailsRoute(onBack:()->Unit,onEdit:(String,String)->Unit,viewModel:ExpensesViewModel=hiltViewModel()){
    val state by viewModel.uiState.collectAsStateWithLifecycle();ExpenseDetailsScreen(state,viewModel.detailExpenseId,onBack,onEdit,viewModel::deleteExpense)
}
@Composable fun AddExpenseRoute(onBack:()->Unit,onSaved:(String)->Unit,viewModel:AddExpenseViewModel=hiltViewModel()){
    val state by viewModel.state.collectAsStateWithLifecycle();LaunchedEffect(state.saved){if(state.saved)onSaved(viewModel.tripId)}
    AddExpenseScreen(state,viewModel::title,viewModel::amount,viewModel::category,viewModel::payer,viewModel::participant,viewModel::selectAll,viewModel::date,viewModel::notes,viewModel::save,onBack)
}
