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
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel class ExpensesViewModel @Inject constructor(private val expenses:ExpensesRepository,private val trips:TripsRepository,savedState:SavedStateHandle):ViewModel(){
    private val requested:String?=savedState["tripId"]; private val mutable=MutableStateFlow(ExpensesData()); val uiState=mutable.asStateFlow()
    init{refresh()}
    fun refresh()=viewModelScope.launch{mutable.value=mutable.value.copy(loading=true,error=null);runCatching{trips.refreshTrips();val available=trips.trips.value.filter{it.remoteId!=null};val selected=available.firstOrNull{it.remoteId==(mutable.value.tripId?:requested)}?:available.firstOrNull();if(selected==null)return@runCatching ExpensesData(availableTrips=available,loading=false);val id=requireNotNull(selected.remoteId);val rows=expenses.expenses(id);val balances=expenses.balances(id).balances;val total=rows.sumOf{it.amount.toBigDecimalOrNull()?:BigDecimal.ZERO};val categories=rows.groupBy{it.category}.map{(name,items)->val amount=items.sumOf{it.amount.toBigDecimalOrNull()?:BigDecimal.ZERO};ExpenseCategoryTotal(name,amount.toLong(),if(total.signum()==0)0 else amount.multiply(BigDecimal(100)).divide(total,0,RoundingMode.HALF_UP).toInt())};ExpensesData(id,selected.customName?:"Trip",available,total.toLong(),categories,rows,balances,false)}.onSuccess{mutable.value=it}.onFailure{mutable.value=mutable.value.copy(loading=false,error=it.message)}}
    fun selectTrip(id:String){mutable.value=mutable.value.copy(tripId=id);refresh()}
}

data class AddExpenseUiState(val members:List<PublicTripMemberDto> = emptyList(),val title:String="",val amount:String="",val category:String="Accommodation",val paidBy:String?=null,val participantIds:Set<String> = emptySet(),val date:String=LocalDate.now().toString(),val notes:String="",val loading:Boolean=true,val saving:Boolean=false,val saved:Boolean=false,val error:String?=null)

@HiltViewModel class AddExpenseViewModel @Inject constructor(private val expenses:ExpensesRepository,private val members:TripMembersRepository,savedState:SavedStateHandle):ViewModel(){
    val tripId:String=savedState["tripId"]?:"";private val mutable=MutableStateFlow(AddExpenseUiState());val state=mutable.asStateFlow()
    init{viewModelScope.launch{runCatching{members.members(tripId)}.onSuccess{rows->mutable.value=mutable.value.copy(members=rows,paidBy=rows.firstOrNull{it.isCurrentUser}?.userId?:rows.firstOrNull()?.userId,participantIds=rows.map{it.userId}.toSet(),loading=false)}.onFailure{mutable.value=mutable.value.copy(loading=false,error=it.message)}}}
    fun title(v:String){mutable.value=mutable.value.copy(title=v)};fun amount(v:String){mutable.value=mutable.value.copy(amount=v.filter{it.isDigit()||it=='.'})};fun category(v:String){mutable.value=mutable.value.copy(category=v)};fun payer(v:String){mutable.value=mutable.value.copy(paidBy=v)}
    fun participant(id:String){val ids=mutable.value.participantIds;mutable.value=mutable.value.copy(participantIds=if(id in ids)ids-id else ids+id)};fun selectAll(){mutable.value=mutable.value.copy(participantIds=mutable.value.members.map{it.userId}.toSet())};fun date(v:String){mutable.value=mutable.value.copy(date=v)};fun notes(v:String){mutable.value=mutable.value.copy(notes=v)}
    fun save()=viewModelScope.launch{val s=mutable.value;val value=s.amount.toBigDecimalOrNull();when{s.title.isBlank()->mutable.value=s.copy(error="Enter an expense title.");value==null||value<=BigDecimal.ZERO->mutable.value=s.copy(error="Enter a valid amount.");s.paidBy==null->mutable.value=s.copy(error="Choose who paid.");s.participantIds.isEmpty()->mutable.value=s.copy(error="Select at least one participant.");else->{mutable.value=s.copy(saving=true,error=null);runCatching{expenses.create(tripId,ExpenseRequest(s.title.trim(),value.setScale(2,RoundingMode.HALF_UP).toPlainString(),s.category,s.paidBy,s.participantIds.toList(),s.date,s.notes.trim().takeIf{it.isNotEmpty()}))}.onSuccess{mutable.value=mutable.value.copy(saving=false,saved=true)}.onFailure{mutable.value=mutable.value.copy(saving=false,error=it.message)}}}}
}
