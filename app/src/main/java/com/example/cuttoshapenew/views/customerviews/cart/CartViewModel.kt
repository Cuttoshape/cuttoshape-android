package com.example.cuttoshapenew.views.customerviews.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.apiclients.CartItemResponse
import com.example.cuttoshapenew.utils.DataStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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