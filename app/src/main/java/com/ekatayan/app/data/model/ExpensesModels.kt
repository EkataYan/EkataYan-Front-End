package com.ekatayan.app.data.model

import com.ekatayan.app.data.remote.api.ExpenseBalanceDto
import com.ekatayan.app.data.remote.api.ExpenseDto

data class ExpenseCategoryTotal(val name: String, val amount: Long, val percentage: Int)
data class ExpensesData(
    val tripId: String? = null, val tripName: String = "", val availableTrips: List<Trip> = emptyList(),
    val totalSpent: Long = 0, val categories: List<ExpenseCategoryTotal> = emptyList(),
    val recentExpenses: List<ExpenseDto> = emptyList(), val balances: List<ExpenseBalanceDto> = emptyList(),
    val loading: Boolean = true, val error: String? = null,
)
