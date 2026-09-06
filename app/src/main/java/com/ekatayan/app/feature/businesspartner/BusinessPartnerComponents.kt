package com.ekatayan.app.feature.businesspartner

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.*
import com.ekatayan.app.feature.wishlist.WishlistPopupBorder
import com.ekatayan.app.feature.wishlist.WishlistPopupSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

internal val PartnerNavy = Color(0xFF072B60)
internal val PartnerShape = RoundedCornerShape(16.dp)

@Composable
internal fun PartnerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = EkataBlue, onPrimary = Color.White, background = EkataBackground,
        surface = Color.White, onSurface = EkataTextPrimary, onBackground = EkataTextPrimary,
        onSurfaceVariant = EkataTextSecondary, surfaceVariant = EkataLightBlue,
        secondaryContainer = EkataLightBlue, onSecondaryContainer = PartnerNavy,
    ), typography = MaterialTheme.typography, content = content)
}

@Composable
internal fun PartnerHeading(text: String, subtitle: String? = null) {
    Text(text, color = PartnerNavy, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    subtitle?.let { Text(it, color = EkataTextSecondary, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
internal fun PartnerField(value: String, onChange: (String) -> Unit, @StringRes label: Int,
    modifier: Modifier = Modifier, multiline: Boolean = false, keyboard: KeyboardType = KeyboardType.Text,
    hint: String? = null, error: String? = null, password: Boolean = false) {
    OutlinedTextField(value, onChange, modifier.fillMaxWidth(), label = { Text(stringResource(label)) },
        placeholder = hint?.let { { Text(it, color = EkataTextSecondary) } },
        singleLine = !multiline, minLines = if (multiline) 4 else 1,
        shape = RoundedCornerShape(12.dp), isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black, unfocusedTextColor = Color.Black, errorTextColor = Color.Black,
            disabledTextColor = Color.Black, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            focusedBorderColor = EkataBlue, unfocusedBorderColor = WishlistPopupBorder,
        ))
}

@Composable
internal fun PartnerButton(@StringRes label: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 50.dp), enabled = enabled, shape = RoundedCornerShape(14.dp)) {
        Text(stringResource(label), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun PartnerCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        color = Color.White, contentColor = EkataTextPrimary, shape = PartnerShape,
        shadowElevation = 2.dp, border = BorderStroke(1.dp, EkataLightBlue)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
internal fun PartnerAction(label: String, icon: ImageVector, onClick: () -> Unit, selected: Boolean = false) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = if (selected) EkataLightBlue else Color.White,
        border = BorderStroke(1.dp, if (selected) EkataBlue else EkataLightBlue), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = EkataBlue, modifier = Modifier.size(28.dp))
            Text(label, Modifier.weight(1f), color = PartnerNavy, style = MaterialTheme.typography.bodyMedium)
            Icon(if (selected) Icons.Default.CheckCircle else Icons.Default.ChevronRight, null, tint = PartnerNavy)
        }
    }
}

@Composable
internal fun PartnerDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    WishlistPopupSurface(onDismiss) {
        Column(Modifier.heightIn(max = 580.dp).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, color = EkataTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
internal fun DialogAction(@StringRes label: Int, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(onClick, enabled = enabled) { Text(stringResource(label), color = if (enabled) EkataTextPrimary else Color.Gray) }
}

@Composable
internal fun PartnerBack(onBack: () -> Unit) {
    IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.bp_back), tint = PartnerNavy) }
}

@Composable
internal fun PartnerImage(uri: String?, imageRes: Int? = null, modifier: Modifier = Modifier, description: String? = null) {
    val context = LocalContext.current
    val result by produceState<Pair<String?, ImageBitmap?>?>(null, uri) {
        value = null
        val decoded = if (uri != null) withContext(Dispatchers.IO) {
            runCatching {
                val parsed = Uri.parse(uri)
                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, parsed)) { decoder, info, _ ->
                        val scale = (720f / maxOf(info.size.width, info.size.height)).coerceAtMost(1f)
                        decoder.setTargetSize((info.size.width * scale).toInt().coerceAtLeast(1), (info.size.height * scale).toInt().coerceAtLeast(1))
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }.asImageBitmap()
                } else {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, options) }
                    var sample = 1
                    while (maxOf(options.outWidth, options.outHeight) / sample > 1024) sample *= 2
                    options.inJustDecodeBounds = false
                    options.inSampleSize = sample
                    context.contentResolver.openInputStream(parsed)?.use { BitmapFactory.decodeStream(it, null, options)?.asImageBitmap() }
                }
            }.getOrNull()
        } else null
        value = uri to decoded
    }
    val bitmap = result?.takeIf { it.first == uri }?.second
    Box(modifier.clip(PartnerShape).background(EkataLightBlue), contentAlignment = Alignment.Center) {
        when {
            bitmap != null -> Image(bitmap, description, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            uri == null && imageRes != null -> Image(painterResource(imageRes), description, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            uri != null && result?.first != uri -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            uri != null -> Text(stringResource(R.string.bp_photo_unavailable), Modifier.padding(8.dp), fontSize = 11.sp)
            else -> Icon(Icons.Default.Storefront, description, Modifier.size(36.dp), tint = EkataBlue)
        }
    }
}

@Composable
internal fun PhotoStrip(uris: List<String>, onAdd: () -> Unit, onRemove: (String) -> Unit, imageRes: Int? = null, onRemoveResource: () -> Unit = {}) {
    if (uris.isEmpty() && imageRes == null) Text(stringResource(R.string.bp_no_images), style = MaterialTheme.typography.bodySmall)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(onClick = onAdd, color = EkataLightBlue, shape = PartnerShape, modifier = Modifier.size(100.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(stringResource(R.string.bp_add_photos), color = PartnerNavy, fontSize = 13.sp) }
            }
        }
        if (imageRes != null) item {
            Box {
                PartnerImage(null, imageRes, Modifier.size(100.dp))
                IconButton(onRemoveResource, Modifier.align(Alignment.TopEnd).size(48.dp)) {
                    Icon(Icons.Default.Cancel, stringResource(R.string.bp_remove_photo), tint = PartnerNavy)
                }
            }
        }
        items(uris, key = { it }) { uri ->
            Box {
                PartnerImage(uri, modifier = Modifier.size(100.dp), description = stringResource(R.string.bp_photo))
                IconButton({ onRemove(uri) }, Modifier.align(Alignment.TopEnd).size(48.dp)) {
                    Icon(Icons.Default.Cancel, stringResource(R.string.bp_remove_photo), tint = PartnerNavy)
                }
            }
        }
    }
}

@Composable
internal fun <T> PartnerFilters(options: List<T>, selected: T, label: @Composable (T) -> String, onSelect: (T) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options.size) { index ->
            val option = options[index]
            FilterChip(selected == option, { onSelect(option) }, label = { Text(label(option)) }, shape = RoundedCornerShape(12.dp))
        }
    }
}

@Composable
internal fun StatusBadge(status: ListingStatus) {
    val background = when (status) { ListingStatus.ACTIVE -> Color(0xFFD8F6DC); ListingStatus.INACTIVE -> Color(0xFFEDF0F4); ListingStatus.DRAFT -> Color(0xFFFFEBC6) }
    Text(stringResource(status.label), Modifier.background(background, CircleShape).padding(horizontal = 10.dp, vertical = 3.dp), color = EkataTextPrimary, fontSize = 12.sp)
}

internal fun money(value: Double): String = String.format(Locale.US, if (value % 1.0 == 0.0) "%,.0f" else "%,.2f", value)
internal fun clockLabel(minutes: Int): String = String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60)
