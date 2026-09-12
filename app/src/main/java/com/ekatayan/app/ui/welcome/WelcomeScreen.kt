package com.ekatayan.app.ui.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AuthActionBlue
import com.ekatayan.app.core.designsystem.component.AuthBrandLockup
import kotlin.math.roundToInt

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, modifier: Modifier = Modifier) {
    val landscape = ImageBitmap.imageResource(R.drawable.signup_background)
    val artwork = ImageBitmap.imageResource(R.drawable.welcome_reference)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawImage(
                image = landscape,
                srcSize = IntSize(landscape.width, (landscape.height * 0.78f).roundToInt()),
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            )
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val availableHeight = maxHeight
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 440.dp).fillMaxWidth()
                        .heightIn(min = availableHeight).padding(horizontal = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height((availableHeight * 0.065f).coerceIn(16.dp, 58.dp)))
                    Image(
                        painter = painterResource(R.drawable.signup_logo),
                        contentDescription = null,
                        modifier = Modifier.size(width = 150.dp, height = 150.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.height(6.dp))
                    AuthBrandLockup()
                    Spacer(Modifier.height((availableHeight * 0.07f).coerceIn(24.dp, 60.dp)))
                    Text(
                        text = stringResource(R.string.welcome_description),
                        color = Color.Black.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 330.dp),
                    )
                    Spacer(Modifier.height((availableHeight * 0.095f).coerceIn(24.dp, 82.dp)))
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(15.dp),
                        border = BorderStroke(1.dp, Color(0xFFE9E9E9)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 23.dp, vertical = 23.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            WelcomeFeatures(artwork)
                            Spacer(Modifier.height(13.dp))
                            Button(
                                onClick = onGetStarted,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).heightIn(min = 48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AuthActionBlue, contentColor = Color.White),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                            ) {
                                Text(stringResource(R.string.welcome_get_started), fontSize = 14.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

private data class WelcomeFeature(
    val title: Int,
    val description: Int,
    val iconOffset: IntOffset,
)

private val welcomeFeatures = listOf(
    WelcomeFeature(R.string.welcome_planner_title, R.string.welcome_planner_description, IntOffset(59, 509)),
    WelcomeFeature(R.string.welcome_group_title, R.string.welcome_group_description, IntOffset(140, 509)),
    WelcomeFeature(R.string.welcome_expense_title, R.string.welcome_expense_description, IntOffset(222, 509)),
    WelcomeFeature(R.string.welcome_all_title, R.string.welcome_all_description, IntOffset(303, 509)),
    WelcomeFeature(R.string.welcome_explore_title, R.string.welcome_explore_description, IntOffset(59, 606)),
    WelcomeFeature(R.string.welcome_weather_title, R.string.welcome_weather_description, IntOffset(140, 606)),
    WelcomeFeature(R.string.welcome_reminders_title, R.string.welcome_reminders_description, IntOffset(222, 606)),
    WelcomeFeature(R.string.welcome_secure_title, R.string.welcome_secure_description, IntOffset(303, 606)),
)

@Composable
private fun WelcomeFeatures(artwork: ImageBitmap) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 260.dp) 2 else 4
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            welcomeFeatures.chunked(columns).forEach { features ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEach { feature ->
                        Column(
                            modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {},
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Canvas(Modifier.size(42.dp)) {
                                drawImage(
                                    image = artwork,
                                    srcOffset = IntOffset(
                                        (artwork.width * feature.iconOffset.x / 402f).roundToInt(),
                                        (artwork.height * feature.iconOffset.y / 874f).roundToInt(),
                                    ),
                                    srcSize = IntSize(
                                        (artwork.width * 42f / 402f).roundToInt(),
                                        (artwork.height * 42f / 874f).roundToInt(),
                                    ),
                                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = stringResource(feature.title),
                                color = Color(0xFF27384D),
                                fontSize = 10.sp,
                                lineHeight = 13.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = stringResource(feature.description),
                                color = Color(0xFF455468),
                                fontSize = 9.sp,
                                lineHeight = 12.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
