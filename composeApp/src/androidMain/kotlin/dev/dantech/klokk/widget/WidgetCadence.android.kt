package dev.dantech.klokk.widget

object HomeWidgetRefresher {
    var refresh: () -> Unit = {}
}

actual fun refreshHomeWidgets() {
    HomeWidgetRefresher.refresh()
}
