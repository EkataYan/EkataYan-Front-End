package com.ekatayan.app.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.EkataLoadingState
import com.ekatayan.app.core.designsystem.component.EkataPrimaryButton
import com.ekatayan.app.core.designsystem.component.EkataTopAppBar
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.viewmodel.AddExpenseUiState

@Composable
fun AddExpenseScreen(
    state: AddExpenseUiState,
    onTitle: (String) -> Unit,
    onAmount: (String) -> Unit,
    onCategory: (String) -> Unit,
    onPayer: (String) -> Unit,
    onParticipant: (String) -> Unit,
    onAll: () -> Unit,
    onDate: (String) -> Unit,
    onNotes: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    val categories = listOf("Accommodation", "Transport", "Food & Drinks", "Activities", "Shopping", "Other")
    var categoryMenu by remember { mutableStateOf(false) }
    var payerMenu by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            EkataTopAppBar(
                stringResource(if(state.editing)R.string.expenses_edit_expense else R.string.expenses_action_add),
                navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.wishlist_back)) } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(EkataSpacing.md),
            verticalArrangement = Arrangement.spacedBy(EkataSpacing.sm),
        ) {
            if (state.loading) {
                item { EkataLoadingState(stringResource(R.string.trip_members_loading)) }
            } else {
                item { OutlinedTextField(state.title, onTitle, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.ui_title)) }, singleLine = true) }
                item { OutlinedTextField(state.amount, onAmount, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.ui_amount_lkr)) }, singleLine = true) }
                item {
                    Box {
                        OutlinedButton({ categoryMenu = true }, Modifier.fillMaxWidth()) { Text(categoryLabel(state.category)) }
                        DropdownMenu(categoryMenu, { categoryMenu = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem({ Text(categoryLabel(category)) }, { categoryMenu = false; onCategory(category) })
                            }
                        }
                    }
                }
                item { OutlinedTextField(state.date, onDate, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.ui_date_yyyy_mm_dd)) }, singleLine = true) }
                item {
                    Box {
                        OutlinedButton({ payerMenu = true }, Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.expenses_paid_by, state.members.firstOrNull { it.userId == state.paidBy }?.displayName ?: stringResource(R.string.select)))
                        }
                        DropdownMenu(payerMenu, { payerMenu = false }) {
                            state.members.forEach { member ->
                                DropdownMenuItem({ Text("${member.displayName}  @${member.username}") }, { payerMenu = false; onPayer(member.userId) })
                            }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.ui_split_between_equal), Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        TextButton(onAll) { Text(stringResource(R.string.ui_select_all)) }
                    }
                }
                items(state.members, key = { it.userId }) { member ->
                    Row(Modifier.fillMaxWidth().clickable { onParticipant(member.userId) }, verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(member.userId in state.participantIds, { onParticipant(member.userId) })
                        Column { Text(member.displayName); Text("@${member.username}", style = MaterialTheme.typography.labelSmall) }
                    }
                }
                item { OutlinedTextField(state.notes, onNotes, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.ui_notes_optional)) }, minLines = 2) }
                state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                item { EkataPrimaryButton(if (state.saving) stringResource(R.string.saving) else stringResource(if(state.editing)R.string.expenses_update_expense else R.string.expenses_save), onSave, Modifier.fillMaxWidth(), enabled = !state.saving) }
            }
        }
    }
}

@Composable
internal fun categoryLabel(value: String): String = stringResource(
    when (value) {
        "Accommodation" -> R.string.expense_category_accommodation
        "Transport" -> R.string.expense_category_transport
        "Food & Drinks" -> R.string.expense_category_food
        "Activities" -> R.string.expense_category_activities
        "Shopping" -> R.string.expense_category_shopping
        else -> R.string.expense_category_other
    },
)
