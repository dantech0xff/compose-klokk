package com.theapache64.klokk.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theapache64.klokk.theme.KlokkTheme

@Composable
fun KlokkRingScreen(
    title: String,
    allowSnooze: Boolean,
    snoozeLabel: String,
    dismissLabel: String,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
) {
    KlokkTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, fontSize = 36.sp)
            if (allowSnooze) {
                Button(
                    onClick = onSnooze,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                ) {
                    Text(snoozeLabel)
                }
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text(dismissLabel)
            }
        }
    }
}
