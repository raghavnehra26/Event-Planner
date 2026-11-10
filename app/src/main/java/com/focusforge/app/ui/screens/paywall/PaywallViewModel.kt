package com.focusforge.app.ui.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusforge.app.data.preferences.UserPreferencesRepository
import com.focusforge.app.monetization.BillingManager
import com.focusforge.app.monetization.MonetizationConfig
import com.focusforge.app.monetization.PurchaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SelectedPlan {
    MONTHLY,
    LIFETIME
}

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val monthlyPrice: StateFlow<String> = billingManager.monthlyPrice
    val lifetimePrice: StateFlow<String> = billingManager.lifetimePrice

    private val _selectedPlan = MutableStateFlow(SelectedPlan.LIFETIME)
    val selectedPlan: StateFlow<SelectedPlan> = _selectedPlan.asStateFlow()

    private val _uiState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val uiState: StateFlow<PurchaseState> = _uiState.asStateFlow()

    val isProUser = preferencesRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            billingManager.purchaseState.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun selectPlan(plan: SelectedPlan) {
        _selectedPlan.value = plan
    }

    fun initiatePurchase(activity: Activity) {
        val productId = when (_selectedPlan.value) {
            SelectedPlan.MONTHLY -> MonetizationConfig.PRODUCT_ID_PRO_MONTHLY
            SelectedPlan.LIFETIME -> MonetizationConfig.PRODUCT_ID_PRO_LIFETIME
        }
        billingManager.launchBillingFlow(activity, productId)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }
}
