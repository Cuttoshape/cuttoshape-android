package com.example.cuttoshapenew.views.customerviews.checkout


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuttoshapenew.apiclients.PaymentIntentRequest
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.CustomerConfiguration
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val paymentRequest: PaymentIntentRequest
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val clientSecret: String? = null,
        val customerConfig: CustomerConfiguration? = null,
        val merchantDisplayName: String = "Cuttoshape",
        val errorMessage: String? = null,
        val paymentResultMessage: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    /**
     * Fetches PI client secret, ephemeral key, and publishable key
     * and initializes Stripe SDK with the publishable key.
     */
    fun preparePaymentSheet(context: Context) {
        _ui.value = _ui.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.getClient(context).getPaymentIntent(paymentRequest)

                // Required: initialize Stripe with your publishable key
                PaymentConfiguration.init(context, resp.publishableKey)

                val customer = CustomerConfiguration(
                    id = resp.customer,
                    ephemeralKeySecret = resp.ephemeralKey
                )

                _ui.value = _ui.value.copy(
                    isLoading = false,
                    clientSecret = resp.paymentIntent,
                    customerConfig = customer
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load payment options: ${t.message}"
                )
            }
        }
    }

    /**
     * Mirrors your Swift code: on success, posts back to the backend,
     * hides checkout (let the caller react), and refreshes cart items.
     */
    fun onPaymentCompletion(
        context: Context,
        result: PaymentSheetResult,
        onDismissCheckout: () -> Unit
    ) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                _ui.value = _ui.value.copy(paymentResultMessage = "Payment complete")
                viewModelScope.launch {
                    try {
                        RetrofitClient.getClient(context).createPaymentSuccess(paymentRequest)
                    } catch (_: Throwable) {
                        // Optional: log

                    }
                    onDismissCheckout()
                }
            }
            is PaymentSheetResult.Failed -> {
                _ui.value = _ui.value.copy(paymentResultMessage = "Payment failed: ${result.error.message}")
            }
            is PaymentSheetResult.Canceled -> {
                _ui.value = _ui.value.copy(paymentResultMessage = "Payment canceled.")
            }
        }
    }
}
