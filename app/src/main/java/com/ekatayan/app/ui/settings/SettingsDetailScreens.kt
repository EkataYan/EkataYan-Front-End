package com.ekatayan.app.ui.settings

import com.ekatayan.app.R

import androidx.compose.ui.res.stringResource

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.BuildConfig
import com.ekatayan.app.data.model.SettingsPreferences
import com.ekatayan.app.viewmodel.SettingsDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun DetailScaffold(title: String,onBack:()->Unit,content:@Composable ColumnScope.()->Unit) {
    Scaffold(topBar={ TopAppBar(title={Text(title)},navigationIcon={IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,null)}}) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp),content=content)
    }
}

@Composable fun AccountInformationScreen(onBack:()->Unit,vm:SettingsDetailViewModel=hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle(); val p=state.profile
    var name by remember(p){mutableStateOf(p?.name.orEmpty())}; var username by remember(p){mutableStateOf(p?.username.orEmpty())}; var phone by remember(p){mutableStateOf(p?.phone.orEmpty())}
    var deleteStep by remember{mutableIntStateOf(0)}
    DetailScaffold(stringResource(R.string.settings_account_information),onBack) {
        if(state.loading && p==null) CircularProgressIndicator() else if(p==null) { Text(state.error ?: stringResource(R.string.account_unavailable)); Button(onClick=vm::refresh){Text(stringResource(R.string.profile_retry))} } else {
            OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.edit_profile_name))})
            OutlinedTextField(username,{username=it.lowercase().filter { c->c.isLetterOrDigit()||c=='_' }.take(20)},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.edit_profile_username))},prefix={Text("@")})
            OutlinedTextField(p.email,{},Modifier.fillMaxWidth(),enabled=false,label={Text(stringResource(R.string.ui_email_read_only))})
            OutlinedTextField(phone,{phone=it},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.signup_phone))})
            state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}; state.message?.let{Text(it,color=MaterialTheme.colorScheme.primary)}
            Button(onClick={vm.saveAccount(name,username,phone)},enabled=!state.saving && name.isNotBlank() && Regex("[a-z0-9_]{3,20}").matches(username),modifier=Modifier.fillMaxWidth()){Text(stringResource(if(state.saving) R.string.saving else R.string.save_changes))}
            HorizontalDivider(Modifier.padding(top=24.dp)); TextButton(onClick={deleteStep=1}){Text(stringResource(R.string.bp_delete_account),color=MaterialTheme.colorScheme.error)}
        }
    }
    if(deleteStep>0) AlertDialog(onDismissRequest={deleteStep=0},title={Text(stringResource(if(deleteStep==1) R.string.delete_account_question else R.string.final_confirmation))},text={Text(stringResource(if(deleteStep==1) R.string.delete_account_permanent else R.string.delete_account_unavailable))},confirmButton={TextButton(onClick={if(deleteStep==1) deleteStep=2 else deleteStep=0}){Text(stringResource(if(deleteStep==1) R.string.continue_action else R.string.wishlist_close))}},dismissButton=if(deleteStep==1){{TextButton(onClick={deleteStep=0}){Text(stringResource(R.string.planner_date_cancel))}}}else null)
}

@Composable fun ChangePasswordScreen(onBack:()->Unit,vm:SettingsDetailViewModel=hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle(); var pass by remember{mutableStateOf("")}; var confirm by remember{mutableStateOf("")}; var show by remember{mutableStateOf(false)}
    DetailScaffold(stringResource(R.string.settings_change_password),onBack) {
        OutlinedTextField(pass,{pass=it},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_new_password))},visualTransformation=if(show)VisualTransformation.None else PasswordVisualTransformation(),trailingIcon={IconButton(onClick={show=!show}){Icon(if(show)Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,null)}})
        OutlinedTextField(confirm,{confirm=it},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_confirm_new_password))},visualTransformation=if(show)VisualTransformation.None else PasswordVisualTransformation())
        if(pass.isNotEmpty()&&pass.length<8) Text(stringResource(R.string.ui_use_at_least_8_characters),color=MaterialTheme.colorScheme.error)
        if(confirm.isNotEmpty()&&pass!=confirm) Text(stringResource(R.string.signup_error_password_mismatch),color=MaterialTheme.colorScheme.error)
        state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}; state.message?.let{Text(it,color=MaterialTheme.colorScheme.primary)}
        Button(onClick={vm.updatePassword(pass)},enabled=!state.saving&&pass.length>=8&&pass==confirm,modifier=Modifier.fillMaxWidth()){Text(stringResource(if(state.saving) R.string.updating else R.string.update_password))}
    }
}

