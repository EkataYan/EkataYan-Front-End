package com.ekatayan.app.ui.expenses

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

const val EXPENSES_ROUTE="expenses"
const val TRIP_EXPENSES_ROUTE="expenses/{tripId}"
const val ADD_EXPENSE_ROUTE="expenses/{tripId}/add"
fun tripExpensesRoute(id:String)="expenses/$id"
fun addExpenseRoute(id:String)="expenses/$id/add"

fun NavGraphBuilder.expensesScreen(onHomeClick:()->Unit,onTripsClick:()->Unit,onPlannerClick:()->Unit,onProfileClick:()->Unit,onSettingsClick:()->Unit,onNotificationClick:()->Unit,notificationsUiState:StateFlow<NotificationsUiState>,onAdd:(String)->Unit,onSaved:(String)->Unit,onBack:()->Unit){
    composable(EXPENSES_ROUTE){ExpensesRoute(onHomeClick,onTripsClick,onPlannerClick,onProfileClick,onSettingsClick,onNotificationClick,notificationsUiState,onAdd)}
    composable(TRIP_EXPENSES_ROUTE){ExpensesRoute(onHomeClick,onTripsClick,onPlannerClick,onProfileClick,onSettingsClick,onNotificationClick,notificationsUiState,onAdd)}
    composable(ADD_EXPENSE_ROUTE){AddExpenseRoute(onBack,onSaved)}
}
