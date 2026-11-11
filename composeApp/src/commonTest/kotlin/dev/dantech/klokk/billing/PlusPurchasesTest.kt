package dev.dantech.klokk.billing

import dev.dantech.klokk.state.KlokkState
import dev.dantech.klokk.state.PaywallReason
import dev.dantech.klokk.state.PlusPlan
import dev.dantech.klokk.state.PurchaseNotice
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlusPurchasesTest {

    @Test
    fun aSuccessfulPurchaseRunsTheActionThatWasGated() = runBlocking {
        val state = KlokkState()
        var ran = false
        state.gate(PaywallReason.CITIES) { ran = true }
        state.unlock()
        state.finish(FakePlusPurchases().purchase(PlusPlan.YEAR))
        assertTrue(state.plus)
        assertTrue(ran)
        assertNull(state.pending)
        assertNull(state.paywall)
        assertFalse(state.buying)
    }

    @Test
    fun cancelAndFailureLeaveTheGatedActionForALaterUnlock() = runBlocking {
        val state = KlokkState()
        var ran = false
        state.gate(PaywallReason.SHOW) { ran = true }
        state.unlock()
        state.finish(PurchaseOutcome.Cancelled)
        assertFalse(ran)
        assertFalse(state.plus)
        assertEquals(PurchaseNotice.CANCELLED, state.purchaseNotice)
        state.unlock()
        state.finish(PurchaseOutcome.Failed("declined"))
        assertFalse(ran)
        assertEquals("declined", state.purchaseDetail)
        state.unlock()
        state.finish(PurchaseOutcome.Success)
        assertTrue(ran)
        assertTrue(state.plus)
    }

    @Test
    fun pendingAndUnavailableDoNotUnlock() {
        val state = KlokkState()
        state.unlock()
        state.finish(PurchaseOutcome.Pending)
        assertEquals(PurchaseNotice.PENDING, state.purchaseNotice)
        assertFalse(state.plus)
        state.finish(PurchaseOutcome.Unavailable)
        assertEquals(PurchaseNotice.UNAVAILABLE, state.purchaseNotice)
        assertFalse(state.plus)
    }

    @Test
    fun restoreAndTheEntitlementListenerFeedPlus() = runBlocking {
        val state = KlokkState()
        val fake = FakePlusPurchases(nextRestore = PurchaseOutcome.Success)
        state.restore()
        state.finish(fake.restore())
        assertTrue(state.plus)
        state.onEntitlement(false)
        assertFalse(state.plus)
        var ran = false
        state.gate(PaywallReason.WIDGETS) { ran = true }
        state.onEntitlement(true)
        assertTrue(state.plus)
        assertTrue(ran)
    }

    @Test
    fun storePricesReplaceTheHardcodedOnesAndAnInertStoreDoesNot() = runBlocking {
        val prices = mapOf(PlusPlan.YEAR to "€9.99", PlusPlan.MONTH to "€1.49", PlusPlan.LIFE to "€19.99")
        val fake = FakePlusPurchases(prices = prices)
        assertEquals(prices, fake.loadPrices())
        val inert = InertPlusPurchases()
        assertFalse(inert.configured)
        assertEquals(PurchaseOutcome.Unavailable, inert.purchase(PlusPlan.YEAR))
        assertTrue(inert.loadPrices().isEmpty())
    }
}
