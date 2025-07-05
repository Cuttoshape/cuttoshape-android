package com.example.cuttoshapenew.views.customerviews.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuttoshapenew.model.Product
import com.example.cuttoshapenew.apiclients.RetrofitClient
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ProductDetailViewModel : ViewModel() {
    private val _product = MutableLiveData<Product?>(null)
    val product: LiveData<Product?> = _product

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchProduct(productId: Int?, context: android.content.Context) {
        if (productId == null) {
            _errorMessage.value = "Invalid product ID"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.getClient(context).getProductById(productId)
                _product.value = response
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load product: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}