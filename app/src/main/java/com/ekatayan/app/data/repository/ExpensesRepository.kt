package com.ekatayan.app.data.repository

import com.ekatayan.app.data.remote.api.EkataYanApiService
import com.ekatayan.app.data.remote.api.ExpenseRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class ExpensesRepository @Inject constructor(private val api: EkataYanApiService) {
    suspend fun expenses(tripId:String)=api.expenses(tripId).data?:error("We couldn't load this trip's expenses.")
    suspend fun balances(tripId:String)=api.expenseBalances(tripId).data?:error("We couldn't calculate trip balances.")
    suspend fun create(tripId:String,request:ExpenseRequest)=api.createExpense(tripId,request).data?:error("The expense could not be saved.")
}
