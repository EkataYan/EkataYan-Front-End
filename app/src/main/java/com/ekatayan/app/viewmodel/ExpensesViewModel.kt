package com.ekatayan.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekatayan.app.data.model.*
import com.ekatayan.app.data.remote.api.*
import com.ekatayan.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.ekatayan.app.utils.runSuspendCatching
import com.ekatayan.app.R
import com.ekatayan.app.core.localization.StringResourceProvider

@HiltViewModel class ExpensesViewModel @Inject constructor(private val expenses:ExpensesRepository,private val trips:TripsRepository,private val members:TripMembersRepository,savedState:SavedStateHandle,private val strings:StringResourceProvider):ViewModel(){
    private val requested:String?=savedState["tripId"]; val detailExpenseId:String?=savedState["expenseId"]; private val mutable=MutableStateFlow(ExpensesData()); val uiState=mutable.asStateFlow()
    init{
        refresh()
        viewModelScope.launch { expenses.changes.collect { changedTripId ->
            if (changedTripId == mutable.value.tripId) refreshInternal(showLoading = false)
        } }
    }
    fun refresh() = refreshInternal(showLoading = true)

    private fun refreshInternal(showLoading:Boolean)=viewModelScope.launch{
        mutable.value=mutable.value.copy(loading=showLoading,error=null,balanceError=null)
        runSuspendCatching{
            trips.refreshTrips()
            val available=trips.trips.value.filter{it.remoteId!=null}
            val selectedId=requested ?: error("An explicit trip ID is required for the Expenses dashboard.")
            val selected=selectedExpenseTrip(available,selectedId)
                ?: error("The selected trip is unavailable.")
            val id=requireNotNull(selected.remoteId)
            val rows=expenses.expenses(id)
            val ledgerResult=runSuspendCatching { expenses.balances(id) }
            val settlementResult=runSuspendCatching { expenses.settlements(id) }
            val membership=runSuspendCatching { members.members(id) }.getOrDefault(emptyList())
            val currentMember=membership.firstOrNull { it.isCurrentUser }
            val total=sumMoneyAmounts(rows.map(ExpenseDto::amount))
            val categories=rows.groupBy{canonicalExpenseCategory(it.category)}.map{(name,items)->
                val amount=sumMoneyAmounts(items.map(ExpenseDto::amount))
                ExpenseCategoryTotal(name,amount,expensePercentage(amount,total))
            }.sortedBy { expenseCategoryOrder.indexOf(it.name) }
            val budget=selected.budget?.toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }
            val today=LocalDate.now()
            val status=when {
                today < selected.startDate -> strings[R.string.expenses_starts_in_days, ChronoUnit.DAYS.between(today,selected.startDate)]
                today == selected.startDate -> strings[R.string.expenses_starts_today]
                today > selected.endDate -> strings[R.string.expenses_trip_ended]
                today == selected.endDate -> strings[R.string.expenses_last_day]
                else -> strings[R.string.expenses_days_remaining, ChronoUnit.DAYS.between(today,selected.endDate)]
            }
            val ledger=ledgerResult.getOrNull()
            ExpensesData(
                tripId=id,tripName=selected.customName?:strings[R.string.trip_fallback_name],availableTrips=available,
                totalSpent=total,categories=categories,recentExpenses=rows.sortedWith(compareByDescending<ExpenseDto>{it.expenseDate}.thenByDescending{it.createdAt}),
                balances=ledger?.balances.orEmpty(),debts=ledger?.debts.orEmpty(),settlements=settlementResult.getOrDefault(emptyList()),loading=false,
                balanceError=ledgerResult.exceptionOrNull()?.let { strings[R.string.expenses_error_balances] },totalBudget=budget,
                tripStatusText=status,currentUserId=currentMember?.userId,currentUserRole=currentMember?.role,
            )
        }.onSuccess{mutable.value=it}.onFailure{mutable.value=mutable.value.copy(loading=false,error=strings[R.string.expenses_error_load])}
    }
    fun setBudget(value:String)=viewModelScope.launch{
        val amount=value.toBigDecimalOrNull();val id=mutable.value.tripId?:return@launch
        if(amount==null||amount<=BigDecimal.ZERO||amount.precision()>14||amount.scale()>2){mutable.value=mutable.value.copy(mutationError=strings[R.string.expense_error_amount_invalid]);return@launch}
        mutable.value=mutable.value.copy(mutating=true,mutationError=null)
        runSuspendCatching{expenses.setBudget(id,amount.setScale(2,RoundingMode.HALF_UP).toPlainString())}
            .onSuccess{mutable.value=mutable.value.copy(mutating=false)}.onFailure{mutable.value=mutable.value.copy(mutating=false,mutationError=strings[R.string.expenses_budget_save_error])}
    }
    fun deleteExpense(expenseId:String)=viewModelScope.launch{
        val id=mutable.value.tripId?:return@launch;mutable.value=mutable.value.copy(mutating=true,mutationError=null)
        runSuspendCatching{expenses.delete(id,expenseId)}.onSuccess{mutable.value=mutable.value.copy(mutating=false)}.onFailure{mutable.value=mutable.value.copy(mutating=false,mutationError=strings[R.string.expenses_delete_error])}
    }
    fun recordSettlement(recipientId:String,value:String,method:String,note:String)=viewModelScope.launch{
        val amount=value.toBigDecimalOrNull();val id=mutable.value.tripId?:return@launch
        if(amount==null||amount<=BigDecimal.ZERO){mutable.value=mutable.value.copy(mutationError=strings[R.string.expense_error_amount_invalid]);return@launch}
        mutable.value=mutable.value.copy(mutating=true,mutationError=null)
        runSuspendCatching{expenses.settle(id,recipientId,amount.setScale(2,RoundingMode.HALF_UP).toPlainString(),method,note.trim())}
            .onSuccess{mutable.value=mutable.value.copy(mutating=false)}.onFailure{mutable.value=mutable.value.copy(mutating=false,mutationError=strings[R.string.expenses_settlement_save_error])}
    }
}

