package com.theapache64.klokk.theme

import androidx.compose.runtime.Composable

/**
 * Keeps the platform status-bar appearance in sync with the app's resolved
 * theme: light icons while [dark], dark icons otherwise.
 */
@Composable
expect fun KlokkSystemBarStyle(dark: Boolean)
