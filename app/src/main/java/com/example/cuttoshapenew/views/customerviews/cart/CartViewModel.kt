package com.example.cuttoshapenew.views.customerviews.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.apiclients.CartItemResponse
import com.example.cuttoshapenew.utils.DataStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import com.stripe.android.paymentsheet.PaymentSheetResult
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.cuttoshapenew.apiclients.PaymentIntentRequest
import com.example.cuttoshapenew.apiclients.PaymentIntentResponse
import com.example.cuttoshapenew.apiclients.ShipAddressRequest
import com.example.cuttoshapenew.apiclients.ShippingAddress
import com.stripe.android.paymentsheet.PaymentSheetResultCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItemResponse>>(emptyList())
    val cartItems: StateFlow<List<CartItemResponse>> = _cartItems

    private val _shipAddresses = MutableStateFlow<List<ShippingAddress>>(emptyList())
    val shipAddresses: StateFlow<List<ShippingAddress>> = _shipAddresses

    private val _paymentIntent = MutableStateFlow<PaymentIntentResponse?>(null)
    val paymentIntent: StateFlow<PaymentIntentResponse?> = _paymentIntent

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        viewModelScope.launch {
            fetchCartItems()
        }
    }

    internal suspend fun fetchCartItems() {
        _isLoading.value = true
        _errorMessage.value = null
        val userId = DataStoreManager.getUserId(context).first()
        try {
            val response = RetrofitClient.getClient(context).getCartItem(userId?.toIntOrNull())
            Log.d("Response", response.toString())
            if (response.success == "true") {
                val items: List<CartItemResponse>? = response.data
                _cartItems.value = items ?: emptyList()
            } else {
                _errorMessage.value = "Failed to load cart: ${response}"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load cart: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    internal suspend fun fetchShipAddress() {
        _isLoading.value = true
        _errorMessage.value = null
        val userId = DataStoreManager.getUserId(context).first()
        try {
            val response = RetrofitClient.getClient(context).getShippingAddress(userId.toString())
            Log.d("Response", response.toString())

                val items: List<ShippingAddress>? = response
                _shipAddresses.value = items ?: emptyList()

        } catch (e: Exception) {
            _errorMessage.value = "Failed to load cart: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    internal suspend fun createShipAddress(shipAddress : ShipAddressRequest) {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val response = RetrofitClient.getClient(context).createShipAddress(shipAddress)
            Log.d("Response", response.toString())


            _shipAddresses.value += response

        } catch (e: Exception) {
            _errorMessage.value = "Failed to load cart: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    internal suspend fun createPaymentIntent(paymentIntent : PaymentIntentRequest) {
        _isLoading.value = true
        _errorMessage.value = null

        try {
            val response = RetrofitClient.getClient(context).getPaymentIntent(paymentIntent)
            Log.d("Response", response.toString())
            _paymentIntent.value = response

        } catch (e: Exception) {
            _errorMessage.value = "Failed to load cart: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult, paymentRequest : PaymentIntentRequest, onDismissCheckout: () -> Unit) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {

                viewModelScope.launch {
                    try {
                        var resp = RetrofitClient.getClient(context).createPaymentSuccess(paymentRequest)
                        Log.d("response", resp.toString())
                    } catch (e: Throwable) {
                        // Optional: log
                        Log.e("PaymentAPI", "Error creating payment success", e)
                    }
                    onDismissCheckout()

                    fetchCartItems()

                }
                //_paymentStatus.value = PaymentResultStatus.Success
                // Update other variables here based on success
            }
            is PaymentSheetResult.Canceled -> {
                //_paymentStatus.value = PaymentResultStatus.Canceled
            }
            is PaymentSheetResult.Failed -> {
                //_paymentStatus.value = PaymentResultStatus.Failed(paymentSheetResult.error)
            }
        }
    }

    suspend fun removeCartItem(item: CartItemResponse) {
        try {
            val response = RetrofitClient.getClient(context).deleteCartItem(item.id)
            if (response.success == "true") {
                _cartItems.value = _cartItems.value.filter { it.id != item.id }
                _errorMessage.value = null
            } else {
                _errorMessage.value = "Failed to delete item: ${response.message}"
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to delete item: ${e.message}"
        }
    }


}