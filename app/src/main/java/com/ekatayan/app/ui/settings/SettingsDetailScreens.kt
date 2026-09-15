package com.ekatayan.app.ui.settings

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
    DetailScaffold("Account Information",onBack) {
        if(state.loading && p==null) CircularProgressIndicator() else if(p==null) { Text(state.error ?: "Account information is unavailable."); Button(onClick=vm::refresh){Text("Retry")} } else {
            OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Full name")})
            OutlinedTextField(username,{username=it.lowercase().filter { c->c.isLetterOrDigit()||c=='_' }.take(20)},Modifier.fillMaxWidth(),label={Text("Username")},prefix={Text("@")})
            OutlinedTextField(p.email,{},Modifier.fillMaxWidth(),enabled=false,label={Text("Email (read-only)")})
            OutlinedTextField(phone,{phone=it},Modifier.fillMaxWidth(),label={Text("Phone number")})
            state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}; state.message?.let{Text(it,color=MaterialTheme.colorScheme.primary)}
            Button(onClick={vm.saveAccount(name,username,phone)},enabled=!state.saving && name.isNotBlank() && Regex("[a-z0-9_]{3,20}").matches(username),modifier=Modifier.fillMaxWidth()){Text(if(state.saving)"Saving…" else "Save changes")}
            HorizontalDivider(Modifier.padding(top=24.dp)); TextButton(onClick={deleteStep=1}){Text("Delete Account",color=MaterialTheme.colorScheme.error)}
        }
    }
    if(deleteStep>0) AlertDialog(onDismissRequest={deleteStep=0},title={Text(if(deleteStep==1)"Delete account?" else "Final confirmation")},text={Text(if(deleteStep==1)"Account deletion is permanent. Continue to the final confirmation?" else "Secure account deletion is not available yet. No data has been deleted.")},confirmButton={TextButton(onClick={if(deleteStep==1) deleteStep=2 else deleteStep=0}){Text(if(deleteStep==1)"Continue" else "Close")}},dismissButton=if(deleteStep==1){{TextButton(onClick={deleteStep=0}){Text("Cancel")}}}else null)
}

@Composable fun ChangePasswordScreen(onBack:()->Unit,vm:SettingsDetailViewModel=hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle(); var pass by remember{mutableStateOf("")}; var confirm by remember{mutableStateOf("")}; var show by remember{mutableStateOf(false)}
    DetailScaffold("Change Password",onBack) {
        OutlinedTextField(pass,{pass=it},Modifier.fillMaxWidth(),label={Text("New password")},visualTransformation=if(show)VisualTransformation.None else PasswordVisualTransformation(),trailingIcon={IconButton(onClick={show=!show}){Icon(if(show)Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,null)}})
        OutlinedTextField(confirm,{confirm=it},Modifier.fillMaxWidth(),label={Text("Confirm new password")},visualTransformation=if(show)VisualTransformation.None else PasswordVisualTransformation())
        if(pass.isNotEmpty()&&pass.length<8) Text("Use at least 8 characters.",color=MaterialTheme.colorScheme.error)
        if(confirm.isNotEmpty()&&pass!=confirm) Text("Passwords do not match.",color=MaterialTheme.colorScheme.error)
        state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}; state.message?.let{Text(it,color=MaterialTheme.colorScheme.primary)}
        Button(onClick={vm.updatePassword(pass)},enabled=!state.saving&&pass.length>=8&&pass==confirm,modifier=Modifier.fillMaxWidth()){Text(if(state.saving)"Updating…" else "Update password")}
    }
}

@Composable fun ChoiceScreen(title:String,choices:List<Pair<String,String>>,selected:String,onSelect:(String)->Unit,onBack:()->Unit){
    DetailScaffold(title,onBack){choices.forEach{(value,label)-> Row(Modifier.fillMaxWidth().clickable{onSelect(value)}.padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically){RadioButton(selected==value,{onSelect(value)});Spacer(Modifier.width(12.dp));Text(label)}}}
}

