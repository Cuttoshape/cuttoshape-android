package com.example.cuttoshapenew.views.customerviews.product

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuttoshapenew.apiclients.ProductRequest
import com.example.cuttoshapenew.apiclients.RetrofitClient
import com.example.cuttoshapenew.model.Product
import kotlinx.coroutines.launch
import android.content.Context

class ProductListView(private val context: Context) : ViewModel() {
    private val _products = mutableStateOf<List<Product>>(emptyList())
    val products: State<List<Product>> = _products

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            try {
                val request = ProductRequest(page = 1, limit = 10)
                val response = RetrofitClient.getClient(context).getProducts(request)
                _products.value = response.products
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load products: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}