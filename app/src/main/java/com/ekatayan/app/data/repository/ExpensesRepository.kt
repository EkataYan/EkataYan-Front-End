package com.ekatayan.app.data.repository

import com.ekatayan.app.data.remote.api.EkataYanApiService
import com.ekatayan.app.data.remote.api.ExpenseRequest
import com.ekatayan.app.data.remote.api.BudgetRequest
import com.ekatayan.app.data.remote.api.SettlementRequest
import javax.inject.Inject
import javax.inject.Singleton
import com.ekatayan.app.data.remote.apiCall
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Singleton class ExpensesRepository @Inject constructor(private val api: EkataYanApiService) {
    private val mutableChanges = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val changes = mutableChanges.asSharedFlow()

    suspend fun expenses(tripId:String) = apiCall("We couldn't load this trip's expenses.") { api.expenses(tripId) }.requireData("We couldn't load this trip's expenses.")
    suspend fun balances(tripId:String) = apiCall("We couldn't calculate trip balances.") { api.expenseBalances(tripId) }.requireData("We couldn't calculate trip balances.")
    suspend fun create(tripId:String,request:ExpenseRequest) = apiCall("The expense could not be saved.") { api.createExpense(tripId,request) }.requireData("The expense could not be saved.").also { mutableChanges.tryEmit(tripId) }
    suspend fun update(tripId:String, expenseId:String, request:ExpenseRequest) = apiCall("The expense could not be updated.") { api.updateExpense(tripId, expenseId, request) }.requireData("The expense could not be updated.").also { mutableChanges.tryEmit(tripId) }
    suspend fun delete(tripId:String, expenseId:String) {
        val response = apiCall("The expense could not be deleted.") { api.deleteExpense(tripId, expenseId) }
        if (!response.success || response.data?.get("deleted") != true) {
            error(response.error?.message ?: "The expense could not be deleted.")
        }
        mutableChanges.tryEmit(tripId)
    }
    suspend fun setBudget(tripId:String, amount:String) = apiCall("The trip budget could not be saved.") { api.setTripBudget(tripId, BudgetRequest(amount)) }.requireData("The trip budget could not be saved.").also { mutableChanges.tryEmit(tripId) }
    suspend fun settlements(tripId:String) = apiCall("Settlement history could not be loaded.") { api.settlements(tripId) }.requireData("Settlement history could not be loaded.")
    suspend fun settle(tripId:String, recipientId:String, amount:String, method:String, note:String) = apiCall("The payment could not be recorded.") { api.recordSettlement(tripId, SettlementRequest(recipientId, amount, method, note)) }.requireData("The payment could not be recorded.").also { mutableChanges.tryEmit(tripId) }

    private fun <T> com.ekatayan.app.data.remote.api.ApiEnvelope<T>.requireData(fallback: String): T =
        data.takeIf { success } ?: error(error?.message ?: fallback)
}
