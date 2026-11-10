package com.focusforge.app.monetization

import android.app.Activity
import android.content.Context
import com.focusforge.app.BuildConfig
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.focusforge.app.data.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    data class Success(val productId: String) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
    object Cancelled : PurchaseState()
}

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var billingClient: BillingClient? = null

    private val _purchaseState = MutableSharedFlow<PurchaseState>()
    val purchaseState: SharedFlow<PurchaseState> = _purchaseState.asSharedFlow()

    private val _monthlyPrice = MutableStateFlow("$3.99 / month")
    val monthlyPrice: StateFlow<String> = _monthlyPrice.asStateFlow()

    private val _lifetimePrice = MutableStateFlow("$19.99 lifetime")
    val lifetimePrice: StateFlow<String> = _lifetimePrice.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        try {
            val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(pendingPurchasesParams)
                .build()
        } catch (e: Exception) {
            _isConnected.value = false
        }
    }

    fun startConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    queryAvailableProducts()
                    queryActivePurchases()
                } else {
                    _isConnected.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
            }
        })
    }

    private fun queryAvailableProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MonetizationConfig.PRODUCT_ID_PRO_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MonetizationConfig.PRODUCT_ID_PRO_LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (details in productDetailsList) {
                    productDetailsMap[details.productId] = details
                    if (details.productId == MonetizationConfig.PRODUCT_ID_PRO_MONTHLY) {
                        val offer = details.subscriptionOfferDetails?.firstOrNull()
                        val pricing = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
                        pricing?.formattedPrice?.let { price ->
                            _monthlyPrice.value = "$price / month"
                        }
                    } else if (details.productId == MonetizationConfig.PRODUCT_ID_PRO_LIFETIME) {
                        details.oneTimePurchaseOfferDetails?.formattedPrice?.let { price ->
                            _lifetimePrice.value = "$price lifetime"
                        }
                    }
                }
            }
        }
    }

    fun queryActivePurchases() {
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient?.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }

        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient?.queryPurchasesAsync(subsParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String) {
        val details = productDetailsMap[productId]
        if (details == null) {
            // If live billing is not connected or products not found on Play Console yet,
            // check if mock mode fallback is enabled for seamless testing
            if (BuildConfig.DEBUG && MonetizationConfig.USE_MOCK_BILLING_FALLBACK) {
                simulateMockPurchase(productId)
                return
            }
            scope.launch {
                _purchaseState.emit(PurchaseState.Error("Product unavailable. Check Google Play Console configuration."))
            }
            return
        }

        val productDetailsParamsList = when (details.productType) {
            BillingClient.ProductType.SUBS -> {
                val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build()
                )
            }
            else -> {
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            }
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                scope.launch { _purchaseState.emit(PurchaseState.Cancelled) }
            }
            else -> {
                scope.launch {
                    _purchaseState.emit(
                        PurchaseState.Error("Billing error (${billingResult.responseCode}): ${billingResult.debugMessage}")
                    )
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch {
                            preferencesRepository.setProUser(true)
                            _purchaseState.emit(PurchaseState.Success(purchase.products.firstOrNull() ?: "pro"))
                        }
                    }
                }
            } else {
                scope.launch {
                    preferencesRepository.setProUser(true)
                    _purchaseState.emit(PurchaseState.Success(purchase.products.firstOrNull() ?: "pro"))
                }
            }
        }
    }

    /**
     * Allows instant local testing & review validation before Play Store merchant account setup
     */
    fun simulateMockPurchase(productId: String) {
        scope.launch {
            _purchaseState.emit(PurchaseState.Loading)
            kotlinx.coroutines.delay(600)
            preferencesRepository.setProUser(true)
            _purchaseState.emit(PurchaseState.Success(productId))
        }
    }

    fun restorePurchases() {
        if (!_isConnected.value) {
            scope.launch {
                _purchaseState.emit(
                    PurchaseState.Error("Google Play Billing is not connected, so purchases cannot be restored yet.")
                )
            }
            return
        }
        queryActivePurchases()
    }
}
