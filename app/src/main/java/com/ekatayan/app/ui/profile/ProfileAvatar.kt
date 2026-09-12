package com.ekatayan.app.ui.profile

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ekatayan.app.R
import androidx.compose.foundation.shape.CircleShape

@Composable
internal fun ProfileAvatar(localPath: String?, modifier: Modifier = Modifier) {
    val bitmap = remember(localPath) {
        localPath?.let { runCatching { BitmapFactory.decodeFile(it)?.asImageBitmap() }.getOrNull() }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = stringResource(R.string.profile_avatar_description),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape),
        )
    } else {
        Image(
            painter = painterResource(R.drawable.profile_avatar_placeholder),
            contentDescription = stringResource(R.string.profile_avatar_description),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(CircleShape),
        )
    }
}