@Composable fun PermissionsScreen(onBack:()->Unit){
    val context=LocalContext.current; val owner=androidx.lifecycle.compose.LocalLifecycleOwner.current; var granted by remember{mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED)}
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){granted=it.values.any{v->v}}
    DisposableEffect(owner){val observer=androidx.lifecycle.LifecycleEventObserver{_,event->if(event==androidx.lifecycle.Lifecycle.Event.ON_RESUME)granted=ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION)==android.content.pm.PackageManager.PERMISSION_GRANTED};owner.lifecycle.addObserver(observer);onDispose{owner.lifecycle.removeObserver(observer)}}
    DetailScaffold("Location & Permissions",onBack){Text("Location",style=MaterialTheme.typography.titleMedium);Text(if(granted)"Allowed" else "Denied");Text("Used for current-location weather, nearby places and map features. Your live location is not shared with other users.");Button(onClick={launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION))}){Text(if(granted)"Review permission" else "Allow location")};TextButton(onClick={context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:${context.packageName}")))}){Text("Open Android app settings")}}
}

@Composable fun PrivacyScreen(onBack:()->Unit,vm:SettingsDetailViewModel=hiltViewModel()){
    val state by vm.state.collectAsStateWithLifecycle(); DetailScaffold("Privacy",onBack){Text("Profile discoverability",style=MaterialTheme.typography.titleMedium);Row(verticalAlignment=Alignment.CenterVertically){Text("Allow other EkataYan users to find me by username",Modifier.weight(1f));Switch(state.profile?.isDiscoverable?:true,{vm.setDiscoverable(it)},enabled=state.profile!=null&&!state.saving)};HorizontalDivider();Text("Profile visibility",style=MaterialTheme.typography.titleMedium);Text("Only safe public profile details—display name, username and avatar—are shown in member search.");Text("Location privacy",style=MaterialTheme.typography.titleMedium);Text("Location is used only for enabled location-based features and is not exposed to other users.");state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}
}

private fun cacheBytes(file:java.io.File):Long=file.listFiles()?.sumOf{if(it.isDirectory)cacheBytes(it)else it.length()}?:0
private fun formatBytes(v:Long)=when{v>=1024*1024->"%.1f MB".format(v/1048576.0);v>=1024->"%.1f KB".format(v/1024.0);else->"$v B"}
@Composable fun StorageScreen(onBack:()->Unit){val c=LocalContext.current;var size by remember{mutableLongStateOf(cacheBytes(c.cacheDir))};var done by remember{mutableStateOf(false)};DetailScaffold("Data & Storage",onBack){Text("Cache size",style=MaterialTheme.typography.titleMedium);Text(formatBytes(size));Text("Clearing cache removes temporary files only. Trips, memberships, expenses and your sign-in remain safe.");Button(onClick={c.cacheDir.listFiles()?.forEach{it.deleteRecursively()};size=cacheBytes(c.cacheDir);done=true}){Text("Clear cache")};if(done)Text("Cache cleared.",color=MaterialTheme.colorScheme.primary)}}

@Composable fun HelpScreen(onBack:()->Unit){
    val context = LocalContext.current
    val faq=listOf("How do I create a trip?" to "Open Trips and choose Add Trip or use the AI Planner.","How do I invite someone to a trip?" to "Open a trip, choose Members, then search their @username.","How do split expenses work?" to "Expenses are shared equally between the selected trip members.","How do AI itineraries work?" to "Your planner preferences are sent securely to the backend to create a structured itinerary.","How do I change my language?" to "Choose Language in Settings.","Why is location permission needed?" to "For current-location weather, nearby places and maps.")
    var subject by remember{mutableStateOf("")};var description by remember{mutableStateOf("")}
    DetailScaffold("Help & Support",onBack){LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp)){
        item{Text("Frequently Asked Questions",style=MaterialTheme.typography.titleLarge)}
        items(faq.size){i->var open by remember{mutableStateOf(false)};Card(Modifier.fillMaxWidth().clickable{open=!open}){Column(Modifier.padding(16.dp)){Text(faq[i].first,style=MaterialTheme.typography.titleSmall);if(open)Text(faq[i].second,Modifier.padding(top=8.dp))}}}
        item{
            Text("Report a Problem",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=16.dp))
            OutlinedTextField(subject,{subject=it},Modifier.fillMaxWidth(),label={Text("Subject / category")})
            OutlinedTextField(description,{description=it},Modifier.fillMaxWidth(),label={Text("Description")},minLines=3)
            Button(
                onClick={
                    val report = "EkataYan ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nAndroid ${Build.VERSION.RELEASE}\n\n$description"
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "EkataYan: $subject")
                        putExtra(Intent.EXTRA_TEXT, report)
                    }, "Share support request"))
                },
                enabled=subject.isNotBlank() && description.isNotBlank(),
            ){Text("Share report")}
            Text("Choose where to send the report. EkataYan does not transmit it automatically.",style=MaterialTheme.typography.bodySmall)
            Text("How EkataYan Works",style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=16.dp))
            Text("Plan trips, invite members, coordinate itineraries and track shared expenses in one place.")
        }
    }}
}

@Composable fun AboutScreen(onBack:()->Unit)=DetailScaffold("About EkataYan",onBack){Text("EkataYan",style=MaterialTheme.typography.headlineMedium);Text("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})");Text("EkataYan is an AI-powered Sri Lankan travel and coordination platform designed to make trip planning, group communication and shared expenses easier.");Text("Developed as part of the IIT InfoSchol project.")}
@Composable fun LegalScreen(onBack:()->Unit){
    val context = LocalContext.current
    var showTerms by remember{mutableStateOf(false)}
    val terms = remember(context) { runCatching { context.assets.open("privacy.txt").bufferedReader().use { it.readText() } }.getOrDefault("") }
    DetailScaffold(if(showTerms)"Terms of Service" else "Terms & Privacy Policy",onBack=if(!showTerms)onBack else {{showTerms=false}}){
        if(!showTerms){
            ListItem(headlineContent={Text("Terms of Service")},supportingContent={Text("View the document supplied with this app")},modifier=Modifier.clickable(enabled=terms.isNotBlank()){showTerms=true})
            ListItem(headlineContent={Text("Privacy Policy")},supportingContent={Text("A Privacy Policy document has not been supplied yet")})
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
