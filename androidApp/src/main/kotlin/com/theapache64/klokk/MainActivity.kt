package com.theapache64.klokk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.theapache64.klokk.persist.initKlokkAndroid
import com.theapache64.klokk.theme.KlokkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initKlokkAndroid(this)

        setContent {
            KlokkTheme {
                App()
            }
        }
    }
}
