package com.ekatayan.app.feature.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AuthBrandLockup
import com.ekatayan.app.core.designsystem.theme.EkataYanTheme

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = BiasAlignment(0f, -0.25f),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.splash_logo),
                    contentDescription = null,
                    modifier = Modifier.size(166.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(Modifier.height(8.dp))
                AuthBrandLockup()
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=402dp,height=874dp,dpi=440")
@Composable
fun SplashScreenPreview() {
    EkataYanTheme {
        SplashScreen()
    }
}