@Composable fun ChoiceScreen(title:String,choices:List<Pair<String,String>>,selected:String,onSelect:(String)->Unit,onBack:()->Unit){
    DetailScaffold(title,onBack){choices.forEach{(value,label)-> Row(Modifier.fillMaxWidth().clickable{onSelect(value)}.padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically){RadioButton(selected==value,{onSelect(value)});Spacer(Modifier.width(12.dp));Text(label)}}}
}

@Composable fun PermissionsScreen(onBack:()->Unit){
    val context=LocalContext.current; val owner=androidx.lifecycle.compose.LocalLifecycleOwner.current; var granted by remember{mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED)}
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){granted=it.values.any{v->v}}
    DisposableEffect(owner){val observer=androidx.lifecycle.LifecycleEventObserver{_,event->if(event==androidx.lifecycle.Lifecycle.Event.ON_RESUME)granted=ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED};owner.lifecycle.addObserver(observer);onDispose{owner.lifecycle.removeObserver(observer)}}
    DetailScaffold(stringResource(R.string.settings_location_permissions),onBack){Text(stringResource(R.string.destination_details_location),style=MaterialTheme.typography.titleMedium);Text(stringResource(if(granted)R.string.permission_allowed else R.string.permission_denied));Text(stringResource(R.string.ui_used_for_current_location_weather_nearby_places_and_ma));Button(onClick={launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION))}){Text(stringResource(if(granted)R.string.permission_review else R.string.permission_allow_location))};TextButton(onClick={context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${context.packageName}")))}){Text(stringResource(R.string.ui_open_android_app_settings))}}
}

@Composable fun PrivacyScreen(onBack:()->Unit,vm:SettingsDetailViewModel=hiltViewModel()){
    val state by vm.state.collectAsStateWithLifecycle(); DetailScaffold(stringResource(R.string.settings_privacy),onBack){Text(stringResource(R.string.ui_profile_discoverability),style=MaterialTheme.typography.titleMedium);Row(verticalAlignment=Alignment.CenterVertically){Text(stringResource(R.string.ui_allow_other_ekatayan_users_to_find_me_by_username),Modifier.weight(1f));Switch(state.profile?.isDiscoverable?:true,{vm.setDiscoverable(it)},enabled=state.profile!=null&&!state.saving)};HorizontalDivider();Text(stringResource(R.string.ui_profile_visibility),style=MaterialTheme.typography.titleMedium);Text(stringResource(R.string.ui_only_safe_public_profile_details_display_name_username));Text(stringResource(R.string.ui_location_privacy),style=MaterialTheme.typography.titleMedium);Text(stringResource(R.string.ui_location_is_used_only_for_enabled_location_based_featu));state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}
}

private fun cacheBytes(file:java.io.File):Long=file.listFiles()?.sumOf{if(it.isDirectory)cacheBytes(it)else it.length()}?:0
private fun formatBytes(v:Long)=when{v>=1024*1024->"%.1f MB".format(v/1048576.0);v>=1024->"%.1f KB".format(v/1024.0);else->"$v B"}
@Composable fun StorageScreen(onBack:()->Unit){val c=LocalContext.current;var size by remember{mutableLongStateOf(cacheBytes(c.cacheDir))};var done by remember{mutableStateOf(false)};DetailScaffold(stringResource(R.string.settings_data_storage),onBack){Text(stringResource(R.string.ui_cache_size),style=MaterialTheme.typography.titleMedium);Text(formatBytes(size));Text(stringResource(R.string.ui_clearing_cache_removes_temporary_files_only_trips_memb));Button(onClick={c.cacheDir.listFiles()?.forEach{it.deleteRecursively()};size=cacheBytes(c.cacheDir);done=true}){Text(stringResource(R.string.ui_clear_cache))};if(done)Text(stringResource(R.string.ui_cache_cleared),color=MaterialTheme.colorScheme.primary)}}

@Composable fun HelpScreen(onBack:()->Unit){
    val context = LocalContext.current
    val faq=listOf(
        R.string.faq_create_trip_question to R.string.faq_create_trip_answer,
        R.string.faq_invite_question to R.string.faq_invite_answer,
        R.string.faq_expenses_question to R.string.faq_expenses_answer,
        R.string.faq_ai_question to R.string.faq_ai_answer,
        R.string.faq_language_question to R.string.faq_language_answer,
        R.string.faq_location_question to R.string.faq_location_answer,
    )
    var subject by remember{mutableStateOf("")};var description by remember{mutableStateOf("")}
    val report = stringResource(R.string.support_report_body, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, Build.VERSION.RELEASE, description)
    val reportSubject = stringResource(R.string.support_report_subject, subject)
    val shareChooserTitle = stringResource(R.string.support_share_chooser)
    DetailScaffold(stringResource(R.string.settings_help_support),onBack){LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){
        item{Text(stringResource(R.string.ui_frequently_asked_questions),style=MaterialTheme.typography.titleLarge)}
        items(faq.size){i->var open by remember{mutableStateOf(false)};Card(Modifier.fillMaxWidth().clickable{open=!open}){Column(Modifier.padding(16.dp)){Text(stringResource(faq[i].first),style=MaterialTheme.typography.titleSmall);if(open)Text(stringResource(faq[i].second),Modifier.padding(top=8.dp))}}}
        item{
            Text(stringResource(R.string.ui_report_a_problem),style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=16.dp))
            OutlinedTextField(subject,{subject=it},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_subject_category))})
            OutlinedTextField(description,{description=it},Modifier.fillMaxWidth(),label={Text(stringResource(R.string.ui_description))},minLines=3)
            Button(
                onClick={
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, reportSubject)
                        putExtra(Intent.EXTRA_TEXT, report)
                    }, shareChooserTitle))
                },
                enabled=subject.isNotBlank() && description.isNotBlank(),
            ){Text(stringResource(R.string.ui_share_report))}
            Text(stringResource(R.string.ui_choose_where_to_send_the_report_ekatayan_does_not_tran),style=MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.ui_how_ekatayan_works),style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=16.dp))
            Text(stringResource(R.string.ui_plan_trips_invite_members_coordinate_itineraries_and_t))
        }
    }}
}

