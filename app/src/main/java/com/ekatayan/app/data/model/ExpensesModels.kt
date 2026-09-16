package com.ekatayan.app.data.model

import com.ekatayan.app.data.remote.api.ExpenseBalanceDto
import com.ekatayan.app.data.remote.api.ExpenseDto
import com.ekatayan.app.data.remote.api.ExpenseDebtDto
import com.ekatayan.app.data.remote.api.SettlementDto

data class ExpenseCategoryTotal(val name: String, val amount: Long, val percentage: Int)
data class ExpensesData(
    val tripId: String? = null, val tripName: String = "", val availableTrips: List<Trip> = emptyList(),
    val totalSpent: Long = 0, val categories: List<ExpenseCategoryTotal> = emptyList(),
    val recentExpenses: List<ExpenseDto> = emptyList(), val balances: List<ExpenseBalanceDto> = emptyList(),
    val debts: List<ExpenseDebtDto> = emptyList(), val settlements: List<SettlementDto> = emptyList(),
    val loading: Boolean = true, val error: String? = null, val balanceError: String? = null,
    val totalBudget: Long? = null, val tripStatusText: String? = null, val currentUserId: String? = null,
    val currentUserRole: String? = null, val mutating: Boolean = false, val mutationError: String? = null,
)
