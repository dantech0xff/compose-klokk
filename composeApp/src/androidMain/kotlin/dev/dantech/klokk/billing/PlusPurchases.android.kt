package dev.dantech.klokk.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import dev.dantech.klokk.state.PlusPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RevenueCatKeyHolder {
    var value: String = ""
}

private var sharedPurchases: PlusPurchases? = null

actual fun defaultPlusPurchases(): PlusPurchases {
    sharedPurchases?.let { return it }
    val key = RevenueCatKeyHolder.value
    val created = if (key.isBlank()) InertPlusPurchases() else RevenueCatPlusPurchases(key)
    sharedPurchases = created
    return created
}

actual fun openSubscriptionManagement() {
    val context = dev.dantech.klokk.persist.KlokkAndroid.appContext ?: return
    val intent = android.content.Intent(
        android.content.Intent.ACTION_VIEW,
        android.net.Uri.parse(
            "https://play.google.com/store/account/subscriptions?package=${context.packageName}",
        ),
    ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

private class RevenueCatPlusPurchases(key: String) : PlusPurchases {
    override val configured: Boolean = true
    private val packages = mutableMapOf<PlusPlan, Package>()
    private val main = CoroutineScope(Dispatchers.Main)

    init {
        if (!configuredKey) {
            Purchases.configure(PurchasesConfiguration.Builder(key).build())
            configuredKey = true
        }
    }

    override suspend fun loadPrices(): Map<PlusPlan, String> {
        val current = Purchases.sharedInstance.awaitOfferings().current ?: return emptyMap()
        packages.clear()
        val prices = mutableMapOf<PlusPlan, String>()
        for (pkg in current.availablePackages) {
            val plan = planOf(pkg) ?: continue
            packages[plan] = pkg
            prices[plan] = pkg.storeProduct.price.formatted
        }
        return prices
    }

    override suspend fun purchase(plan: PlusPlan): PurchaseOutcome {
        val pkg = packages[plan] ?: run {
            loadPrices()
            packages[plan]
        } ?: return PurchaseOutcome.Failed("That plan is not in the current offering.")
        return try {
            val result = Purchases.sharedInstance.awaitPurchase(packageToPurchase = pkg)
            if (isPlus(result.customerInfo)) PurchaseOutcome.Success else PurchaseOutcome.Pending
        } catch (error: PurchasesTransactionException) {
            outcome(error)
        }
    }

    override suspend fun restore(): PurchaseOutcome {
        return try {
            val info = Purchases.sharedInstance.awaitRestore()
            if (isPlus(info)) PurchaseOutcome.Success else PurchaseOutcome.Failed("No active Klokk Plus purchase.")
        } catch (error: PurchasesException) {
            outcome(error)
        }
    }

    override fun listen(onPlus: (Boolean) -> Unit) {
        Purchases.sharedInstance.delegate = object : PurchasesDelegate {
            override fun onPurchasePromoProduct(
                product: StoreProduct,
                startPurchase: (
                    onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
                    onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit,
                ) -> Unit,
            ) = Unit

            override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
                val active = isPlus(customerInfo)
                main.launch { onPlus(active) }
            }
        }
        main.launch {
            try {
                onPlus(isPlus(Purchases.sharedInstance.awaitCustomerInfo()))
            } catch (_: PurchasesException) {
                Unit
            }
        }
    }

    private fun planOf(pkg: Package): PlusPlan? = when (pkg.identifier) {
        PACKAGE_ANNUAL -> PlusPlan.YEAR
        PACKAGE_MONTHLY -> PlusPlan.MONTH
        PACKAGE_LIFETIME -> PlusPlan.LIFE
        else -> when (pkg.packageType) {
            PackageType.ANNUAL -> PlusPlan.YEAR
            PackageType.MONTHLY -> PlusPlan.MONTH
            PackageType.LIFETIME -> PlusPlan.LIFE
            else -> null
        }
    }

    private fun isPlus(info: CustomerInfo): Boolean =
        info.entitlements[ENTITLEMENT_PLUS]?.isActive == true

    private fun outcome(error: PurchasesException): PurchaseOutcome {
        val cancelled = error is PurchasesTransactionException &&
            (error.userCancelled || error.code == PurchasesErrorCode.PurchaseCancelledError)
        if (cancelled || error.code == PurchasesErrorCode.PurchaseCancelledError) {
            return PurchaseOutcome.Cancelled
        }
        if (error.code == PurchasesErrorCode.PaymentPendingError) return PurchaseOutcome.Pending
        return PurchaseOutcome.Failed(error.message)
    }

    companion object {
        private var configuredKey = false
    }
}
