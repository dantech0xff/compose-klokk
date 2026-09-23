package com.theapache64.klokk.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent

@Composable
actual fun KlokkSystemBarStyle(dark: Boolean) {
    SideEffect {
        // UIViewControllerBasedStatusBarAppearance=NO in the iosApp Info.plist
        // lets this app-level API drive the status bar — the modern per-VC
        // preferredStatusBarStyle can't be reached inside the shared
        // ComposeUIViewController.
        UIApplication.sharedApplication.setStatusBarStyle(
            if (dark) UIStatusBarStyleLightContent else UIStatusBarStyleDarkContent,
            animated = false,
        )
    }
}
