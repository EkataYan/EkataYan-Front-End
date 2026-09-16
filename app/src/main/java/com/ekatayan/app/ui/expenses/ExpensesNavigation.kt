package com.ekatayan.app.ui.expenses

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

const val EXPENSES_ROUTE="expenses"
const val TRIP_EXPENSES_ROUTE="expenses/{tripId}"
const val ADD_EXPENSE_ROUTE="expenses/{tripId}/add"
const val EXPENSE_HISTORY_ROUTE="expenses/{tripId}/history"
const val EXPENSE_BALANCES_ROUTE="expenses/{tripId}/balances"
const val EXPENSE_DETAILS_ROUTE="expenses/{tripId}/details/{expenseId}"
const val EDIT_EXPENSE_ROUTE="expenses/{tripId}/edit/{expenseId}"
fun tripExpensesRoute(id:String)="expenses/$id"
fun addExpenseRoute(id:String)="expenses/$id/add"
fun expenseHistoryRoute(id:String)="expenses/$id/history"
fun expenseBalancesRoute(id:String)="expenses/$id/balances"
fun expenseDetailsRoute(tripId:String,expenseId:String)="expenses/$tripId/details/$expenseId"
fun editExpenseRoute(tripId:String,expenseId:String)="expenses/$tripId/edit/$expenseId"

fun NavGraphBuilder.expensesScreen(onHomeClick:()->Unit,onTripsClick:()->Unit,onPlannerClick:()->Unit,onExpensesClick:()->Unit,onProfileClick:()->Unit,onSettingsClick:()->Unit,onNotificationClick:()->Unit,notificationsUiState:StateFlow<NotificationsUiState>,onTripSelected:(String)->Unit,onSwitchTrip:()->Unit,onAdd:(String)->Unit,onViewAll:(String)->Unit,onBalances:(String)->Unit,onDetails:(String,String)->Unit,onEdit:(String,String)->Unit,onSaved:(String)->Unit,onBack:()->Unit){
    composable(EXPENSES_ROUTE){ExpensesTripSelectionRoute(onHomeClick,onTripsClick,onPlannerClick,onExpensesClick,onProfileClick,onSettingsClick,onNotificationClick,notificationsUiState,onTripSelected)}
    composable(TRIP_EXPENSES_ROUTE){ExpensesRoute(onHomeClick,onTripsClick,onPlannerClick,onExpensesClick,onProfileClick,onSettingsClick,onNotificationClick,notificationsUiState,onSwitchTrip,onAdd,onViewAll,onBalances,onDetails)}
    composable(ADD_EXPENSE_ROUTE){AddExpenseRoute(onBack,onSaved)}
    composable(EXPENSE_HISTORY_ROUTE){ExpenseHistoryRoute(onBack,onDetails)}
    composable(EXPENSE_BALANCES_ROUTE){ExpenseBalancesRoute(onBack)}
    composable(EXPENSE_DETAILS_ROUTE){ExpenseDetailsRoute(onBack,onEdit)}
    composable(EDIT_EXPENSE_ROUTE){AddExpenseRoute(onBack,onSaved)}
}
