package dev.dantech.klokk.billing

private val sharedPurchases: PlusPurchases = MockPlusPurchases()

actual fun defaultPlusPurchases(): PlusPurchases = sharedPurchases

actual fun openSubscriptionManagement() = Unit
