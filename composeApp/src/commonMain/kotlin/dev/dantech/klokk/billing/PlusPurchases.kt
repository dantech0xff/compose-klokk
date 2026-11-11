package dev.dantech.klokk.billing

import dev.dantech.klokk.state.KlokkState
import dev.dantech.klokk.state.PlusPlan
import dev.dantech.klokk.state.PurchaseNotice
import kotlinx.coroutines.delay

sealed interface PurchaseOutcome {
    data object Success : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data object Pending : PurchaseOutcome
    data class Failed(val message: String) : PurchaseOutcome
    data object Unavailable : PurchaseOutcome
}

interface PlusPurchases {
    val configured: Boolean
    suspend fun loadPrices(): Map<PlusPlan, String>
    suspend fun purchase(plan: PlusPlan): PurchaseOutcome
    suspend fun restore(): PurchaseOutcome
    fun listen(onPlus: (Boolean) -> Unit)
}

class FakePlusPurchases(
    override val configured: Boolean = true,
    var prices: Map<PlusPlan, String> = emptyMap(),
    var nextPurchase: PurchaseOutcome = PurchaseOutcome.Success,
    var nextRestore: PurchaseOutcome = PurchaseOutcome.Success,
    private val listenerPlus: Boolean? = null,
) : PlusPurchases {
    private var listener: ((Boolean) -> Unit)? = null

    override suspend fun loadPrices(): Map<PlusPlan, String> = prices

    override suspend fun purchase(plan: PlusPlan): PurchaseOutcome = nextPurchase

    override suspend fun restore(): PurchaseOutcome = nextRestore

    override fun listen(onPlus: (Boolean) -> Unit) {
        listener = onPlus
        listenerPlus?.let(onPlus)
    }

    fun emit(active: Boolean) {
        listener?.invoke(active)
    }
}

class InertPlusPurchases : PlusPurchases {
    override val configured: Boolean = false
    override suspend fun loadPrices(): Map<PlusPlan, String> = emptyMap()
    override suspend fun purchase(plan: PlusPlan): PurchaseOutcome = PurchaseOutcome.Unavailable
    override suspend fun restore(): PurchaseOutcome = PurchaseOutcome.Unavailable
    override fun listen(onPlus: (Boolean) -> Unit) = Unit
}

/** Desktop stand-in. A purchase succeeds after the same short wait the app used before. */
class MockPlusPurchases : PlusPurchases {
    override val configured: Boolean = true
    override suspend fun loadPrices(): Map<PlusPlan, String> = emptyMap()
    override suspend fun purchase(plan: PlusPlan): PurchaseOutcome {
        delay(900)
        return PurchaseOutcome.Success
    }
    override suspend fun restore(): PurchaseOutcome = PurchaseOutcome.Success
    override fun listen(onPlus: (Boolean) -> Unit) = Unit
}

fun KlokkState.finish(outcome: PurchaseOutcome) {
    when (outcome) {
        PurchaseOutcome.Success -> onUnlocked()
        PurchaseOutcome.Cancelled -> {
            buying = false
            restoring = false
            purchaseNotice = PurchaseNotice.CANCELLED
        }
        PurchaseOutcome.Pending -> {
            buying = false
            restoring = false
            purchaseNotice = PurchaseNotice.PENDING
        }
        is PurchaseOutcome.Failed -> {
            buying = false
            restoring = false
            purchaseNotice = PurchaseNotice.FAILED
            purchaseDetail = outcome.message
        }
        PurchaseOutcome.Unavailable -> {
            buying = false
            restoring = false
            purchaseNotice = PurchaseNotice.UNAVAILABLE
        }
    }
}

const val ENTITLEMENT_PLUS = "plus"
const val PACKAGE_ANNUAL = "\$rc_annual"
const val PACKAGE_MONTHLY = "\$rc_monthly"
const val PACKAGE_LIFETIME = "\$rc_lifetime"

expect fun defaultPlusPurchases(): PlusPurchases

expect fun openSubscriptionManagement()
