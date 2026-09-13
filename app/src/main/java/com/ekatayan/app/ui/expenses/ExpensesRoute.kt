package com.ekatayan.app.ui.expenses

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.viewmodel.*
import kotlinx.coroutines.flow.StateFlow

@Composable fun ExpensesRoute(onHomeClick:()->Unit,onTripsClick:()->Unit,onPlannerClick:()->Unit,onProfileClick:()->Unit,onSettingsClick:()->Unit,onNotificationClick:()->Unit,notificationsUiState:StateFlow<NotificationsUiState>,onAdd:(String)->Unit,viewModel:ExpensesViewModel=hiltViewModel()){
    val state by viewModel.uiState.collectAsStateWithLifecycle();val notifications by notificationsUiState.collectAsStateWithLifecycle()
    ExpensesScreen(state,viewModel::selectTrip,viewModel::refresh,onAdd,onHomeClick,onTripsClick,onPlannerClick,onProfileClick,onSettingsClick,onNotificationClick,notifications.hasUnreadNotifications)
}
@Composable fun AddExpenseRoute(onBack:()->Unit,onSaved:(String)->Unit,viewModel:AddExpenseViewModel=hiltViewModel()){
    val state by viewModel.state.collectAsStateWithLifecycle();LaunchedEffect(state.saved){if(state.saved)onSaved(viewModel.tripId)}
    AddExpenseScreen(state,viewModel::title,viewModel::amount,viewModel::category,viewModel::payer,viewModel::participant,viewModel::selectAll,viewModel::date,viewModel::notes,viewModel::save,onBack)
}
