package com.ekatayan.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.ekatayan.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class BudgetSummary(val totalBudget: Long, val totalSpent: Long, val remaining: Long, val usedFraction: Float, val daysRemaining: Int)
data class ExpenseCategory(@StringRes val nameRes: Int, val amount: Long, val percentage: Int, val color: Color, val icon: ImageVector)
data class QuickAction(@StringRes val labelRes: Int, val icon: ImageVector, val tint: Color)
data class Expense(@StringRes val titleRes: Int, @StringRes val categoryRes: Int, val amount: Long, @StringRes val dateTimeRes: Int, val icon: ImageVector, val tint: Color, val participantCount: Int? = null)
data class ExpensesData(val budget: BudgetSummary, val categories: List<ExpenseCategory>, val quickActions: List<QuickAction>, val recentExpenses: List<Expense>)

