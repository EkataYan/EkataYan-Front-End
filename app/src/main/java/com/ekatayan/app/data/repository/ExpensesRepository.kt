package com.ekatayan.app.data.repository

import com.ekatayan.app.data.remote.api.EkataYanApiService
import com.ekatayan.app.data.remote.api.ExpenseRequest
import com.ekatayan.app.data.remote.api.ExpenseUpdateRequest
import javax.inject.Inject
import javax.inject.Singleton
import com.ekatayan.app.data.remote.apiCall

@Singleton class ExpensesRepository @Inject constructor(private val api: EkataYanApiService) {
    suspend fun expenses(tripId:String) = apiCall("We couldn't load this trip's expenses.") { api.expenses(tripId) }.requireData("We couldn't load this trip's expenses.")
    suspend fun balances(tripId:String) = apiCall("We couldn't calculate trip balances.") { api.expenseBalances(tripId) }.requireData("We couldn't calculate trip balances.")
    suspend fun create(tripId:String,request:ExpenseRequest) = apiCall("The expense could not be saved.") { api.createExpense(tripId,request) }.requireData("The expense could not be saved.")
    suspend fun update(expenseId:String, request:ExpenseUpdateRequest) = apiCall("The expense could not be updated.") { api.updateExpense(expenseId, request) }.requireData("The expense could not be updated.")
    suspend fun delete(expenseId:String) {
        val response = apiCall("The expense could not be deleted.") { api.deleteExpense(expenseId) }
        if (!response.success || response.data?.get("deleted") != true) {
            error(response.error?.message ?: "The expense could not be deleted.")
        }
    }

    private fun <T> com.ekatayan.app.data.remote.api.ApiEnvelope<T>.requireData(fallback: String): T =
        data.takeIf { success } ?: error(error?.message ?: fallback)
}
