package com.theapache64.klokk.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.Foundation.NSNotificationCenter

/**
 * Notifies the Swift container view controller (KlokkRootViewController in
 * iosApp) which drives `preferredStatusBarStyle` — `UIViewControllerBasedStatusBarAppearance`
 * is YES in the iosApp Info.plist.
 */
@Composable
actual fun KlokkSystemBarStyle(dark: Boolean) {
    SideEffect {
        NSNotificationCenter.defaultCenter.postNotificationName(
            if (dark) "KlokkStatusBarStyleDark" else "KlokkStatusBarStyleLight",
            null,
        )
    }
}