@Composable fun AboutScreen(onBack:()->Unit)=DetailScaffold(stringResource(R.string.settings_about_ekatayan),onBack){Text(stringResource(R.string.bp_brand),style=MaterialTheme.typography.headlineMedium);Text(stringResource(R.string.app_version_full,BuildConfig.VERSION_NAME,BuildConfig.VERSION_CODE));Text(stringResource(R.string.ui_ekatayan_is_an_ai_powered_sri_lankan_travel_and_coordi));Text(stringResource(R.string.ui_developed_as_part_of_the_iit_infoschol_project))}
@Composable fun LegalScreen(onBack:()->Unit){
    val context = LocalContext.current
    var showTerms by remember{mutableStateOf(false)}
    val terms = remember(context) { runCatching { context.assets.open("privacy.txt").bufferedReader().use { it.readText() } }.getOrDefault("") }
    DetailScaffold(if(showTerms)stringResource(R.string.terms_of_service) else stringResource(R.string.settings_terms_privacy),onBack=if(!showTerms)onBack else {{showTerms=false}}){
        if(!showTerms){
            ListItem(headlineContent={Text(stringResource(R.string.signup_terms))},supportingContent={Text(stringResource(R.string.ui_view_the_document_supplied_with_this_app))},modifier=Modifier.clickable(enabled=terms.isNotBlank()){showTerms=true})
            ListItem(headlineContent={Text(stringResource(R.string.signup_privacy))},supportingContent={Text(stringResource(R.string.ui_a_privacy_policy_document_has_not_been_supplied_yet))})
        } else {
            LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(10.dp)){
                items(terms.lines().size){index->
                    val line=terms.lines()[index]
                    when {
                        line.startsWith("# ") -> Text(line.removePrefix("# "),style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                        line.startsWith("## ") -> Text(line.removePrefix("## "),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp))
                        line.startsWith("* ") -> Text("• ${line.removePrefix("* ").replace("**","")}",style=MaterialTheme.typography.bodyMedium)
                        line == "---" || line.isBlank() -> Spacer(Modifier.height(2.dp))
                        else -> Text(line.replace("**","").replace("â€œ","“").replace("â€","”").replace("â€™","’"),style=MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