internal fun selectedExpenseTrip(available:List<Trip>,selectedId:String?):Trip? =
    selectedId?.let { id -> available.firstOrNull { it.remoteId == id } }

data class AddExpenseUiState(val members:List<PublicTripMemberDto> = emptyList(),val title:String="",val amount:String="",val category:String="Accommodation",val paidBy:String?=null,val participantIds:Set<String> = emptySet(),val date:String=LocalDate.now().toString(),val notes:String="",val loading:Boolean=true,val saving:Boolean=false,val saved:Boolean=false,val editing:Boolean=false,val error:String?=null)

@HiltViewModel class AddExpenseViewModel @Inject constructor(private val expenses:ExpensesRepository,private val members:TripMembersRepository,savedState:SavedStateHandle,private val strings:StringResourceProvider):ViewModel(){
    val tripId:String=savedState["tripId"]?:"";private val expenseId:String?=savedState["expenseId"];private val mutable=MutableStateFlow(AddExpenseUiState());val state=mutable.asStateFlow()
    init{viewModelScope.launch{runSuspendCatching{val people=members.members(tripId);val existing=expenseId?.let{id->expenses.expenses(tripId).firstOrNull{it.id==id}};people to existing}.onSuccess{(rows,item)->mutable.value=mutable.value.copy(members=rows,title=item?.title.orEmpty(),amount=item?.amount.orEmpty(),category=item?.category?:"Accommodation",paidBy=item?.paidBy?:rows.firstOrNull{it.isCurrentUser}?.userId?:rows.firstOrNull()?.userId,participantIds=item?.participants?.map{it.userId}?.toSet()?:rows.map{it.userId}.toSet(),date=item?.expenseDate?:LocalDate.now().toString(),notes=item?.notes.orEmpty(),loading=false,editing=item!=null)}.onFailure{mutable.value=mutable.value.copy(loading=false,error=strings[R.string.trip_members_load_error])}}}
    fun title(v:String){mutable.value=mutable.value.copy(title=v)};fun amount(v:String){mutable.value=mutable.value.copy(amount=v.filter{it.isDigit()||it=='.'})};fun category(v:String){mutable.value=mutable.value.copy(category=v)};fun payer(v:String){mutable.value=mutable.value.copy(paidBy=v)}
    fun participant(id:String){val ids=mutable.value.participantIds;mutable.value=mutable.value.copy(participantIds=if(id in ids)ids-id else ids+id)};fun selectAll(){mutable.value=mutable.value.copy(participantIds=mutable.value.members.map{it.userId}.toSet())};fun date(v:String){mutable.value=mutable.value.copy(date=v)};fun notes(v:String){mutable.value=mutable.value.copy(notes=v)}
    fun save()=viewModelScope.launch{
        val s=mutable.value
        if (s.saving || s.saved) return@launch
        val value=s.amount.toBigDecimalOrNull()
        val validDate=runCatching { LocalDate.parse(s.date) }.getOrNull()
        when{
            s.title.isBlank()->mutable.value=s.copy(error=strings[R.string.expense_error_title_required])
            s.title.trim().length > 160->mutable.value=s.copy(error=strings[R.string.expense_error_title_length])
            value==null||value<=BigDecimal.ZERO->mutable.value=s.copy(error=strings[R.string.expense_error_amount_invalid])
            value.precision() > 14 || value.scale() > 2->mutable.value=s.copy(error=strings[R.string.expense_error_amount_precision])
            s.paidBy==null->mutable.value=s.copy(error=strings[R.string.expense_error_payer_required])
            s.participantIds.isEmpty()->mutable.value=s.copy(error=strings[R.string.expense_error_participant_required])
            validDate==null->mutable.value=s.copy(error=strings[R.string.expense_error_date_invalid])
            s.notes.length > 2000->mutable.value=s.copy(error=strings[R.string.expense_error_notes_length])
            else->{
                mutable.value=s.copy(saving=true,error=null)
                val request=ExpenseRequest(s.title.trim(),value.setScale(2,RoundingMode.HALF_UP).toPlainString(),s.category,s.paidBy,s.participantIds.toList(),validDate.toString(),s.notes.trim().takeIf{it.isNotEmpty()})
                runSuspendCatching{if(expenseId==null)expenses.create(tripId,request) else expenses.update(tripId,expenseId,request)}
                    .onSuccess{mutable.value=mutable.value.copy(saving=false,saved=true)}
                    .onFailure{mutable.value=mutable.value.copy(saving=false,error=strings[R.string.expenses_error_save])}
            }
        }
    }
}

private val expenseCategoryOrder=listOf("Accommodation","Transport","Food & Drinks","Activities","Shopping","Other")
internal fun sumMoneyAmounts(values: Iterable<String>): BigDecimal =
    values.fold(BigDecimal.ZERO) { total, value -> total + (value.toBigDecimalOrNull() ?: BigDecimal.ZERO) }

internal fun expensePercentage(amount: BigDecimal, total: BigDecimal): Int =
    if (total.signum() == 0) 0 else amount.multiply(BigDecimal(100)).divide(total, 0, RoundingMode.HALF_UP).toInt()

private fun canonicalExpenseCategory(value:String)=when(value.trim().lowercase()){
    "accommodation","lodging","hotel","hotels"->"Accommodation"
    "transport","transportation","travel"->"Transport"
    "food","food & drinks","food and drinks","dining"->"Food & Drinks"
    "activity","activities"->"Activities"
    "shopping"->"Shopping"
    else->"Other"
}
