package com.ekatayan.app.ui.trips

import com.ekatayan.app.R

import androidx.compose.ui.res.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.core.designsystem.component.*
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.data.remote.api.PublicTripMemberDto
import com.ekatayan.app.viewmodel.TripMembersViewModel

@Composable fun TripMembersRoute(addMode:Boolean,onBack:()->Unit,onAdd:()->Unit,viewModel:TripMembersViewModel=hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(topBar={ EkataTopAppBar(title=stringResource(if(addMode) R.string.trip_members_add_title else R.string.trip_members_title),navigationIcon={ IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,null)} }) }) { padding ->
        if(addMode) AddMembersScreen(state.query,state.results,state.searching,state.invitingUserIds,viewModel::search,viewModel::invite,Modifier.padding(padding))
        else MembersScreen(state.members,state.canInvite,state.canManageMembers,state.loading,state.error,onAdd,viewModel::refresh,viewModel::remove,Modifier.padding(padding))
    }
}

@Composable private fun MembersScreen(members:List<PublicTripMemberDto>,canInvite:Boolean,canManageMembers:Boolean,loading:Boolean,error:String?,onAdd:()->Unit,onRetry:()->Unit,onRemove:(String)->Unit,modifier:Modifier) {
    LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(EkataSpacing.md),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        when { loading -> item { EkataLoadingState(stringResource(R.string.trip_members_loading)) }; error!=null -> item { EkataErrorState(stringResource(R.string.trip_members_load_error), error, actionLabel = stringResource(R.string.retry), onAction = onRetry) }; else -> {
            if(canInvite) item { EkataPrimaryButton(stringResource(R.string.trip_members_add_button),onAdd,modifier=Modifier.fillMaxWidth()) }
            items(members,key={it.userId}) { member -> MemberCard(member,canManageMembers && member.role=="member" && !member.isCurrentUser,onRemove) }
            if(members.size<=1) item { EkataEmptyState(stringResource(R.string.trip_members_empty),stringResource(R.string.trip_members_empty_message)) }
        }}
    }
}
@Composable private fun MemberCard(member:PublicTripMemberDto,removable:Boolean,onRemove:(String)->Unit) { EkataCard(Modifier.fillMaxWidth()) { Row(verticalAlignment=Alignment.CenterVertically) { Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.primaryContainer,modifier=Modifier.size(48.dp)) { Box(contentAlignment=Alignment.Center){Icon(Icons.Outlined.Person,null)} }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)){Text(member.displayName,style=MaterialTheme.typography.titleMedium);Text("@${member.username}",color=MaterialTheme.colorScheme.primary);Text(stringResource(when(member.role.lowercase()){"owner"->R.string.owner;"admin"->R.string.admin;else->R.string.member}),style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)}; if(removable) TextButton(onClick={onRemove(member.userId)}){Text(stringResource(R.string.bp_remove))} } } }
@Composable private fun AddMembersScreen(query:String,results:List<com.ekatayan.app.data.remote.api.PublicUserDto>,loading:Boolean,invitingUserIds:Set<String>,onQuery:(String)->Unit,onInvite:(com.ekatayan.app.data.remote.api.PublicUserDto)->Unit,modifier:Modifier) { LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(EkataSpacing.md),verticalArrangement=Arrangement.spacedBy(12.dp)) { item { OutlinedTextField(query,onQuery,Modifier.fillMaxWidth(),placeholder={Text(stringResource(R.string.ui_search_by_username))},singleLine=true) }; if(loading)item{LinearProgressIndicator(Modifier.fillMaxWidth())}; if(query.isBlank())item{EkataEmptyState(stringResource(R.string.trip_members_search_title),stringResource(R.string.trip_members_search_hint))}; else if(!loading&&results.isEmpty())item{EkataEmptyState(stringResource(R.string.trip_members_no_users),stringResource(R.string.trip_members_try_another))}; items(results,key={it.id}){user-> EkataCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(user.displayName,style=MaterialTheme.typography.titleMedium);Text("@${user.username}",color=MaterialTheme.colorScheme.primary)}; val sending=user.id in invitingUserIds; Button(onClick={onInvite(user)},enabled=user.relationship=="invite"&&!sending,colors=ButtonDefaults.buttonColors(disabledContainerColor=MaterialTheme.colorScheme.surfaceVariant,disabledContentColor=MaterialTheme.colorScheme.onSurfaceVariant)){Text(if(sending) stringResource(R.string.sending) else relationshipLabel(user.relationship))}}}} } }

@Composable private fun relationshipLabel(value:String)=stringResource(when(value.lowercase()){"invite"->R.string.invite;"invited"->R.string.invited;"member"->R.string.member;else->R.string.unavailable})
